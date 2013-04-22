package org.smbarbour.mcu.util;

import java.net.*;
import j7compat.Files;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//import java.nio.file.StandardOpenOption;
import j7compat.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.ImageIcon;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.smbarbour.mcu.FMLStyleFormatter;
import org.smbarbour.mcu.MCUApp;
import org.smbarbour.mcu.Version;
import org.smbarbour.mcu.util.Archive;
import org.w3c.dom.*;


public class MCUpdater {
	
	//private List<Module> modList = new ArrayList<Module>();
	private Path MCFolder;
	private Path archiveFolder;
	private Path instanceRoot;
	private MCUApp parent;
	private String sep = System.getProperty("file.separator");
	public MessageDigest md5;
	public ImageIcon defaultIcon;
	private String newestMC = "";
	private Map<String,String> versionMap = new HashMap<String,String>();
	public Logger apiLogger;
	
	private static MCUpdater INSTANCE;

	public static File getJarFile() {
		try {
			return new File(MCUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			INSTANCE.apiLogger.log(Level.SEVERE, "Error getting MCUpdater JAR URI", e);
		}
		return null;
	}
	
	public static MCUpdater getInstance() {
		if( INSTANCE == null ) {
			INSTANCE = new MCUpdater();
		}
		return INSTANCE;
	}
	
	public static String cpDelimiter() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return ";";
		} else {
			return ":";
		}
	}
	
	private MCUpdater()
	{
		apiLogger = Logger.getLogger("MCU-API");
		apiLogger.setLevel(Level.ALL);
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			MCFolder = new Path(System.getenv("APPDATA")).resolve(".minecraft");
			archiveFolder = new Path(System.getenv("APPDATA")).resolve(".MCUpdater");
		} else if(System.getProperty("os.name").startsWith("Mac"))
		{
			MCFolder = new Path(System.getProperty("user.home")).resolve("Library").resolve("Application Support").resolve("minecraft");
			archiveFolder = new Path(System.getProperty("user.home")).resolve("Library").resolve("Application Support").resolve("MCUpdater");
		}
		else
		{
			MCFolder = new Path(System.getProperty("user.home")).resolve(".minecraft");
			archiveFolder = new Path(System.getProperty("user.home")).resolve(".MCUpdater");
		}
		try {
			FileHandler apiHandler = new FileHandler(archiveFolder.resolve("MCU-API.log").toString(), 0, 3);
			apiHandler.setFormatter(new FMLStyleFormatter());
			apiLogger.addHandler(apiHandler);
			
		} catch (SecurityException e1) {
			e1.printStackTrace(); // Will only be thrown if there is a problem with logging.
		} catch (IOException e1) {
			e1.printStackTrace(); // Will only be thrown if there is a problem with logging.
		}
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			apiLogger.log(Level.SEVERE, "No MD5 support!", e);
		}

		try {
			defaultIcon = new ImageIcon(MCUpdater.class.getResource("/minecraft.png"));
		} catch( NullPointerException e ) {
			_debug( "Unable to load default icon?!" );
			defaultIcon = new ImageIcon(new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB));
		}
		// configure the download cache
		try {
			DownloadCache.init(archiveFolder.resolve("cache").toFile(), this);
		} catch (IllegalArgumentException e) {
			_debug( "Suppressed attempt to re-init download cache?!" );
		}
		try {
			long start = System.currentTimeMillis();
			URL md5s = new URL("http://files.mcupdater.com/md5.dat");
			InputStreamReader input = new InputStreamReader(md5s.openStream());
			BufferedReader buffer = new BufferedReader(input);
			String currentLine = null;
			while(true){
				currentLine = buffer.readLine();
				if(currentLine != null){
					String entry[] = currentLine.split("\\|");
					versionMap.put(entry[0], entry[1]);
					newestMC = entry[1]; // Most recent entry in md5.dat is the current release
				} else {
					break;
				}
			}
			buffer.close();
			input.close();
			apiLogger.fine("Took "+(System.currentTimeMillis()-start)+"ms to load md5.dat");
			apiLogger.fine("newest Minecraft in md5.dat: " + newestMC);
		} catch (MalformedURLException e) {
			apiLogger.log(Level.SEVERE, "Bad URL", e);
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
	}
	
	public MCUApp getParent() {
		return parent;
	}

	public void setParent(MCUApp parent) {
		this.parent = parent;
	}

	public void writeServerList(List<ServerList> serverlist)
	{
		try
		{
			archiveFolder.toFile().mkdirs();
			BufferedWriter writer = Files.newBufferedWriter(archiveFolder.resolve("mcuServers.dat"));
			
			Iterator<ServerList> it = serverlist.iterator();
			
			Set<String> urls = new HashSet<String>();
			while(it.hasNext())
			{
				ServerList entry = it.next();
				urls.add(entry.getPackUrl());
			}
			Iterator<String> urlIterator = urls.iterator();
			while (urlIterator.hasNext())
			{
				writer.write(urlIterator.next());
				writer.newLine();
			}
			
			writer.close();
		}
		catch( IOException x)
		{
			apiLogger.log(Level.SEVERE, "I/O Error", x);
		}
	}
	
	public List<Backup> loadBackupList() {
		List<Backup> bList = new ArrayList<Backup>();
		try {
			BufferedReader reader = Files.newBufferedReader(archiveFolder.resolve("mcuBackups.dat"));
			
			String entry = reader.readLine();
			while(entry != null) {
				String[] ele = entry.split("~~~~~");
				bList.add(new Backup(ele[0], ele[1]));
				entry = reader.readLine();
			}
			reader.close();
			return bList;
			
		} catch(FileNotFoundException notfound) {
			apiLogger.log(Level.SEVERE, "File not found", notfound);
		} catch(IOException ioe) {
			apiLogger.log(Level.SEVERE, "I/O Error", ioe);		
		}
		return bList;
	}
	
	public void writeBackupList(List<Backup> backupList) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(archiveFolder.resolve("mcuBackups.dat"));
			
			Iterator<Backup> it = backupList.iterator();
			
			while(it.hasNext()) {
				Backup entry = it.next();
				writer.write(entry.getDescription() + "~~~~~" + entry.getFilename());
				writer.newLine();
			}
			
			writer.close();
		} catch(IOException ioe) {
			apiLogger.log(Level.SEVERE, "I/O Error", ioe);
		}
	}
	
	public List<ServerList> loadServerList(String defaultUrl)
	{
		List<ServerList> slList = new ArrayList<ServerList>();
		try
		{
			Set<String> urls = new HashSet<String>();
			urls.add(defaultUrl);
			BufferedReader reader = Files.newBufferedReader(archiveFolder.resolve("mcuServers.dat"));

			String entry = reader.readLine();
			while(entry != null)
			{
				urls.add(entry);
				entry = reader.readLine();
			}
			reader.close();
			Iterator<String> it = urls.iterator();
			while (it.hasNext()){
				String serverUrl = it.next();
				try {
					Element docEle = null;
					Document serverHeader = ServerPackParser.readXmlFromUrl(serverUrl);
					if (!(serverHeader == null)) {
						Element parent = serverHeader.getDocumentElement();
						if (parent.getNodeName().equals("ServerPack")){
							String mcuVersion = parent.getAttribute("version");
							NodeList servers = parent.getElementsByTagName("Server");
							for (int i = 0; i < servers.getLength(); i++){
								docEle = (Element)servers.item(i);
								ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), serverUrl, docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), ServerPackParser.parseBoolean(docEle.getAttribute("generateList")), ServerPackParser.parseBoolean(docEle.getAttribute("autoConnect")), docEle.getAttribute("revision"));
								sl.setMCUVersion(mcuVersion);
								slList.add(sl);
							}					
						} else {
							slList.add(new ServerList(parent.getAttribute("id"), parent.getAttribute("name"), serverUrl, parent.getAttribute("newsUrl"), parent.getAttribute("iconUrl"), parent.getAttribute("version"), parent.getAttribute("serverAddress"), ServerPackParser.parseBoolean(parent.getAttribute("generateList")), ServerPackParser.parseBoolean(parent.getAttribute("autoConnect")), parent.getAttribute("revision")));
						}
					} else {
						apiLogger.warning("Unable to get server information from " + serverUrl);
					}
				} catch (Exception e) {
					apiLogger.log(Level.SEVERE, "General Error", e);
				}
			}
			//	String[] arrString = entry.split("\\|");
			//	slList.add(new ServerList(arrString[0], arrString[1], arrString[2]));

			return slList;

		}
		catch( FileNotFoundException notfound)
		{
			apiLogger.log(Level.SEVERE, "File not found", notfound);
		}
		catch (IOException x)
		{
			apiLogger.log(Level.SEVERE, "I/O Error", x);
		}
		return slList;
	}
		
	public Path getMCFolder()
	{
		return MCFolder;
	}

	public Path getArchiveFolder() {
		return archiveFolder;
	}

	public Path getInstanceRoot() {
		return instanceRoot;
	}

	public void setInstanceRoot(Path instanceRoot) {
		this.instanceRoot = instanceRoot;
	}

	public String getMCVersion() {
		File jar = MCFolder.resolve("bin").resolve("minecraft.jar").toFile();
		byte[] hash;
		try {
			InputStream is = new FileInputStream(jar);
			hash = DigestUtils.md5(is);
			is.close();		
		} catch (FileNotFoundException e) {
			return "Not found";
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
			return "Error reading file";
		}
		String hashString = new String(Hex.encodeHex(hash));
		String version = lookupHash(hashString);
		if(!version.isEmpty()) {
			File backupJar = archiveFolder.resolve("mc-" + version + ".jar").toFile();
			if(!backupJar.exists()) {
				backupJar.getParentFile().mkdirs();
				copyFile(jar, backupJar);
			}
			return version;
		} else {
			return "Unknown version";
		}
	}

	private String lookupHash(String hash) {
		String out = versionMap.get(hash);
		if (out == null) {
			out = "";
		}
		return out;
	}
	
	private void copyFile(File jar, File backupJar) {
		try {
			InputStream in = new FileInputStream(jar);
			OutputStream out = new FileOutputStream(backupJar);
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch(IOException ioe) {
			apiLogger.log(Level.SEVERE, "I/O Error", ioe);
		}
	}

	public void saveConfig(String description) {
		File folder = MCFolder.toFile();
		List<File> contents = recurseFolder(folder, false);
		try {
			String uniqueName = UUID.randomUUID().toString() + ".zip";
			Iterator<File> it = new ArrayList<File>(contents).iterator();
			while(it.hasNext()) {
				File entry = it.next();
				if(getExcludedNames(entry.getPath(), false) || entry.getPath().contains("temp")){
					contents.remove(entry);
				}
			}
			Archive.createZip(archiveFolder.resolve(uniqueName).toFile(), contents, MCFolder, parent);
			Backup entry = new Backup(description, uniqueName);
			_debug("DEBUG: LoadBackupList");
			List<Backup> bList = loadBackupList();
			_debug("DEBUG: add");
			bList.add(entry);
			_debug("DEBUG: writeBackupList");
			writeBackupList(bList);
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
	}

	private boolean getExcludedNames(String path, boolean forDelete) {
		if(path.contains("mcu" + sep)) {
			// never delete from the mcu folder
			return true;
		}
		if (path.contains("mods") && (path.contains(".zip") || path.contains(".jar"))) {
			// always delete mods in archive form
			return false;
		}
		if(path.contains("bin" + sep + "minecraft.jar")) {
			// always delete bin/minecraft.jar
			return false;
		}
		if(path.contains("bin" + sep)) {
			// never delete anything else in bin/
			return true;
		}
		if(path.contains("resources") && !path.contains("mods")) {
			// never delete resources unless it is under the mods directory
			return true;
		}
		if(path.contains("saves")) {
			// never delete saves
			return true;
		}
		if(path.contains("screenshots")) {
			// never delete screenshots
			return true;
		}
		if(path.contains("stats")) {
			return true;
		}
		if(path.contains("texturepacks")) {
			return true;
		}
		if(path.contains("lastlogin")) {
			return true;
		}
		if(path.contains("mcuServers.dat")) {
			return true;
		}
		if(path.contains("instance.dat")) {
			return true;
		}
		if(path.contains("minecraft.jar")) {
			return true;
		}
		if(path.contains("options.txt")) {
			return forDelete;
		}
		if(path.contains("META-INF" + sep)) {
			return true;
		}
		// Temporary hardcoding of client specific mod configs (i.e. Don't clobber on update)
		if(path.contains("rei_minimap" + sep)) {
			return true;
		}
		if(path.contains("macros" + sep)) {
			return true;
		}
		if(path.contains("InvTweaks")) {
			return true;
		}
		if(path.contains("optionsof.txt")){
			return true;
		}
		if(path.contains("voxelMap")) {
			return true;
		}
		//
		return false;
	}

	private List<File> recurseFolder(File folder, boolean includeFolders)
	{
		List<File> output = new ArrayList<File>();
		List<File> input = new ArrayList<File>(Arrays.asList(folder.listFiles()));
		Iterator<File> fi = input.iterator();
		if(includeFolders) {
			output.add(folder);
		}
		while(fi.hasNext())
		{
			File entry = fi.next();
			if(entry.isDirectory())
			{
				List<File> subfolder = recurseFolder(entry, includeFolders);
				Iterator<File> sfiterator = subfolder.iterator();
				while(sfiterator.hasNext())
				{
					output.add(sfiterator.next());
				}
			} else {
				output.add(entry);
			}
		}
		return output;
	}
	
	public void restoreBackup(File archive) {
		File folder = MCFolder.toFile();
		List<File> contents = recurseFolder(folder, true);
		Iterator<File> it = new ArrayList<File>(contents).iterator();
		while(it.hasNext()) {
			File entry = it.next();
			if(getExcludedNames(entry.getPath(), true)){
				contents.remove(entry);
			}
		}
		ListIterator<File> liClear = contents.listIterator(contents.size());
		while(liClear.hasPrevious()) { 
			File entry = liClear.previous();
			entry.delete();
		}
		Archive.extractZip(archive, MCFolder.toFile());
	}

	public boolean checkForBackup(ServerList server) {
		File jar = archiveFolder.resolve("mc-" + server.getVersion() + ".jar").toFile();
		return jar.exists();
	}
	
	public boolean installMods(ServerList server, List<Module> toInstall, boolean clearExisting, Properties instData) throws FileNotFoundException {
		if (Version.requestedFeatureLevel(server.getMCUVersion(), "2.2")) {
			// Sort mod list for InJar
			Collections.sort(toInstall, new ModuleComparator());
		}
		Path instancePath = instanceRoot.resolve(server.getServerId());
		Boolean updateJar = clearExisting;
		if (!instancePath.resolve("bin").resolve("minecraft.jar").toFile().exists()) {
			updateJar = true;
		}
		Iterator<Module> iMods = toInstall.iterator();
		int jarModCount = 0;
		while (iMods.hasNext() && !updateJar) {
			Module current = iMods.next();
			if (current.getInJar()) {
				if (current.getMD5().isEmpty() || (!current.getMD5().equalsIgnoreCase(instData.getProperty("mod:" + current.getId(), "NoHash")))) {
					updateJar = true;
				}
				jarModCount++;
			}
		}
		if (jarModCount != Integer.parseInt(instData.getProperty("jarModCount","0"))) {
			updateJar = true;
		}
		jarModCount = 0;
		apiLogger.info("Instance path: " + instancePath.toString());
		List<File> contents = recurseFolder(instancePath.toFile(), true);
		File jar = archiveFolder.resolve("mc-" + server.getVersion() + ".jar").toFile();
		if(!jar.exists()) {
			parent.log("! Unable to find a backup copy of minecraft.jar for "+server.getVersion());
			throw new FileNotFoundException("A backup copy of minecraft.jar for version " + server.getVersion() + " was not found.");
		}
		if (clearExisting){
			parent.setStatus("Clearing existing configuration");
			parent.log("Clearing existing configuration...");
			Iterator<File> it = new ArrayList<File>(contents).iterator();
			while(it.hasNext()) {
				File entry = it.next();
				if(getExcludedNames(entry.getPath(), true)){
					contents.remove(entry);
				}
			}
			ListIterator<File> liClear = contents.listIterator(contents.size());
			while(liClear.hasPrevious()) { 
				File entry = liClear.previous();
				entry.delete();
			}
		}
		Iterator<Module> itMods = toInstall.iterator();
		File tmpFolder = archiveFolder.resolve("temp").toFile();
		tmpFolder.mkdirs();
		File buildJar = archiveFolder.resolve("build.jar").toFile();		
		if(buildJar.exists()) {
			buildJar.delete();
		}
		if (updateJar) {
			parent.setStatus("Preparing to build minecraft.jar");
			parent.log("Preparing to build minecraft.jar...");
			Archive.extractZip(jar, tmpFolder);
			try {
				FileUtils.deleteDirectory(new Path(tmpFolder).resolve("META-INF").toFile());
			} catch (IOException e) {
				apiLogger.log(Level.SEVERE, "I/O Error", e);
			}
		} else {
			parent.log("No jar changes necessary.  Skipping jar rebuild.");
		}

		File branding = new File(tmpFolder, "fmlbranding.properties");
		try {
			branding.createNewFile();
			Properties propBrand = new Properties();
			propBrand.setProperty("fmlbranding", "MCUpdater: " + server.getName() + " (rev " + server.getRevision() + ")");
			propBrand.store(new FileOutputStream(branding), "MCUpdater ServerPack branding");
		} catch (IOException e1) {
			apiLogger.log(Level.SEVERE, "I/O Error", e1);
		}
		
		int modCount = toInstall.size();
		int modsLoaded = 0;
		int errorCount = 0;
		
		// TODO: consolidate download logic for mods & configs
		
		while(itMods.hasNext()) {
			Module entry = itMods.next();
			parent.setStatus("Mod: " + entry.getName());
			parent.log("Mod: "+entry.getName());
			try {
				_debug("Mod @ "+entry.getUrl());
				URL modURL = new URL(entry.getUrl());
				//String modFilename = modURL.getFile().substring(modURL.getFile().lastIndexOf('/'));
				File modPath;
				if(entry.getInJar()) {
					if (updateJar) {
						//modPath = new File(tmpFolder.getPath() + sep + loadOrder + ".zip");
						//loadOrder++;
						//_log(modPath.getPath());
						ModDownload jarMod;
						try {
							jarMod = new ModDownload(modURL, new File(tmpFolder, (entry.getId() + ".jar")), entry.getMD5());
							if( jarMod.cacheHit ) {
								parent.log("  Adding to jar (cached).");
							} else {
								parent.log("  Adding to jar (downloaded).");
							}
							_debug(jarMod.url + " -> " + jarMod.getDestFile().getPath());
							//FileUtils.copyURLToFile(modURL, modPath);
							Archive.extractZip(jarMod.getDestFile(), tmpFolder);
							jarMod.getDestFile().delete();
							instData.setProperty("mod:" + entry.getId(), entry.getMD5());
							jarModCount++;
						} catch (Exception e) {
							++errorCount;
							apiLogger.log(Level.SEVERE, "General Error", e);						}
					} else {
						parent.log("Skipping jar mod: " + entry.getName());
					}
				} else if (entry.getExtract()) {
					//modPath = new File(tmpFolder.getPath() + sep + modFilename);
					//modPath.getParentFile().mkdirs();
					//_log(modPath.getPath());
					ModDownload extractMod;
					try {
						extractMod = new ModDownload(modURL, new File(tmpFolder, (entry.getId() + ".jar")), entry.getMD5());
						if( extractMod.cacheHit ) {
							parent.log("  Extracting to filesystem (cached).");
						} else {
							parent.log("  Extracting to filesystem (downloaded).");
						}
						_debug(extractMod.url + " -> " + extractMod.getDestFile().getPath());
						//FileUtils.copyURLToFile(modURL, modPath);
						Path destPath = instancePath;
						if(!entry.getInRoot()) destPath = instancePath.resolve("mods");
						Archive.extractZip(extractMod.getDestFile(), destPath.toFile());
						extractMod.getDestFile().delete();
					} catch (Exception e) {
						++errorCount;
						apiLogger.log(Level.SEVERE, "General Error", e);
					}
				} else if (entry.getCoreMod()) {
					modPath = instancePath.resolve("coremods").resolve(cleanForFile(entry.getId()) + ".jar").toFile();
					modPath.getParentFile().mkdirs();
					try {
						ModDownload normalMod = new ModDownload(modURL, modPath, entry.getMD5());
						if( normalMod.cacheHit ) {
							parent.log("  Installing in /coremods (cached).");
						} else {
							parent.log("  Installing in /coremods (downloaded).");
						}
						_debug(normalMod.url + " -> " + normalMod.getDestFile().getPath());
					} catch (Exception e) {
						++errorCount;
						apiLogger.log(Level.SEVERE, "General Error", e);
					}					
				} else {
					if (entry.getPath().equals("")){
						modPath = instancePath.resolve("mods").resolve(cleanForFile(entry.getId()) + ".jar").toFile();
					} else {
						modPath = instancePath.resolve(entry.getPath()).toFile();
					}
					modPath.getParentFile().mkdirs();
					//_log("~~~ " + modPath.getPath());
					try {
						ModDownload normalMod = new ModDownload(modURL, modPath, entry.getMD5());
						if( normalMod.cacheHit ) {
							parent.log("  Installing in /mods (cached).");
						} else {
							parent.log("  Installing in /mods (downloaded).");
						}
						_debug(normalMod.url + " -> " + normalMod.getDestFile().getPath());
					} catch (Exception e) {
						++errorCount;
						apiLogger.log(Level.SEVERE, "General Error", e);
					}
					//FileUtils.copyURLToFile(modURL, modPath);
				}
				Iterator<ConfigFile> itConfigs = entry.getConfigs().iterator();
				while(itConfigs.hasNext()) {
					final ConfigFile cfEntry = itConfigs.next();
					final String MD5 = cfEntry.getMD5(); 
					_debug(cfEntry.getUrl());
					URL configURL = new URL(cfEntry.getUrl());
					final File confFile = instancePath.resolve(cfEntry.getPath()).toFile();
					confFile.getParentFile().mkdirs();
					if( MD5 != null ) {
						final File cacheFile = DownloadCache.getFile(MD5);
						if( cacheFile.exists() ) {
							parent.log("  Found config for "+cfEntry.getPath()+" (cached)");
							FileUtils.copyFile(cacheFile, confFile);
							continue;
						}
					}
					parent.log("  Found config for "+cfEntry.getPath()+", downloading...");
					_debug(confFile.getPath());
					if (cfEntry.isNoOverwrite() && confFile.exists()) {
						parent.log("  Skipped - NoOverwrite is true");
					} else {
						FileUtils.copyURLToFile(configURL, confFile);
					}
					// save in cache for future reference
					if( MD5 != null ) {
						final boolean cached = DownloadCache.cacheFile(confFile, MD5);
						if( cached ) {
							_debug(confFile.getName() + " saved in cache");							
						}
					}
				}
			} catch (MalformedURLException e) {
				++errorCount;
				apiLogger.log(Level.SEVERE, "General Error", e);
			} catch (IOException e) {
				++errorCount;
				apiLogger.log(Level.SEVERE, "General Error", e);
			}
			modsLoaded++;
			parent.setProgressBar((int)( (65 / modCount) * modsLoaded + 25));
			parent.log("  Done ("+modsLoaded+"/"+modCount+")");
		}
		instData.setProperty("jarModCount", Integer.toString(jarModCount));
		try {
			buildJar.createNewFile();
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
		parent.log("All mods loaded.");
		if( errorCount > 0 ) {
			parent.baseLogger.severe("Errors were detected with this update, please verify your files. There may be a problem with the serverpack configuration or one of your download sites.");
			return false;
		}
		copyFile(jar, buildJar);
		boolean doManifest = true;
		List<File> buildList = recurseFolder(tmpFolder,true);
		Iterator<File> blIt = new ArrayList<File>(buildList).iterator();
		while(blIt.hasNext()) {
			File entry = blIt.next();
			if(entry.getPath().contains("META-INF")) {
				doManifest = false;
			}
		}
		Path binPath = instancePath.resolve("bin");
		if (!updateJar) {
			try {
				Archive.updateArchive(binPath.resolve("minecraft.jar").toFile(), new File[]{ branding });
			} catch (IOException e1) {
				apiLogger.log(Level.SEVERE, "I/O Error", e1);
			}
		} else {
			parent.log("Packaging updated jar...");
			try {
				Archive.createJar(buildJar, buildList, tmpFolder.getPath() + sep, doManifest);
			} catch (IOException e1) {
				parent.log("Failed to create jar!");
				apiLogger.log(Level.SEVERE, "I/O Error", e1);
				return false;
			}
			//Archive.patchJar(jar, buildJar, new ArrayList<File>(Arrays.asList(tmpFolder.listFiles())));
			//copyFile(buildJar, new File(MCFolder + sep + "bin" + sep + "minecraft.jar"));
			try {
				Files.copy(new Path(buildJar), binPath.resolve("minecraft.jar"));
			} catch (IOException e) {
				apiLogger.log(Level.SEVERE, "Failed to copy new jar to instance!", e);
			}
		}
		List<File> tempFiles = recurseFolder(tmpFolder,true);
		ListIterator<File> li = tempFiles.listIterator(tempFiles.size());
		while(li.hasPrevious()) { 
			File entry = li.previous();
			entry.delete();
		}
		return true;
	}
	
	private String cleanForFile(String id) {
		return id.replaceAll("[^a-zA-Z_0-9]", "_");
	}

	public void writeMCServerFile(String name, String ip, String instance) {
		byte[] header = new byte[]{
				0x0A,0x00,0x00,0x09,0x00,0x07,0x73,0x65,0x72,0x76,0x65,0x72,0x73,0x0A,
				0x00,0x00,0x00,0x01,0x01,0x00,0x0B,0x68,0x69,0x64,0x65,0x41,0x64,0x64,
				0x72,0x65,0x73,0x73,0x01,0x08,0x00,0x04,0x6E,0x61,0x6D,0x65,0x00,
				(byte) (name.length() + 12), (byte) 0xC2,(byte) 0xA7,0x41,0x5B,0x4D,0x43,0x55,0x5D,0x20,(byte) 0xC2,(byte) 0xA7,0x46
				};
		byte[] nameBytes = name.getBytes();
		byte[] ipBytes = ip.getBytes();
		byte[] middle = new byte[]{0x08,0x00,0x02,0x69,0x70,0x00,(byte) ip.length()};
		byte[] end = new byte[]{0x00,0x00};
		int size = header.length + nameBytes.length + middle.length + ipBytes.length + end.length;
		byte[] full = new byte[size];
		int pos = 0;
		System.arraycopy(header, 0, full, pos, header.length);
		pos += header.length;
		System.arraycopy(nameBytes, 0, full, pos, nameBytes.length);
		pos += nameBytes.length;
		System.arraycopy(middle, 0, full, pos, middle.length);
		pos += middle.length;
		System.arraycopy(ipBytes, 0, full, pos, ipBytes.length);
		pos += ipBytes.length;
		System.arraycopy(end, 0, full, pos, end.length);
		File serverFile = instanceRoot.resolve(instance).resolve("servers.dat").toFile();
		try {
			serverFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(serverFile);
			fos.write(full,0,full.length);
			fos.close();
		} catch (IOException e) {
			apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
		
	}

	public static void openLink(URI uri) {
		try {
			Object o = Class.forName("java.awt.Desktop").getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
			o.getClass().getMethod("browse", new Class[] { URI.class }).invoke(o, new Object[] { uri });
		} catch (Throwable e) {
			_log("Failed to open link " + uri.toString());
		}
	}
	
	private static void _log(String msg) {
		INSTANCE.apiLogger.info(msg);
	}
	private static void _debug(String msg) {
		INSTANCE.apiLogger.fine(msg);
	}

	public boolean checkVersionCache(String version) {
		File requestedJar = archiveFolder.resolve("mc-" + version + ".jar").toFile();
		File newestJar = archiveFolder.resolve("mc-" + newestMC + ".jar").toFile();
		if (requestedJar.exists()) return true;
		if (newestJar.exists()) {
			doPatch(requestedJar, newestJar, version);
			return true;
		} else {
			if (this.getParent().requestLogin()) {
				try {
					FileUtils.copyURLToFile(new URL("http://assets.minecraft.net/" + newestMC.replace(".","_") + "/minecraft.jar"), newestJar);
				} catch (MalformedURLException e) {
					apiLogger.log(Level.SEVERE, "Bad URL", e);
				} catch (IOException e) {
					apiLogger.log(Level.SEVERE, "I/O Error", e);
				}
				if (!requestedJar.toString().equals(newestJar.toString())) {
					doPatch(requestedJar, newestJar, version);
				}
				return true;
			} else {
				return false;
			}
		}
	}
	
	private void doPatch(File requestedJar, File newestJar, String version) {
		try {
			URL patchURL = new URL("http://files.mcupdater.com/mcu_patches/" + newestMC.replace(".", "") + "to" + version.replace(".","") + ".patch");
			_debug(patchURL.toString());
			File patchFile = archiveFolder.resolve("temp.patch").toFile();
			FileUtils.copyURLToFile(patchURL, patchFile);
			Transmogrify.applyPatch(new Path(newestJar), new Path(requestedJar), new Path(patchFile));
			patchFile.delete();
		} catch (Exception e) {
			apiLogger.log(Level.SEVERE, "General Error", e);
		}
	}

}

package org.smbarbour.mcu.util;

import java.net.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.io.*;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.smbarbour.mcu.MCUApp;
import org.smbarbour.mcu.util.Archive;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class MCUpdater {
	
	private List<Module> modList = new ArrayList<Module>();
	private String MCFolder;
	private File archiveFolder;
	private File instanceRoot;
	private MCUApp parent;
	public final static String sep = System.getProperty("file.separator");
	public MessageDigest md5;
	public ImageIcon defaultIcon;
	
	private static MCUpdater INSTANCE;

	public static MCUpdater getInstance() {
		if( INSTANCE == null ) {
			INSTANCE = new MCUpdater();
		}
		return INSTANCE;
	}
	
	private MCUpdater()
	{
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			MCFolder = System.getenv("APPDATA") + sep + ".minecraft";
			archiveFolder = new File(System.getenv("APPDATA") + sep + ".MCUpdater");
		} else if(System.getProperty("os.name").startsWith("Mac"))
		{
			MCFolder = System.getProperty("user.home") + sep + "Library" + sep + "Application Support" + sep + "minecraft";
			archiveFolder = new File(System.getProperty("user.home") + sep + "Library" + sep + "Application Support" + sep + "MCUpdater");
		}
		else
		{
			MCFolder = System.getProperty("user.home") + sep + ".minecraft";
			archiveFolder = new File(System.getProperty("user.home") + sep + ".MCUpdater");
		}
		//archiveFolder = new File(MCFolder + sep + "mcu");
		//instanceRoot = new File(archiveFolder, "instances");
		try {
			defaultIcon = new ImageIcon(new URL("http://www.minecraft.net/favicon.png"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		// configure the download cache
		try {
			DownloadCache.init(new File(archiveFolder,"cache"));
		} catch (IllegalArgumentException e) {
			_debug( "Suppressed attempt to re-init download cache?!" );
		}
	}
	
	public MCUApp getParent() {
		return parent;
	}

	public void setParent(MCUApp parent) {
		this.parent = parent;
	}

	public List<Module> loadFromFile(File packFile, String serverId) {
		try {
			parseDocument(readXmlFromFile(packFile), serverId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return modList;
	}
	
	public List<Module> loadFromURL(String serverUrl, String serverId)
	{
		try {
			parseDocument(readXmlFromUrl(serverUrl), serverId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return modList;
	}
	
	public List<Backup> loadBackupList() {
		List<Backup> bList = new ArrayList<Backup>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(archiveFolder.getPath() + sep + "mcuBackups.dat"));
			
			String entry = reader.readLine();
			while(entry != null) {
				String[] ele = entry.split("~~~~~");
				bList.add(new Backup(ele[0], ele[1]));
				entry = reader.readLine();
			}
			reader.close();
			return bList;
			
		} catch(FileNotFoundException notfound) {
			_debug("File not found");
		} catch(IOException ioe) {
			ioe.printStackTrace();		
		}
		return bList;
	}
	
	public void writeBackupList(List<Backup> backupList) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter((archiveFolder.getPath() + sep + "mcuBackups.dat"),false));
			
			Iterator<Backup> it = backupList.iterator();
			
			while(it.hasNext()) {
				Backup entry = it.next();
				writer.write(entry.getDescription() + "~~~~~" + entry.getFilename());
				writer.newLine();
			}
			
			writer.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public List<ServerList> loadServerList(String defaultUrl)
	{
		List<ServerList> slList = new ArrayList<ServerList>();
		try
		{
			Set<String> urls = new HashSet<String>();
			urls.add(defaultUrl);
			BufferedReader reader = new BufferedReader(new FileReader(archiveFolder.getPath() + sep + "mcuServers.dat"));

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
					Document serverHeader = readXmlFromUrl(serverUrl);
					if (!(serverHeader == null)) {
						Element parent = serverHeader.getDocumentElement();
						if (parent.getNodeName().equals("ServerPack")){
							String mcuVersion = parent.getAttribute("version");
							NodeList servers = parent.getElementsByTagName("Server");
							for (int i = 0; i < servers.getLength(); i++){
								docEle = (Element)servers.item(i);
								ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), serverUrl, docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), parseBoolean(docEle.getAttribute("generateList")), docEle.getAttribute("revision"));
								sl.setMCUVersion(mcuVersion);
								slList.add(sl);
							}					
						} else {
							slList.add(new ServerList(parent.getAttribute("id"), parent.getAttribute("name"), serverUrl, parent.getAttribute("newsUrl"), parent.getAttribute("iconUrl"), parent.getAttribute("version"), parent.getAttribute("serverAddress"), parseBoolean(parent.getAttribute("generateList")), parent.getAttribute("revision")));
						}
					} else {
						_log("Unable to get server information from " + serverUrl);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//	String[] arrString = entry.split("\\|");
			//	slList.add(new ServerList(arrString[0], arrString[1], arrString[2]));

			return slList;

		}
		catch( FileNotFoundException notfound)
		{
			_debug("File not found");
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
		return slList;
	}
	
	public boolean parseBoolean(String attribute) {
		if (attribute.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	public void writeServerList(List<ServerList> serverlist)
	{
		try
		{
			archiveFolder.mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter((archiveFolder.getPath() + sep + "mcuServers.dat"),false));
			
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
			x.printStackTrace();
		}
	}
	
	public static Document readXmlFromFile(File packFile) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(packFile);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception
	{
		if (serverUrl.equals("http://www.example.org/ServerPack.xml")) {
			return null;
		}
		_log("Reading "+serverUrl+"...");
		URL server = new URL(serverUrl);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(server.openStream());
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	private void parseDocument(Document dom, String serverId)
	{
		modList.clear();
		Element parent = dom.getDocumentElement();
		Element docEle = null;
		if (parent.getNodeName().equals("ServerPack")){
			NodeList servers = parent.getElementsByTagName("Server");
			for (int i = 0; i < servers.getLength(); i++){
				docEle = (Element)servers.item(i);
				if (docEle.getAttribute("id").equals(serverId)) { break; }
			}
		} else {
			docEle = parent;
		}
		NodeList nl = docEle.getElementsByTagName("Module");
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0; i < nl.getLength(); i++)
			{
				Element el = (Element)nl.item(i);
				Module m = getModule(el);
				modList.add(m);
			}
		}
	}
	
	private Module getModule(Element modEl)
	{
		String name = modEl.getAttribute("name");
		String url = getTextValue(modEl,"URL");
		Boolean required = getBooleanValue(modEl,"Required");
		Boolean isDefault = getBooleanValue(modEl,"IsDefault");
		Boolean inJar = getBooleanValue(modEl,"InJar");
		Boolean extract = getBooleanValue(modEl,"Extract");
		Boolean inRoot = getBooleanValue(modEl,"InRoot");
		Boolean coreMod = getBooleanValue(modEl,"CoreMod");
		String md5 = getTextValue(modEl,"MD5");
		List<ConfigFile> configs = new ArrayList<ConfigFile>();
		NodeList nl = modEl.getElementsByTagName("ConfigFile");
//		_log("NodeList[getLength]: " + nl.getLength());
		for(int i = 0; i < nl.getLength(); i++) 
		{
			Element el = (Element)nl.item(i);
			ConfigFile cf = getConfigFile(el);
			configs.add(cf);
		}
		Module m = new Module(name, url, required, inJar, extract, inRoot, isDefault, coreMod, md5, configs);
		return m;
	}
	
	private ConfigFile getConfigFile(Element cfEl)
	{
		String url = getTextValue(cfEl,"URL");
		String path = getTextValue(cfEl,"Path");
		String md5 = getTextValue(cfEl,"MD5");
		ConfigFile cf = new ConfigFile(url,path,md5);
		return cf;
	}
	
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			if(el != null) {
				Node node = el.getFirstChild();
				if(node != null) textVal = node.getNodeValue();
			}
		}
		return textVal;
	}
	
	private Boolean getBooleanValue(Element ele, String tagName) {
		return Boolean.parseBoolean(getTextValue(ele,tagName));
	}
	
	public String getMCFolder()
	{
		return MCFolder;
	}

	public File getArchiveFolder() {
		return archiveFolder;
	}

	public File getInstanceRoot() {
		return instanceRoot;
	}

	public void setInstanceRoot(File instanceRoot) {
		this.instanceRoot = instanceRoot;
	}

	public String getMCVersion() {
		File jar = new File(MCFolder + sep + "bin" + sep + "minecraft.jar");
		byte[] hash;
		try {
			InputStream is = new FileInputStream(jar);
			hash = DigestUtils.md5(is);
			is.close();		
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			return "Not found";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error reading file";
		}
		String hashString = new String(Hex.encodeHex(hash));
		String version = lookupHash(hashString);
		if(!version.isEmpty()) {
			File backupJar = new File(archiveFolder.getPath() + sep + "mc-" + version + ".jar");
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
		Map<String,String> map = new HashMap<String,String>();
		try {
			long start = System.currentTimeMillis();
			URL md5s = new URL("http://mcupdater.net46.net/mcu_patches/md5.dat");
			InputStreamReader input = new InputStreamReader(md5s.openStream());
			BufferedReader buffer = new BufferedReader(input);
			String currentLine = null;
			while(true){
				currentLine = buffer.readLine();
				if(currentLine != null){
					String entry[] = currentLine.split("\\|");
					map.put(entry[0], entry[1]);
				} else {
					break;
				}
			}
			buffer.close();
			input.close();
			_debug("Took "+(System.currentTimeMillis()-start)+"ms to load md5.dat");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String out = map.get(hash);
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
			ioe.printStackTrace();
		}
	}

	public void saveConfig(String description) {
		File folder = new File(MCFolder);
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
			Archive.createZip(new File(archiveFolder.getPath() + sep + uniqueName), contents, (MCFolder + sep), parent);
			Backup entry = new Backup(description, uniqueName);
			_debug("DEBUG: LoadBackupList");
			List<Backup> bList = loadBackupList();
			_debug("DEBUG: add");
			bList.add(entry);
			_debug("DEBUG: writeBackupList");
			writeBackupList(bList);
		} catch (IOException e) {
			e.printStackTrace();
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
		File folder = new File(MCFolder);
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
		Archive.extractZip(archive, new File(MCFolder));
	}

	public boolean checkForBackup(ServerList server) {
		File jar = new File(archiveFolder.getPath() + sep + "mc-" + server.getVersion() + ".jar");
		return jar.exists();
	}
	
	public void installMods(ServerList server, List<Module> toInstall, boolean clearExisting) throws FileNotFoundException {
		File folder;
		try {
			folder = Files.readSymbolicLink(new File(MCFolder).toPath()).toFile();
			System.out.println(MCFolder);
			System.out.println(new File(MCFolder).getAbsolutePath());
			System.out.println(new File(MCFolder).toPath().toString());
			System.out.println((Files.readSymbolicLink(new File(MCFolder).toPath()).toString()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			folder = new File(MCFolder);
		}
		System.out.println(folder.getAbsolutePath());
		List<File> contents = recurseFolder(folder, true);
		File jar = new File(archiveFolder.getPath() + sep + "mc-" + server.getVersion() + ".jar");
		if(!jar.exists()) {
			parent.log("! Unable to find a backup copy of minecraft.jar for "+server.getVersion());
			throw new FileNotFoundException("A backup copy of minecraft.jar for version " + server.getVersion() + " was not found.");
		}
		if (clearExisting){
			parent.setLblStatus("Clearing existing configuration");
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
		parent.setLblStatus("Preparing to build minecraft.jar");
		parent.log("Preparing to build minecraft.jar...");
		Iterator<Module> itMods = toInstall.iterator();
		File tmpFolder = new File(archiveFolder, "temp");
		tmpFolder.mkdirs();
		File buildJar = new File(archiveFolder, "build.jar");
		if(buildJar.exists()) {
			buildJar.delete();
		}
		Archive.extractZip(jar, tmpFolder);
		
		int modCount = toInstall.size();
		int modsLoaded = 0;
		int errorCount = 0;
		
		// TODO: consolidate download logic for mods & configs
		
		while(itMods.hasNext()) {
			Module entry = itMods.next();
			parent.setLblStatus("Mod: " + entry.getName());
			parent.log("Mod: "+entry.getName());
			try {
				_debug("Mod @ "+entry.getUrl());
				URL modURL = new URL(entry.getUrl());
				//String modFilename = modURL.getFile().substring(modURL.getFile().lastIndexOf('/'));
				File modPath;
				if(entry.getInJar()) {
					//modPath = new File(tmpFolder.getPath() + sep + loadOrder + ".zip");
					//loadOrder++;
					//_log(modPath.getPath());
					ModDownload jarMod;
					try {
						jarMod = new ModDownload(modURL, tmpFolder, entry.getMD5());
						if( jarMod.cacheHit ) {
							parent.log("  Adding to jar (cached).");
						} else {
							parent.log("  Adding to jar (downloaded).");
						}
						_debug(jarMod.getRemoteFilename() + " -> " + jarMod.getDestFile().getPath());
						//FileUtils.copyURLToFile(modURL, modPath);
						Archive.extractZip(jarMod.getDestFile(), tmpFolder);
						jarMod.getDestFile().delete();
					} catch (Exception e) {
						++errorCount;
						parent.log("! "+e.getMessage());
						e.printStackTrace();
					}
				} else if (entry.getExtract()) {
					//modPath = new File(tmpFolder.getPath() + sep + modFilename);
					//modPath.getParentFile().mkdirs();
					//_log(modPath.getPath());
					ModDownload extractMod;
					try {
						extractMod = new ModDownload(modURL, tmpFolder, entry.getMD5());
						if( extractMod.cacheHit ) {
							parent.log("  Extracting to filesystem (cached).");
						} else {
							parent.log("  Extracting to filesystem (downloaded).");
						}
						_debug(extractMod.getRemoteFilename() + " -> " + extractMod.getDestFile().getPath());
						//FileUtils.copyURLToFile(modURL, modPath);
						String outPath = folder.getPath() + sep;
						if(!entry.getInRoot()) outPath += "mods" + sep;
						Archive.extractZip(extractMod.getDestFile(), new File(outPath));
						extractMod.getDestFile().delete();
					} catch (Exception e) {
						++errorCount;
						parent.log("! "+e.getMessage());
						e.printStackTrace();
					}
				} else if (entry.getCoreMod()) {
					modPath = new File(folder.getPath() + sep + "coremods");
					modPath.mkdirs();
					try {
						ModDownload normalMod = new ModDownload(modURL, modPath, entry.getMD5());
						if( normalMod.cacheHit ) {
							parent.log("  Installing in /coremods (cached).");
						} else {
							parent.log("  Installing in /coremods (downloaded).");
						}
						_debug(normalMod.getRemoteFilename() + " -> " + normalMod.getDestFile().getPath());
					} catch (Exception e) {
						++errorCount;
						parent.log("! "+e.getMessage());
						e.printStackTrace();
					}					
				} else {
					modPath = new File(folder.getPath() + sep + "mods");
					modPath.mkdirs();
					//_log(modPath.getPath());
					try {
						ModDownload normalMod = new ModDownload(modURL, modPath, entry.getMD5());
						if( normalMod.cacheHit ) {
							parent.log("  Installing in /mods (cached).");
						} else {
							parent.log("  Installing in /mods (downloaded).");
						}
						_debug(normalMod.getRemoteFilename() + " -> " + normalMod.getDestFile().getPath());
					} catch (Exception e) {
						++errorCount;
						parent.log("! "+e.getMessage());
						e.printStackTrace();
					}
					//FileUtils.copyURLToFile(modURL, modPath);
				}
				Iterator<ConfigFile> itConfigs = entry.getConfigs().iterator();
				while(itConfigs.hasNext()) {
					final ConfigFile cfEntry = itConfigs.next();
					final String MD5 = cfEntry.getMD5(); 
					_debug(cfEntry.getUrl());
					parent.log("  Found config for "+cfEntry.getPath());
					URL configURL = new URL(cfEntry.getUrl());
					final File confFile = new File(MCFolder + sep + cfEntry.getPath());
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
					FileUtils.copyURLToFile(configURL, confFile);
					// save in cache for future reference
					if( MD5 != null ) {
						final boolean cached = DownloadCache.cacheFile(confFile, MD5);
						if( cached ) {
							_debug("\nSaved in cache");							
						}
					}
				}
			} catch (MalformedURLException e) {
				++errorCount;
				parent.log("! "+e.getMessage());
				_log(e.getMessage());
			} catch (IOException e) {
				++errorCount;
				parent.log("! "+e.getMessage());
				e.printStackTrace();
			}
			modsLoaded++;
			parent.setProgressBar((int)( (65 / modCount) * modsLoaded + 25));
			parent.log("  Done ("+modsLoaded+"/"+modCount+")");
		}
		try {
			buildJar.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parent.log("All mods loaded.");
		if( errorCount > 0 ) {
			parent.log("WARNING: Errors were detected with this update, please verify your files. There may be a problem with the serverpack configuration or one of your download sites.");
		}
		copyFile(jar, buildJar);
		List<File> buildList = recurseFolder(tmpFolder,true);
		Iterator<File> blIt = new ArrayList<File>(buildList).iterator();
		while(blIt.hasNext()) {
			File entry = blIt.next();
			if(entry.getPath().contains("META-INF")) {
				buildList.remove(entry);
			}
		}
		parent.log("Packaging updated jar...");
		Archive.createJar(buildJar, buildList, tmpFolder.getPath() + sep);
		//Archive.patchJar(jar, buildJar, new ArrayList<File>(Arrays.asList(tmpFolder.listFiles())));
		//copyFile(buildJar, new File(MCFolder + sep + "bin" + sep + "minecraft.jar"));
		try {
			Path binPath = folder.toPath().resolve("bin");
			Files.copy(buildJar.toPath(), binPath.resolve("minecraft.jar"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<File> tempFiles = recurseFolder(tmpFolder,true);
		ListIterator<File> li = tempFiles.listIterator(tempFiles.size());
		while(li.hasPrevious()) { 
			File entry = li.previous();
			entry.delete();
		}
		
	}
	
	public void writeMCServerFile(String name, String ip) {
		byte[] header = new byte[]{
				0x0A,0x00,0x00,0x09,0x00,0x07,0x73,0x65,0x72,0x76,0x65,0x72,0x73,0x0A,
				0x00,0x00,0x00,0x01,0x08,0x00,0x04,0x6E,0x61,0x6D,0x65,0x00,
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
		File serverFile = new File(MCFolder + sep + "servers.dat");
		try {
			serverFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(serverFile);
			fos.write(full,0,full.length);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		if( INSTANCE.parent != null ) {
			INSTANCE.parent.log(msg);
		} else {
			System.out.println(msg);
		}
	}
	private static void _debug(String msg) {
		System.out.println(msg);
	}

}

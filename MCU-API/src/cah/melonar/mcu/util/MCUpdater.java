package cah.melonar.mcu.util;

import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import cah.melonar.mcu.MCUApp;
import cah.melonar.mcu.util.Archive;

public class MCUpdater {
	
	private List<Module> modList = new ArrayList<Module>();
	private String MCFolder;
	private File archiveFolder;
	private MCUApp parent;
	public final static String sep = System.getProperty("file.separator");
	public MessageDigest md5;

	public MCUpdater()
	{
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			MCFolder = System.getenv("APPDATA") + sep + ".minecraft"; 
		} else if(System.getProperty("os.name").startsWith("Mac"))
		{
			MCFolder = System.getProperty("user.home") + sep + "Library" + sep + "Application Support" + sep + ".minecraft";
		}
		else
		{
			MCFolder = System.getProperty("user.home") + sep + ".minecraft";
		}
		archiveFolder = new File(MCFolder + sep + "mcu");
	}
	
	public MCUApp getParent() {
		return parent;
	}

	public void setParent(MCUApp parent) {
		this.parent = parent;
	}

	public List<Module> loadFromURL(String serverUrl)
	{
		try {
			parseDocument(readXmlFromUrl(serverUrl));
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
			System.out.println("File not found");
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
	
	public List<ServerList> loadServerList()
	{
		List<ServerList> slList = new ArrayList<ServerList>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(archiveFolder.getPath() + sep + "mcuServers.dat"));
			
			String entry = reader.readLine();
			while(entry != null)
			{
				try {
					Document serverHeader = readXmlFromUrl(entry);
					Element docEle = serverHeader.getDocumentElement();
					slList.add(new ServerList(docEle.getAttribute("name"), entry, docEle.getAttribute("newsUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress")));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
//		String[] arrString = entry.split("\\|");
//		slList.add(new ServerList(arrString[0], arrString[1], arrString[2]));
				entry = reader.readLine();
			}
			
			reader.close();
			return slList;
			
		}
		catch( FileNotFoundException notfound)
		{
			System.out.println("File not found");
		}
		catch (IOException x)
		{
			x.printStackTrace();
		}
		return slList;
	}
	
	public void writeServerList(List<ServerList> serverlist)
	{
		try
		{
			archiveFolder.mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter((archiveFolder.getPath() + sep + "mcuServers.dat"),false));
			
			Iterator<ServerList> it = serverlist.iterator();
			
			while(it.hasNext())
			{
				ServerList entry = it.next();
				writer.write(entry.getPackUrl());
				writer.newLine();
			}
			
			writer.close();
		}
		catch( IOException x)
		{
			x.printStackTrace();
		}
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception
	{
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
	
	private void parseDocument(Document dom)
	{
		modList.clear();
		Element docEle = dom.getDocumentElement();
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
		Boolean inJar = getBooleanValue(modEl,"InJar");
		List<ConfigFile> configs = new ArrayList<ConfigFile>();
		NodeList nl = modEl.getElementsByTagName("ConfigFile");
//		System.out.println("NodeList[getLength]: " + nl.getLength());
		for(int i = 0; i < nl.getLength(); i++) 
		{
			Element el = (Element)nl.item(i);
			ConfigFile cf = getConfigFile(el);
			configs.add(cf);
		}
		Module m = new Module(name, url, required, inJar, configs);
		return m;
	}
	
	private ConfigFile getConfigFile(Element cfEl)
	{
		String url = getTextValue(cfEl,"URL");
		String path = getTextValue(cfEl,"Path");
		ConfigFile cf = new ConfigFile(url,path);
		return cf;
	}
	
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
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

	public String getMCVersion() {
		File jar = new File(MCFolder + sep + "bin" + sep + "minecraft.jar");
		byte[] hash;
		try {
			InputStream is = new FileInputStream(jar);
			hash = DigestUtils.md5(is);
			is.close();		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
			URL md5s = new URL("https://sites.google.com/site/smbmcupdater/downloads/md5.dat");
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
			System.out.println("DEBUG: LoadBackupList");
			List<Backup> bList = loadBackupList();
			System.out.println("DEBUG: add");
			bList.add(entry);
			System.out.println("DEBUG: writeBackupList");
			writeBackupList(bList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean getExcludedNames(String path, boolean forDelete) {
		if(path.contains("bin" + sep + "minecraft.jar")) {
			return false;
		}
		if(path.contains("bin" + sep)) {
			return true;
		}
		if(path.contains("resources")) {
			return true;
		}
		if(path.contains("saves")) {
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
		if(path.contains("mcu" + sep)) {
			return true;
		}
		if(path.contains("META-INF" + sep)) {
			return true;
		}
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
	
	public void installMods(ServerList server, List<Module> toInstall) throws FileNotFoundException {
		File folder = new File(MCFolder);
		List<File> contents = recurseFolder(folder, true);
		parent.setLblStatus("Clearing existing configuration");
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
		Iterator<Module> itMods = toInstall.iterator();
		File tmpFolder = new File(MCFolder + sep + "temp");
		tmpFolder.mkdirs();
		File buildJar = new File(archiveFolder.getPath() + sep + "build.jar");
		if(buildJar.exists()) {
			buildJar.delete();
		}
		File jar = new File(archiveFolder.getPath() + sep + "mc-" + server.getVersion() + ".jar");
		if(!jar.exists()) {
			throw new FileNotFoundException("A backup copy of minecraft.jar for version " + server.getVersion() + " was not found.");
		}
		Archive.extractZip(jar, tmpFolder);
		
		int loadOrder = 0;
		int modCount = toInstall.size();
		int modsLoaded = 0;
		while(itMods.hasNext()) {
			Module entry = itMods.next();
			parent.setLblStatus("Mod: " + entry.getName());
			try {
				System.out.println(entry.getUrl());
				URL modURL = new URL(entry.getUrl());
				String modFilename = modURL.getFile().substring(modURL.getFile().lastIndexOf('/'));
				File modPath;
				if(entry.getInJar()) {
					modPath = new File(tmpFolder.getPath() + sep + loadOrder + ".zip");
					loadOrder++;
					System.out.println(modPath.getPath());
					FileUtils.copyURLToFile(modURL, modPath);
					Archive.extractZip(modPath, tmpFolder);
					modPath.delete();
				} else {
					modPath = new File(MCFolder + sep + "mods" + sep + modFilename);
					modPath.getParentFile().mkdirs();
					System.out.println(modPath.getPath());
					FileUtils.copyURLToFile(modURL, modPath);
				}
				Iterator<ConfigFile> itConfigs = entry.getConfigs().iterator();
				while(itConfigs.hasNext()) {
					ConfigFile cfEntry = itConfigs.next();
					System.out.println(cfEntry.getUrl());
					URL configURL = new URL(cfEntry.getUrl());
					File confFile = new File(MCFolder + sep + cfEntry.getPath());
					confFile.getParentFile().mkdirs();
					System.out.println(confFile.getPath());
					FileUtils.copyURLToFile(configURL, confFile);
				}
				modsLoaded++;
				parent.setProgressBar((int)( (65 / modCount) * modsLoaded + 25));
			} catch (MalformedURLException e) {
				//e.printStackTrace();
				System.out.println(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			buildJar.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
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
		Archive.createJar(buildJar, buildList, tmpFolder.getPath() + sep);
		//Archive.patchJar(jar, buildJar, new ArrayList<File>(Arrays.asList(tmpFolder.listFiles())));
		copyFile(buildJar, new File(MCFolder + sep + "bin" + sep + "minecraft.jar"));
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
}
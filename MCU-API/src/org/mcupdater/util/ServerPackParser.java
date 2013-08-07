package org.mcupdater.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mcupdater.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ServerPackParser {

	public static Document readXmlFromFile(File packFile) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(packFile);
		}catch(ParserConfigurationException pce) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", pce);
		}catch(SAXException se) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", se);
		}catch(IOException ioe) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O error", ioe);
		}
		return null;
	}
	
	public static Document readXmlFromUrl(String serverUrl) throws Exception
	{
		MCUpdater.apiLogger.fine("readXMLFromUrl(" + serverUrl + ")");
		if (serverUrl.equals("http://www.example.org/ServerPack.xml")) {
			return null;
		}
		//_log("Reading "+serverUrl+"...");
		final URL server;
		try {
			server = new URL(serverUrl);
		} catch( MalformedURLException e ) {
			MCUpdater.apiLogger.log(Level.WARNING, "Malformed URL", e);
			return null;
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		URLConnection serverConn = server.openConnection();
		serverConn.setRequestProperty("User-Agent", "MCUpdater/" + Version.VERSION);
		serverConn.setConnectTimeout(MCUpdater.getInstance().getTimeout());
		serverConn.setReadTimeout(MCUpdater.getInstance().getTimeout());
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(serverConn.getInputStream());
		}catch(ParserConfigurationException pce) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", pce);
		}catch(SAXException se) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Parser error", se);
		}catch(IOException ioe) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O error", ioe);
		}
		return null;
	}

	private static List<Module> parseDocument(Document dom, String serverId)
	{
		int version;
		List<Module> modList = new ArrayList<Module>();
		Element parent = dom.getDocumentElement();
		Element docEle = null;
		if (parent.getNodeName().equals("ServerPack")){
			if (Version.requestedFeatureLevel(parent.getAttribute("version"),"3.0")) {
				version = 2;
			} else {
				version = 1;
			}
			NodeList servers = parent.getElementsByTagName("Server");
			for (int i = 0; i < servers.getLength(); i++){
				docEle = (Element)servers.item(i);
				if (docEle.getAttribute("id").equals(serverId)) { break; }
			}
		} else {
			docEle = parent;
			version = 1;
		}
		NodeList nl;
		switch (version) {
		case 2:
			// Handle ServerPacks designed for MCUpdater 3.0 and later
			nl = docEle.getElementsByTagName("Import");
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					modList.addAll(doImportV2(el));
				}
			}
			nl = docEle.getElementsByTagName("Module");
			if(nl != null && nl.getLength() > 0)
			{
				for(int i = 0; i < nl.getLength(); i++)
				{
					Element el = (Element)nl.item(i);
					Module m = getModuleV2(el);
					modList.add(m);
				}
			}
			return modList;
			
		case 1:
			// Handle ServerPacks designed for MCUpdater 2.7 and earlier
			nl = docEle.getElementsByTagName("Module");
			if(nl != null && nl.getLength() > 0)
			{
				for(int i = 0; i < nl.getLength(); i++)
				{
					Element el = (Element)nl.item(i);
					Module m = getModuleV1(el);
					modList.add(m);
				}
			}
			return modList;

		default:
			return null;
		}
	}
	
	private static Collection<? extends Module> doImportV2(Element el) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Module getModuleV2(Element el) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Module getModuleV1(Element modEl)
	{
		String name = modEl.getAttribute("name");
		String id = modEl.getAttribute("id");
		PrioritizedURL url = new PrioritizedURL(getTextValue(modEl,"URL"),0);
		List<PrioritizedURL> urls = new ArrayList<PrioritizedURL>();
		urls.add(url);
		String path = getTextValue(modEl,"ModPath");
		String depends = modEl.getAttribute("depends");
		String side = modEl.getAttribute("side");
		Boolean required = getBooleanValue(modEl,"Required");
		Boolean isDefault = getBooleanValue(modEl,"IsDefault");
		Boolean inJar = getBooleanValue(modEl,"InJar");
		int jarOrder = getIntValue(modEl,"JarOrder");
		Boolean keepMeta = getBooleanValue(modEl,"KeepMeta");
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
			ConfigFile cf = getConfigFileV1(el);
			configs.add(cf);
		}
		HashMap<String,String> mapMeta = new HashMap<String,String>();
		NodeList nlMeta = modEl.getElementsByTagName("Meta");
		if (nlMeta.getLength() > 0){
			Element elMeta = (Element) nlMeta.item(0);
			NodeList nlMetaChildren = elMeta.getElementsByTagName("*");
			for(int i = 0; i < nlMetaChildren.getLength(); i++)
			{
				Node child = nlMetaChildren.item(i);
				mapMeta.put(child.getNodeName(), getTextValue(elMeta, child.getNodeName()));
			}
		}
		//TODO:Meta
		Module m = new Module(name, id, urls, depends, required, inJar, jarOrder, keepMeta, extract, inRoot, isDefault, coreMod, md5, configs, side, path, mapMeta);	
		return m;
	}
	
	private static ConfigFile getConfigFileV1(Element cfEl)
	{
		String url = getTextValue(cfEl,"URL");
		String path = getTextValue(cfEl,"Path");
		String md5 = getTextValue(cfEl,"MD5");
		boolean noOverwrite = getBooleanValue(cfEl, "NoOverwrite");
		ConfigFile cf = new ConfigFile(url,path,noOverwrite,md5);
		return cf;
	}
	
	private static int getIntValue(Element ele, String tagName) {
		int value = 0;
		try {
			value = Integer.parseInt(getTextValue(ele,tagName));
		} catch (NumberFormatException e) {			
		}
		return value;
	}
	
	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			if(el != null) {
				Node node = el.getFirstChild();
				if(node != null) textVal = unescapeXML(node.getNodeValue());
			}
		}
		return textVal;
	}
	
	private static String unescapeXML(String nodeValue) {
		return nodeValue.replace("&amp;", "&").replace("&quot;", "\"").replace("&apos;","'").replace("&lt;", "<").replace("&gt;", ">");
	}

	private static Boolean getBooleanValue(Element ele, String tagName) {
		return Boolean.parseBoolean(getTextValue(ele,tagName));
	}

	public static List<Module> loadFromFile(File packFile, String serverId) {
		try {
			return parseDocument(readXmlFromFile(packFile), serverId);
		} catch (Exception e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "General error", e);
			return null;
		}
		//return modList;
	}
	
	public static List<Module> loadFromURL(String serverUrl, String serverId)
	{
		try {
			return parseDocument(readXmlFromUrl(serverUrl), serverId);
		} catch (Exception e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "General error", e);
			return null;
		}
		//return modList;
	}
	
	public static boolean parseBoolean(String attribute, boolean defaultValue) {
		if (attribute.isEmpty()) {
			return defaultValue;
		}
		if (attribute.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

}

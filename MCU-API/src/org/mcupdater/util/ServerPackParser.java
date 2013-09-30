package org.mcupdater.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.mcupdater.Version;
import org.w3c.dom.DOMException;
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
		if (serverUrl.equals("http://www.example.org/ServerPack.xml") || serverUrl.isEmpty()) {
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
		//TODO: Pass the username as a header
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
		MCUpdater.apiLogger.log(Level.FINE, serverId + ": format=" + version);
		NodeList nl;
		switch (version) {
		case 2:
			// Handle ServerPacks designed for MCUpdater 3.0 and later
			nl = docEle.getElementsByTagName("Import");
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0; i < nl.getLength(); i++) {
					Element el = (Element)nl.item(i);
					modList.addAll(doImportV2(el, dom));
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
	
	private static List<Module> doImportV2(Element el, Document dom) {
		String url = el.getAttribute("url");
		if (!url.isEmpty()){
			try {
				dom = readXmlFromUrl(url);
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return parseDocument(dom, el.getTextContent());
	}

	private static Module getModuleV2(Element el) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {

			String name = el.getAttribute("name");
			String id = el.getAttribute("id");
			String depends = el.getAttribute("depends");
			String side = el.getAttribute("side");
			List<PrioritizedURL> urls = new ArrayList<PrioritizedURL>();
			NodeList nl;
			nl = (NodeList) xpath.evaluate("URL", el, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				Element elURL = (Element) nl.item(i);
				String url = elURL.getTextContent();
				int priority = parseInt(elURL.getAttribute("priority"));
				urls.add(new PrioritizedURL(url, priority));
			}
			String path = (String) xpath.evaluate("ModPath", el, XPathConstants.STRING);
			Element elReq = (Element) el.getElementsByTagName("Required").item(0);
			boolean required;
			boolean isDefault;
			if (elReq == null) {
				required = true;
				isDefault = true;
			} else {
				required = parseBooleanWithDefault(elReq.getTextContent(),true);
				isDefault = parseBooleanWithDefault(elReq.getAttribute("isDefault"),false);
			}
			Element elType = (Element) el.getElementsByTagName("ModType").item(0);
			boolean inRoot = parseBooleanWithDefault(elType.getAttribute("inRoot"),false);
			int order = parseInt(elType.getAttribute("order"));
			boolean keepMeta = parseBooleanWithDefault(elType.getAttribute("keepMeta"),false);
			String launchArgs = elType.getAttribute("launchArgs");
			String jreArgs = elType.getAttribute("jreArgs");
			ModType modType = ModType.valueOf(elType.getTextContent());
			boolean coremod = false;
			boolean jar = false;
			boolean library = false;
			boolean extract = false;
			boolean litemod = false;
			switch (modType) {
			case Coremod:
				coremod = true;
				break;
			case Extract:
				extract = true;
				break;
			case Jar:
				jar = true;
				break;
			case Library:
				library = true;
				break;
			case Litemod:
				litemod = true;
				break;
			case Option:
				throw new RuntimeException("Module type 'Option' not implemented");
			default:
				break;
			}
			String md5 = (String) xpath.evaluate("MD5", el, XPathConstants.STRING);
			List<ConfigFile> configs = new ArrayList<ConfigFile>();
			List<Module> submodules = new ArrayList<Module>();
			nl = el.getElementsByTagName("ConfigFile");
			for(int i = 0; i < nl.getLength(); i++) 
			{
				Element elConfig = (Element)nl.item(i);
				ConfigFile cf = getConfigFileV1(elConfig);
				configs.add(cf);
			}
			nl = el.getElementsByTagName("Submodule");
			for(int i = 0; i < nl.getLength(); i++)
			{
				Element elSubmod = (Element)nl.item(i);
				Module sm = getModuleV2(elSubmod);
				submodules.add(sm);
			}
			HashMap<String,String> mapMeta = new HashMap<String,String>();
			NodeList nlMeta = el.getElementsByTagName("Meta");
			if (nlMeta.getLength() > 0){
				Element elMeta = (Element) nlMeta.item(0);
				NodeList nlMetaChildren = elMeta.getElementsByTagName("*");
				for(int i = 0; i < nlMetaChildren.getLength(); i++)
				{
					Node child = nlMetaChildren.item(i);
					mapMeta.put(child.getNodeName(), getTextValue(elMeta, child.getNodeName()));
				}
			}
			Module m = new Module(name, id, urls, depends, required, jar, order, keepMeta, extract, inRoot, isDefault, coremod, md5, configs, side, path, mapMeta, library, litemod, launchArgs, jreArgs, submodules);	
			return m;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static int parseInt(String attribute) {
		try {
			return Integer.parseInt(attribute);
		} catch (NumberFormatException e) {
			return 0;
		}
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
		return parseInt(getTextValue(ele,tagName));
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
		return parseBooleanWithDefault(getTextValue(ele,tagName), false);
	}

	private static Boolean parseBooleanWithDefault(String textValue, boolean state) {
		try {
			return Boolean.parseBoolean(textValue);
		} catch (Exception e) {
			return state;
		}
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

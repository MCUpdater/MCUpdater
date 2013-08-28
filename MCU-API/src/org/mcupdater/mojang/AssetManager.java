package org.mcupdater.mojang;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mcupdater.DownloadQueue;
import org.mcupdater.Downloadable;
import org.mcupdater.TrackerListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AssetManager {
	public static DownloadQueue downloadAssets(File baseDirectory, TrackerListener listener) {
		DownloadQueue queue = new DownloadQueue("Assets", "Minecraft", listener, getAssets(baseDirectory), baseDirectory, null);
		return queue;
	}
	
	private static Set<Downloadable> getAssets(File baseDirectory){
		Set<Downloadable> assets = new HashSet<Downloadable>();
		try {
			URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document doc = db.parse(resourceUrl.openConnection().getInputStream());
		    NodeList nodes = doc.getElementsByTagName("Contents");
		    
		    for (int i = 0; i < nodes.getLength(); i++) {
		    	Node node = nodes.item(i);
		    	if (node.getNodeType() == 1) {
		    		Element element = (Element)node;
		    		String key = getNodeValue(element, "Key");
		    		String etag = element.getElementsByTagName("ETag") != null ? getNodeValue(element,"ETag") : "-";
		    		long size = Long.parseLong(getNodeValue(element,"Size"));
		    		
		    		if (size > 0L) {
		    			File file = new File(baseDirectory, key);
		    			if (etag.length() > 1) {
		    				etag = scrubEtag(etag);
		    				if ((file.isFile()) && (file.length() == size)) {
		    					String localMD5 = Downloadable.getMD5(file);
		    					if (localMD5.equals(etag)) continue;
		    				}
		    			}
		    			List<URL> urls = new ArrayList<URL>();
		    			urls.add(new URL(resourceUrl + key));
		    			Downloadable download = new Downloadable(key, key, etag, size, urls);
		    			assets.add(download);
		    		}
		    	}
		    }
		} catch (Exception e) {}
		return assets;
	}
	
	private static String scrubEtag(String etag) {
		if (etag == null) {
			etag = "-";
		} else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
			etag = etag.substring(1, etag.length() - 1);
		}
		return etag;
	}

	private static String getNodeValue(Element element, String key) {
		return element.getElementsByTagName(key).item(0).getChildNodes().item(0).getNodeValue();
	}
}

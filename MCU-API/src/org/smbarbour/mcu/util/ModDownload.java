package org.smbarbour.mcu.util;
// Credit for this class goes to Peter Koeleman, who graciously provided the initial code.

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;

import org.apache.commons.io.FileUtils;
import org.smbarbour.mcu.Version;

public class ModDownload extends javax.swing.text.html.HTMLEditorKit.ParserCallback {

	@SuppressWarnings("static-access")
	Logger logger = MCUpdater.getInstance().apiLogger.getLogger("ModDownload");
	private boolean isAdfly = false, isMediafire = false, isOptifined = false, readingScript = false;
	private String redirectURL = null;
	//private String remoteFilename;
	private File destFile = null;
	public final URL url;
	public final String expectedMD5;
	private String content;
	
	public boolean cacheHit = false;

	public ModDownload(URL url, File destination, String MD5) throws Exception {
		this(url, destination, null, MD5);
	}
	public ModDownload(URL url, File destination) throws Exception {
		this(url, destination, null, null);
	}
	public ModDownload(URL url, File destination, ModDownload referer) throws Exception {
		this(url, destination, referer, null);
	}

	public ModDownload(URL url, File destination, ModDownload referer, String MD5) throws Exception {
		this.url = url;
		this.expectedMD5 = MD5;
		logger.fine("URL: " + url.toString());
		//this.remoteFilename = url.getFile().substring(url.getFile().lastIndexOf('/')+1);
		// TODO: check for md5 in download cache first
		if( MD5 != null ) {
			final File cacheFile = DownloadCache.getFile(MD5);
			if( cacheFile.exists() ) {
				cacheHit = true;
				logger.fine(" Cache hit - "+MD5);
				this.destFile = destination;
				FileUtils.copyFile(cacheFile, this.destFile);
				return;
			} else {
				logger.fine(" Cache miss - "+MD5);
			}
		}
		if (url.getProtocol().equals("file")){
			this.destFile = destination;
			FileUtils.copyURLToFile(url, this.destFile);
			return;
		}
		isOptifined = url.getHost().endsWith("optifined.net");
		isMediafire = url.getHost().toLowerCase().contains("mediafire");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("User-Agent", "MCUpdater/" + Version.VERSION);
		if (referer != null)
			connection.setRequestProperty("Referer", referer.url.toString());
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(false);
		logger.fine(" Downloading: "+url);printheaders(connection.getHeaderFields());
		if (connection.getResponseCode() / 100 == 3) {
			String newLocation = connection.getHeaderField("Location");
			url = redirect(url, newLocation);
			ModDownload redirect = new ModDownload(url, destination, this, MD5);
			//this.remoteFilename = redirect.getRemoteFilename();
			this.destFile = redirect.getDestFile();
			return;
		}
		String contentType = connection.getContentType();
		logger.fine(" Content type: "+contentType);
		if( contentType == null ) {
			logger.warning(" No content type found, download server may have issues.");
		} else if (contentType.toLowerCase().startsWith("text/html")) {
//			InputStreamReader sr = new InputStreamReader(connection.getInputStream());
//			StringWriter sw = new StringWriter();
//			IOUtils.copy(sr,sw);
//			sr.close();
//			content = sw.toString();
			//System.out.println(content);
//			sw.close();
			InputStreamReader r = new InputStreamReader(connection.getInputStream());
			javax.swing.text.html.HTMLEditorKit.Parser parser;
			parser = new javax.swing.text.html.parser.ParserDelegator();
			parser.parse(r, this, true);
			r.close();
			connection = null;
		}
		if (redirectURL != null) {
			url = redirect(url, redirectURL);
			ModDownload redirect = new ModDownload(url, destination, this, MD5);
			//this.remoteFilename = redirect.getRemoteFilename();
			this.destFile = redirect.getDestFile();
			return;
		}
		if (connection != null) {
			this.destFile = destination; 
			InputStream is = connection.getInputStream();
			//InputStreamReader isr = new InputStreamReader(connection.getInputStream());
			FileOutputStream fos = new FileOutputStream(this.destFile);
			byte[] inBuffer = new byte[4096];
			int bytesRead;
		    while (((bytesRead = is.read(inBuffer)) != -1)) {
		    	byte[] outBuffer = Arrays.copyOf(inBuffer, bytesRead);
		    	fos.write(outBuffer);
		    }
			is.close();
			fos.close();
			// verify md5 && cache the newly retrieved file
			if( MD5 != null ) {
				final boolean cached = DownloadCache.cacheFile(destFile, MD5);
				if( cached ) {
					logger.fine(destFile.getName() + " saved in cache");
				}
			}
		}
		if (connection == null && redirectURL == null) {
			logger.finest(content);
		}
	}

	public File getDestFile() {
		return this.destFile;
	}
	
	public static void printheaders(Map<String, List<String>> headers) {
		System.out.println("\nPrinting headers:\n=====================");
		Iterator<String> it = headers.keySet().iterator();
		while (it.hasNext()) {
			String header = it.next();
			System.out.println(header);
			List<String> ls = headers.get(header);
			for (String t : ls)
				System.out.println("\t"+t);
		}
		System.out.println("=====================");
	}

	@Override
	public void handleStartTag(Tag t, MutableAttributeSet attributes, int pos) {
		if (t == Tag.HTML) {
			Enumeration<?> e = attributes.getAttributeNames();
		    while (e.hasMoreElements()) {
		    	Object name = e.nextElement();
		        String value = "";
				try {
					value = (String) attributes.getAttribute(name);
				} catch (Exception e1) {}
		        if (name == HTML.Attribute.ID && value.equalsIgnoreCase("adfly_html"))
		        	isAdfly = true;
		    }
		}
		if (isOptifined && t == Tag.A) {
			Enumeration<?> e = attributes.getAttributeNames();
		    while (e.hasMoreElements()) {
		    	Object name = e.nextElement();
		        String value = (String) attributes.getAttribute(name);
		        if (name == HTML.Attribute.HREF && value.startsWith("downloadx.php"))
		        	redirectURL = value;
		    }
		}
		// MediaFire detection - 11-Mar-2013
		if (t == Tag.A) {
			Enumeration<?> e = attributes.getAttributeNames();
		    while (e.hasMoreElements()) {
		    	Object name = e.nextElement();
		    	String value = (String) attributes.getAttribute(name);
		    	String potentialLink = "";
		    	if (name == HTML.Attribute.HREF) {
		    		try {
						URL temp = new URL(value);
						if (temp.getHost().matches("(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))")) {
							redirectURL = value;
							logger.fine("Link found: " + potentialLink);
						}
					} catch (MalformedURLException e1) {}
		    	}
		    }			
		}
		readingScript = (t == Tag.SCRIPT);
	}

	@Override
	public void handleSimpleTag(Tag t, MutableAttributeSet attributes, int pos) {
		if (t == Tag.META) {
			Enumeration<?> e = attributes.getAttributeNames();
			boolean readsitename = false, contentmediafire = false;
			//boolean httprefresh = false;
			//String localRedirectURL = null;
		    while (e.hasMoreElements()) {
		    	Object name = e.nextElement();
		        String value = (String) attributes.getAttribute(name);
		        //System.out.println("name: "+name.toString()+" value: "+value);
		        if (name.toString().equalsIgnoreCase("property") && value.equalsIgnoreCase("og:site_name"))
		        	readsitename = true;
		        if (name.toString().equalsIgnoreCase("content") && value.equals("MediaFire"))
		        	contentmediafire = true;
//		        if (name == HTML.Attribute.HTTPEQUIV && value.equalsIgnoreCase("Refresh"))
//		        	httprefresh = true;
//		        if (name == HTML.Attribute.CONTENT) {
//		        	String[] tokens = value.split(";");
//		        	for (String token : tokens) {
//		        		String[] parts = token.split("=");
//		        		if (parts.length == 2 && parts[0].trim().equalsIgnoreCase("url")) {
//		        			localRedirectURL = parts[1].trim();
//		        		}
//		        	}
//		        }
		    }
//		    if (httprefresh && localRedirectURL != null)
//		    	redirectURL = localRedirectURL;
		    if (readsitename && contentmediafire) {
		    	isMediafire = true;
		    	logger.fine(" isMediafire: " + isMediafire);
		    }
		}
	}

	@Override
	public void handleComment(char[] data, int pos) {
		if (readingScript) {
			String code = new String(data);
			//System.out.println(code);
			if (isAdfly) {
				String[] tokens = code.split("'");
				for (int j = 0; j < tokens.length; j++) {
					if (tokens[j].startsWith("/go/")) {
						redirectURL = tokens[j];
						break;
					}
					if (tokens[j].startsWith("https://adf.ly/go/")) {
						redirectURL = tokens[j];
						break;
					}
					if (tokens[j].contains("var zzz =")) {
						redirectURL = tokens[j+1];
					}
					//System.out.println("Token: [[ " + tokens[j] + " ]]");
				}
			}
			if (isMediafire) {
				//System.out.println(code);
				String[] tokens = code.split("\"");
				for (int j = 0; j < tokens.length; j++) {
					if (tokens[j].contains("kNO =")) {
						redirectURL = tokens[j+1];
						break;
					}
					//System.out.println("Token: [[ " + tokens[j] + " ]]");
				}
			}
		}
	}

/*
	@Override
	public void handleText(char[] data, int pos) {
		System.out.println("Pos: " + pos + " (" + data.length + " bytes)");
//		if (isMediafire) {
		String code = new String(data);
		System.out.println(code);
		if(code.contains("kNO =")) {
			String[] tokens = code.split(";");
			for (int j = 0; j < tokens.length; j++) {
				if (tokens[j].contains("kNO =")) {
					String[] pair = tokens[j].split("=");
					redirectURL = pair[1].replace("\"", "").replace(" ", "");
				}
			}
		}
//		}
	}
*/
	
	private static URL redirect(URL url, String newLocation) throws MalformedURLException, URISyntaxException {
		newLocation = unescape(newLocation);
		if (newLocation.startsWith("http")) {
			url = new URL(newLocation);
		} else if (!newLocation.startsWith("/")) {
			newLocation = url.getPath().substring(0, url.getPath().lastIndexOf('/')+1) + newLocation;
			url = new URL(url.getProtocol()+"://"+url.getHost()+newLocation);
		} else {
			url = new URL(url.getProtocol()+"://"+url.getHost()+newLocation);
		}
		URI nu = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null);
		return nu.toURL();
	}

	private static String unescape(String s) {
		int index = -1;
		do {
			index = s.indexOf("%", index);
			if (index == -1) break;
			if (s.charAt(index + 1) == '%') continue;
			String charcode;
			char c;
			try {
				charcode = s.substring(index + 1, index + 3);
				c = (char)Integer.parseInt(charcode, 16);
			} catch (IndexOutOfBoundsException ioobe) {
				break;
			}
			s = s.substring(0, index) + c + ((s.length() > index + 3) ? s.substring(index + 3) : "");
		} while (index != -1);
		return s;
	}

//	public String getRemoteFilename() {
//		return unescape(remoteFilename);
//	}

	/* Code for testing.  Not needed for API usage.
	public static void main(String[] args) {
		try {
			URL url;
			url = new URL("http://adf.ly/3X3dW"); // NEI
			url = new URL("http://goo.gl/x5mGr"); // REI's minimap
			url = new URL("http://adf.ly/89H6K"); // backpacks
			url = new URL("http://optifined.net/adload.php?f=OptiFine_1.2.5_HD_MT_C6.zip");
			File tmp = new File("/tmp/testdownload");
			System.out.println("File downloaded from "+url+": "+(new ModDownload(url, tmp)).remoteFilename+" into "+tmp);
			//url = new URL("http://www.example.com");
			//url = redirect(url, "/Files/goto.php?file=WR-CBE%20Core-Client");
			//System.out.println(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}

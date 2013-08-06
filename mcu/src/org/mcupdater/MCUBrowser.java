package org.mcupdater;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JTextPane;

import org.mcupdater.Version;

public class MCUBrowser extends JTextPane {
	private static final long serialVersionUID = 4058068077578841597L;

	@Override
	protected InputStream getStream(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", "MCUpdater/" + Version.VERSION);
		return conn.getInputStream();
	}
}

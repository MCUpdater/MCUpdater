package org.smbarbour.mcu;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.smbarbour.mcu.util.LoginData;
import org.smbarbour.mcu.util.MCUpdater;

public class MCULaunchApplet extends Applet implements AppletStub {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6604634479463399020L;
	private final Map<String, String> params;
	private Applet mcApplet;
	private boolean active;
	
	public MCULaunchApplet(Path instance, Path lwjgl, LoginData login, String host, String port) {
		params = new HashMap<String, String>();
		params.put("username", login.getUserName());
		params.put("sessionid", login.getSessionId());
		params.put("stand-alone", "true");
		params.put("server", host);
		params.put("port", port);
		URL[] urls = new URL[4];
		urls[0] = pathToUrl(instance.resolve("bin").resolve("minecraft.jar"));
		urls[1] = pathToUrl(lwjgl.resolve("lwjgl.jar"));
		urls[2] = pathToUrl(lwjgl.resolve("lwjgl_util.jar"));
		urls[3] = pathToUrl(lwjgl.resolve("jinput.jar"));
		URLClassLoader classLoader = new URLClassLoader(urls, MCUpdater.class.getClassLoader());
		try {
			mcApplet = (Applet)classLoader.loadClass("net.minecraft.client.MinecraftApplet").newInstance();
			this.add(mcApplet, "Center");
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			classLoader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private URL pathToUrl(Path path) {
		try {
			return path.toUri().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void appletResize(int width, int height) {
		mcApplet.resize(width, height);
	}
	
	@Override
	public void resize(int width, int height) {
		mcApplet.resize(width, height);
	}
	
	@Override
	public void resize(Dimension dim) {
		mcApplet.resize(dim);
	}

	@Override
	public String getParameter(String name) {
		String value = params.get(name);
		if (value != null) {
			return value;
		}
		try {
			return super.getParameter(name);
		} catch (Exception e) {}
		return null;
	}
	
	@Override
	public boolean isActive() {
		return this.active;
	}
	
	@Override
	public void init() {
		if (mcApplet != null) {
			mcApplet.init();
		}
	}
	
	@Override
	public void start() {
		mcApplet.start();
		active = true;
	}
	
	@Override
	public void stop() {
		mcApplet.stop();
		active = false;
	}
	
	@Override
	public URL getCodeBase() {
		return mcApplet.getCodeBase();
	}
	
	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		mcApplet.setVisible(flag);
	}
}

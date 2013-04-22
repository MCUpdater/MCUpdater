package net.minecraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class Launcher extends Applet implements AppletStub {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6604634479463399020L;
	private final Map<String, String> params;
	private Applet mcApplet;
	private boolean active;
	private URLClassLoader classLoader;
	
	public Launcher(File instance, File lwjgl, String username, String sessionid, String host, String port, boolean doConnect) {
		params = new HashMap<String, String>();
		params.put("username", username);
		params.put("sessionid", sessionid);
		params.put("stand-alone", "true");
		if (doConnect) {
			params.put("server", host);
			params.put("port", port);
		}
		params.put("fullscreen","false"); //Required param for vanilla. Forge handles the absence gracefully.
		URL[] urls = new URL[4];
		urls[0] = pathToUrl(new File(new File(instance,"bin"), "minecraft.jar"));
		urls[1] = pathToUrl(new File(lwjgl, "lwjgl.jar"));
		urls[2] = pathToUrl(new File(lwjgl, "lwjgl_util.jar"));
		urls[3] = pathToUrl(new File(lwjgl, "jinput.jar"));
		classLoader = new URLClassLoader(urls, Launcher.class.getClassLoader());
		try {
			Class<?> minecraft = classLoader.loadClass("net.minecraft.client.Minecraft");
			Field pathField = findPathField(minecraft);
			if (pathField == null) {
				System.err.println("Unable to find a matching field.  Aborting launch.");
				System.exit(-1);
			}
			pathField.setAccessible(true);
			pathField.set(null, instance);
			mcApplet = (Applet)classLoader.loadClass("net.minecraft.client.MinecraftApplet").newInstance();
			this.add(mcApplet, "Center");
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void replace(Applet applet) {
		this.mcApplet = applet;
		
		applet.setStub(this);
		applet.setSize(getWidth(), getHeight());
		
		this.setLayout(new BorderLayout());
		this.add(applet, "Center");
		
		applet.init();
		active = true;
		applet.start();
		validate();
	}

	private Field findPathField(Class<?> minecraft) {
		Field[] fields = minecraft.getDeclaredFields();
		
		for (int i=0; i<fields.length; i++) {
			Field current = fields[i];
			if (current.getType() != File.class || (current.getModifiers() != (Modifier.PRIVATE + Modifier.STATIC))) {continue;}
			return current;
		}
		return null;
	}

	private URL pathToUrl(File path) {
		try {
			return path.toURI().toURL();
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
			mcApplet.setStub(this);
			mcApplet.setSize(getWidth(),getHeight());
			this.setLayout(new BorderLayout());
			this.add(mcApplet, "Center");
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
	public URL getDocumentBase()
	{
		try {
			return new URL("http://www.minecraft.net/game");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		mcApplet.setVisible(flag);
	}
}

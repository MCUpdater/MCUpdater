package org.smbarbour.mcu;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.ImageIcon;

import net.minecraft.Launcher;

import org.smbarbour.mcu.util.LoginData;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.ServerList;

public class MinecraftFrame extends Frame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2949740978305392280L;

	private Launcher applet = null;

	public static void main(String[] args) {
		LoginData login = new LoginData();
		login.setUserName(args[0]);
		login.setSessionId(args[1]);
		ServerList server = new ServerList(null, args[2], null, null, null, null, args[5], true, null);
		Path instPath = new File(args[3]).toPath();
		Path lwjglPath = new File(args[4]).toPath();
		ImageIcon icon = null;
		try {
			icon = new ImageIcon(new URL(args[6]));
		} catch (MalformedURLException e) { 
			System.out.println("No valid icon URL specified in server pack");
		} catch (IndexOutOfBoundsException indexException) {}
		if (icon == null) { icon = MCUpdater.getInstance().defaultIcon; }
			
		MinecraftFrame me = new MinecraftFrame("MCUpdater - " + server.getName(), icon);
		me.launch(instPath, lwjglPath, login, server);
	}
	
	public MinecraftFrame(String title, ImageIcon icon) {
		super(title);
		Image source = icon.getImage();
		int w = source.getWidth(null);
		int h = source.getHeight(null);
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		g2d.drawImage(source, 0, 0, null);
		setIconImage(image);
		this.addWindowListener(this);
		g2d.dispose();
	}
	
	public void launch(Path instance, Path lwjgl, LoginData login, ServerList server) {
		try {
			URI address;
			String port;
			address = new URI("my://" + server.getAddress());
			if (address.getPort() != -1) {
				port = Integer.toString(address.getPort());
			} else {
				port = Integer.toString(25565);
			}
			applet = new Launcher(instance, lwjgl, login, address.getHost(), port);
			System.setProperty("minecraft.applet.TargetDirectory", instance.toString());
			System.setProperty("org.lwjgl.librarypath", lwjgl.resolve("natives").toString());
			System.setProperty("net.java.games.input.librarypath", lwjgl.resolve("natives").toString());
			this.add(applet);
			applet.setPreferredSize(new Dimension(1280, 720));
			this.pack();
			this.setLocationRelativeTo(null);
			this.setResizable(true);
			validate();
			applet.init();
			applet.start();
			setVisible(true);
						
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void windowClosing(WindowEvent e) {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(30000L);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				System.out.println("~~~ Forcing exit! ~~~");
				System.exit(0);
			}
		}.start();
		
		if (applet != null)
		{
			applet.stop();
			applet.destroy();
		}
		System.exit(0);
	}

	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}

}

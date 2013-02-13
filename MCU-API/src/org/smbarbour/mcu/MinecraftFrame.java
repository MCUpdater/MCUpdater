package org.smbarbour.mcu;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.smbarbour.mcu.util.LoginData;
import org.smbarbour.mcu.util.ServerList;

public class MinecraftFrame extends Frame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2949740978305392280L;

	private MCULaunchApplet applet = null;

	public MinecraftFrame(String title, BufferedImage icon) {
		super(title);
		setIconImage(icon);
		this.addWindowListener(this);
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
			applet = new MCULaunchApplet(instance, lwjgl, login, address.getHost(), port);
			this.add(applet);
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

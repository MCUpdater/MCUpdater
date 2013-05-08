package org.smbarbour.mcu;

import j7compat.Path;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.channels.Channels;
//import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.text.Style;

import org.smbarbour.mcu.util.LoginData;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.ServerList;

public class AppletLauncherThread implements GenericLauncherThread, Runnable {

	private MainForm parent;
	private LoginData session;
	private String jrePath;
	private String minMem;
	private String maxMem;
	private String maxPerm;
	private File output;
	private Thread thread;
	private boolean forceKilled;
	private ServerList server;
	private Process task;
	private JButton launchButton;
	private MenuItem killItem;
	private boolean ready;
	private ConsoleArea console;

	public AppletLauncherThread(MainForm parent, LoginData session,	String jrePath, String minMem, String maxMem, String maxPerm, File output, ConsoleArea console, ServerList server) {
		this.parent = parent;
		this.session = session;
		this.jrePath = jrePath;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.maxPerm = maxPerm;
		this.output = output;
		this.console = console;
		this.server = server;
	}

	public static AppletLauncherThread launch(MainForm parent, LoginData session, String jrePath, String minMem, String maxMem, String maxPerm, File output, ConsoleArea console, ServerList server) {
		AppletLauncherThread me = new AppletLauncherThread(parent, session, jrePath, minMem, maxMem, maxPerm, output, console, server);
		return me;
	}

	@Override
	public void run() {
//		File launcher = MCUpdater.getInstance().getArchiveFolder().resolve("MCU-Launcher.jar").toFile();
//		if(!launcher.exists())
//		{
//			try {
//				URL launcherURL = new URL("http://files.mcupdater.com/MCU-Launcher.jar");
//				ReadableByteChannel rbc = Channels.newChannel(launcherURL.openStream());
//				FileOutputStream fos = new FileOutputStream(launcher);
//				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//				fos.close();
//			} catch (MalformedURLException mue) {
//				mue.printStackTrace();
//
//			} catch (IOException ioe) {
//				ioe.printStackTrace();
//			}
//		}
		List<String> jvmOpts = new ArrayList<String>();
		if (!parent.getConfig().getProperty("jvmOpts","").isEmpty()){
			jvmOpts = Arrays.asList(parent.getConfig().getProperty("jvmOpts").split("\\s"));
		}

		String javaBin = "java";
		File binDir;
		if (System.getProperty("os.name").startsWith("Mac")) {
			binDir = new Path(new File(jrePath)).resolve("Commands").toFile();
		} else {
			binDir = new Path(new File(jrePath)).resolve("bin").toFile();
		}
		if( binDir.exists() ) {
			javaBin = new Path(binDir).resolve("java").toString();
		}
		List<String> args = new ArrayList<String>();
        if (!parent.getConfig().getProperty("jvmContainer", "").isEmpty()) {
            args.add(parent.getConfig().getProperty("jvmContainer"));
        }
		args.add(javaBin);
		args.add("-XX:+UseConcMarkSweepGC");
		args.add("-XX:+CMSIncrementalMode");
		args.add("-XX:+AggressiveOpts");
		if (System.getProperty("os.name").startsWith("Mac")) {
			args.add("-Xdock:name=MCUpdater - " + server.getName());
		}
		args.addAll(jvmOpts);
		args.add("-Xms" + this.minMem);
		args.add("-Xmx" + this.maxMem);
		args.add("-XX:PermSize=" + this.maxPerm);
		args.add("-classpath");
		args.add(MCUpdater.getJarFile().toString());
		args.add("org.smbarbour.mcu.MinecraftFrame");
		//args.add("-jar");
		//args.add(launcher.getAbsolutePath());
		args.add(session.getUserName());
		args.add(session.getSessionId());
		args.add(server.getName());
		args.add(MCUpdater.getInstance().getInstanceRoot().resolve(server.getServerId()).toString());
		args.add(MCUpdater.getInstance().getInstanceRoot().resolve(server.getServerId()).resolve("bin").toString());
		args.add((server.getIconUrl().equals("")) ? "https://minecraft.net/favicon.png" : server.getIconUrl());
		args.add(parent.getConfig().getProperty("width"));
		args.add(parent.getConfig().getProperty("height"));
		String address = server.getAddress();
		if (address.isEmpty()) {
			address = "localhost";
		}
		args.add(address);
		args.add(Boolean.toString(server.isAutoConnect() && Boolean.parseBoolean(parent.getConfig().getProperty("allowAutoConnect"))));

		if (!Version.isMasterBranch()) {
			parent.log("Process args:");
			Iterator<String> itArgs = args.iterator();
			while (itArgs.hasNext()) {
				String entry = itArgs.next();
				parent.log(entry);
			}
		}

		ProcessBuilder pb = new ProcessBuilder(args);
		parent.baseLogger.fine("Running on: " + System.getProperty("os.name"));
		if(System.getProperty("os.name").startsWith("Linux")) {
			if (new Path(new File(jrePath)).resolve("lib").resolve("amd64").toFile().exists()) {
				pb.environment().put("LD_LIBRARY_PATH", new Path(new File(jrePath)).resolve("lib").resolve("amd64").toString());
			} else {
				pb.environment().put("LD_LIBRARY_PATH", new Path(new File(jrePath)).resolve("lib").resolve("i386").toString());
			}
//		} else if(System.getProperty("os.name").startsWith("Mac")) {
//			pb.environment().put("DYLD_LIBRARY_PATH", (new File(jrePath)).toPath().resolve("Lib").resolve("amd64").toString());
		}
		pb.redirectErrorStream(true);
		BufferedWriter buffWrite = null;
		try {
			buffWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		} catch (FileNotFoundException e) {
			parent.baseLogger.log(Level.SEVERE, "File not found", e);
		}
		try {
			task = pb.start();
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
			String line;
			buffRead.mark(1024);
			final String firstLine = buffRead.readLine();
			setReady();
			if (firstLine == null ||
					firstLine.startsWith("Error occurred during initialization of VM") ||
					firstLine.startsWith("Could not create the Java virtual machine.")) {
				parent.baseLogger.severe("Failure to launch detected.");
				// fetch the whole error message
				StringBuilder err = new StringBuilder(firstLine);
				while ((line = buffRead.readLine()) != null) {
					err.append('\n');
					err.append(line);
					parent.baseLogger.severe(line);
				}
				JOptionPane.showMessageDialog(null, err);
			} else {
				buffRead.reset();
				minimizeFrame();
				parent.baseLogger.info("Launching client...");
				int counter = 0;
				while ((line = buffRead.readLine()) != null)
				{
					if (buffWrite != null) {
						buffWrite.write(line);
						buffWrite.newLine();
						counter++;
						if (counter >= 20)
						{
							buffWrite.flush();
							counter = 0;
						}
					} else {
						parent.baseLogger.fine(line);
					}
					if( line.length() > 0) {
						Style lineStyle = null;
						if (line.contains("WARNING")) { lineStyle = console.warnStyle; }
						if (line.contains("SEVERE")) { lineStyle = console.errorStyle; }
						console.log(line + "\n", lineStyle);
						//parent.baseLogger.info(line);
					}
				}
			}
			buffWrite.flush();
			buffWrite.close();
			restoreFrame();

			parent.baseLogger.info("Exiting Minecraft"+(forceKilled?" (killed)":""));

		} catch (IOException ioe) {
			parent.baseLogger.log(Level.SEVERE, "I/O Error", ioe);
		}
	}

	private void restoreFrame() {
		parent.restore();
		toggleKillable(false);
	}

	private void minimizeFrame() {
		parent.minimize(true);
		toggleKillable(true);
	}

	private void setReady() {
		ready = true;
		if(launchButton != null) {
			launchButton.setEnabled(true);
		}
	}

	private void toggleKillable(boolean enabled) {
		if( killItem == null ) return;
		killItem.setEnabled(enabled);
		if( enabled ) {
			final AppletLauncherThread thread = this;
			killItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					thread.stop();
				}
			});
		} else {
			for( ActionListener listener : killItem.getActionListeners() ) {
				killItem.removeActionListener(listener);
			}
			killItem = null;
		}
	}

	@Override
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void stop() {
		if( task != null ) {
			final int confirm = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to kill Minecraft?\nThis could result in corrupted world save data.",
				"Kill Minecraft",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if( confirm == JOptionPane.YES_OPTION ) {
				forceKilled  = true;
				try {
					task.destroy();
				} catch( Exception e ) {
					// maximum paranoia here
					parent.baseLogger.log(Level.SEVERE, "General error", e);
				}
			}
		}
	}

//	private void log(String msg) {
//		if( console == null ) return;
//		console.log(msg);
//	}

	@Override
	public void register(MainForm form, JButton btnLaunchMinecraft, MenuItem killItem) {
		launchButton = btnLaunchMinecraft;
		if (!(killItem == null)) {
			this.killItem = killItem;
		}
		if( ready ) {
			setReady();
		}
	}

}

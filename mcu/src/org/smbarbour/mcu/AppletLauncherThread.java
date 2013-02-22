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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.smbarbour.mcu.util.LoginData;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.ServerList;

public class AppletLauncherThread implements GenericLauncherThread, Runnable {

	private ConsoleArea console;
	private MainForm parent;
	private LoginData session;
	private String jrePath;
	private String minMem;
	private String maxMem;
	private File output;
	private Thread thread;
	private boolean forceKilled;
	private ServerList server;
	private Process task;
	private JButton launchButton;
	private MenuItem killItem;
	private boolean ready;

	public AppletLauncherThread(MainForm parent, LoginData session,	String jrePath, String minMem, String maxMem, File output, ServerList server) {
		this.parent = parent;
		this.session = session;
		this.jrePath = jrePath;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.output = output;
		this.server = server;
	}

	public static AppletLauncherThread launch(MainForm parent, LoginData session, String jrePath, String minMem, String maxMem, File output, ConsoleArea console, ServerList server) {
		AppletLauncherThread me = new AppletLauncherThread(parent, session, jrePath, minMem, maxMem, output, server);
		me.console = console;
		console.setText("");
		return me;
	}

	@Override
	public void run() {
		File launcher = MCUpdater.getInstance().getArchiveFolder().resolve("MCU-Launcher.jar").toFile();
		if(!launcher.exists())
		{
			try {
				URL launcherURL = new URL("http://files.mcupdater.com/MCU-Launcher.jar");
				ReadableByteChannel rbc = Channels.newChannel(launcherURL.openStream());
				FileOutputStream fos = new FileOutputStream(launcher);
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
				fos.close();
			} catch (MalformedURLException mue) {
				mue.printStackTrace();

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		List<String> jvmOpts = Arrays.asList(parent.getConfig().getProperty("jvmOpts").split("\\s"));
		
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
		args.add("-jar");
		args.add(launcher.getAbsolutePath());
		args.add(session.getUserName());
		args.add(session.getSessionId());
		args.add(server.getName());
		args.add(MCUpdater.getInstance().getInstanceRoot().resolve(server.getServerId()).toString());
		args.add(MCUpdater.getInstance().getInstanceRoot().resolve(server.getServerId()).resolve("bin").toString());
		args.add((server.getIconUrl().equals("")) ? "https://minecraft.net/favicon.png" : server.getIconUrl());
		args.add(parent.getConfig().getProperty("width"));
		args.add(parent.getConfig().getProperty("height"));
		args.add(server.getAddress());
		
		if (!Version.isMasterBranch()) {
			parent.log("Process args:");
			Iterator<String> itArgs = args.iterator();
			while (itArgs.hasNext()) {
				String entry = itArgs.next();
				parent.log(entry);
			}
		}
		
		ProcessBuilder pb = new ProcessBuilder(args);
		System.out.println("Running on: " + System.getProperty("os.name"));
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
			log(e.getMessage()+"\n");
			e.printStackTrace();
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
				log("!!! Failure to launch detected.\n");
				// fetch the whole error message
				StringBuilder err = new StringBuilder(firstLine);
				while ((line = buffRead.readLine()) != null) {
					err.append('\n');
					err.append(line);
				}
				log(err+"\n");
				JOptionPane.showMessageDialog(null, err);
			} else {
				buffRead.reset();
				minimizeFrame();
				log("* Launching client...\n");
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
						System.out.println(line);
					}
					if( line.length() > 0) {
						log(line+"\n");
					}
				}
			}
			buffWrite.flush();
			buffWrite.close();
			restoreFrame();
			
			log("* Exiting Minecraft"+(forceKilled?" (killed)":"")+"\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
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
					e.printStackTrace();
				}
			}
		}
	}

	private void log(String msg) {
		if( console == null ) return;
		console.log(msg);
	}

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

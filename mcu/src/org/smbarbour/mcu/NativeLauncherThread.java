package org.smbarbour.mcu;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.smbarbour.mcu.util.MCUpdater;

public class NativeLauncherThread implements Runnable {

	private MainForm parent;
	private String jrePath;
	private String minMem;
	private String maxMem;
	private File output;
	private ConsoleArea console;
	private Thread thread;
	private Process task;
	private MenuItem killItem;
	private boolean forceKilled = false;
	private LoginData session;
	private boolean ready;
	private JButton launchButton;

	public NativeLauncherThread(MainForm parent, LoginData session, String jrePath, String minMem, String maxMem, File output) {
		this.parent = parent;
		this.session = session;
		this.jrePath = jrePath;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.output = output;
	}
	
	public static NativeLauncherThread launch(MainForm parent, LoginData session, String jrePath, String minMem, String maxMem, File output, ConsoleArea console) {
		NativeLauncherThread me = new NativeLauncherThread(parent, session, jrePath, minMem, maxMem, output);
		me.console = console;
		console.setText("");
		return me;
	}
	
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

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
	public void run() {
		String javaBin = "java";
		File binDir = new File(jrePath+MCUpdater.sep+"bin");
		if( binDir.exists() ) {
			javaBin = binDir + MCUpdater.sep + "java";
		}

		StringBuilder sbClassPath = new StringBuilder();
		String mcBinPath = parent.mcu.getMCFolder() + MCUpdater.sep + "bin" + MCUpdater.sep;
		sbClassPath.append(handleWhitespace(mcBinPath + "minecraft.jar"));
		sbClassPath.append(MCUpdater.cpDelimiter() + handleWhitespace(mcBinPath + "lwjgl.jar"));
		sbClassPath.append(MCUpdater.cpDelimiter() + handleWhitespace(mcBinPath + "lwjgl_util.jar"));
		sbClassPath.append(MCUpdater.cpDelimiter() + handleWhitespace(mcBinPath + "jinput.jar"));
		
		String jlp = "-Djava.library.path=" + handleWhitespace(mcBinPath + "natives");
		String className = "net.minecraft.client.Minecraft";
		
		List<String> args = new ArrayList<String>();
		args.add(javaBin);
		args.add("-XX:+UseConcMarkSweepGC");
		args.add("-XX:+CMSIncrementalMode");
		args.add("-XX:+AggressiveOpts");
		args.add("-Xms" + this.minMem);
		args.add("-Xmx" + this.maxMem);
		args.add("-classpath");
		args.add(sbClassPath.toString());
		args.add(jlp);
		args.add(className);
		args.add(session.getUserName());
		args.add(session.getSessionId());
		
		if (!Version.isMasterBranch()) {
			parent.log("Process args:");
			Iterator<String> itArgs = args.iterator();
			while (itArgs.hasNext()) {
				String entry = itArgs.next();
				parent.log(entry);
			}
		}
		
		ProcessBuilder pb = new ProcessBuilder(args);
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

	private String handleWhitespace(String path) {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return "\"" + path + "\"";
		} else {
			return path.replace(" ", "\\ ");
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

	public void register(JButton btnLaunchMinecraft, MenuItem killItem) {
		launchButton = btnLaunchMinecraft;
		this.killItem = killItem;
		if( ready ) {
			setReady();
		}
	}
	
	private void toggleKillable(boolean enabled) {
		if( killItem == null ) return;
		killItem.setEnabled(enabled);
		if( enabled ) {
			final NativeLauncherThread thread = this;
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

}

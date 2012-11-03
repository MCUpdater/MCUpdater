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

import javax.swing.JButton;
import javax.swing.JOptionPane;

public class LauncherThread implements Runnable {
	private File launcher;
	private String minMem;
	private String maxMem;
	private File output;
	private boolean suppressUpdates;
	
	private boolean ready;
	private ConsoleArea console;
	private JButton launchButton;
	private MainForm form;
	private Thread thread;
	private Process task;
	private MenuItem killItem;
	private boolean forceKilled = false;

	public LauncherThread(File launcher, String minMem, String maxMem, boolean suppressUpdates, File output)
	{
		this.launcher = launcher;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.output = output;
		this.suppressUpdates = suppressUpdates;
		ready = false;
	}
	
	public static LauncherThread launch(File launcher, String minMem, String maxMem, boolean suppressUpdates, File output, ConsoleArea console)
	{
		LauncherThread me = new LauncherThread(launcher, minMem, maxMem, suppressUpdates, output);
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
		String suppress = "";
		if (this.suppressUpdates) {
			suppress = "--noupdate";
		}
		ProcessBuilder pb = new ProcessBuilder("java","-Xms"+minMem, "-Xmx"+maxMem, "-jar", launcher.getPath(), suppress);
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
		form.restore();
		toggleKillable(false);
	}

	private void minimizeFrame() {
		form.minimize(true);
		toggleKillable(true);
	}

	private void setReady() {
		ready = true;
		if(launchButton != null) {
			launchButton.setEnabled(true);
		}
	}

	public void register(MainForm form, JButton btnLaunchMinecraft, MenuItem killItem) {
		launchButton = btnLaunchMinecraft;
		this.killItem = killItem;
		this.form = form;
		if( ready ) {
			setReady();
		}
	}
	
	private void toggleKillable(boolean enabled) {
		if( killItem == null ) return;
		killItem.setEnabled(enabled);
		if( enabled ) {
			final LauncherThread thread = this;
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

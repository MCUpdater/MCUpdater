package org.smbarbour.mcu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public class LauncherThread implements Runnable {
	File launcher;
	String minMem;
	String maxMem;
	File output;
	boolean suppressUpdates;
	
	JTextArea console;

	public LauncherThread(File launcher, String minMem, String maxMem, boolean suppressUpdates, File output)
	{
		this.launcher = launcher;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.output = output;
		this.suppressUpdates = suppressUpdates;
	}
	
	public static void launch(File launcher, String minMem, String maxMem, boolean suppressUpdates, File output, JTextArea console)
	{
		LauncherThread me = new LauncherThread(launcher, minMem, maxMem, suppressUpdates, output);
		me.console = console;
		console.setText("");
		(new Thread(me)).start();
	}
	
	private void log(String msg) {
		if( console == null ) return;
		console.append(msg);
		console.setCaretPosition(console.getCaretPosition()+msg.length());
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
			Process task = pb.start();
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
			String line;
			buffRead.mark(1024);
			final String firstLine = buffRead.readLine();
			if (firstLine == null ||
					firstLine.startsWith("Error occurred during initialization of VM") ||
					firstLine.startsWith("Could not create the Java virtual machine.")) {
				log("Failure to launch detected.\n");
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
				log("Launching client...\n");
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
					log(line+"\n");
				}
			}
			buffWrite.flush();
			buffWrite.close();
			
			log("!!! Exiting Minecraft\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

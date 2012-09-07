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

public class LauncherThread implements Runnable {
	File launcher;
	String minMem;
	String maxMem;
	File output;
	boolean suppressUpdates;

	public LauncherThread(File launcher, String minMem, String maxMem, boolean suppressUpdates, File output)
	{
		this.launcher = launcher;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.output = output;
		this.suppressUpdates = suppressUpdates;
	}
	
	public static void launch(File launcher, String minMem, String maxMem, boolean suppressUpdates, File output)
	{
		(new Thread(new LauncherThread(launcher, minMem, maxMem, suppressUpdates, output))).start();
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
				//System.out.println("Failure to launch detected.");
				// fetch the whole error message
				StringBuilder err = new StringBuilder(firstLine);
				while ((line = buffRead.readLine()) != null) {
					err.append('\n');
					err.append(line);
				}
				JOptionPane.showMessageDialog(null, err);
			} else {
				buffRead.reset();
				//System.out.println("Launching client...");
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
				}
			}
			buffWrite.flush();
			buffWrite.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

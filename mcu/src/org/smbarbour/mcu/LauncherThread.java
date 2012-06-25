package org.smbarbour.mcu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class LauncherThread implements Runnable {
	File launcher;
	String minMem;
	String maxMem;
	File output;

	public LauncherThread(File launcher, String minMem, String maxMem, File output)
	{
		this.launcher = launcher;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.output = output;
	}
	
	public static void launch(File launcher, String minMem, String maxMem, File output)
	{
		(new Thread(new LauncherThread(launcher, minMem, maxMem, output))).start();
	}
	
	@Override
	public void run() {
		ProcessBuilder pb = new ProcessBuilder("java","-Xms"+minMem, "-Xmx"+maxMem, "-jar", launcher.getPath());
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
			buffWrite.flush();
			buffWrite.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

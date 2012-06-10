package org.smbarbour.mcu;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class LauncherThread implements Runnable {
	File launcher;

	public LauncherThread(File launcher)
	{
		this.launcher = launcher;
	}
	
	public static void launch(File launcher)
	{
		(new Thread(new LauncherThread(launcher))).start();
	}
	
	@Override
	public void run() {
		ProcessBuilder pb = new ProcessBuilder("java","-jar",launcher.getPath());
		pb.redirectErrorStream(true);
		try {
			Process task = pb.start();
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
			String line;
			while ((line = buffRead.readLine()) != null)
			{
				System.out.println(line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
	}
}

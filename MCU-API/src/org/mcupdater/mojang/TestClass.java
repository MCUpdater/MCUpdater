package org.mcupdater.mojang;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.mcupdater.DownloadQueue;
import org.mcupdater.Downloadable;
import org.mcupdater.TrackerListener;
import org.mcupdater.util.Archive;
import org.mcupdater.util.MCUpdater;

public class TestClass {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		MCUpdater.getInstance();
		String versionNum = "1.6.2";
		MinecraftVersion version = MinecraftVersion.loadVersion(versionNum);
		
		/*
		System.out.println(version.getId());
		for (Library lib : version.getLibraries()) {
			System.out.println("  lib: " + lib.getName() + " (" + lib.getDownloadUrl() + ") OS Valid: " + lib.validForOS());
		}
		*/
		
		TrackerListener listener = new TrackerListener() {
			@Override
			public void printMessage(String msg) { System.out.println(msg); }
			@Override
			public void onQueueProgress(DownloadQueue queue) { System.out.println("Progress: " + queue.getName() + " - " + queue.getProgress()); }
			@Override
			public void onQueueFinished(DownloadQueue queue) { System.out.println("Finished: " + queue.getName()); }
		};
		File base = new File("/home/sbarbour/MCU3-Test");
		base.mkdirs();
		DownloadQueue q1 = AssetManager.downloadAssets(new File(base, "assets"), listener);
		HashSet<Downloadable> libSet = new HashSet<Downloadable>();
		List<String> extract = new ArrayList<String>();
		for (Library lib : version.getLibraries()) {
			if (lib.validForOS()) {
				List<URL> urls = new ArrayList<URL>();
				urls.add(new URL(lib.getDownloadUrl()));
				Downloadable entry = new Downloadable(lib.getName(),lib.getFilename(),"",0,urls);
				libSet.add(entry);
				if (lib.hasNatives()) {
					extract.add(lib.getFilename());
				}
			}
		}
		DownloadQueue q2 = new DownloadQueue("Libraries", "Test", listener, libSet, new File(base, "lib"), null);
		HashSet<Downloadable> jar = new HashSet<Downloadable>();
		List<URL> jarUrl = new ArrayList<URL>();
		jarUrl.add(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + versionNum + "/" + versionNum + ".jar"));
		jar.add(new Downloadable("Minecraft Jar", "mc-" + versionNum +".jar", "", 0, jarUrl));
		DownloadQueue q3 = new DownloadQueue("Main", "Test", listener, jar, base, null);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 8, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		q1.processQueue(executor);
		q2.processQueue(executor);
		q3.processQueue(executor);
		while(!(q1.isFinished() && q2.isFinished() && q3.isFinished())){
			try {
				Thread.sleep(500);
				System.out.println("Waiting...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (String entry : extract){
			Archive.extractZip(new File(new File(base,"lib"), entry),new File(new File(base,"lib"),"natives"),false);
		}
		StringBuilder classpath = new StringBuilder();
		synchronized(q2.getSuccesses()) {
			for (Downloadable d : q2.getSuccesses()) {
				classpath.append(MCUpdater.cpDelimiter() + new File(new File(base, "lib"), d.getFilename()).getAbsolutePath());
			}
		}
		synchronized(q3.getSuccesses()) {
			for (Downloadable d : q3.getSuccesses()) {
				classpath.append(MCUpdater.cpDelimiter() + new File(base, d.getFilename()).getAbsolutePath());
			}
		}
		System.out.println("CP: " + classpath.toString());
		List<String> procArgs = new ArrayList<String>();
		procArgs.add("/usr/bin/java");
		procArgs.add("-Xmx2G");
		procArgs.add("-Djava.library.path="+ new File(new File(base, "lib"),"natives").getAbsolutePath());
		procArgs.add("-cp");
		procArgs.add(classpath.toString().substring(1));
		procArgs.add(version.getMainClass());
		procArgs.add("--username");
		procArgs.add("Melonar");
		procArgs.add("--session");
		procArgs.add("InvalidToken");
		procArgs.add("--version");
		procArgs.add(version.getId());
		procArgs.add("--gameDir");
		procArgs.add(base.getAbsolutePath());
		procArgs.add("--assetsDir");
		procArgs.add(new File(base,"assets").getAbsolutePath());
		procArgs.add("--fullscreen");
		
		System.out.println(procArgs.toArray(new String[0]));
		ProcessBuilder pb = new ProcessBuilder(procArgs);
		pb.directory(base);
		pb.redirectErrorStream(true);
		try {
			Process task = pb.start();
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
			String line;
			while ((line = buffRead.readLine()) != null)
			{
				if( line.length() > 0) {
					System.out.println(line);
					//parent.baseLogger.info(line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

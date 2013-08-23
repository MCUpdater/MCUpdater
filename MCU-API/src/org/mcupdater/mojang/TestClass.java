package org.mcupdater.mojang;

import java.io.File;
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

public class TestClass {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		String versionNum = "1.6.2";
		MinecraftVersion version = MinecraftVersion.loadVersion(versionNum);
		String cp = "";
		
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
		for (Library lib : version.getLibraries()) {
			if (lib.validForOS()) {
				List<URL> urls = new ArrayList<URL>();
				urls.add(new URL(lib.getDownloadUrl()));
				Downloadable entry = new Downloadable(lib.getName(),lib.getFilename(),"",0,urls);
				libSet.add(entry);
			}
		}
		DownloadQueue q2 = new DownloadQueue("Libraries", listener, libSet, new File(base, "lib"));
		HashSet<Downloadable> jar = new HashSet<Downloadable>();
		List<URL> jarUrl = new ArrayList<URL>();
		jarUrl.add(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + versionNum + "/" + versionNum + ".jar"));
		jar.add(new Downloadable("Minecraft Jar", "minecraft.jar", "", 0, jarUrl));
		DownloadQueue q3 = new DownloadQueue("Main", listener, jar, base);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 8, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		q1.processQueue(executor);
		q2.processQueue(executor);
		q3.processQueue(executor);
		System.out.println(q2.getFailures().size());
	}

}

package org.smbarbour.mcu.util;

import j7compat.Path;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.*;
import java.util.zip.*;

import org.smbarbour.mcu.MCUApp;


public class Archive {

	public static void extractZip(File archive, File destination) {
		try{
			ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
			ZipEntry entry;

			entry = zis.getNextEntry();
			while(entry != null) {
				String entryName = entry.getName();

				if(entry.isDirectory()) {
					File newDir = new Path(destination).resolve(entryName).toFile();
					newDir.mkdirs();
					System.out.println("   Directory: " + newDir.getPath());
				} else {
					if (entryName.contains("aux.class")) {
						entryName = "mojangDerpyClass1.class";
					}
					File outFile = new Path(destination).resolve(entryName).toFile();
					outFile.getParentFile().mkdirs();
					System.out.println("   Extract: " + outFile.getPath());
					FileOutputStream fos = new FileOutputStream(outFile);

					int len;
					byte[] buf = new byte[1024];
					while((len = zis.read(buf, 0, 1024)) > -1) {
						fos.write(buf, 0, len);
					}

					fos.close();
				}
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
			zis.close();
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void createZip(File archive, List<File> files, Path mCFolder, MCUApp parent) throws IOException
	{
		if(!archive.getParentFile().exists()){
			archive.getParentFile().mkdirs();
		}
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive));
		int fileCount = files.size();
		int filePos = 0;
		Iterator<File> iter = files.iterator();
		while(iter.hasNext()) {
			File entry = iter.next();
			filePos++;
			parent.setStatus("Writing backup: (" + filePos + "/" + fileCount + ")");
			String relPath = entry.getPath().replace(mCFolder.toString(), "");
			System.out.println(relPath);
			if(entry.isDirectory()) {
				out.putNextEntry(new ZipEntry(relPath + "/"));
				out.closeEntry();
			} else {
				FileInputStream in = new FileInputStream(entry);
				out.putNextEntry(new ZipEntry(relPath));

				byte[] buf = new byte[1024];
				int count;
				while ((count = in.read(buf)) > 0) {
					out.write(buf,0,count);
				}
				in.close();
				out.closeEntry();
			}
		}
		out.close();
		parent.setStatus("Backup written");
	}

	public static void addToZip(File archive, List<File> files, File basePath) throws IOException
	{
		File tempFile = File.createTempFile(archive.getName(), null);
		tempFile.delete();

		if(!archive.exists())
		{
			archive.getParentFile().mkdirs();
			archive.createNewFile();
		}
		byte[] buf = new byte[1024];

		boolean renameStatus = archive.renameTo(tempFile);
		if (!renameStatus)
		{
			throw new RuntimeException("could not rename the file " + archive.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}

		ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive));

		ZipEntry entry = zis.getNextEntry();
		while (entry != null)
		{
			String name = entry.getName();
			boolean notInFiles = true;
			Iterator<File> iterator = files.iterator();
			while (iterator.hasNext())
			{
				File f = iterator.next();
				if (f.getName().equals(name)) {
					notInFiles = false;
					break;
				}
			}
			if (notInFiles) {
				zos.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zis.read(buf)) > 0) {
					zos.write(buf,0,len);
				}
			}
			entry = zis.getNextEntry();
		}
		zis.close();
		Iterator<File> iterator = files.iterator();
		while (iterator.hasNext())
		{
			File f = iterator.next();
			if(f.isDirectory()) {
				System.out.println("addToZip: " + f.getPath().replace(basePath.getPath(), "") + "/");
				zos.putNextEntry(new ZipEntry(f.getPath().replace(basePath.getPath(), "") + "/"));
				zos.closeEntry();
			} else {
				InputStream in = new FileInputStream(f);
				System.out.println("addToZip: " + f.getPath().replace(basePath.getPath(), ""));
				zos.putNextEntry(new ZipEntry(f.getPath().replace(basePath.getPath(), "")));
				int len;
				while ((len = in.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
				in.close();
			}
		}
		zos.close();
		tempFile.delete();
	}

	public static void createJar(File outJar, List<File> inputFiles, String basePath) {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		try {
			JarOutputStream jos = new JarOutputStream(new FileOutputStream(outJar), manifest);
			BufferedInputStream in;
			Iterator<File> it = inputFiles.iterator();
			while(it.hasNext()) {
				File entry = it.next();
				String path = entry.getPath().replace(basePath, "").replace("\\", "/");
				if(entry.isDirectory()) {
					if (!path.isEmpty()) {
						if(!path.endsWith("/")) {
							path += "/";
						}
						JarEntry jEntry = new JarEntry(path);
						jEntry.setTime(entry.lastModified());
						jos.putNextEntry(jEntry);
						jos.closeEntry();
					}
				} else {
					if (path.contains("mojangDerpyClass1.class")) {
						path = path.replace("mojangDerpyClass1.class","aux.class");
					}
					JarEntry jEntry = new JarEntry(path);
					jEntry.setTime(entry.lastModified());
					jos.putNextEntry(jEntry);
					in = new BufferedInputStream(new FileInputStream(entry));
					byte[] buffer = new byte[1024];
					int count;
					while((count = in.read(buffer)) > -1) {
						jos.write(buffer, 0, count);
					}
					jos.closeEntry();
					in.close();
				}
			}
			jos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void patchJar(final File jar, final File outputFile, final ArrayList<File> inputFiles)
	{
		if(jar == null) {
			//TODO: Log message("No jar selected!");
			return;
		}
		if(inputFiles.size() <= 0) {
			//TODO: Log message("No Mods selected!");
			return;
		}
		new Thread() {
			@Override
			public void run() {
				ZipInputStream zipIn;

				try {
					JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile, true)));

					zipIn = new ZipInputStream(new FileInputStream(jar));
					putIntoJar(zipIn, out);
					zipIn.close();

					Iterator<File> iterator = inputFiles.iterator();
					while(iterator.hasNext())
					{
						File inFile = iterator.next();
						//TODO: Log message("Adding " + inFile.getName() + " ...")
						System.out.println("JAR: " + inFile.getPath());
						zipIn = new ZipInputStream(new FileInputStream(inFile));
						putIntoJar(zipIn, out);
						zipIn.close();
						//TODO: Log progress
					}
					out.close();
					//TODO: Log progress (complete)
				} catch (FileNotFoundException fnfe)
				{
					fnfe.printStackTrace();
					//TODO: Log message ("File not found: " + fnfe.getMessage())
				} catch (IOException ioe)
				{
					ioe.printStackTrace();
					//TODO: Log message ("Could not read zip file!")
				}
			}

			private void putIntoJar(ZipInputStream zipIn, JarOutputStream out) throws IOException {
				ZipEntry zentry = new ZipEntry(zipIn.getNextEntry().getName());	
				while(zentry != null) {
					try {
						out.putNextEntry(zentry);
					}catch (ZipException e) {
						//TODO: Log message("Skipping existing entry: " + e.getMessage());
						zentry = new ZipEntry(zipIn.getNextEntry().getName());
						continue;
					}
					byte[] buffer = new byte[1024];
					int len;
					while((len = zipIn.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					out.closeEntry();
					try {
						zentry = new ZipEntry(zipIn.getNextEntry().getName());
					}catch (NullPointerException e) {
						zentry = null;
					}
				}				
			}
		}.start();
		//TODO: Log message ("Done")
	}
}

/*
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;


public class Patcher {

	public static void patch(final File jar, final File outputfile, final ArrayList<File> zips, final BukkitPatcher bp) {
		if(jar == null) {
			bp.logMessage("No jar selected!");
			return;
		}
		if(zips.size() <= 0) {
			bp.logMessage("No Mods selected!");
			return;
		}
		new Thread() {
			@Override
			public void run() {
				ZipInputStream zipIn;

				try {
					JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(outputfile, true)));

					for(int i=0; i<zips.size(); i++) {
						bp.logMessage("Adding " + zips.get(i).getName() + " ...");
						// Open Zip
						zipIn = new ZipInputStream(new FileInputStream(zips.get(i)));
						putIntoJar(zipIn, out);
						zipIn.close();
						bp.setProgress(100/(zips.size()+1)*i);
					}
					zipIn = new ZipInputStream(new FileInputStream(jar));
					putIntoJar(zipIn, out);
					zipIn.close();
					out.close();
					bp.setProgress(100);
				}catch (FileNotFoundException e) {
					e.printStackTrace();
					bp.logMessage("File not found: " + e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					bp.logMessage("Could not read zip file!\n");
				}
			}

			private void putIntoJar(ZipInputStream zipIn, JarOutputStream out) throws IOException {
				// Get Zip entry
				ZipEntry zentry = new ZipEntry(zipIn.getNextEntry().getName());	
				while(zentry != null) {
					try {
						out.putNextEntry(zentry);
					}catch (ZipException e) {
						bp.logMessage("Skipping existing entry: " + e.getMessage());
						zentry = new ZipEntry(zipIn.getNextEntry().getName());
						continue;
					}
					byte[] buffer = new byte[1024];
					int len;
					while((len = zipIn.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					out.closeEntry();
					try {
						zentry = new ZipEntry(zipIn.getNextEntry().getName());
					}catch (NullPointerException e) {
						zentry = null;
					}
				}
			}
		}.start();
		bp.logMessage("DONE");
	}
}
 */
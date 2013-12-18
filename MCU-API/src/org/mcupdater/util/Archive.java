package org.mcupdater.util;

import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
//import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.zip.*;

import org.apache.commons.io.FileUtils;
import org.mcupdater.MCUApp;

public class Archive {

	public static void extractZip(File archive, File destination) {
		extractZip(archive, destination, false);
	}
	
	public static void extractZip(File archive, File destination, Boolean keepMeta) {
		try{
			ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
			ZipEntry entry;

			entry = zis.getNextEntry();
			while(entry != null) {
				String entryName = entry.getName();

				if(entry.isDirectory()) {
					File newDir = destination.toPath().resolve(entryName).toFile();
					newDir.mkdirs();
					MCUpdater.apiLogger.finest("   Directory: " + newDir.getPath());
				} else {
					if (!keepMeta && entryName.contains("META-INF")) {
						zis.closeEntry();
						entry = zis.getNextEntry();
						continue;
					}
					if (entryName.contains("aux.class")) {
						entryName = "mojangDerpyClass1.class";
					}
					File outFile = destination.toPath().resolve(entryName).toFile();
					outFile.getParentFile().mkdirs();
					MCUpdater.apiLogger.finest("   Extract: " + outFile.getPath());
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
			MCUpdater.apiLogger.log(Level.SEVERE, "File not found", fnf);
		} catch (IOException ioe) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O error", ioe);
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
			MCUpdater.apiLogger.finest(relPath);
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
				MCUpdater.apiLogger.finer("addToZip: " + f.getPath().replace(basePath.getPath(), "") + "/");
				zos.putNextEntry(new ZipEntry(f.getPath().replace(basePath.getPath(), "") + "/"));
				zos.closeEntry();
			} else {
				InputStream in = new FileInputStream(f);
				MCUpdater.apiLogger.finer("addToZip: " + f.getPath().replace(basePath.getPath(), ""));
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

	public static void createJar(File outJar, List<File> inputFiles, String basePath, boolean doManifest) throws IOException {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		try {
			JarOutputStream jos;
			if (doManifest) {
				jos = new JarOutputStream(new FileOutputStream(outJar), manifest);
			} else {
				jos = new JarOutputStream(new FileOutputStream(outJar));
			}
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
			MCUpdater.apiLogger.log(Level.SEVERE, "File not found", e);
		} catch (IOException e) {
			throw e;
		}

	}

    public static void updateArchive(File zipFile, File[] files) throws IOException {
       File tempFile = File.createTempFile(zipFile.getName(), null);
       tempFile.delete();

       boolean renameOk=zipFile.renameTo(tempFile);
       if (!renameOk)
       {
    	   FileUtils.copyFile(zipFile, tempFile);
    	   zipFile.delete();
       }
       byte[] buf = new byte[1024];

       ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
       ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

       ZipEntry entry = zin.getNextEntry();
       while (entry != null) {
           String name = entry.getName();
           boolean notInFiles = true;
           for (File f : files) {
               if (f.getName().equals(name)) {
                   notInFiles = false;
                   break;
               }
           }
           if (notInFiles) {
               // Add ZIP entry to output stream.
               out.putNextEntry(new ZipEntry(name));
               // Transfer bytes from the ZIP file to the output file
               int len;
               while ((len = zin.read(buf)) > 0) {
                   out.write(buf, 0, len);
               }
           }
           entry = zin.getNextEntry();
       }
       // Close the streams        
       zin.close();
       // Compress the files
       for (int i = 0; i < files.length; i++) {
           InputStream in = new FileInputStream(files[i]);
           // Add ZIP entry to output stream.
           out.putNextEntry(new ZipEntry(files[i].getName()));
           // Transfer bytes from the file to the ZIP file
           int len;
           while ((len = in.read(buf)) > 0) {
               out.write(buf, 0, len);
           }
           // Complete the entry
           out.closeEntry();
           in.close();
       }
       // Complete the ZIP file
       out.close();
       tempFile.delete();
   }
}
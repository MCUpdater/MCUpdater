package org.smbarbour.mcu.util;

import j7compat.Files;
import j7compat.Path;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;

public class InstanceManager {
	
//	public static boolean checkSymlink(Path target) {
//		return Files.isSymbolicLink(target);
//	}
//	
//	public static void createLink(Path source, Path target) {
//		try {
//			Files.createSymbolicLink(source, target);
//		} catch (IOException e) {
//			//parent.baseLogger.error("Failed to create link", e);
//			e.printStackTrace();
//		}
//	}
	
	public static Path createInstance(String instanceName) {
		try {
			Path instance = MCUpdater.getInstance().getInstanceRoot().resolve(instanceName);
			Files.createDirectories(instance);
			Path instanceFile = instance.resolve("instance.dat");
			Files.createFile(instanceFile);
			Properties instData = new Properties();
			instData.setProperty("serverID", instanceName);
			instData.setProperty("revision", "0");
			instData.store(Files.newOutputStream(instanceFile), "Instance data");
			Path binPath = Files.createDirectory(instance.resolve("bin"));
			Path nativesPath = Files.createDirectory(binPath.resolve("natives"));
			String baseURL = "http://s3.amazonaws.com/MinecraftDownload/";
			FileUtils.copyURLToFile(new URL(baseURL + "lwjgl.jar"), binPath.resolve("lwjgl.jar").toFile());
			FileUtils.copyURLToFile(new URL(baseURL + "lwjgl_util.jar"), binPath.resolve("lwjgl_util.jar").toFile());
			FileUtils.copyURLToFile(new URL(baseURL + "jinput.jar"), binPath.resolve("jinput.jar").toFile());

			String osName = System.getProperty("os.name");
			String nativeJar = null;
		    if (osName.startsWith("Win"))
		        nativeJar = "windows_natives.jar";
		      else if (osName.startsWith("Linux"))
		        nativeJar = "linux_natives.jar";
		      else if (osName.startsWith("Mac"))
		        nativeJar = "macosx_natives.jar";
		      else if ((osName.startsWith("Solaris")) || (osName.startsWith("SunOS")))
		        nativeJar = "solaris_natives.jar";
		      else {
		        throw new Exception("Unsupported OS detected");
		      }
		    File nativesFile = binPath.resolve("natives.zip").toFile();
			FileUtils.copyURLToFile(new URL(baseURL + nativeJar), nativesFile);
			Archive.extractZip(nativesFile, nativesPath.toFile());
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(binPath.resolve("version").toFile()));
			dos.writeUTF("MCUpdater");
			dos.close();
			nativesFile.delete();
			return instance;
		} catch (IOException e) {
			MCUpdater.getInstance().apiLogger.log(Level.SEVERE, "I/O Error", e);
		} catch (Exception e) {
			MCUpdater.getInstance().apiLogger.log(Level.SEVERE, "General Error", e);
		}
		return null;
	}
}

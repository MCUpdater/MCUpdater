package org.smbarbour.mcu.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class InstanceManager {

	private MCUpdater parent;
	
	public InstanceManager(MCUpdater parent) {
		this.parent = parent;
	}
	
	public boolean checkSymlink(Path target) {
		return Files.isSymbolicLink(target);
	}
	
	public void createLink(Path source, Path target) {
		try {
			Files.createSymbolicLink(source, target);
		} catch (IOException e) {
			//parent.baseLogger.error("Failed to create link", e);
			e.printStackTrace();
		}
	}
	
	public Path createInstance(String instanceName) {
		try {
			Path instance = parent.getInstanceRoot().toPath().resolve(instanceName);
			Files.createDirectories(instance);
			Files.createFile(instance.resolve("instance.dat"));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

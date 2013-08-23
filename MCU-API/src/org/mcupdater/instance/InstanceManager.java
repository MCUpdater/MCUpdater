package org.mcupdater.instance;

import j7compat.Files;
import j7compat.Path;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import org.mcupdater.util.MCUpdater;

public class InstanceManager {
		
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

			DataOutputStream dos = new DataOutputStream(new FileOutputStream(binPath.resolve("version").toFile()));
			dos.writeUTF("MCUpdater");
			dos.close();
			return instance;
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "I/O Error", e);
		} catch (Exception e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "General Error", e);
		}
		return null;
	}
}

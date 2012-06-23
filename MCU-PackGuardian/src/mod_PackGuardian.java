import java.io.File;
import java.lang.reflect.Method;

import forge.NetworkMod;
import forge.Configuration;

public class mod_PackGuardian extends NetworkMod {
	static Configuration config;
	private String versionText = "";
	
	@Override
	public String getVersion() {
		return versionText;
	}

	@Override
	public void load() {
		File root;
		try {
			Class<?> mcClient = Class.forName("net.minecraft.client.Minecraft");
			Method getDirectory = mcClient.getMethod("a", (Class<?>)null);
			root = (File) getDirectory.invoke(null, (Class<?>)null);
		} catch (Exception e) {
			root = new File(".");
		}
		File configFile = new File(root, "config/PackGuardian.cfg");
		System.out.println(configFile.getAbsolutePath());
		config = new Configuration(configFile);
		versionText = config.getOrCreateProperty("versionText", Configuration.CATEGORY_GENERAL, "CHANGEME ServerPack version 1.0").value;
		config.save();
	}
	
	@Override
	public boolean clientSideRequired() {
		return true;
	}
}

package org.mcupdater;
import java.io.File;
import net.minecraftforge.common.Configuration;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;


@Mod(modid = "PackGuardian")
@NetworkMod(clientSideRequired = true)
public class PackGuardian {
	public String version;
	static Configuration config;
	
	@Mod.PreInit
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(new File(event.getModConfigurationDirectory(), "PackGuardian.cfg"));
		event.getModMetadata().version = config.get("General", "versionText", "CHANGEME ServerPack version 1.0").getString();
		config.save();
	}
}

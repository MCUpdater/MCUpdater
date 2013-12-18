package org.mcupdater.mojang;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mcupdater.DownloadQueue;
import org.mcupdater.Downloadable;
import org.mcupdater.Downloadable.HashAlgorithm;
import org.mcupdater.TrackerListener;
import org.mcupdater.util.MCUpdater;
import com.google.gson.Gson;

public class AssetManager {
	public static DownloadQueue downloadAssets(String queueName, String parent, File baseDirectory, TrackerListener listener, MinecraftVersion version) {
		DownloadQueue queue = new DownloadQueue(queueName, parent, listener, getAssets(baseDirectory, version), baseDirectory, null);
		return queue;
	}
	
	private static Set<Downloadable> getAssets(File baseDirectory, MinecraftVersion version){
		Gson gson = new Gson();
		Set<Downloadable> assets = new HashSet<Downloadable>();
		String indexName = version.getAssets();
		if (indexName == null) {
			indexName = "legacy";
		}
		try {
			File objectsPath = new File(baseDirectory, "objects");
			File indexesPath = new File(baseDirectory, "indexes");
			File indexFile = new File(indexesPath, indexName + ".json");
			URL indexUrl = new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + indexName + ".json");
			URL resourceUrl = new URL("http://resources.download.minecraft.net/");
			URL localUrl = MCUpdater.getInstance().getMCFolder().resolve("assets").toFile().toURI().toURL();
			
			InputStream indexStream = indexUrl.openConnection().getInputStream();
			String json = IOUtils.toString(indexStream);
			FileUtils.writeStringToFile(indexFile, json);
			AssetIndex index = (AssetIndex)gson.fromJson(json, AssetIndex.class);
			
			for (AssetIndex.Asset object : index.getUniqueObjects()) {
				String assetName = object.getHash().substring(0, 2) + "/" + object.getHash();
				File asset = new File(objectsPath, assetName);
				if ((!asset.isFile()) || (FileUtils.sizeOf(asset) != object.getSize())) {
	    			List<URL> urls = new ArrayList<URL>();
	    			File localAsset = MCUpdater.getInstance().getMCFolder().resolve("assets").resolve("objects").resolve(object.getHash().substring(0, 2)).resolve(object.getHash()).toFile();
	    			if ((localAsset.isFile()) && (FileUtils.sizeOf(localAsset) == object.getSize())) {
	    				urls.add(new URL(localUrl + "objects" + "/" + assetName));
	    			} else {
	    				urls.add(new URL(resourceUrl + assetName));
	    			}
					Downloadable download = new Downloadable(object.getHash(),"objects" + "/" + assetName, HashAlgorithm.SHA, object.getHash(), object.getSize(),urls);
					assets.add(download);
				}
			}
		} catch (Exception e) {}
		return assets;
	}
}

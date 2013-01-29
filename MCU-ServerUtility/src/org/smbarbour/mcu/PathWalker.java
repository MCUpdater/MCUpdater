package org.smbarbour.mcu;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.smbarbour.mcu.util.ConfigFile;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.Module;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class PathWalker extends SimpleFileVisitor<Path> {

	private Path rootPath;
	private ServerForm parent;
	private String urlBase;

	public PathWalker(ServerForm parent, Path rootPath, String urlBase) {
		this.setParent(parent);
		this.setRootPath(rootPath);
		this.setUrlBase(urlBase);
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Path relativePath = rootPath.relativize(file);
		String downloadURL = urlBase + "/" + relativePath.toString().replace("\\","/").replace(" ", "%20");
		InputStream is = Files.newInputStream(file);
		byte[] hash = DigestUtils.md5(is);
		String md5 = new String(Hex.encodeHex(hash));
		String name = file.getFileName().toString();
		String id = file.getFileName().toString();
		name = name.substring(0,name.length()-4);
		id = name.replace(" ", "");
		String depends = "";
		Boolean required = true;
		Boolean inJar = false;
		Boolean extract = false;
		Boolean inRoot = false;
		Boolean isDefault = true;
		Boolean coreMod = false;
		System.out.println(relativePath.toString());
		if (relativePath.toString().contains(".DS_Store")) { return FileVisitResult.CONTINUE; }
		if (relativePath.toString().indexOf(MCUpdater.sep) >= 0) {
			switch (relativePath.toString().substring(0, relativePath.toString().indexOf(MCUpdater.sep))) {
			case "jar": {
				inJar = true;
				break;
			}
			case "coremods": {
				coreMod = true;
				break;
			}
			case "config": {
				String newPath = relativePath.toString();
				if (MCUpdater.sep.equals("\\")) {
					newPath = newPath.replace("\\", "/");
				}
				ConfigFile newConfig = new ConfigFile(downloadURL, newPath, md5);
				parent.AddConfig(new ConfigFileWrapper("", newConfig));
				return FileVisitResult.CONTINUE;
			}
			default:
			}
		}
		try {
			ZipFile zf = new ZipFile(file.toFile());
			System.out.println(zf.size() + " entries in file.");
			JdomParser parser = new JdomParser();
			JsonRootNode modInfo = parser.parse(new InputStreamReader(zf.getInputStream(zf.getEntry("mcmod.info"))));
			JsonNode subnode;
			if (modInfo.hasElements()) {
				subnode = modInfo.getElements().get(0);
			} else {
				subnode = modInfo.getNode("modlist").getElements().get(0);
			}
			id = subnode.getStringValue("modid");
			name = subnode.getStringValue("name");
			zf.close();
		} catch (NullPointerException e) {
		} catch (ZipException e) {
			System.out.println("Not an archive.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		} finally {
			Module newMod = new Module(name, id, downloadURL, depends, required, inJar, extract, inRoot, isDefault, coreMod, md5, null, "both");
			parent.AddModule(newMod);
		}			
		return FileVisitResult.CONTINUE;
	}

	public void setRootPath(Path rootPath) {
		this.rootPath = rootPath;
	}

	public void setParent(ServerForm parent) {
		this.parent = parent;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}
}

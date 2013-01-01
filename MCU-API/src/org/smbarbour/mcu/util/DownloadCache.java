package org.smbarbour.mcu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class DownloadCache {
	private static DownloadCache instance;
	private File dir;
	
	private DownloadCache(File dir) {
		this.dir = dir;
	}
	
	public static void init(File dir) {
		if( instance != null ) {
			throw new IllegalArgumentException("Attempt to reinitialize download cache.");
		}
		System.out.println("Initializing DownloadCache in "+dir);
		instance = new DownloadCache(dir);
	}
	
	public static File getDir() {
		instance.dir.mkdirs();
		return instance.dir;
	}
	
	public static boolean cacheFile(File file, String expectedMD5) {
		byte[] hash;
		try {
			InputStream is = new FileInputStream(file);
			hash = DigestUtils.md5(is);
			is.close();		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		String chksum = new String(Hex.encodeHex(hash));
		if( !chksum.equals(expectedMD5) ) {
			// checksums do not match, abort :)
			return false;
		}
		
		File destFile = getFile(chksum);
		if( !destFile.exists() ) {
			try {
				FileUtils.copyFile(file, destFile);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	public static File getFile(String chksum) {
		final File file = new File( getDir().getAbsolutePath() + MCUpdater.sep + chksum + ".bin");
		return file;
	}
}

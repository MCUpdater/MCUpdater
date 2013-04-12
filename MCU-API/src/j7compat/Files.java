package j7compat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.smbarbour.mcu.util.MCUpdater;

public class Files {

	public static BufferedWriter newBufferedWriter(Path path) {
		try {
			return new BufferedWriter(new FileWriter(path.toFile()));
		} catch (IOException e) {
			MCUpdater.getInstance().apiLogger.log(Level.SEVERE, "I/O Error", e);
		}
		return null;
	}

	public static BufferedReader newBufferedReader(Path path) {
		try {
			return new BufferedReader(new FileReader(path.toFile()));
		} catch (FileNotFoundException e) {
			MCUpdater.getInstance().apiLogger.log(Level.SEVERE, "File not found", e);
		}
		return null;
	}

	public static void copy(Path source, Path dest) throws IOException {
		FileUtils.copyFile(source.toFile(), dest.toFile());
	}

	public static void createDirectories(Path path) {
		path.toFile().mkdirs();
	}

	public static void createFile(Path path) throws IOException {
		path.toFile().createNewFile();
	}

	public static OutputStream newOutputStream(Path path) throws FileNotFoundException {
		return new FileOutputStream(path.toFile());
	}

	public static Path createDirectory(Path path) {
		path.toFile().mkdir();
		return path;
	}

	public static InputStream newInputStream(Path path) throws FileNotFoundException {
		return new FileInputStream(path.toFile());
	}

	public static void delete(Path path) {
		path.toFile().delete();
	}

	public static boolean notExists(Path path) {
		return !path.toFile().exists();
	}

}

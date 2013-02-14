package j7compat;

import java.io.File;

public class Path {
	private File internal;
	
	public Path(String path) {
		internal = new File(path);
	}
	
	public Path(File file) {
		internal = file;
	}

	public Path resolve(String path) {
		return new Path((new File(internal, path)).getAbsolutePath());
	}
	
	public File toFile() {
		return internal;
	}
	
	public String toString() {
		return internal.getAbsolutePath();
	}
}

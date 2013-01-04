package org.smbarbour.mcu;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.FileVisitResult.*;

public class CopyFiles extends SimpleFileVisitor<Path> {

	private Path fromPath;
	private Path toPath;
	private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

	public CopyFiles(Path fromPath, Path toPath) {
		this.fromPath = fromPath;
		this.toPath = toPath;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if(!Files.exists(targetPath)){
			Files.createDirectory(targetPath);
		}
		return CONTINUE;		
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
        Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
        return CONTINUE;		
	}
}

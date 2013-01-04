package org.smbarbour.mcu;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class PrepareFiles extends SimpleFileVisitor<Path> {

	private Path fromPath;
	private Path toPath;
	private boolean prepareInstance;
	private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;


	public PrepareFiles(Path fromPath, Path toPath, boolean prepareInstance) {
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.prepareInstance = prepareInstance;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (prepareInstance) {
			Path targetPath = toPath.resolve(fromPath.relativize(dir));
			if(!Files.exists(targetPath)){
				Files.createDirectory(targetPath);
			}
		}
		return CONTINUE;		
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (prepareInstance) {
			Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
		}
		Files.delete(file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if(exc == null){
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
		throw exc;
	}
}

package org.mcupdater.util;

import j7compat.Path;

import java.io.IOException;

import net.md_5.jbeat.LinearCreator;
import net.md_5.jbeat.Patcher;

public class Transmogrify {
	public static void applyPatch(Path source, Path target, Path patch) throws IOException {
		Patcher beatPatch = new Patcher(patch.toFile(), source.toFile(), target.toFile());
		beatPatch.patch();
	}
	
	public static void createPatch(Path source, Path target, Path patch) throws IOException {
		LinearCreator patchMaker = new LinearCreator(source.toFile(), target.toFile(), patch.toFile());
		patchMaker.create();
	}
}

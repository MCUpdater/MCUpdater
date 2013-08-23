package org.mcupdater.mojang;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MinecraftVersion version = MinecraftVersion.loadVersion("1.6.2");
		
		System.out.println(version.getId());
		for (Library lib : version.getLibraries()) {
			System.out.println("  lib: " + lib.getName() + " (" + lib.getDownloadUrl() + ") OS Valid: " + lib.validForOS());
		}
	}

}

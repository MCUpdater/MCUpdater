package org.mcupdater;

import java.awt.MenuItem;

import javax.swing.JButton;

public abstract interface GenericLauncherThread {

	void start();

	void stop();

	void register(MainForm form, JButton btnLaunchMinecraft, MenuItem killItem);

}

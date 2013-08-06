package org.mcupdater;

import java.awt.EventQueue;

import javax.swing.UIManager;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new ServerForm();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}

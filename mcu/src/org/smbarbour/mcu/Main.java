package org.smbarbour.mcu;

import java.awt.EventQueue;

import javax.swing.UIManager;

public class Main {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new MainForm();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}

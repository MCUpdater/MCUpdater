package org.smbarbour.mcu;

import java.awt.EventQueue;

import javax.swing.UIManager;

public class Main {
	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new MainForm(args);		//give args to MainForm
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}

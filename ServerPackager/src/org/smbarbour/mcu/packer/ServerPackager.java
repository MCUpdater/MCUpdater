package org.smbarbour.mcu.packer;

import java.awt.EventQueue;

import javax.swing.UIManager;
import org.smbarbour.mcu.packer.ui.PackagerFrame;

public class ServerPackager {
	private static PackagerFrame _frame;
	
	public static void main(String[] args) {
		EventQueue.invokeLater( new Runnable() {			
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				_frame = new PackagerFrame();
			}
		});
	}

	public static PackagerFrame getFrame() {
		return _frame;
	}

}

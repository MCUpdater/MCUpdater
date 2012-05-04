package org.smbarbour.mcu;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.smbarbour.mcu.util.MCUpdater;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.border.Border;

import java.awt.FlowLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.BevelBorder;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Component;
import javax.swing.Box;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import java.awt.CardLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;

public class ServerForm extends MCUApp {

	private static ServerForm window;
	private JFrame frmMain;
	final MCUpdater mcu = new MCUpdater();
	private JTextField txtServerName;
	private JTextField txtNewsURL;
	private JTextField txtServerAddress;
	private JTextField txtModuleName;
	private JTextField txtUrl;
	
	public ServerForm() {
		initialize();
		window = this;
		window.frmMain.setVisible(true);
		mcu.setParent(window);
	}
	
	private void initialize() {
		frmMain = new JFrame();
		frmMain.setTitle("Minecraft Updater - ServerPack Utility");
		frmMain.setResizable(false);
		frmMain.setBounds(100,100,834,592);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel pnlFooter = new JPanel();
		frmMain.getContentPane().add(pnlFooter, BorderLayout.SOUTH);
		pnlFooter.setLayout(new BorderLayout(0,0));
		
		JPanel pnlStatus = new JPanel();
		pnlFooter.add(pnlStatus, BorderLayout.CENTER);
		pnlStatus.setLayout(new BorderLayout(0,0));
		
		JLabel lblStatus = new JLabel("Idle");
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		pnlStatus.add(lblStatus);
		
		JPanel pnlServerInfo = new JPanel();
		pnlServerInfo.setBorder(null);
		frmMain.getContentPane().add(pnlServerInfo, BorderLayout.NORTH);
		GridBagLayout gbl_pnlServerInfo = new GridBagLayout();
		gbl_pnlServerInfo.columnWidths = new int[]{0, 66, 7, 234, 52, 246, 0, 0};
		gbl_pnlServerInfo.rowHeights = new int[]{0, 20, 20, 0, 0};
		gbl_pnlServerInfo.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlServerInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlServerInfo.setLayout(gbl_pnlServerInfo);
		
		Component vStrut1 = Box.createVerticalStrut(3);
		GridBagConstraints gbc_vStrut1 = new GridBagConstraints();
		gbc_vStrut1.insets = new Insets(0, 0, 5, 5);
		gbc_vStrut1.gridx = 1;
		gbc_vStrut1.gridy = 0;
		pnlServerInfo.add(vStrut1, gbc_vStrut1);
		
		Component vStrut2 = Box.createVerticalStrut(3);
		GridBagConstraints gbc_vStrut2 = new GridBagConstraints();
		gbc_vStrut2.insets = new Insets(0, 0, 0, 5);
		gbc_vStrut2.gridx = 1;
		gbc_vStrut2.gridy = 3;
		pnlServerInfo.add(vStrut2, gbc_vStrut2);
		
		Component hStrut1 = Box.createHorizontalStrut(3);
		GridBagConstraints gbc_hStrut1 = new GridBagConstraints();
		gbc_hStrut1.insets = new Insets(0, 0, 5, 5);
		gbc_hStrut1.gridx = 0;
		gbc_hStrut1.gridy = 1;
		pnlServerInfo.add(hStrut1, gbc_hStrut1);
		
		JLabel lblServerName = new JLabel("Server Name:");
		GridBagConstraints gbc_lblServerName = new GridBagConstraints();
		gbc_lblServerName.anchor = GridBagConstraints.WEST;
		gbc_lblServerName.insets = new Insets(0, 0, 5, 5);
		gbc_lblServerName.gridx = 1;
		gbc_lblServerName.gridy = 1;
		pnlServerInfo.add(lblServerName, gbc_lblServerName);
		
		txtServerName = new JTextField();
		GridBagConstraints gbc_txtServerName = new GridBagConstraints();
		gbc_txtServerName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtServerName.anchor = GridBagConstraints.NORTHWEST;
		gbc_txtServerName.insets = new Insets(0, 0, 5, 5);
		gbc_txtServerName.gridx = 3;
		gbc_txtServerName.gridy = 1;
		pnlServerInfo.add(txtServerName, gbc_txtServerName);
		txtServerName.setColumns(30);
		lblServerName.setLabelFor(txtServerName);
		
		JLabel lblNewsURL = new JLabel("News URL:");
		GridBagConstraints gbc_lblNewsURL = new GridBagConstraints();
		gbc_lblNewsURL.anchor = GridBagConstraints.WEST;
		gbc_lblNewsURL.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewsURL.gridx = 4;
		gbc_lblNewsURL.gridy = 1;
		pnlServerInfo.add(lblNewsURL, gbc_lblNewsURL);
		
		txtNewsURL = new JTextField();
		GridBagConstraints gbc_txtNewsURL = new GridBagConstraints();
		gbc_txtNewsURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtNewsURL.anchor = GridBagConstraints.NORTHWEST;
		gbc_txtNewsURL.insets = new Insets(0, 0, 5, 5);
		gbc_txtNewsURL.gridx = 5;
		gbc_txtNewsURL.gridy = 1;
		pnlServerInfo.add(txtNewsURL, gbc_txtNewsURL);
		txtNewsURL.setColumns(30);
		lblNewsURL.setLabelFor(txtNewsURL);
		
		JLabel lblVersion = new JLabel("Minecraft Version:");
		GridBagConstraints gbc_lblVersion = new GridBagConstraints();
		gbc_lblVersion.anchor = GridBagConstraints.WEST;
		gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
		gbc_lblVersion.gridx = 4;
		gbc_lblVersion.gridy = 2;
		pnlServerInfo.add(lblVersion, gbc_lblVersion);
		
		JComboBox optVersion = new JComboBox();
		optVersion.setModel(new DefaultComboBoxModel(new String[] {"1.0", "1.1", "1.2.3", "1.2.4", "1.2.5"}));
		GridBagConstraints gbc_optVersion = new GridBagConstraints();
		gbc_optVersion.anchor = GridBagConstraints.NORTHWEST;
		gbc_optVersion.insets = new Insets(0, 0, 5, 5);
		gbc_optVersion.gridx = 5;
		gbc_optVersion.gridy = 2;
		pnlServerInfo.add(optVersion, gbc_optVersion);
		
		Component hStrut2 = Box.createHorizontalStrut(3);
		GridBagConstraints gbc_hStrut2 = new GridBagConstraints();
		gbc_hStrut2.insets = new Insets(0, 0, 5, 0);
		gbc_hStrut2.gridx = 6;
		gbc_hStrut2.gridy = 1;
		pnlServerInfo.add(hStrut2, gbc_hStrut2);
		
		JLabel lblServerAddress = new JLabel("Server Address:");
		GridBagConstraints gbc_lblServerAddress = new GridBagConstraints();
		gbc_lblServerAddress.anchor = GridBagConstraints.WEST;
		gbc_lblServerAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblServerAddress.gridwidth = 2;
		gbc_lblServerAddress.gridx = 1;
		gbc_lblServerAddress.gridy = 2;
		pnlServerInfo.add(lblServerAddress, gbc_lblServerAddress);
		
		txtServerAddress = new JTextField();
		GridBagConstraints gbc_txtServerAddress = new GridBagConstraints();
		gbc_txtServerAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtServerAddress.anchor = GridBagConstraints.NORTHWEST;
		gbc_txtServerAddress.insets = new Insets(0, 0, 5, 5);
		gbc_txtServerAddress.gridx = 3;
		gbc_txtServerAddress.gridy = 2;
		pnlServerInfo.add(txtServerAddress, gbc_txtServerAddress);
		txtServerAddress.setColumns(30);
		
		JScrollPane scrollPane = new JScrollPane();
		frmMain.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JPanel ScollBase = new JPanel();
		ScollBase.setBackground(Color.GRAY);
		scrollPane.setViewportView(ScollBase);
		ScollBase.setLayout(new GridLayout(10, 1, 0, 0));
		
		JPanel Foo = new JPanel();
		Foo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		ScollBase.add(Foo);
		GridBagLayout gbl_Foo = new GridBagLayout();
		gbl_Foo.columnWidths = new int[]{0, 25, 166, 23, 246, 69, 57, 0, 0};
		gbl_Foo.rowHeights = new int[]{0, 0, 0, 25, 76, 0};
		gbl_Foo.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_Foo.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		Foo.setLayout(gbl_Foo);
		
		Component vStrutMod1 = Box.createVerticalStrut(3);
		GridBagConstraints gbc_vStrutMod1 = new GridBagConstraints();
		gbc_vStrutMod1.insets = new Insets(0, 0, 5, 5);
		gbc_vStrutMod1.gridx = 1;
		gbc_vStrutMod1.gridy = 0;
		Foo.add(vStrutMod1, gbc_vStrutMod1);
		
		Component vStrutMod2 = Box.createVerticalStrut(3);
		GridBagConstraints gbc_vStrutMod2 = new GridBagConstraints();
		gbc_vStrutMod2.insets = new Insets(0,0,5,5);
		gbc_vStrutMod2.gridx = 1;
		gbc_vStrutMod2.gridy = 5;
		Foo.add(vStrutMod2, gbc_vStrutMod2);
		
		Component hStrutMod2 = Box.createHorizontalStrut(3);
		GridBagConstraints gbc_hStrutMod2 = new GridBagConstraints();
		gbc_hStrutMod2.insets = new Insets(0, 0, 5, 0);
		gbc_hStrutMod2.gridx = 7;
		gbc_hStrutMod2.gridy = 1;
		Foo.add(hStrutMod2, gbc_hStrutMod2);
		
		Component hStrutMod1 = Box.createHorizontalStrut(3);
		GridBagConstraints gbc_hStrutMod1 = new GridBagConstraints();
		gbc_hStrutMod1.insets = new Insets(0, 0, 5, 5);
		gbc_hStrutMod1.gridx = 0;
		gbc_hStrutMod1.gridy = 2;
		Foo.add(hStrutMod1, gbc_hStrutMod1);
		
		JScrollPane subScroller = new JScrollPane();
		subScroller.setBorder(null);
		subScroller.setViewportBorder(new TitledBorder(null, "Additional Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_subScroller = new GridBagConstraints();
		gbc_subScroller.gridwidth = 6;
		gbc_subScroller.insets = new Insets(5, 5, 0, 5);
		gbc_subScroller.fill = GridBagConstraints.BOTH;
		gbc_subScroller.gridx = 1;
		gbc_subScroller.gridy = 4;
		Foo.add(subScroller, gbc_subScroller);
		
		JLabel lblModuleName = new JLabel("Module Name:");
		GridBagConstraints gbc_lblModuleName = new GridBagConstraints();
		gbc_lblModuleName.anchor = GridBagConstraints.EAST;
		gbc_lblModuleName.insets = new Insets(0, 0, 5, 5);
		gbc_lblModuleName.gridx = 1;
		gbc_lblModuleName.gridy = 3;
		Foo.add(lblModuleName, gbc_lblModuleName);
		lblModuleName.setLabelFor(txtModuleName);
		
		txtModuleName = new JTextField();
		GridBagConstraints gbc_txtModuleName = new GridBagConstraints();
		gbc_txtModuleName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtModuleName.anchor = GridBagConstraints.WEST;
		gbc_txtModuleName.insets = new Insets(0, 0, 5, 5);
		gbc_txtModuleName.gridx = 2;
		gbc_txtModuleName.gridy = 3;
		Foo.add(txtModuleName, gbc_txtModuleName);
		txtModuleName.setColumns(20);
		
		JLabel lblUrl = new JLabel("URL:");
		GridBagConstraints gbc_lblUrl = new GridBagConstraints();
		gbc_lblUrl.anchor = GridBagConstraints.WEST;
		gbc_lblUrl.insets = new Insets(0, 0, 5, 5);
		gbc_lblUrl.gridx = 3;
		gbc_lblUrl.gridy = 3;
		Foo.add(lblUrl, gbc_lblUrl);
		
		txtUrl = new JTextField();
		GridBagConstraints gbc_txtUrl = new GridBagConstraints();
		gbc_txtUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUrl.anchor = GridBagConstraints.WEST;
		gbc_txtUrl.insets = new Insets(0, 0, 5, 5);
		gbc_txtUrl.gridx = 4;
		gbc_txtUrl.gridy = 3;
		Foo.add(txtUrl, gbc_txtUrl);
		txtUrl.setColumns(30);
		
		JCheckBox chkRequired = new JCheckBox("Required");
		GridBagConstraints gbc_chkRequired = new GridBagConstraints();
		gbc_chkRequired.anchor = GridBagConstraints.WEST;
		gbc_chkRequired.insets = new Insets(0, 0, 5, 5);
		gbc_chkRequired.gridx = 5;
		gbc_chkRequired.gridy = 3;
		Foo.add(chkRequired, gbc_chkRequired);
		
		JCheckBox chckbxInJar = new JCheckBox("In JAR");
		GridBagConstraints gbc_chckbxInJar = new GridBagConstraints();
		gbc_chckbxInJar.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxInJar.anchor = GridBagConstraints.WEST;
		gbc_chckbxInJar.gridx = 6;
		gbc_chckbxInJar.gridy = 3;
		Foo.add(chckbxInJar, gbc_chckbxInJar);
		
		JMenuBar menuBar = new JMenuBar();
		frmMain.setJMenuBar(menuBar);
	}

	@Override
	public void setLblStatus(String string) {
		
	}

	@Override
	public void setProgressBar(int i) {
		
	}

}
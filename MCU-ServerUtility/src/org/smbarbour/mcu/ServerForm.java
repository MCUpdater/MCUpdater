package org.smbarbour.mcu;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.smbarbour.mcu.util.ConfigFile;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.Module;
import org.smbarbour.mcu.util.ServerList;

import javax.swing.JMenuBar;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.border.EtchedBorder;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.border.TitledBorder;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ServerForm extends MCUApp {

	private static ServerForm window;
	private JFrame frmMain;
	final MCUpdater mcu = MCUpdater.getInstance();
	private JTextField txtServerName;
	private JTextField txtNewsUrl;
	private JTextField txtServerAddress;
	private JTextField txtModName;
	private JTextField txtUrl;
	private JTextField txtConfigURL;
	private JTextField txtConfigPath;
	private JTextField txtVersion;
	private List<Module> modList = new ArrayList<Module>();
	private List<ConfigFile> configList = new ArrayList<ConfigFile>();
	private ServerList serverInfo = new ServerList(null, null, null, null, null, null, null, false, null);
	private JTextField txtIconURL;
	private JTextField txtRevision;
	private JTextField txtMCUVersion;
	private JTextField txtServerID;
	private JTextField txtMD5;
	private JTextField txtModId;
	private JTextField txtConfigMD5;
	
	public ServerForm() {
		initialize();
		window = this;
		window.frmMain.setVisible(true);
		mcu.setParent(window);
	}
	
	private void initialize() {
		frmMain = new JFrame();
		frmMain.setTitle("MCUpdater - ServerPack Utility (Implementing MCU-API " + Version.VERSION + ")");
		frmMain.setBounds(100,100,800,600);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmMain.setJMenuBar(menuBar);
		
		JMenu mnuFile = new JMenu("File");
		mnuFile.setMnemonic('F');
		menuBar.add(mnuFile);
		
		JMenuItem mnuNew = new JMenuItem("New");
		mnuNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		mnuFile.add(mnuNew);
		
		JMenuItem mnuOpen = new JMenuItem("Open...");
		mnuOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		mnuFile.add(mnuOpen);
		
		JMenuItem mnuSave = new JMenuItem("Save");
		mnuSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		mnuFile.add(mnuSave);
		
		JMenuItem mnuSaveAs = new JMenuItem("Save As...");
		mnuSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		mnuFile.add(mnuSaveAs);
		
		mnuFile.addSeparator();
		
		JMenuItem mnuExit = new JMenuItem("Exit");
		mnuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		mnuFile.add(mnuExit);
		frmMain.getContentPane().setLayout(new BorderLayout(0, 0));
				
		JPanel serverPanel = new JPanel();
		serverPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		frmMain.getContentPane().add(serverPanel, BorderLayout.NORTH);
		GridBagLayout gbl_serverPanel = new GridBagLayout();
		gbl_serverPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_serverPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_serverPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_serverPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		serverPanel.setLayout(gbl_serverPanel);
		
		{ // serverPanel
			int row = 0;

			Component rigidArea = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea = new GridBagConstraints();
			gbc_rigidArea.insets = new Insets(0, 0, 5, 5);
			gbc_rigidArea.gridx = 0;
			gbc_rigidArea.gridy = row;
			serverPanel.add(rigidArea, gbc_rigidArea);

			row++;

			JLabel lblMCUVersion = new JLabel("MCU Version:");
			GridBagConstraints gbc_lblMCUVersion = new GridBagConstraints();
			gbc_lblMCUVersion.insets = new Insets(0, 0, 5, 5);
			gbc_lblMCUVersion.anchor = GridBagConstraints.EAST;
			gbc_lblMCUVersion.gridx = 1;
			gbc_lblMCUVersion.gridy = row;
			serverPanel.add(lblMCUVersion, gbc_lblMCUVersion);

			txtMCUVersion = new JTextField();
			GridBagConstraints gbc_txtMCUVersion = new GridBagConstraints();
			gbc_txtMCUVersion.insets = new Insets(0, 0, 5, 5);
			gbc_txtMCUVersion.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtMCUVersion.gridx = 2;
			gbc_txtMCUVersion.gridy = row;
			serverPanel.add(txtMCUVersion, gbc_txtMCUVersion);
			txtMCUVersion.setColumns(10);

			JLabel lblServerID = new JLabel("Server ID:");
			GridBagConstraints gbc_lblServerID = new GridBagConstraints();
			gbc_lblServerID.anchor = GridBagConstraints.EAST;
			gbc_lblServerID.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerID.gridx = 3;
			gbc_lblServerID.gridy = row;
			serverPanel.add(lblServerID, gbc_lblServerID);

			txtServerID = new JTextField();
			GridBagConstraints gbc_txtServerID = new GridBagConstraints();
			gbc_txtServerID.insets = new Insets(0, 0, 5, 5);
			gbc_txtServerID.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtServerID.gridx = 4;
			gbc_txtServerID.gridy = row;
			serverPanel.add(txtServerID, gbc_txtServerID);
			txtServerID.setColumns(10);

			row++;

			JLabel lblServerName = new JLabel("Server Name:");
			GridBagConstraints gbc_lblServerName = new GridBagConstraints();
			gbc_lblServerName.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerName.anchor = GridBagConstraints.EAST;
			gbc_lblServerName.gridx = 1;
			gbc_lblServerName.gridy = row;
			serverPanel.add(lblServerName, gbc_lblServerName);

			txtServerName = new JTextField();
			GridBagConstraints gbc_txtServerName = new GridBagConstraints();
			gbc_txtServerName.insets = new Insets(0, 0, 5, 5);
			gbc_txtServerName.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtServerName.gridx = 2;
			gbc_txtServerName.gridy = row;
			serverPanel.add(txtServerName, gbc_txtServerName);
			txtServerName.setColumns(10);

			JLabel lblNewsUrl = new JLabel("News URL:");
			GridBagConstraints gbc_lblNewsUrl = new GridBagConstraints();
			gbc_lblNewsUrl.anchor = GridBagConstraints.EAST;
			gbc_lblNewsUrl.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewsUrl.gridx = 3;
			gbc_lblNewsUrl.gridy = row;
			serverPanel.add(lblNewsUrl, gbc_lblNewsUrl);

			txtNewsUrl = new JTextField();
			GridBagConstraints gbc_txtNewsUrl = new GridBagConstraints();
			gbc_txtNewsUrl.insets = new Insets(0, 0, 5, 5);
			gbc_txtNewsUrl.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtNewsUrl.gridx = 4;
			gbc_txtNewsUrl.gridy = row;
			serverPanel.add(txtNewsUrl, gbc_txtNewsUrl);
			txtNewsUrl.setColumns(10);

			row++;

			JLabel lblServerAddress = new JLabel("Server Address:");
			GridBagConstraints gbc_lblServerAddress = new GridBagConstraints();
			gbc_lblServerAddress.anchor = GridBagConstraints.EAST;
			gbc_lblServerAddress.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerAddress.gridx = 1;
			gbc_lblServerAddress.gridy = row;
			serverPanel.add(lblServerAddress, gbc_lblServerAddress);

			txtServerAddress = new JTextField();
			GridBagConstraints gbc_txtServerAddress = new GridBagConstraints();
			gbc_txtServerAddress.insets = new Insets(0, 0, 5, 5);
			gbc_txtServerAddress.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtServerAddress.gridx = 2;
			gbc_txtServerAddress.gridy = row;
			serverPanel.add(txtServerAddress, gbc_txtServerAddress);
			txtServerAddress.setColumns(10);

			JLabel lblVersion = new JLabel("Minecraft Version:");
			GridBagConstraints gbc_lblVersion = new GridBagConstraints();
			gbc_lblVersion.anchor = GridBagConstraints.EAST;
			gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
			gbc_lblVersion.gridx = 3;
			gbc_lblVersion.gridy = row;
			serverPanel.add(lblVersion, gbc_lblVersion);

			txtVersion = new JTextField();
			GridBagConstraints gbc_txtVersion = new GridBagConstraints();
			gbc_txtVersion.insets = new Insets(0, 0, 5, 5);
			gbc_txtVersion.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtVersion.gridx = 4;
			gbc_txtVersion.gridy = row;
			serverPanel.add(txtVersion, gbc_txtVersion);
			txtVersion.setColumns(10);

			row++;
			
			JLabel lblIconURL = new JLabel("Icon URL:");
			GridBagConstraints gbc_lblIconURL = new GridBagConstraints();
			gbc_lblIconURL.anchor = GridBagConstraints.EAST;
			gbc_lblIconURL.insets = new Insets(0, 0, 5, 5);
			gbc_lblIconURL.gridx = 1;
			gbc_lblIconURL.gridy = row;
			serverPanel.add(lblIconURL, gbc_lblIconURL);

			txtIconURL = new JTextField();
			GridBagConstraints gbc_txtIconURL = new GridBagConstraints();
			gbc_txtIconURL.insets = new Insets(0, 0, 5, 5);
			gbc_txtIconURL.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtIconURL.gridx = 2;
			gbc_txtIconURL.gridy = row;
			serverPanel.add(txtIconURL, gbc_txtIconURL);
			txtIconURL.setColumns(10);

			JLabel lblRevision = new JLabel("ServerPack Revision:");
			GridBagConstraints gbc_lblRevision = new GridBagConstraints();
			gbc_lblRevision.anchor = GridBagConstraints.EAST;
			gbc_lblRevision.insets = new Insets(0, 0, 5, 5);
			gbc_lblRevision.gridx = 3;
			gbc_lblRevision.gridy = row;
			serverPanel.add(lblRevision, gbc_lblRevision);

			txtRevision = new JTextField();
			GridBagConstraints gbc_txtRevision = new GridBagConstraints();
			gbc_txtRevision.insets = new Insets(0, 0, 5, 5);
			gbc_txtRevision.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtRevision.gridx = 4;
			gbc_txtRevision.gridy = row;
			serverPanel.add(txtRevision, gbc_txtRevision);
			txtRevision.setColumns(10);
			
			row++;

			Component rigidArea_1 = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea_1 = new GridBagConstraints();
			gbc_rigidArea_1.gridx = 5;
			gbc_rigidArea_1.gridy = row;
			serverPanel.add(rigidArea_1, gbc_rigidArea_1);

		}
		serverPanel.setSize(serverPanel.getWidth(), (int)serverPanel.getMinimumSize().getHeight());
		
		JPanel detailPanel = new JPanel();
		frmMain.getContentPane().add(detailPanel, BorderLayout.CENTER);
		detailPanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel modulePanel = new JPanel();
		modulePanel.setBorder(new TitledBorder(null, "Modules", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		detailPanel.add(modulePanel);
		GridBagLayout gbl_modulePanel = new GridBagLayout();
		gbl_modulePanel.columnWidths = new int[]{0, 0};
		gbl_modulePanel.rowHeights = new int[]{0, 0, 0};
		gbl_modulePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_modulePanel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		modulePanel.setLayout(gbl_modulePanel);
		
		JPanel modListPanel = new JPanel();
		GridBagConstraints gbc_modListPanel = new GridBagConstraints();
		gbc_modListPanel.insets = new Insets(0, 0, 5, 0);
		gbc_modListPanel.fill = GridBagConstraints.BOTH;
		gbc_modListPanel.gridx = 0;
		gbc_modListPanel.gridy = 0;
		modulePanel.add(modListPanel, gbc_modListPanel);
		modListPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane modListScroll = new JScrollPane();
		modListPanel.add(modListScroll, BorderLayout.CENTER);

		JList lstModules = new JList();
		lstModules.setModel(new ModuleListModel(modList));
		lstModules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modListScroll.setViewportView(lstModules);
		
		JPanel modDetailPanel = new JPanel();
		modDetailPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_modDetailPanel = new GridBagConstraints();
		gbc_modDetailPanel.fill = GridBagConstraints.BOTH;
		gbc_modDetailPanel.gridx = 0;
		gbc_modDetailPanel.gridy = 1;
		modulePanel.add(modDetailPanel, gbc_modDetailPanel);
		GridBagLayout gbl_modDetailPanel = new GridBagLayout();
		gbl_modDetailPanel.columnWidths = new int[]{0, 60, 86, 0, 0, 0};
		gbl_modDetailPanel.rowHeights = new int[]{0, 20, 0, 0, 0, 0, 0, 0};
		gbl_modDetailPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_modDetailPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		modDetailPanel.setLayout(gbl_modDetailPanel);
		
		{ // modDetailPanel

			int row = 0;

			{
				Component rigidArea_6 = Box.createRigidArea(new Dimension(3, 3));
				GridBagConstraints gbc_rigidArea_6 = new GridBagConstraints();
				gbc_rigidArea_6.anchor = GridBagConstraints.WEST;
				gbc_rigidArea_6.insets = new Insets(0, 0, 5, 5);
				gbc_rigidArea_6.gridx = 0;
				gbc_rigidArea_6.gridy = row;
				modDetailPanel.add(rigidArea_6, gbc_rigidArea_6);

				row++;
			}
			{
				JLabel lblModName = new JLabel("Name:");
				lblModName.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblModName = new GridBagConstraints();
				gbc_lblModName.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblModName.insets = new Insets(0, 0, 5, 5);
				gbc_lblModName.gridx = 1;
				gbc_lblModName.gridy = row;
				modDetailPanel.add(lblModName, gbc_lblModName);

				txtModName = new JTextField();
				GridBagConstraints gbc_txtModName = new GridBagConstraints();
				gbc_txtModName.gridwidth = 3;
				gbc_txtModName.insets = new Insets(0, 0, 5, 5);
				gbc_txtModName.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtModName.anchor = GridBagConstraints.NORTH;
				gbc_txtModName.gridx = 2;
				gbc_txtModName.gridy = row;
				modDetailPanel.add(txtModName, gbc_txtModName);
				txtModName.setColumns(10);

				row++;
			}
			{
				JLabel lblModId = new JLabel("ID:");
				lblModId.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblModId = new GridBagConstraints();
				gbc_lblModId.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblModId.insets = new Insets(0, 0, 5, 5);
				gbc_lblModId.gridx = 1;
				gbc_lblModId.gridy = row;
				modDetailPanel.add(lblModId, gbc_lblModId);

				txtModId = new JTextField();
				GridBagConstraints gbc_txtModId = new GridBagConstraints();
				gbc_txtModId.gridwidth = 3;
				gbc_txtModId.insets = new Insets(0, 0, 5, 5);
				gbc_txtModId.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtModId.anchor = GridBagConstraints.NORTH;
				gbc_txtModId.gridx = 2;
				gbc_txtModId.gridy = row;
				modDetailPanel.add(txtModId, gbc_txtModId);
				txtModId.setColumns(10);

				row++;
			}
			{
				JLabel lblUrl = new JLabel("URL:");
				lblUrl.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblUrl = new GridBagConstraints();
				gbc_lblUrl.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblUrl.insets = new Insets(0, 0, 5, 5);
				gbc_lblUrl.gridx = 1;
				gbc_lblUrl.gridy = row;
				modDetailPanel.add(lblUrl, gbc_lblUrl);

				txtUrl = new JTextField();
				GridBagConstraints gbc_txtUrl = new GridBagConstraints();
				gbc_txtUrl.gridwidth = 3;
				gbc_txtUrl.insets = new Insets(0, 0, 5, 5);
				gbc_txtUrl.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtUrl.gridx = 2;
				gbc_txtUrl.gridy = row;
				modDetailPanel.add(txtUrl, gbc_txtUrl);
				txtUrl.setColumns(10);

				row++;
			}
			{
				JLabel lblMD5 = new JLabel("MD5 Checksum:");
				lblMD5.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblMD5 = new GridBagConstraints();
				gbc_lblMD5.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblMD5.insets = new Insets(0, 0, 5, 5);
				gbc_lblMD5.gridx = 1;
				gbc_lblMD5.gridy = row;
				modDetailPanel.add(lblMD5, gbc_lblMD5);

				txtMD5 = new JTextField();
				GridBagConstraints gbc_txtMD5 = new GridBagConstraints();
				gbc_txtMD5.gridwidth = 3;
				gbc_txtMD5.insets = new Insets(0, 0, 5, 5);
				gbc_txtMD5.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtMD5.gridx = 2;
				gbc_txtMD5.gridy = row;
				modDetailPanel.add(txtMD5, gbc_txtMD5);
				txtMD5.setColumns(10);

				row++;
			}
			{
				JLabel lblRequired = new JLabel("Required:");
				lblRequired.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblRequired = new GridBagConstraints();
				gbc_lblRequired.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblRequired.insets = new Insets(0, 0, 5, 5);
				gbc_lblRequired.gridx = 1;
				gbc_lblRequired.gridy = row;
				modDetailPanel.add(lblRequired, gbc_lblRequired);

				JCheckBox chkRequired = new JCheckBox("");
				GridBagConstraints gbc_chkRequired = new GridBagConstraints();
				gbc_chkRequired.insets = new Insets(0, 0, 5, 5);
				gbc_chkRequired.anchor = GridBagConstraints.WEST;
				gbc_chkRequired.gridx = 2;
				gbc_chkRequired.gridy = row;
				modDetailPanel.add(chkRequired, gbc_chkRequired);

				JLabel lblInJar = new JLabel("In JAR:");
				lblInJar.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblInJar = new GridBagConstraints();
				gbc_lblInJar.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblInJar.insets = new Insets(0, 0, 5, 5);
				gbc_lblInJar.gridx = 3;
				gbc_lblInJar.gridy = row;
				modDetailPanel.add(lblInJar, gbc_lblInJar);

				JCheckBox chkInJar = new JCheckBox("");
				GridBagConstraints gbc_chkInJar = new GridBagConstraints();
				gbc_chkInJar.insets = new Insets(0, 0, 5, 5);
				gbc_chkInJar.anchor = GridBagConstraints.WEST;
				gbc_chkInJar.gridx = 4;
				gbc_chkInJar.gridy = row;
				modDetailPanel.add(chkInJar, gbc_chkInJar);

				row++;
			}
			{
				JLabel lblCoreMod = new JLabel("Core Mod:");
				lblCoreMod.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblCoreMod = new GridBagConstraints();
				gbc_lblCoreMod.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblCoreMod.insets = new Insets(0, 0, 5, 5);
				gbc_lblCoreMod.gridx = 1;
				gbc_lblCoreMod.gridy = row;
				modDetailPanel.add(lblCoreMod, gbc_lblCoreMod);

				JCheckBox chkCoreMod = new JCheckBox("");
				GridBagConstraints gbc_chkCoreMod = new GridBagConstraints();
				gbc_chkCoreMod.insets = new Insets(0, 0, 5, 5);
				gbc_chkCoreMod.anchor = GridBagConstraints.WEST;
				gbc_chkCoreMod.gridx = 2;
				gbc_chkCoreMod.gridy = row;
				modDetailPanel.add(chkCoreMod, gbc_chkCoreMod);

				JLabel lblIsDefault = new JLabel("Is Default:");
				lblIsDefault.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblIsDefault = new GridBagConstraints();
				gbc_lblIsDefault.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblIsDefault.insets = new Insets(0, 0, 5, 5);
				gbc_lblIsDefault.gridx = 3;
				gbc_lblIsDefault.gridy = row;
				modDetailPanel.add(lblIsDefault, gbc_lblIsDefault);

				JCheckBox chkIsDefault = new JCheckBox("");
				GridBagConstraints gbc_chkIsDefault = new GridBagConstraints();
				gbc_chkIsDefault.insets = new Insets(0, 0, 5, 5);
				gbc_chkIsDefault.anchor = GridBagConstraints.WEST;
				gbc_chkIsDefault.gridx = 4;
				gbc_chkIsDefault.gridy = row;
				modDetailPanel.add(chkIsDefault, gbc_chkIsDefault);

				row++;
			}
			{
				JLabel lblExtract = new JLabel("Extract:");
				lblExtract.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblExtract = new GridBagConstraints();
				gbc_lblExtract.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblExtract.insets = new Insets(0, 0, 5, 5);
				gbc_lblExtract.gridx = 1;
				gbc_lblExtract.gridy = row;
				modDetailPanel.add(lblExtract, gbc_lblExtract);

				JCheckBox chkExtract = new JCheckBox("");
				GridBagConstraints gbc_chkExtract = new GridBagConstraints();
				gbc_chkExtract.insets = new Insets(0, 0, 5, 5);
				gbc_chkExtract.anchor = GridBagConstraints.WEST;
				gbc_chkExtract.gridx = 2;
				gbc_chkExtract.gridy = row;
				modDetailPanel.add(chkExtract, gbc_chkExtract);

				JLabel lblInRoot = new JLabel("In Root:");
				lblInRoot.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblInRoot = new GridBagConstraints();
				gbc_lblInRoot.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblInRoot.insets = new Insets(0, 0, 5, 5);
				gbc_lblInRoot.gridx = 3;
				gbc_lblInRoot.gridy = row;
				modDetailPanel.add(lblInRoot, gbc_lblInRoot);

				JCheckBox chkInRoot = new JCheckBox("");
				GridBagConstraints gbc_chkInRoot = new GridBagConstraints();
				gbc_chkInRoot.insets = new Insets(0, 0, 5, 5);
				gbc_chkInRoot.anchor = GridBagConstraints.WEST;
				gbc_chkInRoot.gridx = 4;
				gbc_chkInRoot.gridy = row;
				modDetailPanel.add(chkInRoot, gbc_chkInRoot);

				row++;
			}
			{
				JButton btnModAdd = new JButton("Add");
				btnModAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				GridBagConstraints gbc_btnModAdd = new GridBagConstraints();
				gbc_btnModAdd.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModAdd.insets = new Insets(0, 0, 5, 5);
				gbc_btnModAdd.gridx = 1;
				gbc_btnModAdd.gridy = row;
				modDetailPanel.add(btnModAdd, gbc_btnModAdd);

				row++;
			}
			{
				JButton btnModRemove = new JButton("Remove");
				btnModRemove.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				GridBagConstraints gbc_btnModRemove = new GridBagConstraints();
				gbc_btnModRemove.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModRemove.insets = new Insets(0, 0, 5, 5);
				gbc_btnModRemove.gridx = 1;
				gbc_btnModRemove.gridy = row;
				modDetailPanel.add(btnModRemove, gbc_btnModRemove);

				row++;
			}
			{
				Component rigidArea_7 = Box.createRigidArea(new Dimension(3, 3));
				GridBagConstraints gbc_rigidArea_7 = new GridBagConstraints();
				gbc_rigidArea_7.gridx = 5;
				gbc_rigidArea_7.gridy = row;
				modDetailPanel.add(rigidArea_7, gbc_rigidArea_7);
			}
		}
		
		JPanel configFilePanel = new JPanel();
		configFilePanel.setBorder(new TitledBorder(null, "Config Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		detailPanel.add(configFilePanel);
		GridBagLayout gbl_configFilePanel = new GridBagLayout();
		gbl_configFilePanel.columnWidths = new int[]{0, 0};
		gbl_configFilePanel.rowHeights = new int[]{0, 0, 0};
		gbl_configFilePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_configFilePanel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		configFilePanel.setLayout(gbl_configFilePanel);
		
		JPanel configListPanel = new JPanel();
		GridBagConstraints gbc_configListPanel = new GridBagConstraints();
		gbc_configListPanel.insets = new Insets(0, 0, 5, 0);
		gbc_configListPanel.fill = GridBagConstraints.BOTH;
		gbc_configListPanel.gridx = 0;
		gbc_configListPanel.gridy = 0;
		configFilePanel.add(configListPanel, gbc_configListPanel);
		configListPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane configListScroll = new JScrollPane();
		configListPanel.add(configListScroll, BorderLayout.CENTER);
		
		JList lstConfigFiles = new JList();
		lstConfigFiles.setModel(new ConfigFileListModel(configList ));
		lstConfigFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configListScroll.setViewportView(lstConfigFiles);
		
		JPanel configDetailPanel = new JPanel();
		configDetailPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_configDetailPanel = new GridBagConstraints();
		gbc_configDetailPanel.fill = GridBagConstraints.BOTH;
		gbc_configDetailPanel.gridx = 0;
		gbc_configDetailPanel.gridy = 1;
		configFilePanel.add(configDetailPanel, gbc_configDetailPanel);
		GridBagLayout gbl_configDetailPanel = new GridBagLayout();
		gbl_configDetailPanel.columnWidths = new int[]{0, 0, 23, 0, 0};
		gbl_configDetailPanel.rowHeights = new int[]{0, 14, 0, 0, 0, 0, 0};
		gbl_configDetailPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_configDetailPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		configDetailPanel.setLayout(gbl_configDetailPanel);
		
		{ // configDetailPanel
			int row = 0;

			Component rigidArea_2 = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea_2 = new GridBagConstraints();
			gbc_rigidArea_2.insets = new Insets(0, 0, 5, 5);
			gbc_rigidArea_2.gridx = 0;
			gbc_rigidArea_2.gridy = row;
			configDetailPanel.add(rigidArea_2, gbc_rigidArea_2);

			row++;
			
			JLabel lblConfigURL = new JLabel("URL:");
			lblConfigURL.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_lblConfigURL = new GridBagConstraints();
			gbc_lblConfigURL.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblConfigURL.insets = new Insets(0, 0, 5, 5);
			gbc_lblConfigURL.anchor = GridBagConstraints.NORTH;
			gbc_lblConfigURL.gridx = 1;
			gbc_lblConfigURL.gridy = row;
			configDetailPanel.add(lblConfigURL, gbc_lblConfigURL);

			txtConfigURL = new JTextField();
			GridBagConstraints gbc_txtConfigURL = new GridBagConstraints();
			gbc_txtConfigURL.insets = new Insets(0, 0, 5, 5);
			gbc_txtConfigURL.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtConfigURL.gridx = 2;
			gbc_txtConfigURL.gridy = row;
			configDetailPanel.add(txtConfigURL, gbc_txtConfigURL);
			txtConfigURL.setColumns(10);

			row++;
			
			JLabel lblConfigPath = new JLabel("Path:");
			lblConfigPath.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_lblConfigPath = new GridBagConstraints();
			gbc_lblConfigPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblConfigPath.insets = new Insets(0, 0, 5, 5);
			gbc_lblConfigPath.gridx = 1;
			gbc_lblConfigPath.gridy = row;
			configDetailPanel.add(lblConfigPath, gbc_lblConfigPath);

			txtConfigPath = new JTextField();
			GridBagConstraints gbc_txtConfigPath = new GridBagConstraints();
			gbc_txtConfigPath.insets = new Insets(0, 0, 5, 5);
			gbc_txtConfigPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtConfigPath.gridx = 2;
			gbc_txtConfigPath.gridy = row;
			configDetailPanel.add(txtConfigPath, gbc_txtConfigPath);
			txtConfigPath.setColumns(10);

			row++;
			
			JLabel lblConfigMD5 = new JLabel("MD5 Checksum:");
			lblConfigMD5.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_lblConfigMD5 = new GridBagConstraints();
			gbc_lblConfigMD5.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblConfigMD5.insets = new Insets(0, 0, 5, 5);
			gbc_lblConfigMD5.gridx = 1;
			gbc_lblConfigMD5.gridy = row;
			configDetailPanel.add(lblConfigMD5, gbc_lblConfigMD5);

			txtConfigMD5 = new JTextField();
			GridBagConstraints gbc_txtConfigMD5 = new GridBagConstraints();
			gbc_txtConfigMD5.insets = new Insets(0, 0, 5, 5);
			gbc_txtConfigMD5.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtConfigMD5.gridx = 2;
			gbc_txtConfigMD5.gridy = row;
			configDetailPanel.add(txtConfigMD5, gbc_txtConfigMD5);
			txtConfigMD5.setColumns(10);

			row++;
			
			JButton btnAdd_1 = new JButton("Add");
			btnAdd_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				}
			});
			GridBagConstraints gbc_btnAdd_1 = new GridBagConstraints();
			gbc_btnAdd_1.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnAdd_1.insets = new Insets(0, 0, 5, 5);
			gbc_btnAdd_1.gridx = 1;
			gbc_btnAdd_1.gridy = row;
			configDetailPanel.add(btnAdd_1, gbc_btnAdd_1);

			row++;
			
			JButton btnRemove_1 = new JButton("Remove");
			btnRemove_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				}
			});
			GridBagConstraints gbc_btnRemove_1 = new GridBagConstraints();
			gbc_btnRemove_1.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnRemove_1.insets = new Insets(0, 0, 5, 5);
			gbc_btnRemove_1.gridx = 1;
			gbc_btnRemove_1.gridy = row;
			configDetailPanel.add(btnRemove_1, gbc_btnRemove_1);

			row++;
			
			Component rigidArea_3 = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea_3 = new GridBagConstraints();
			gbc_rigidArea_3.gridx = 3;
			gbc_rigidArea_3.gridy = row;
			configDetailPanel.add(rigidArea_3, gbc_rigidArea_3);

		}
	}

	@Override
	public void setLblStatus(String string) {
		
	}

	@Override
	public void setProgressBar(int i) {
		
	}
	
	protected String getServerName() {
		return txtServerName.getText();
	}
	protected String getNewsUrl() {
		return txtNewsUrl.getText();
	}
	protected String getServerAddress() {
		return txtServerAddress.getText();
	}
	protected String getVersion() {
		return txtVersion.getText();
	}

	@Override
	public void log(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean requestLogin() {
		// TODO Auto-generated method stub
		return false;
	}
}

class ModuleListModel extends AbstractListModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8669589670935830304L;
	List<Module> modules = new ArrayList<Module>();
	
	public ModuleListModel(List<Module> modList) {
		this.modules = modList;
	}
	
	@Override
	public int getSize() {
		return modules.size();
	}

	@Override
	public Object getElementAt(int index) {
		return modules.get(index);
	}
}

class ConfigFileListModel extends AbstractListModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4310927230482995630L;
	List<ConfigFile> configs = new ArrayList<ConfigFile>();
	
	public ConfigFileListModel(List<ConfigFile> configList) {
		this.configs = configList;
	}
	
	@Override
	public int getSize() {
		return configs.size();
	}
	
	@Override
	public Object getElementAt(int index) {
		return configs.get(index);
	}
}
package org.smbarbour.mcu;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.smbarbour.mcu.util.ConfigFile;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.Module;
import org.smbarbour.mcu.util.ServerList;
import org.smbarbour.mcu.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JCheckBoxMenuItem;

public class ServerForm extends MCUApp {

	private static ServerForm window;
	private JFrame frmMain;
	final MCUpdater mcu = MCUpdater.getInstance();
	private JTextField txtServerName;
	private JTextField txtServerNewsUrl;
	private JTextField txtServerAddress;
	private JTextField txtModName;
	private JTextField txtModUrl;
	private JTextField txtConfigUrl;
	private JTextField txtConfigPath;
	private JTextField txtServerMCVersion;
	private JTextField txtServerIconUrl;
	private JTextField txtServerRevision;
	private JTextField txtServerID;
	private JTextField txtModMD5;
	private JTextField txtModId;
	private JTextField txtConfigMD5;
	private JComboBox<String> lstConfigParentId;
	private JList<ConfigFileWrapper> lstConfigFiles;
	private JList<Module> lstModules;
	private ServerListModel modelServer;
	private ModuleListModel modelModule;
	private ConfigFileListModel modelConfig;
	private ModIdListModel modelParentId;
	private JTextField txtModDepends;
	private JCheckBox chkModInRoot;
	private JCheckBox chkModExtract;
	private JCheckBox chkModIsDefault;
	private JCheckBox chkModCoreMod;
	private JCheckBox chkModInJar;
	private JCheckBox chkModRequired;
	private JButton btnModMoveUp;
	private JButton btnModMoveDown;
	private JMenuItem mnuSave;
	private JList<ServerDefinition> lstServers;
	private boolean packDirty = false;
	private boolean serverDirty = false;
	private boolean moduleDirty = false;
	private boolean configDirty = false;
	private int serverCurrentSelection;
	private int moduleCurrentSelection;
	private int configCurrentSelection;

	protected Path currentFile;
	protected FileFilter xmlFilter = new FileFilter() {
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			
			String extension = getExtension(f);
			if (extension != null && extension.equalsIgnoreCase("xml")) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public String getDescription() {
			return "XML Files";
		}
		
		private String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }

	};
	private JCheckBox chkServerGenerateList;
	private JButton btnModSort;
	private JTextField txtModPath;
	private JComboBox<String> lstModSide;
	
	public ServerForm() {
		initialize();
		window = this;
		window.frmMain.setVisible(true);
		mcu.setParent(window);
	}
	
	private void initialize() {
		frmMain = new JFrame();
		frmMain.setTitle("MCUpdater - ServerPack Utility build " + Version.BUILD_VERSION + " (Implementing MCU-API " + Version.API_VERSION + ")");
		frmMain.setBounds(100,100,900,700);
		frmMain.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frmMain.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent ev) {
				doExit();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		frmMain.setJMenuBar(menuBar);
		
		JMenu mnuFile = new JMenu("File");
		mnuFile.setMnemonic('F');
		menuBar.add(mnuFile);
		
//		JMenuItem mnuNew = new JMenuItem("New");
//		mnuNew.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				modelModule.clear();
//				modelConfig.clear();
//				modelParentId.clear();
//			}
//		});
//		mnuFile.add(mnuNew);
		
		JMenuItem mnuOpen = new JMenuItem("Open...");
		mnuOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelModule.clear();
				modelConfig.clear();
				modelParentId.clear();
				modelParentId.add("");
				JFileChooser jfc = new JFileChooser();
				jfc.setFileFilter(xmlFilter);
				int result = jfc.showOpenDialog(frmMain);
				if (!(result == JFileChooser.APPROVE_OPTION)) { return; }				
				if (modelServer.getSize() > 0) { packDirty=true; }
				currentFile = jfc.getSelectedFile().toPath();
				mnuSave.setEnabled(true);
				
				try {
					Element docEle = null;
					Document serverHeader = ServerPackParser.readXmlFromFile(currentFile.toFile());
					if (!(serverHeader == null)) {
						Element parent = serverHeader.getDocumentElement();
						if (parent.getNodeName().equals("ServerPack")){
							NodeList servers = parent.getElementsByTagName("Server");
							for (int i = 0; i < servers.getLength(); i++){
								docEle = (Element)servers.item(i);
								ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), "", docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), ServerPackParser.parseBoolean(docEle.getAttribute("generateList")), docEle.getAttribute("revision"));
								List<Module> modules = new ArrayList<Module>(ServerPackParser.loadFromFile(currentFile.toFile(), docEle.getAttribute("id")));
								List<ConfigFileWrapper> configs = new ArrayList<ConfigFileWrapper>();
								for (Module modEntry : modules) {
									if (modEntry.getId().equals("")){
										modEntry.setId(modEntry.getName().replace(" ", ""));
									}
									for (ConfigFile cfEntry : modEntry.getConfigs()) {
										configs.add(new ConfigFileWrapper(modEntry.getId(), cfEntry));
									}
								}
								modelServer.add(new ServerDefinition(sl, modules, configs));
							}					
						} else {
							docEle = parent;
							ServerList sl = new ServerList(parent.getAttribute("id"), parent.getAttribute("name"), "", parent.getAttribute("newsUrl"), parent.getAttribute("iconUrl"), parent.getAttribute("version"), parent.getAttribute("serverAddress"), ServerPackParser.parseBoolean(parent.getAttribute("generateList")), parent.getAttribute("revision"));
							List<Module> modules = new ArrayList<Module>(ServerPackParser.loadFromFile(currentFile.toFile(), docEle.getAttribute("id")));
							List<ConfigFileWrapper> configs = new ArrayList<ConfigFileWrapper>();
							for (Module modEntry : modules) {
								if (modEntry.getId().equals("")){
									modEntry.setId(modEntry.getName().replace(" ", ""));
								}
								for (ConfigFile cfEntry : modEntry.getConfigs()) {
									configs.add(new ConfigFileWrapper(modEntry.getId(), cfEntry));
								}
							}
							modelServer.add(new ServerDefinition(sl, modules, configs));
						}
					} else {
						log("Unable to get server information from " + currentFile.toString());
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		//mnuOpen.setEnabled(false);
		mnuFile.add(mnuOpen);

		JMenuItem mntmScanFolder = new JMenuItem("Scan Folder...");
		mntmScanFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (lstServers.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(frmMain, "You must have a server selected first!", "MCUpdater", JOptionPane.WARNING_MESSAGE);
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(frmMain, "This will clear all existing modules and configurations.\n\nDo you wish to continue?", "MCU-ServerUtility", JOptionPane.YES_NO_OPTION);
				if (!(confirm == JOptionPane.YES_OPTION)) return;
				modelModule.clear();
				modelConfig.clear();
				modelParentId.clear();
				modelParentId.add("");
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showOpenDialog(frmMain);
				
				String urlBase = new String();
				urlBase = JOptionPane.showInputDialog("Enter base URL for downloads");
				
				Path rootPath = jfc.getSelectedFile().toPath();
				PathWalker pathWalk = new PathWalker(window, rootPath, urlBase);
				try {
					Files.walkFileTree(rootPath, pathWalk);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				modelParentId.sort();
				serverDirty = true;
				packDirty = true;
			}
		});
		mnuFile.add(mntmScanFolder);
		
		mnuSave = new JMenuItem("Save");
		mnuSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave(currentFile);
			}
		});
		mnuSave.setEnabled(false);		
		mnuFile.add(mnuSave);
		
		JMenuItem mnuSaveAs = new JMenuItem("Save As...");
		mnuSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveRequest();				
			}
		});
		mnuFile.add(mnuSaveAs);
		
		mnuFile.addSeparator();
		
		JMenuItem mnuExit = new JMenuItem("Exit");
		mnuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doExit();				
			}
		});
		mnuFile.add(mnuExit);
		
		JMenu mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);
		
		JCheckBoxMenuItem mnuAutosave = new JCheckBoxMenuItem("Auto-save updates");
		mnOptions.add(mnuAutosave);
		
		frmMain.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel serverListPanel = new JPanel();
		serverListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		frmMain.getContentPane().add(serverListPanel, BorderLayout.WEST);
		/*
		GridBagLayout gbl_serverListPanel = new GridBagLayout();		
		gbl_serverListPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_serverListPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_serverListPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_serverListPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		*/
		serverListPanel.setLayout(new BorderLayout(0,0));
		{
			lstServers = new JList<ServerDefinition>();
			lstServers.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting() == false) {
						if (serverDirty || moduleDirty || configDirty) {
							int confirm = JOptionPane.showConfirmDialog(frmMain, "Changes to the current selection(s) have been made.  Do you wish to save these changes?", "MCU-ServerUtility", JOptionPane.YES_NO_OPTION);
							if (confirm == JOptionPane.YES_OPTION) {
								if (configDirty) updateConfigEntry();
								if (moduleDirty) updateModuleEntry();
								updateServerEntry();
							}
						}
						if (lstServers.getSelectedIndex() > -1) {
							ServerDefinition changeTo = lstServers.getSelectedValue();
							txtServerName.setText(changeTo.getServer().getName());
							txtServerID.setText(changeTo.getServer().getServerId());
							txtServerAddress.setText(changeTo.getServer().getAddress());
							txtServerNewsUrl.setText(changeTo.getServer().getNewsUrl());
							txtServerIconUrl.setText(changeTo.getServer().getIconUrl());
							txtServerRevision.setText(changeTo.getServer().getRevision());
							txtServerMCVersion.setText(changeTo.getServer().getVersion());
							chkServerGenerateList.setSelected(changeTo.getServer().isGenerateList());
							modelModule.clear();
							modelParentId.clear();
							for (Module entry : changeTo.getModules()) {
								modelModule.add(entry);
								modelParentId.add(entry.getId());
							}
							modelParentId.sort();
							modelConfig.clear();
							for (ConfigFileWrapper entry : changeTo.getConfigs()) {
								modelConfig.add(entry);
							}
							serverCurrentSelection = lstServers.getSelectedIndex();
						}
						serverDirty=false;
						moduleDirty=false;
						configDirty=false;
					}
				}
			});
			modelServer = new ServerListModel();
			lstServers.setModel(modelServer);
			serverListPanel.add(lstServers, BorderLayout.CENTER);
			
			JPanel pnlSLCommands = new JPanel();
			GridBagLayout gbl_pnlSLCommands = new GridBagLayout();
			pnlSLCommands.setLayout(gbl_pnlSLCommands);
			serverListPanel.add(pnlSLCommands, BorderLayout.SOUTH);
			{
				Component SLCTopLeft = Box.createRigidArea(new Dimension(3,3));
				GridBagConstraints gbc_SLCTopLeft = new GridBagConstraints();
				gbc_SLCTopLeft.insets=new Insets(0, 0, 5, 5);
				gbc_SLCTopLeft.gridx=0;
				gbc_SLCTopLeft.gridy=0;
				pnlSLCommands.add(SLCTopLeft, gbc_SLCTopLeft);
				
				JButton btnServerNew = new JButton("New");
				btnServerNew.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modelServer.add(new ServerDefinition(new ServerList("newServer","New Server","","","","","",true,"1"), new ArrayList<Module>(), new ArrayList<ConfigFileWrapper>()));
						packDirty = true;
					}
				});
				GridBagConstraints gbc_btnServerNew = new GridBagConstraints();
				gbc_btnServerNew.insets=new Insets(0, 0, 5, 5);
				gbc_btnServerNew.gridx=1;
				gbc_btnServerNew.gridy=1;
				pnlSLCommands.add(btnServerNew, gbc_btnServerNew);
				
				JButton btnServerRemove = new JButton("Remove");
				btnServerRemove.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modelServer.remove(serverCurrentSelection);
						lstServers.clearSelection();
						lstModules.clearSelection();
						lstConfigFiles.clearSelection();
						lstConfigParentId.setSelectedIndex(-1);
						modelModule.clear();
						modelConfig.clear();
						modelParentId.clear();
						clearServerDetailPane();
						clearModuleDetailPane();
						clearConfigDetailPane();
						packDirty = true;
					}
				});
				GridBagConstraints gbc_btnServerRemove = new GridBagConstraints();
				gbc_btnServerRemove.insets=new Insets(0, 0, 5, 5);
				gbc_btnServerRemove.gridx=2;
				gbc_btnServerRemove.gridy=1;
				pnlSLCommands.add(btnServerRemove, gbc_btnServerRemove);
				
				JButton btnServerUpdate = new JButton("Update");
				btnServerUpdate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateServerEntry();
					}
				});
				GridBagConstraints gbc_btnServerUpdate = new GridBagConstraints();
				gbc_btnServerUpdate.insets=new Insets(0, 0, 5, 5);
				gbc_btnServerUpdate.gridx=3;
				gbc_btnServerUpdate.gridy=1;
				pnlSLCommands.add(btnServerUpdate, gbc_btnServerUpdate);

				Component SLCBottomRight = Box.createRigidArea(new Dimension(3,3));
				GridBagConstraints gbc_SLCBottomRight = new GridBagConstraints();
				gbc_SLCBottomRight.insets=new Insets(0, 0, 5, 5);
				gbc_SLCBottomRight.gridx=4;
				gbc_SLCBottomRight.gridy=2;
				pnlSLCommands.add(SLCBottomRight, gbc_SLCBottomRight);
			}
		}

		JPanel serverDetailPanel = new JPanel();
		serverDetailPanel.setLayout(new BorderLayout(0,0));
		frmMain.getContentPane().add(serverDetailPanel, BorderLayout.CENTER);
		
		JPanel serverPanel = new JPanel();
		serverPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		serverDetailPanel.add(serverPanel, BorderLayout.NORTH);
		GridBagLayout gbl_serverPanel = new GridBagLayout();
		gbl_serverPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_serverPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_serverPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_serverPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		serverPanel.setLayout(gbl_serverPanel);
		
		{ // serverPanel
			DocumentListener serverDocumentListener = new DocumentListener(){

				@Override
				public void insertUpdate(DocumentEvent e) {
					serverDirty = true;
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					serverDirty = true;				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					serverDirty = true;				}
				
			};
			
			int row = 0;

			Component rigidArea = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea = new GridBagConstraints();
			gbc_rigidArea.insets = new Insets(0, 0, 5, 5);
			gbc_rigidArea.gridx = 0;
			gbc_rigidArea.gridy = row;
			serverPanel.add(rigidArea, gbc_rigidArea);

			row++;

			JLabel lblServerName = new JLabel("Server Name:");
			GridBagConstraints gbc_lblServerName = new GridBagConstraints();
			gbc_lblServerName.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerName.anchor = GridBagConstraints.EAST;
			gbc_lblServerName.gridx = 1;
			gbc_lblServerName.gridy = row;
			serverPanel.add(lblServerName, gbc_lblServerName);

			txtServerName = new JTextField();
			txtServerName.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtServerName = new GridBagConstraints();
			gbc_txtServerName.insets = new Insets(0, 0, 5, 5);
			gbc_txtServerName.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtServerName.gridx = 2;
			gbc_txtServerName.gridy = row;
			serverPanel.add(txtServerName, gbc_txtServerName);
			txtServerName.setColumns(10);

			JLabel lblServerID = new JLabel("Server ID:");
			GridBagConstraints gbc_lblServerID = new GridBagConstraints();
			gbc_lblServerID.anchor = GridBagConstraints.EAST;
			gbc_lblServerID.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerID.gridx = 3;
			gbc_lblServerID.gridy = row;
			serverPanel.add(lblServerID, gbc_lblServerID);

			txtServerID = new JTextField();
			txtServerID.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtServerID = new GridBagConstraints();
			gbc_txtServerID.insets = new Insets(0, 0, 5, 5);
			gbc_txtServerID.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtServerID.gridx = 4;
			gbc_txtServerID.gridy = row;
			serverPanel.add(txtServerID, gbc_txtServerID);
			txtServerID.setColumns(10);

			row++;

			JLabel lblServerAddress = new JLabel("Server Address:");
			GridBagConstraints gbc_lblServerAddress = new GridBagConstraints();
			gbc_lblServerAddress.anchor = GridBagConstraints.EAST;
			gbc_lblServerAddress.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerAddress.gridx = 1;
			gbc_lblServerAddress.gridy = row;
			serverPanel.add(lblServerAddress, gbc_lblServerAddress);

			txtServerAddress = new JTextField();
			txtServerAddress.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtServerAddress = new GridBagConstraints();
			gbc_txtServerAddress.insets = new Insets(0, 0, 5, 5);
			gbc_txtServerAddress.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtServerAddress.gridx = 2;
			gbc_txtServerAddress.gridy = row;
			serverPanel.add(txtServerAddress, gbc_txtServerAddress);
			txtServerAddress.setColumns(10);

			JLabel lblNewsUrl = new JLabel("News URL:");
			GridBagConstraints gbc_lblNewsUrl = new GridBagConstraints();
			gbc_lblNewsUrl.anchor = GridBagConstraints.EAST;
			gbc_lblNewsUrl.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewsUrl.gridx = 3;
			gbc_lblNewsUrl.gridy = row;
			serverPanel.add(lblNewsUrl, gbc_lblNewsUrl);

			txtServerNewsUrl = new JTextField();
			txtServerNewsUrl.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtNewsUrl = new GridBagConstraints();
			gbc_txtNewsUrl.insets = new Insets(0, 0, 5, 5);
			gbc_txtNewsUrl.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtNewsUrl.gridx = 4;
			gbc_txtNewsUrl.gridy = row;
			serverPanel.add(txtServerNewsUrl, gbc_txtNewsUrl);
			txtServerNewsUrl.setColumns(10);

			row++;

			JLabel lblIconURL = new JLabel("Icon URL:");
			GridBagConstraints gbc_lblIconURL = new GridBagConstraints();
			gbc_lblIconURL.anchor = GridBagConstraints.EAST;
			gbc_lblIconURL.insets = new Insets(0, 0, 5, 5);
			gbc_lblIconURL.gridx = 1;
			gbc_lblIconURL.gridy = row;
			serverPanel.add(lblIconURL, gbc_lblIconURL);

			txtServerIconUrl = new JTextField();
			txtServerIconUrl.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtIconURL = new GridBagConstraints();
			gbc_txtIconURL.insets = new Insets(0, 0, 5, 5);
			gbc_txtIconURL.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtIconURL.gridx = 2;
			gbc_txtIconURL.gridy = row;
			serverPanel.add(txtServerIconUrl, gbc_txtIconURL);
			txtServerIconUrl.setColumns(10);

			JLabel lblVersion = new JLabel("Minecraft Version:");
			GridBagConstraints gbc_lblVersion = new GridBagConstraints();
			gbc_lblVersion.anchor = GridBagConstraints.EAST;
			gbc_lblVersion.insets = new Insets(0, 0, 5, 5);
			gbc_lblVersion.gridx = 3;
			gbc_lblVersion.gridy = row;
			serverPanel.add(lblVersion, gbc_lblVersion);

			txtServerMCVersion = new JTextField();
			txtServerMCVersion.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtVersion = new GridBagConstraints();
			gbc_txtVersion.insets = new Insets(0, 0, 5, 5);
			gbc_txtVersion.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtVersion.gridx = 4;
			gbc_txtVersion.gridy = row;
			serverPanel.add(txtServerMCVersion, gbc_txtVersion);
			txtServerMCVersion.setColumns(10);

			row++;
			
			JLabel lblRevision = new JLabel("ServerPack Revision:");
			GridBagConstraints gbc_lblRevision = new GridBagConstraints();
			gbc_lblRevision.anchor = GridBagConstraints.EAST;
			gbc_lblRevision.insets = new Insets(0, 0, 5, 5);
			gbc_lblRevision.gridx = 1;
			gbc_lblRevision.gridy = row;
			serverPanel.add(lblRevision, gbc_lblRevision);

			txtServerRevision = new JTextField();
			txtServerRevision.getDocument().addDocumentListener(serverDocumentListener);
			GridBagConstraints gbc_txtRevision = new GridBagConstraints();
			gbc_txtRevision.insets = new Insets(0, 0, 5, 5);
			gbc_txtRevision.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtRevision.gridx = 2;
			gbc_txtRevision.gridy = row;
			serverPanel.add(txtServerRevision, gbc_txtRevision);
			txtServerRevision.setColumns(10);
			
			JLabel lblGenerateList = new JLabel("Generate List:");
			lblGenerateList.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_lblGenerateList = new GridBagConstraints();
			gbc_lblGenerateList.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblGenerateList.insets = new Insets(0, 0, 5, 5);
			gbc_lblGenerateList.gridx = 3;
			gbc_lblGenerateList.gridy = row;
			serverPanel.add(lblGenerateList, gbc_lblGenerateList);

			chkServerGenerateList = new JCheckBox("");
			chkServerGenerateList.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					serverDirty = true;
				}
			});
			GridBagConstraints gbc_chkGenerateList = new GridBagConstraints();
			gbc_chkGenerateList.insets = new Insets(0, 0, 5, 5);
			gbc_chkGenerateList.anchor = GridBagConstraints.WEST;
			gbc_chkGenerateList.gridx = 4;
			gbc_chkGenerateList.gridy = row;
			serverPanel.add(chkServerGenerateList, gbc_chkGenerateList);
			
			row++;

			Component rigidArea_1 = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea_1 = new GridBagConstraints();
			gbc_rigidArea_1.gridx = 5;
			gbc_rigidArea_1.gridy = row;
			serverPanel.add(rigidArea_1, gbc_rigidArea_1);

		}
		serverPanel.setSize(serverPanel.getWidth(), (int)serverPanel.getMinimumSize().getHeight());
		
		JPanel detailPanel = new JPanel();
		serverDetailPanel.add(detailPanel, BorderLayout.CENTER);
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

		lstModules = new JList<Module>();
		lstModules.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (moduleDirty) {
						int confirm = JOptionPane.showConfirmDialog(frmMain, "Changes to the currently selected module have been made.  Do you wish to save these changes?", "MCU-ServerUtility", JOptionPane.YES_NO_OPTION);
						if (confirm == JOptionPane.YES_OPTION) {
							updateModuleEntry();
						}
					}
					if (lstModules.getSelectedIndex() > -1) {
						Module selected = lstModules.getSelectedValue();
						txtModName.setText(selected.getName());
						txtModId.setText(selected.getId());
						txtModMD5.setText(selected.getMD5());
						txtModDepends.setText(selected.getDepends());
						txtModUrl.setText(selected.getUrl());
						chkModRequired.setSelected(selected.getRequired());
						chkModInJar.setSelected(selected.getInJar());
						chkModCoreMod.setSelected(selected.getCoreMod());
						chkModIsDefault.setSelected(selected.getIsDefault());
						chkModExtract.setSelected(selected.getExtract());
						chkModInRoot.setSelected(selected.getInRoot());
						btnModMoveUp.setEnabled(moduleCurrentSelection == 0 ? false : true);
						btnModMoveDown.setEnabled(moduleCurrentSelection == modelModule.getSize()-1 ? false : true);
						moduleDirty=false;
						moduleCurrentSelection = lstModules.getSelectedIndex();
					}
				}
			}
		});
		modelModule = new ModuleListModel(new ArrayList<Module>());
		lstModules.setModel(modelModule);
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
			DocumentListener moduleDocumentListener = new DocumentListener(){
				@Override
				public void insertUpdate(DocumentEvent e) {moduleDirty = true;}

				@Override
				public void removeUpdate(DocumentEvent e) {moduleDirty = true;}

				@Override
				public void changedUpdate(DocumentEvent e) {moduleDirty = true;}				
			};
			
			ChangeListener moduleChangeListener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					moduleDirty = true;
				}
			};

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
				txtModName.getDocument().addDocumentListener(moduleDocumentListener);
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
				txtModId.getDocument().addDocumentListener(moduleDocumentListener);
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
				JLabel lblModDepends = new JLabel("Depends:");
				lblModDepends.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblModDepends = new GridBagConstraints();
				gbc_lblModDepends.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblModDepends.insets = new Insets(0, 0, 5, 5);
				gbc_lblModDepends.gridx = 1;
				gbc_lblModDepends.gridy = row;
				modDetailPanel.add(lblModDepends, gbc_lblModDepends);

				txtModDepends = new JTextField();
				txtModDepends.getDocument().addDocumentListener(moduleDocumentListener);
				GridBagConstraints gbc_txtModDepends = new GridBagConstraints();
				gbc_txtModDepends.gridwidth = 3;
				gbc_txtModDepends.insets = new Insets(0, 0, 5, 5);
				gbc_txtModDepends.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtModDepends.anchor = GridBagConstraints.NORTH;
				gbc_txtModDepends.gridx = 2;
				gbc_txtModDepends.gridy = row;
				modDetailPanel.add(txtModDepends, gbc_txtModDepends);
				txtModDepends.setColumns(10);

				row++;
			}
			{
				JLabel lblModUrl = new JLabel("URL:");
				lblModUrl.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblModUrl = new GridBagConstraints();
				gbc_lblModUrl.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblModUrl.insets = new Insets(0, 0, 5, 5);
				gbc_lblModUrl.gridx = 1;
				gbc_lblModUrl.gridy = row;
				modDetailPanel.add(lblModUrl, gbc_lblModUrl);

				txtModUrl = new JTextField();
				txtModUrl.getDocument().addDocumentListener(moduleDocumentListener);
				GridBagConstraints gbc_txtModUrl = new GridBagConstraints();
				gbc_txtModUrl.gridwidth = 3;
				gbc_txtModUrl.insets = new Insets(0, 0, 5, 5);
				gbc_txtModUrl.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtModUrl.gridx = 2;
				gbc_txtModUrl.gridy = row;
				modDetailPanel.add(txtModUrl, gbc_txtModUrl);
				txtModUrl.setColumns(10);

				row++;
			}
			{
				JLabel lblModPath = new JLabel("Path:");
				lblModPath.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblModPath = new GridBagConstraints();
				gbc_lblModPath.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblModPath.insets = new Insets(0, 0, 5, 5);
				gbc_lblModPath.gridx = 1;
				gbc_lblModPath.gridy = row;
				modDetailPanel.add(lblModPath, gbc_lblModPath);

				txtModPath = new JTextField();
				txtModPath.getDocument().addDocumentListener(moduleDocumentListener);
				GridBagConstraints gbc_txtModPath = new GridBagConstraints();
				gbc_txtModPath.gridwidth = 3;
				gbc_txtModPath.insets = new Insets(0, 0, 5, 5);
				gbc_txtModPath.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtModPath.gridx = 2;
				gbc_txtModPath.gridy = row;
				modDetailPanel.add(txtModPath, gbc_txtModPath);
				txtModPath.setColumns(10);

				row++;
			}
			{
				JLabel lblModSide = new JLabel("Side:");
				lblModSide.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblModSide = new GridBagConstraints();
				gbc_lblModSide.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblModSide.insets = new Insets(0, 0, 5, 5);
				gbc_lblModSide.gridx = 1;
				gbc_lblModSide.gridy = row;
				modDetailPanel.add(lblModSide, gbc_lblModSide);

				lstModSide = new JComboBox<String>(new String[]{"BOTH", "CLIENT", "SERVER"});
				lstModSide.addItemListener(new ItemListener(){
					@Override
					public void itemStateChanged(ItemEvent e) {
						moduleDirty = true;
					}						
				});
				GridBagConstraints gbc_lstModSide = new GridBagConstraints();
				gbc_lstModSide.gridwidth = 3;
				gbc_lstModSide.insets = new Insets(0, 0, 5, 5);
				gbc_lstModSide.fill = GridBagConstraints.HORIZONTAL;
				gbc_lstModSide.gridx = 2;
				gbc_lstModSide.gridy = row;
				modDetailPanel.add(lstModSide, gbc_lstModSide);

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

				txtModMD5 = new JTextField();
				txtModMD5.getDocument().addDocumentListener(moduleDocumentListener);
				GridBagConstraints gbc_txtMD5 = new GridBagConstraints();
				gbc_txtMD5.gridwidth = 3;
				gbc_txtMD5.insets = new Insets(0, 0, 5, 5);
				gbc_txtMD5.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtMD5.gridx = 2;
				gbc_txtMD5.gridy = row;
				modDetailPanel.add(txtModMD5, gbc_txtMD5);
				txtModMD5.setColumns(10);

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

				chkModRequired = new JCheckBox("");
				chkModRequired.addChangeListener(moduleChangeListener);
				GridBagConstraints gbc_chkRequired = new GridBagConstraints();
				gbc_chkRequired.insets = new Insets(0, 0, 5, 5);
				gbc_chkRequired.anchor = GridBagConstraints.WEST;
				gbc_chkRequired.gridx = 2;
				gbc_chkRequired.gridy = row;
				modDetailPanel.add(chkModRequired, gbc_chkRequired);

				JLabel lblInJar = new JLabel("In JAR:");
				lblInJar.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblInJar = new GridBagConstraints();
				gbc_lblInJar.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblInJar.insets = new Insets(0, 0, 5, 5);
				gbc_lblInJar.gridx = 3;
				gbc_lblInJar.gridy = row;
				modDetailPanel.add(lblInJar, gbc_lblInJar);

				chkModInJar = new JCheckBox("");
				chkModInJar.addChangeListener(moduleChangeListener);
				GridBagConstraints gbc_chkInJar = new GridBagConstraints();
				gbc_chkInJar.insets = new Insets(0, 0, 5, 5);
				gbc_chkInJar.anchor = GridBagConstraints.WEST;
				gbc_chkInJar.gridx = 4;
				gbc_chkInJar.gridy = row;
				modDetailPanel.add(chkModInJar, gbc_chkInJar);

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

				chkModCoreMod = new JCheckBox("");
				chkModCoreMod.addChangeListener(moduleChangeListener);
				GridBagConstraints gbc_chkCoreMod = new GridBagConstraints();
				gbc_chkCoreMod.insets = new Insets(0, 0, 5, 5);
				gbc_chkCoreMod.anchor = GridBagConstraints.WEST;
				gbc_chkCoreMod.gridx = 2;
				gbc_chkCoreMod.gridy = row;
				modDetailPanel.add(chkModCoreMod, gbc_chkCoreMod);

				JLabel lblIsDefault = new JLabel("Is Default:");
				lblIsDefault.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblIsDefault = new GridBagConstraints();
				gbc_lblIsDefault.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblIsDefault.insets = new Insets(0, 0, 5, 5);
				gbc_lblIsDefault.gridx = 3;
				gbc_lblIsDefault.gridy = row;
				modDetailPanel.add(lblIsDefault, gbc_lblIsDefault);

				chkModIsDefault = new JCheckBox("");
				chkModIsDefault.addChangeListener(moduleChangeListener);
				GridBagConstraints gbc_chkIsDefault = new GridBagConstraints();
				gbc_chkIsDefault.insets = new Insets(0, 0, 5, 5);
				gbc_chkIsDefault.anchor = GridBagConstraints.WEST;
				gbc_chkIsDefault.gridx = 4;
				gbc_chkIsDefault.gridy = row;
				modDetailPanel.add(chkModIsDefault, gbc_chkIsDefault);

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

				chkModExtract = new JCheckBox("");
				chkModExtract.addChangeListener(moduleChangeListener);
				GridBagConstraints gbc_chkExtract = new GridBagConstraints();
				gbc_chkExtract.insets = new Insets(0, 0, 5, 5);
				gbc_chkExtract.anchor = GridBagConstraints.WEST;
				gbc_chkExtract.gridx = 2;
				gbc_chkExtract.gridy = row;
				modDetailPanel.add(chkModExtract, gbc_chkExtract);

				JLabel lblInRoot = new JLabel("In Root:");
				lblInRoot.setHorizontalAlignment(SwingConstants.TRAILING);
				GridBagConstraints gbc_lblInRoot = new GridBagConstraints();
				gbc_lblInRoot.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblInRoot.insets = new Insets(0, 0, 5, 5);
				gbc_lblInRoot.gridx = 3;
				gbc_lblInRoot.gridy = row;
				modDetailPanel.add(lblInRoot, gbc_lblInRoot);

				chkModInRoot = new JCheckBox("");
				chkModInRoot.addChangeListener(moduleChangeListener);
				GridBagConstraints gbc_chkInRoot = new GridBagConstraints();
				gbc_chkInRoot.insets = new Insets(0, 0, 5, 5);
				gbc_chkInRoot.anchor = GridBagConstraints.WEST;
				gbc_chkInRoot.gridx = 4;
				gbc_chkInRoot.gridy = row;
				modDetailPanel.add(chkModInRoot, gbc_chkInRoot);

				row++;
			}
			{
				JButton btnModAdd = new JButton("Add");
				btnModAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Module newMod = new Module(txtModName.getText(), txtModId.getText(), txtModUrl.getText(), txtModDepends.getText(), chkModRequired.isSelected(), chkModInJar.isSelected(), chkModExtract.isSelected(), chkModInRoot.isSelected(), chkModIsDefault.isSelected(), chkModCoreMod.isSelected(), txtModMD5.getText(), null, lstModSide.getSelectedItem().toString(), txtModPath.getText());
						modelModule.add(newMod);
						modelParentId.add(newMod.getId());
						modelParentId.sort();
						serverDirty = true;
						packDirty = true;
					}
				});
				GridBagConstraints gbc_btnModAdd = new GridBagConstraints();
				gbc_btnModAdd.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModAdd.insets = new Insets(0, 0, 5, 5);
				gbc_btnModAdd.gridx = 1;
				gbc_btnModAdd.gridy = row;
				modDetailPanel.add(btnModAdd, gbc_btnModAdd);

				JButton btnModRemove = new JButton("Remove");
				btnModRemove.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modelParentId.remove(modelParentId.find(lstModules.getSelectedValue().getId()));
						modelModule.remove(moduleCurrentSelection);
						clearModuleDetailPane();
						lstModules.clearSelection();
						serverDirty = true;
						packDirty = true;
					}
				});
				GridBagConstraints gbc_btnModRemove = new GridBagConstraints();
				gbc_btnModRemove.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModRemove.insets = new Insets(0, 0, 5, 5);
				gbc_btnModRemove.gridx = 2;
				gbc_btnModRemove.gridy = row;
				modDetailPanel.add(btnModRemove, gbc_btnModRemove);

				JButton btnModUpdate = new JButton("Update");
				btnModUpdate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateModuleEntry();
					}
				});
				GridBagConstraints gbc_btnModUpdate = new GridBagConstraints();
				gbc_btnModUpdate.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModUpdate.insets = new Insets(0, 0, 5, 5);
				gbc_btnModUpdate.gridx = 3;
				gbc_btnModUpdate.gridy = row;
				modDetailPanel.add(btnModUpdate, gbc_btnModUpdate);
				
				row++;
			}
			{
				btnModMoveUp = new JButton("Move Up");
				btnModMoveUp.setEnabled(false);
				btnModMoveUp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int current = moduleCurrentSelection;
						modelModule.moveUp(current);
						lstModules.setSelectedIndex(current-1);
					}
				});
				GridBagConstraints gbc_btnModMoveUp = new GridBagConstraints();
				gbc_btnModMoveUp.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModMoveUp.insets = new Insets(0, 0, 5, 5);
				gbc_btnModMoveUp.gridx = 1;
				gbc_btnModMoveUp.gridy = row;
				modDetailPanel.add(btnModMoveUp, gbc_btnModMoveUp);
				
				btnModMoveDown = new JButton("Move Down");
				btnModMoveDown.setEnabled(false);
				btnModMoveDown.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int current = moduleCurrentSelection;
						modelModule.moveDown(current);
						lstModules.setSelectedIndex(current+1);
					}
				});
				GridBagConstraints gbc_btnModMoveDown = new GridBagConstraints();
				gbc_btnModMoveDown.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModMoveDown.insets = new Insets(0, 0, 5, 5);
				gbc_btnModMoveDown.gridx = 2;
				gbc_btnModMoveDown.gridy = row;
				modDetailPanel.add(btnModMoveDown, gbc_btnModMoveDown);

				btnModSort = new JButton("IntelliSort");
				btnModSort.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modelModule.sort();
					}
				});
				GridBagConstraints gbc_btnModSort = new GridBagConstraints();
				gbc_btnModSort.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnModSort.insets = new Insets(0, 0, 5, 5);
				gbc_btnModSort.gridx = 3;
				gbc_btnModSort.gridy = row;
				modDetailPanel.add(btnModSort, gbc_btnModSort);

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
		
		lstConfigFiles = new JList<ConfigFileWrapper>();
		modelConfig = new ConfigFileListModel(new ArrayList<ConfigFileWrapper>());
		lstConfigFiles.setModel(modelConfig);
		lstConfigFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstConfigFiles.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (configDirty) {
						int confirm = JOptionPane.showConfirmDialog(frmMain, "Changes to the currently selected configuration file have been made.  Do you wish to save these changes?", "MCU-ServerUtility", JOptionPane.YES_NO_OPTION);
						if (confirm == JOptionPane.YES_OPTION) {
							updateConfigEntry();
						}
					}
					if (lstConfigFiles.getSelectedIndex() > -1) {
						ConfigFileWrapper selected = lstConfigFiles.getSelectedValue();
						lstConfigParentId.setSelectedIndex(modelParentId.find(selected.getParentId()));
						lstConfigParentId.repaint();
						txtConfigMD5.setText(selected.getConfigFile().getMD5());
						txtConfigUrl.setText(selected.getConfigFile().getUrl());
						txtConfigPath.setText(selected.getConfigFile().getPath());
						configDirty=false;
						configCurrentSelection = lstConfigFiles.getSelectedIndex();
					}
				}
			}
		});
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
		gbl_configDetailPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0};
		gbl_configDetailPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		configDetailPanel.setLayout(gbl_configDetailPanel);
		
		{ // configDetailPanel
			DocumentListener configDocumentListener = new DocumentListener(){
				@Override
				public void insertUpdate(DocumentEvent e) {configDirty = true;}

				@Override
				public void removeUpdate(DocumentEvent e) {configDirty = true;}

				@Override
				public void changedUpdate(DocumentEvent e) {configDirty = true;}				
			};

			int row = 0;

			Component rigidArea_2 = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea_2 = new GridBagConstraints();
			gbc_rigidArea_2.insets = new Insets(0, 0, 5, 5);
			gbc_rigidArea_2.gridx = 0;
			gbc_rigidArea_2.gridy = row;
			configDetailPanel.add(rigidArea_2, gbc_rigidArea_2);

			row++;
			
			JLabel lblParentId = new JLabel("Parent Mod:");
			lblParentId.setHorizontalAlignment(SwingConstants.TRAILING);
			GridBagConstraints gbc_lblParentId = new GridBagConstraints();
			gbc_lblParentId.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblParentId.insets = new Insets(0, 0, 5, 5);
			gbc_lblParentId.anchor = GridBagConstraints.NORTH;
			gbc_lblParentId.gridx = 1;
			gbc_lblParentId.gridy = row;
			configDetailPanel.add(lblParentId, gbc_lblParentId);
			
			modelParentId = new ModIdListModel();
			lstConfigParentId = new JComboBox<String>(modelParentId);
			lstConfigParentId.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					configDirty=true;
				}		
			});
			GridBagConstraints gbc_lstParentId = new GridBagConstraints();
			gbc_lstParentId.gridwidth = 3;
			gbc_lstParentId.insets = new Insets(0, 0, 5, 5);
			gbc_lstParentId.fill = GridBagConstraints.HORIZONTAL;
			gbc_lstParentId.gridx = 2;
			gbc_lstParentId.gridy = row;
			configDetailPanel.add(lstConfigParentId, gbc_lstParentId);
			
			
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

			txtConfigUrl = new JTextField();
			txtConfigUrl.getDocument().addDocumentListener(configDocumentListener);
			GridBagConstraints gbc_txtConfigURL = new GridBagConstraints();
			gbc_txtConfigURL.gridwidth = 3;
			gbc_txtConfigURL.insets = new Insets(0, 0, 5, 5);
			gbc_txtConfigURL.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtConfigURL.gridx = 2;
			gbc_txtConfigURL.gridy = row;
			configDetailPanel.add(txtConfigUrl, gbc_txtConfigURL);
			txtConfigUrl.setColumns(10);

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
			txtConfigPath.getDocument().addDocumentListener(configDocumentListener);
			GridBagConstraints gbc_txtConfigPath = new GridBagConstraints();
			gbc_txtConfigPath.gridwidth = 3;
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
			txtConfigMD5.getDocument().addDocumentListener(configDocumentListener);
			GridBagConstraints gbc_txtConfigMD5 = new GridBagConstraints();
			gbc_txtConfigMD5.gridwidth = 3;
			gbc_txtConfigMD5.insets = new Insets(0, 0, 5, 5);
			gbc_txtConfigMD5.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtConfigMD5.gridx = 2;
			gbc_txtConfigMD5.gridy = row;
			configDetailPanel.add(txtConfigMD5, gbc_txtConfigMD5);
			txtConfigMD5.setColumns(10);

			row++;
			
			JButton btnConfigAdd = new JButton("Add");
			btnConfigAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConfigFileWrapper newConfig = new ConfigFileWrapper(lstConfigParentId.getSelectedItem().toString(), new ConfigFile(txtConfigUrl.getText(), txtConfigPath.getText(), txtConfigMD5.getText()));
					modelConfig.add(newConfig);
					serverDirty = true;
					packDirty = true;
				}
			});
			GridBagConstraints gbc_btnConfigAdd = new GridBagConstraints();
			gbc_btnConfigAdd.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnConfigAdd.insets = new Insets(0, 0, 5, 5);
			gbc_btnConfigAdd.gridx = 1;
			gbc_btnConfigAdd.gridy = row;
			configDetailPanel.add(btnConfigAdd, gbc_btnConfigAdd);

			JButton btnConfigRemove = new JButton("Remove");
			btnConfigRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					modelConfig.remove(configCurrentSelection);
					clearConfigDetailPane();
					lstConfigParentId.setSelectedIndex(-1);
					lstConfigFiles.clearSelection();
					serverDirty = true;
					packDirty = true;
				}
			});
			GridBagConstraints gbc_btnConfigRemove = new GridBagConstraints();
			gbc_btnConfigRemove.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnConfigRemove.insets = new Insets(0, 0, 5, 5);
			gbc_btnConfigRemove.gridx = 2;
			gbc_btnConfigRemove.gridy = row;
			configDetailPanel.add(btnConfigRemove, gbc_btnConfigRemove);

			JButton btnConfigUpdate = new JButton("Update");
			btnConfigUpdate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateConfigEntry();
				}
			});
			GridBagConstraints gbc_btnConfigUpdate = new GridBagConstraints();
			gbc_btnConfigUpdate.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnConfigUpdate.insets = new Insets(0, 0, 5, 5);
			gbc_btnConfigUpdate.gridx = 3;
			gbc_btnConfigUpdate.gridy = row;
			configDetailPanel.add(btnConfigUpdate, gbc_btnConfigUpdate);

			row++;
			
			Component rigidArea_3 = Box.createRigidArea(new Dimension(3, 3));
			GridBagConstraints gbc_rigidArea_3 = new GridBagConstraints();
			gbc_rigidArea_3.gridx = 5;
			gbc_rigidArea_3.gridy = row;
			configDetailPanel.add(rigidArea_3, gbc_rigidArea_3);

		}
	}

	protected void doExit() {
		if (packDirty) {
			int confirm = JOptionPane.showConfirmDialog(frmMain, "Changes to the currently selected server pack have been made.  Do you wish to save these changes?", "MCU-ServerUtility", JOptionPane.YES_NO_CANCEL_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				if (currentFile == null) {
					if (!doSaveRequest()) return;
				} else {
					doSave(currentFile);
				}
			} else if (confirm == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		frmMain.dispose();		
	}

	protected void clearConfigDetailPane() {
		lstConfigParentId.setSelectedIndex(-1);
		txtConfigPath.setText("");
		txtConfigUrl.setText("");
		txtConfigMD5.setText("");
		configDirty = false;
	}

	protected void clearModuleDetailPane() {
		txtModName.setText("");
		txtModId.setText("");
		txtModDepends.setText("");
		txtModMD5.setText("");
		txtModUrl.setText("");
		chkModCoreMod.setSelected(false);
		chkModExtract.setSelected(false);
		chkModInJar.setSelected(false);
		chkModInRoot.setSelected(false);
		chkModIsDefault.setSelected(false);
		chkModRequired.setSelected(false);
		moduleDirty = false;
	}

	protected void clearServerDetailPane() {
		txtServerAddress.setText("");
		txtServerID.setText("");
		txtServerName.setText("");
		txtServerNewsUrl.setText("");
		txtServerIconUrl.setText("");
		txtServerMCVersion.setText("");
		txtServerRevision.setText("");
		chkServerGenerateList.setSelected(false);
		serverDirty = false;
	}

	private String xmlEscape(String input) {
		return input.replace("\"", "&quot;").replace("'", "&apos;").replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;");
	}
	
	protected void doSave(Path outputFile) {
		try {
			if (configDirty) updateConfigEntry();
			if (moduleDirty) updateModuleEntry();
			if (serverDirty) updateServerEntry();
			BufferedWriter fileWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
			fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			fileWriter.newLine();
			fileWriter.write("<ServerPack version=\"" + Version.API_VERSION + "\" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='https://raw.github.com/smbarbour/MCUpdater/master/MCU-API/ServerPack.xsd'>");
			fileWriter.newLine();
			for (ServerDefinition server : modelServer.getContents()) {
				fileWriter.write("\t<Server id=\"" + server.getServer().getServerId() + "\" name=\"" + server.getServer().getName() + "\" newsUrl=\"" + server.getServer().getNewsUrl() + "\" iconUrl=\"" + server.getServer().getIconUrl() + "\" version=\"" + server.getServer().getVersion() + "\" serverAddress=\"" + server.getServer().getAddress() + "\" revision=\"" + server.getServer().getRevision() + "\" generateList=\"" + server.getServer().isGenerateList() + "\">");
				fileWriter.newLine();
				for (Module entry : server.getModules()) {
					fileWriter.write("\t\t<Module name=\"" + entry.getName() + "\" id=\"" + entry.getId() + "\" depends=\"" + entry.getDepends() + "\">");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<URL>" + xmlEscape(entry.getUrl()) + "</URL>");
					fileWriter.newLine();
					if (!entry.getPath().equals("")) {
						fileWriter.write("\t\t\t<Path>" + xmlEscape(entry.getPath()) + "</Path>");
						fileWriter.newLine();
					}
					fileWriter.write("\t\t\t<Required>" + (entry.getRequired() == true ? "true" : "false") + "</Required>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<IsDefault>" + (entry.getIsDefault() == true ? "true" : "false") + "</IsDefault>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<InJar>" + (entry.getInJar() == true ? "true" : "false") + "</InJar>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<Extract>" + (entry.getExtract() == true ? "true" : "false") + "</Extract>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<InRoot>" + (entry.getInRoot() == true ? "true" : "false") + "</InRoot>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<CoreMod>" + (entry.getCoreMod() == true ? "true" : "false") + "</CoreMod>");
					fileWriter.newLine();
					fileWriter.write("\t\t\t<MD5>" + xmlEscape(entry.getMD5()) + "</MD5>");
					fileWriter.newLine();
					for (ConfigFileWrapper cfw : server.getConfigs()) {
						if (cfw.getParentId().equals(entry.getId())) {
							fileWriter.write("\t\t\t\t<ConfigFile>");
							fileWriter.newLine();
							fileWriter.write("\t\t\t\t\t<URL>" + xmlEscape(cfw.getConfigFile().getUrl()) + "</URL>");
							fileWriter.newLine();
							fileWriter.write("\t\t\t\t\t<Path>" + xmlEscape(cfw.getConfigFile().getPath()) + "</Path>");
							fileWriter.newLine();
							fileWriter.write("\t\t\t\t\t<MD5>" + xmlEscape(cfw.getConfigFile().getMD5()) + "</MD5>");
							fileWriter.newLine();
							fileWriter.write("\t\t\t\t</ConfigFile>");
							fileWriter.newLine();
						}
					}
					fileWriter.write("\t\t</Module>");
					fileWriter.newLine();
				}
				fileWriter.write("\t</Server>");
				fileWriter.newLine();
			}
			fileWriter.write("</ServerPack>");
			fileWriter.newLine();
			fileWriter.close();
			packDirty = false;
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}

	public void AddModule(Module newMod) {
		modelModule.add(newMod);
		modelParentId.add(newMod.getId());
	}
	
	public void AddConfig(ConfigFileWrapper newConfig) {
		modelConfig.add(newConfig);
	}
	
	@Override
	public void setStatus(String string) {
		
	}

	@Override
	public void setProgressBar(int i) {
		
	}

	@Override
	public void log(String msg) {
		
	}

	@Override
	public boolean requestLogin() {
		return false;
	}

	private void updateServerEntry() {
		ServerList entry = new ServerList(txtServerID.getText(),txtServerName.getText(),"",txtServerNewsUrl.getText(),txtServerIconUrl.getText(),txtServerMCVersion.getText(),txtServerAddress.getText(),chkServerGenerateList.isSelected(),txtServerRevision.getText());
		List<Module> newModules = new ArrayList<Module>();
		newModules.addAll(modelModule.getContents());
		List<ConfigFileWrapper> newConfigs = new ArrayList<ConfigFileWrapper>();
		newConfigs.addAll(modelConfig.getContents());
		modelServer.replace(serverCurrentSelection, new ServerDefinition(entry, newModules, newConfigs));
		serverDirty = false;
		packDirty = true;
	}

	private void updateModuleEntry() {
		modelParentId.replaceEntry(lstModules.getSelectedValue().getId(), txtModId.getText());
		Module newMod = new Module(txtModName.getText(), txtModId.getText(), txtModUrl.getText(), txtModDepends.getText(), chkModRequired.isSelected(), chkModInJar.isSelected(), chkModExtract.isSelected(), chkModInRoot.isSelected(), chkModIsDefault.isSelected(), chkModCoreMod.isSelected(), txtModMD5.getText(), null, lstModSide.getSelectedItem().toString(), txtModPath.getText());
		modelModule.replace(moduleCurrentSelection, newMod);
		moduleDirty = false;
		serverDirty = true;
		packDirty = true;
	}

	private void updateConfigEntry() {
		String modId = "";
		try {
			modId = lstConfigParentId.getSelectedItem().toString();
		} catch (Exception e) { /* no op */ }
		ConfigFileWrapper newConfig = new ConfigFileWrapper(modId, new ConfigFile(txtConfigUrl.getText(), txtConfigPath.getText(), txtConfigMD5.getText()));
		modelConfig.replace(configCurrentSelection, newConfig);
		configDirty = false;
		serverDirty = true;
		packDirty = true;
	}

	private boolean doSaveRequest() {
		JFileChooser saveDialog = new JFileChooser();
		saveDialog.setFileFilter(xmlFilter );
		int result = saveDialog.showSaveDialog(frmMain);
		if (result == JFileChooser.APPROVE_OPTION) {
			Path outputFile = saveDialog.getSelectedFile().toPath();

			currentFile = outputFile;
			mnuSave.setEnabled(true);

			doSave(currentFile);
			return true;
		} else {
			return false;
		}
	}
}

class ModuleListModel extends AbstractListModel<Module> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8669589670935830304L;
	private List<Module> modules = new ArrayList<Module>();
	
	public ModuleListModel(List<Module> modList) {
		this.modules = modList;
	}
	
	public void moveUp(int current) {
		Collections.swap(modules, current, current-1);
		this.fireContentsChanged(this, current-1, current);
	}

	public void moveDown(int current) {
		Collections.swap(modules, current, current+1);
		this.fireContentsChanged(this, current, current+1);
	}
	
	public void sort() {
		Collections.sort(this.modules, new SortModules());
		this.fireContentsChanged(this, 0, this.modules.size());				
	}

	public void clear() {
		int current = modules.size() - 1;
		modules.clear();
		this.fireContentsChanged(this, 0, current);
	}

	public void replace(int index, Module newModule) {
		this.modules.set(index, newModule);
		this.fireContentsChanged(this, index, index);
	}
	
	public void add(Module newModule) {
		this.modules.add(newModule);
		this.fireContentsChanged(this, 0, modules.size());
	}

	public void remove(int index) {
		this.modules.remove(index);
		this.fireContentsChanged(this, 0, modules.size());
	}
	
	public List<Module> getContents() {
		return this.modules;
	}
	
	@Override
	public int getSize() {
		return this.modules.size();
	}

	@Override
	public Module getElementAt(int index) {
		return this.modules.get(index);
	}
}

class ConfigFileListModel extends AbstractListModel<ConfigFileWrapper> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4310927230482995630L;
	private List<ConfigFileWrapper> configs = new ArrayList<ConfigFileWrapper>();
	
	public ConfigFileListModel(List<ConfigFileWrapper> configList) {
		this.configs = configList;
	}
	
	public void clear() {
		int current = configs.size() - 1;
		this.configs.clear();
		this.fireContentsChanged(this, 0, current);
	}
		
	public void add(ConfigFileWrapper newConfig) {
		this.configs.add(newConfig);
		this.fireContentsChanged(this, 0, configs.size());
	}
	
	public void replace(int index, ConfigFileWrapper newConfig) {
		this.configs.set(index, newConfig);
		this.fireContentsChanged(this, 0, configs.size());
	}
	
	public void remove(int index) {
		this.configs.remove(index);
		this.fireContentsChanged(this, 0, configs.size());
	}
	
	public List<ConfigFileWrapper> getContents() {
		return this.configs;
	}
	
	@Override
	public int getSize() {
		return this.configs.size();
	}
	
	@Override
	public ConfigFileWrapper getElementAt(int index) {
		return this.configs.get(index);
	}
}

class ModIdListModel extends AbstractListModel<String> implements ComboBoxModel<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1133359312481243116L;
	private List<String> list = new ArrayList<String>();
	private String selected;

	public void clear() {
		int current = list.size() - 1;
		this.list.clear();
		this.fireContentsChanged(this, 0, current);
	}
	
	public int find(String parentId) {
		return this.list.indexOf(parentId);
	}

	public void add(String entry) {
		this.list.add(entry);
		sort();
	}
	
	public void remove(int index) {
		this.list.remove(index);
		sort();
	}
	
	public void replaceEntry(String oldEntry, String newEntry) {
		this.list.remove(oldEntry);
		this.list.add(newEntry);
		sort();
	}
	
	public void sort() {
		Collections.sort(this.list, new SortIgnoreCase());
		this.fireContentsChanged(this, 0, this.list.size()-1);		
	}
	
	@Override
	public int getSize() {
		return this.list.size();
	}

	@Override
	public String getElementAt(int index) {
		return this.list.get(index);
	}

	@Override
	public void setSelectedItem(Object anItem) {
		int newIndex = this.list.indexOf(anItem);
		if (newIndex >= 0) {
			this.selected = this.list.get(newIndex);
		} else {
			this.selected = "";
		}
	}

	@Override
	public Object getSelectedItem() {
		return this.selected;
	}	
}

class ServerListModel extends AbstractListModel<ServerDefinition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1724821606072665288L;
	private List<ServerDefinition> servers = new ArrayList<ServerDefinition>();
	@Override
	public int getSize() {
		return this.servers.size();
	}

	public void clear() {
		int current = servers.size() - 1;
		this.servers.clear();
		this.fireContentsChanged(this, 0, current);
	}
	
	public void add(ServerDefinition newServer) {
		this.servers.add(newServer);
		this.fireContentsChanged(this, 0, servers.size());
	}
	
	public void replace(int index, ServerDefinition newServer) {
		this.servers.set(index, newServer);
		this.fireContentsChanged(this, 0, servers.size());
	}
	
	public void remove(int index) {
		this.servers.remove(index);
		this.fireContentsChanged(this, 0, servers.size());
	}
	
	@Override
	public ServerDefinition getElementAt(int index) {
		return this.servers.get(index);
	}
	
	public List<ServerDefinition> getContents() {
		return this.servers;
	}
}

class SortIgnoreCase implements Comparator<String> {

	@Override
	public int compare(String arg0, String arg1) {
		return arg0.compareToIgnoreCase(arg1.toLowerCase());
	}
}

class SortModules implements Comparator<Module> {

	@Override
	public int compare(Module o1, Module o2) {
		Integer o1weight = (o1.getInJar() ? 0 : (o1.getCoreMod() ? 1 : 2));
		Integer o2weight = (o2.getInJar() ? 0 : (o2.getCoreMod() ? 1 : 2));
		if (o1weight == o2weight) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		} else {
			return o1weight.compareTo(o2weight);
		}
	}
	
}
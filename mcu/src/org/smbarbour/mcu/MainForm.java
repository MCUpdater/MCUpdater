package org.smbarbour.mcu;

import j7compat.Files;
import j7compat.Path;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import org.smbarbour.mcu.MCUApp;
import org.smbarbour.mcu.MCLoginException.ResponseType;
import org.smbarbour.mcu.util.InstanceManager;
import org.smbarbour.mcu.util.Localization;
import org.smbarbour.mcu.util.LoginData;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.Module;
import org.smbarbour.mcu.util.ServerList;
import org.smbarbour.mcu.util.ServerPackParser;
import org.smbarbour.mcu.util.ServerStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.JProgressBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import java.awt.GridLayout;

public class MainForm extends MCUApp {
	private static final ResourceBundle Customization = ResourceBundle.getBundle("customization"); //$NON-NLS-1$

	//Access check booleans
//	private boolean canWriteMinecraft = false;
	private boolean canWriteMCUpdater = false;
	private boolean canWriteInstances = false;
//	private boolean canCreateLinks = false;

	private static MainForm window;
	private Properties config = new Properties();
	private JFrame frmMain;
	final MCUpdater mcu = MCUpdater.getInstance();

	private JTabbedPane tabs;
	private final JTextPane browser = new MCUBrowser();
	private final ConsoleArea console = new ConsoleArea();

	private ServerList selected;
	private JPanel pnlRight;
	private final JPanel pnlModList = new JPanel();
	private JCheckBox chkHardUpdate;
	private JLabel lblStatus;
	private JProgressBar progressBar;
	private JList serverList;
	private SLListModel slModel;

	private JButton btnUpdate;
	private JButton btnLaunchMinecraft;

	private boolean minimized;
	private TrayIcon trayIcon;
	private ImageIcon mcuIcon;
	private JLabel lblPlayerName2;

	private LoginData loginData = new LoginData();

	private JLabel lblAvatar;

	public ResourceBundle getCustomization(){
		return Customization;
	}
	/**
	 * Create the application.
	 */
	public MainForm() {
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		CALogHandler consoleHandler = new CALogHandler(console);
		consoleHandler.setLevel(Level.INFO);
		FileHandler mcuHandler;
		try {
			mcuHandler = new FileHandler(mcu.getArchiveFolder().resolve("MCUpdater.log").toString(), 0, 3);
			mcuHandler.setFormatter(new FMLStyleFormatter());
			baseLogger.addHandler(mcuHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		baseLogger.addHandler(consoleHandler);
		mcu.apiLogger.addHandler(consoleHandler);
		Version.setApp(this);
		window = this;
		mcu.setParent(window);
		initialize();
		window.frmMain.setVisible(true);
		baseLogger.info("MCUpdater initialized.");
	}

	public Properties getConfig()
	{
		return config;
	}

	public void writeConfig(Properties newConfig)
	{
		baseLogger.info("Writing configuration file");
		File configFile = mcu.getArchiveFolder().resolve("config.properties").toFile();
		try {
			configFile.getParentFile().mkdirs();
			newConfig.store(new FileOutputStream(configFile), "User-specific configuration options");
			config = newConfig;
		} catch (FileNotFoundException e) {
			baseLogger.log(Level.SEVERE, "File not found: " + configFile.getAbsolutePath(), e);
		} catch (IOException e) {
			baseLogger.log(Level.SEVERE, "I/O Error writing configuration file", e);
		}
	}

	private void createDefaultConfig(File configFile) {
		baseLogger.info("Creating default configuration file");
		Properties newConfig = new Properties();
		newConfig.setProperty("minimumMemory", "512M");
		newConfig.setProperty("maximumMemory", "1G");
		newConfig.setProperty("permGen", "128M");
		newConfig.setProperty("jvmOpts", "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+AggressiveOpts");
		//newConfig.setProperty("currentConfig", "");
		//newConfig.setProperty("packRevision","");
		//newConfig.setProperty("suppressUpdates", "false");
		newConfig.setProperty("minimizeOnLaunch", (System.getProperty("os.name").startsWith("Mac")) ? "false" : "true");
		newConfig.setProperty("instanceRoot", mcu.getArchiveFolder().resolve("instances").toString());
		newConfig.setProperty("width", String.valueOf(1280));
		newConfig.setProperty("height", String.valueOf(720));
		newConfig.setProperty("allowAutoConnect", "true");
		if (System.getProperty("os.name").startsWith("Mac")) { newConfig.setProperty("jrePath", "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0"); }
		newConfig.setProperty("storePassword", "false");
		try {
			configFile.getParentFile().mkdirs();
			newConfig.store(new FileOutputStream(configFile), "User-specific configuration options");
		} catch (FileNotFoundException e) {
			baseLogger.log(Level.SEVERE, "File not found: " + configFile.getAbsolutePath(), e);
		} catch (IOException e) {
			baseLogger.log(Level.SEVERE, "I/O Error writing configuration file", e);
		}
	}

	public boolean validateConfig(Properties current)
	{
		boolean hasChanged = false;
		if (current.getProperty("minimumMemory") == null) {	current.setProperty("minimumMemory", "512M"); hasChanged = true; }
		if (current.getProperty("maximumMemory") == null) {	current.setProperty("maximumMemory", "1G"); hasChanged = true; }
		if (current.getProperty("permGen") == null) {	current.setProperty("permGen", "128M"); hasChanged = true; }
		if (current.getProperty("jvmOpts") == null || current.getProperty("jvmOpts").isEmpty()) {	current.setProperty("jvmOpts", "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+AggressiveOpts"); hasChanged = true; }
		//if (current.getProperty("currentConfig") == null) {	current.setProperty("currentConfig", ""); hasChanged = true; } // Made obsolete by instancing
		//if (current.getProperty("packRevision") == null) {	current.setProperty("packRevision",""); hasChanged = true; } // Made obsolete by instancing
		if (current.getProperty("minimizeOnLaunch") == null) { current.setProperty("minimizeOnLaunch", (System.getProperty("os.name").startsWith("Mac")) ? "false" : "true"); hasChanged = true; }
		//if (current.getProperty("suppressUpdates") == null) { current.setProperty("suppressUpdates", "false"); hasChanged = true; } // Made obsolete by native launcher
		if (current.getProperty("instanceRoot") == null) { current.setProperty("instanceRoot", mcu.getArchiveFolder().resolve("instances").toString()); }
		if (current.getProperty("width") == null) { current.setProperty("width", String.valueOf(1280)); hasChanged = true; }
		if (current.getProperty("height") == null) { current.setProperty("height", String.valueOf(720)); hasChanged = true; }
		if (current.getProperty("allowAutoConnect") == null) { current.setProperty("allowAutoConnect", "true"); hasChanged = true; }
		if (current.getProperty("storePassword") == null) { current.setProperty("storePassword", "false"); hasChanged = true; }
		if (current.getProperty("jrePath") == null && System.getProperty("os.name").startsWith("Mac")) { current.setProperty("jrePath", "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0"); }
		return hasChanged;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	void initialize() {
		loadConfig();
		checkAccess();
		baseLogger.fine("Start building GUI");
		frmMain = new JFrame();
		frmMain.setTitle("[No Server Selected] - MCUpdater " + Version.VERSION + Version.BUILD_LABEL);
		frmMain.setResizable(false);
		frmMain.setBounds(100, 100, 1175, 592);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			mcuIcon = new ImageIcon(MainForm.class.getResource("/art/mcu-icon.png"));	// was "/icons/briefcase.png"
			frmMain.setIconImage(mcuIcon.getImage());
		} catch( NullPointerException npe ) {
			baseLogger.log(Level.SEVERE, "Unable to find mcu-icon.png. Malformed JAR detected.", npe);
		}

		JPanel pnlFooter = new JPanel();
		frmMain.getContentPane().add(pnlFooter, BorderLayout.SOUTH);
		pnlFooter.setLayout(new BorderLayout(0, 0));

		JPanel pnlButtons = new JPanel();
		pnlFooter.add(pnlButtons, BorderLayout.EAST);

		btnUpdate = new JButton(Localization.getText("btn_Update"));
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateInstance();
			}
		});
		btnUpdate.setEnabled(false);
		btnUpdate.setToolTipText(Localization.getText("tip_Update"));
		pnlButtons.add(btnUpdate);

		btnLaunchMinecraft = new JButton(Localization.getText("btn_Launch"));
		btnLaunchMinecraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchMinecraft();
			}
		});
		btnLaunchMinecraft.setEnabled(false);
		btnLaunchMinecraft.setToolTipText(Localization.getText("tip_Launch"));
		pnlButtons.add(btnLaunchMinecraft);

		JPanel pnlStatus = new JPanel();
		pnlFooter.add(pnlStatus, BorderLayout.CENTER);
		pnlStatus.setLayout(new BorderLayout(0, 0));

		lblStatus = new JLabel("Idle");
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		pnlStatus.add(lblStatus);

		JPanel panel = new JPanel();
		pnlStatus.add(panel, BorderLayout.EAST);
		panel.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		panel.add(progressBar);
		progressBar.setStringPainted(true);

		Component TopStrut = Box.createVerticalStrut(5);
		panel.add(TopStrut, BorderLayout.NORTH);

		Component BottomStrut = Box.createVerticalStrut(5);
		panel.add(BottomStrut, BorderLayout.SOUTH);

		Component horizontalStrut = Box.createHorizontalStrut(5);
		pnlFooter.add(horizontalStrut, BorderLayout.WEST);

		JPanel pnlLeft = new JPanel();
		frmMain.getContentPane().add(pnlLeft, BorderLayout.WEST);
		pnlLeft.setLayout(new BorderLayout(0, 0));

		JLabel lblServers = new JLabel("Instances");
		lblServers.setHorizontalAlignment(SwingConstants.CENTER);
		lblServers.setFont(new Font("Dialog", Font.BOLD, 14));
		pnlLeft.add(lblServers, BorderLayout.NORTH);

		slModel = new SLListModel();
		serverList = new JList();
		serverList.setModel(slModel);
		serverList.setCellRenderer(new ServerListCellRenderer());
		serverList.addListSelectionListener(new InstanceListener());

		JScrollPane serverScroller = new JScrollPane(serverList);
		pnlLeft.add(serverScroller, BorderLayout.CENTER);

		JButton btnReload = new JButton("Reload instances");
		btnReload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateInstanceList();
				if (serverList.getSelectedIndex() > -1) {
					changeSelectedServer((ServerList) serverList.getSelectedValue());
				}
			}

		});
		pnlLeft.add(btnReload, BorderLayout.SOUTH);

		pnlRight = new JPanel();
		frmMain.getContentPane().add(pnlRight, BorderLayout.EAST);
		pnlRight.setLayout(new BorderLayout(0, 0));

		JPanel pnlChangesTitle = new JPanel();
		pnlChangesTitle.setLayout(new BorderLayout(0,0));
		JLabel lblChanges = new JLabel("Modules");
		lblChanges.setHorizontalAlignment(SwingConstants.CENTER);
		lblChanges.setFont(new Font("Dialog", Font.BOLD, 14));
		pnlChangesTitle.add(lblChanges, BorderLayout.CENTER);
		pnlRight.add(pnlChangesTitle, BorderLayout.NORTH);

		Component hstrut_ChangesLeft = Box.createHorizontalStrut(75);
		pnlChangesTitle.add(hstrut_ChangesLeft, BorderLayout.WEST);

		Component hstrut_ChangesRight = Box.createHorizontalStrut(75);
		pnlChangesTitle.add(hstrut_ChangesRight, BorderLayout.EAST);

		JScrollPane modScroller = new JScrollPane(pnlModList);
		pnlRight.add(modScroller, BorderLayout.CENTER);
		pnlModList.setLayout(new BoxLayout(pnlModList, BoxLayout.Y_AXIS));

		JPanel pnlUpdateOptions = new JPanel();
		pnlRight.add(pnlUpdateOptions, BorderLayout.SOUTH);
		pnlUpdateOptions.setLayout(new GridLayout(0, 1, 0, 0));

		chkHardUpdate = new JCheckBox(Localization.getText("btn_Hard_Update"));
		chkHardUpdate.setToolTipText(Localization.getText("tip_Hard_Update"));
		pnlUpdateOptions.add(chkHardUpdate);
		browser.setEditable(false);
		browser.setContentType("text/html");
		browser.addHyperlinkListener(new NewsPaneListener());

		tabs = new JTabbedPane();

		browser.setText(Localization.getText("Default_News"));
		JScrollPane browserScrollPane = new JScrollPane(browser);
		browserScrollPane.setViewportBorder(null);
		browser.setBorder(null);
		tabs.add("News",browserScrollPane);

		console.setBorder(null);
		//console.setLineWrap(true);
		console.setEditable(false);
		Font f = new Font("Monospaced",Font.PLAIN,11);
		console.setFont(f);
		JScrollPane consoleScrollPane = new JScrollPane(console);
		consoleScrollPane.setViewportBorder(null);
		consoleScrollPane.setAutoscrolls(true);
		consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		consoleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tabs.add("Console",consoleScrollPane);
		log("MCUpdater starting...");

		frmMain.getContentPane().add(tabs, BorderLayout.CENTER);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		frmMain.getContentPane().add(toolBar, BorderLayout.NORTH);

		JButton btnManageServers = new JButton("");
		btnManageServers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showServerManager();
			}
		});
		btnManageServers.setToolTipText(Localization.getText("hdr_Manage_Servers"));
		btnManageServers.setIcon(new ImageIcon(MainForm.class.getResource("/icons/server_database.png")));
		toolBar.add(btnManageServers);

		JButton btnOptions = new JButton("");
		btnOptions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showClientConfig();
			}
		});
		btnOptions.setToolTipText(Localization.getText("hdr_Options"));
		btnOptions.setIcon(new ImageIcon(MainForm.class.getResource("/icons/application_edit.png")));
		toolBar.add(btnOptions);

//		JButton btnBackups = new JButton("");
//		btnBackups.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				showBackupManager();
//			}
//		});
//		btnBackups.setIcon(new ImageIcon(MainForm.class.getResource("/icons/folder_database.png")));
//		btnBackups.setToolTipText("Backups");
//		toolBar.add(btnBackups);

		Component horizontalGlue = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue);

		log("minecraft.jar version: " + mcu.getMCVersion());

		JLabel lblPlayerName1 = new JLabel("Player name:  ");
		toolBar.add(lblPlayerName1);

		lblAvatar = new JLabel();
		lblAvatar.setOpaque(true);
		lblAvatar.setHorizontalAlignment(JLabel.CENTER);
		lblAvatar.setVerticalAlignment(JLabel.CENTER);
		lblAvatar.setBackground(Color.WHITE);
		toolBar.add(lblAvatar);

		Component hStrut3 = Box.createHorizontalStrut(5);
		toolBar.add(hStrut3);

		lblPlayerName2 = new JLabel("Not Logged In");
		toolBar.add(lblPlayerName2);

		JButton btnLogin = new JButton("");
		btnLogin.setHorizontalAlignment(SwingConstants.TRAILING);
		btnLogin.setIcon(new ImageIcon(MainForm.class.getResource("/icons/key.png")));
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showLoginForm();
			}
		});

		Component horizontalStrut_2 = Box.createHorizontalStrut(5);
		toolBar.add(horizontalStrut_2);
		toolBar.add(btnLogin);

		Component horizontalStrut_1 = Box.createHorizontalStrut(5);
		toolBar.add(horizontalStrut_1);
		baseLogger.fine("Finished building GUI");
		initializeInstanceList();
		checkSelectedInstance();

		initTray();

		if (config.getProperty("storePassword").toLowerCase().equals("true")) {
			if (config.containsKey("password")) {
				String user = config.getProperty("userName");
				String password = decrypt(config.getProperty("password"));
				try {
					login(user, password, true);
				} catch (MCLoginException e1) {
				} catch (Exception e1) {
					baseLogger.log(Level.SEVERE, "General error", e1);
				}
			}
		}
	}
	private void loadConfig() {
		File configFile = mcu.getArchiveFolder().resolve("config.properties").toFile();
		if (!configFile.exists())
		{
			createDefaultConfig(configFile);
		}
		try {
			config.load(new FileInputStream(configFile));
			if (validateConfig(config))
			{
				writeConfig(config);
			}
			mcu.setInstanceRoot(new Path(new File(config.getProperty("instanceRoot"))));
		} catch (FileNotFoundException e1) {
			baseLogger.log(Level.SEVERE, "File not found: " + configFile.getAbsolutePath(), e1);
		} catch (IOException e1) {
			baseLogger.log(Level.SEVERE, "I/O Error reading configuration file", e1);
		}
	}

	private void checkSelectedInstance() {
//		Properties instData = new Properties();
//		try {
//			instData.load(Files.newInputStream(mcu.getMCFolder().resolve("instance.dat")));
//		} catch (NoSuchFileException nsfe) {
//			instData.setProperty("serverID", "unmanaged");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		int selectIndex = ((SLListModel)serverList.getModel()).getEntryIdByTag(instData.getProperty("serverID"));
//		serverList.setSelectedIndex(selectIndex);
	}

	private void initializeInstanceList() {
		File serverFile = mcu.getArchiveFolder().resolve("mcuServers.dat").toFile();
		String packUrl = Customization.getString("InitialServer.text");
		if (packUrl.equals("http://www.example.org/ServerPack.xml")){
			packUrl="";
		}
		while(!serverFile.exists() && !(serverFile.length() > 0)){
			if(packUrl.isEmpty()) {
				packUrl = (String) JOptionPane.showInputDialog(null, "No default server defined.\nPlease enter URL to ServerPack.xml: ", "MCUpdater", JOptionPane.INFORMATION_MESSAGE, null, null, "http://www.example.com/ServerPack.xml");
				if (packUrl.isEmpty()) {
					JOptionPane.showMessageDialog(null, "This program requires a valid server pack URL to run which is provided to you by a server operator. This program will now close.", "MCUpdater", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
			try {
				Document serverHeader = ServerPackParser.readXmlFromUrl(packUrl);
				Element docEle = serverHeader.getDocumentElement();
				ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), packUrl, docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), ServerPackParser.parseBoolean(docEle.getAttribute("generateList")), ServerPackParser.parseBoolean(docEle.getAttribute("autoConnect")), docEle.getAttribute("revision"));
				List<ServerList> servers = new ArrayList<ServerList>();
				servers.add(sl);
				mcu.writeServerList(servers);
			} catch (Exception x) {
				baseLogger.log(Level.SEVERE, "General error", x);
				packUrl = "";
			}
		}

		updateInstanceList();
	}

	private String encrypt(String password) {
		try {
			Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, "MCUpdater");
			byte[] utf8 = password.getBytes("UTF8");
			byte[] enc = cipher.doFinal(utf8);

			return Base64.encodeBase64String(enc);
		} catch (Exception e) {
			baseLogger.log(Level.SEVERE, "General error", e);
		}
		return null;
	}

	private String decrypt(String property) {
		try {
			Cipher cipher = getCipher(Cipher.DECRYPT_MODE, "MCUpdater");
			byte[] dec = Base64.decodeBase64(property);
			byte[] utf8 = cipher.doFinal(dec);

			return new String(utf8, "UTF8");
		} catch (Exception e) {
			baseLogger.log(Level.SEVERE, "General error", e);
		}
		return null;
	}

	@Override
	public boolean requestLogin() {
		if (this.loginData.getUserName().isEmpty()) {
			LoginForm login = new LoginForm(window);
			login.setModal(true);
			login.setVisible(true);
			return !this.loginData.getUserName().isEmpty();
		} else {
			return true;
		}
	}

	private void checkAccess() {
		//Path MCFolder = mcu.getMCFolder();
		Path MCUFolder = mcu.getArchiveFolder();
		Path InstancesFolder = mcu.getInstanceRoot();
		//Path testMCFile = MCFolder.resolve("MCUTest.dat");
		Path testMCUFile = MCUFolder.resolve("MCUTest.dat");
		Path testInstancesFile = InstancesFolder.resolve("MCUTest.dat");
		//Path testLink = MCUFolder.resolve("LinkTest.dat");

//		try {
//			Files.createFile(testMCFile);
//			Files.delete(testMCFile);
//			canWriteMinecraft = true;
//		} catch (IOException ioe) {
//			canWriteMinecraft = false;
//		}

		try {
			Files.createFile(testMCUFile);
			canWriteMCUpdater = true;
		} catch (IOException ioe) {
			canWriteMCUpdater = false;
		}

		try {
			Files.createFile(testInstancesFile);
			Files.delete(testInstancesFile);
			canWriteInstances = true;
		} catch (IOException ioe) {
			canWriteInstances=false;
		}

//		try {
//			Files.createSymbolicLink(testLink, testMCUFile);
//			Files.delete(testLink);
//			canCreateLinks = true;
//		} catch (IOException ioe) {
//			canCreateLinks = false;
//		} catch (UnsupportedOperationException uoe) {
//			canCreateLinks = false;
//		}

		Files.delete(testMCUFile);

		log("Access checks: MCU-" + canWriteMCUpdater + " Instance-" + canWriteInstances);
//		if (canCreateLinks == false) {
//			JOptionPane.showMessageDialog(null, "MCUpdater has detected that symbolic linking cannot be performed.\nTrue instancing will be disabled and switching between instances will take considerably longer.\n\nOn Windows, this can be caused by not running MCUpdater as Administrator.", "MCUpdater", JOptionPane.WARNING_MESSAGE);
//		}
	}

	protected void changeSelectedServer(ServerList entry) {
		try {
			btnUpdate.setEnabled(true);
			selected = entry;
			try {
				browser.setPage(entry.getNewsUrl());
			} catch (Exception ie) {
				browser.setText("<HTML><BODY>Unable to read news.</BODY></HTML>");
			}
			frmMain.setTitle(entry.getName() + " - MCUpdater " + Version.VERSION + Version.BUILD_LABEL);
			// switching servers should show newst
			if( tabs != null ) {
				tabs.setSelectedIndex(0);
			}
			//			if (!selected.getServerId().equals("unmanaged")) {
			List<Module> modules = ServerPackParser.loadFromURL(entry.getPackUrl(), entry.getServerId());
			Iterator<Module> itMods = modules.iterator();
			pnlModList.setVisible(false);
			pnlModList.removeAll();
			List<String> modIds = new ArrayList<String>();
			while(itMods.hasNext())
			{
				Module modEntry = itMods.next();
				if (modEntry.isClientSide()){
					if (modEntry.getId().equals("")) {
						modEntry.setId(modEntry.getName().replace(" ", ""));
					}
					if (modIds.contains(modEntry.getId())) {
						tabs.setSelectedIndex(tabs.getTabCount()-1);
						this.log("The " + selected.getName() + " ServerPack contains multiple mods with id (" + modEntry.getId() + ").  This is an invalid ServerPack.  Please contact the server operator.");
						//JOptionPane.showMessageDialog(frmMain, "This ServerPack contains multiple mods with id (" + modEntry.getId() + ").  This is an invalid ServerPack.  Please contact the server operator.", "MCUpdater", JOptionPane.ERROR_MESSAGE);
						btnUpdate.setEnabled(false);
					} else {
						modIds.add(modEntry.getId());
					}
					JModuleCheckBox chkModule = new JModuleCheckBox(modEntry.getName());
					if(modEntry.getInJar())
					{
						chkModule.setFont(chkModule.getFont().deriveFont(Font.BOLD));
					}
					chkModule.setModule(modEntry);
					if(modEntry.getRequired())
					{
						chkModule.setSelected(true);
						chkModule.setEnabled(false);
					}
					if(modEntry.getIsDefault())
					{
						chkModule.setSelected(true);
					}
					pnlModList.add(chkModule);
				}
			}
			pnlModList.setVisible(true);
			pnlRight.setVisible(true);
			btnUpdate.setEnabled(true);
			btnLaunchMinecraft.setEnabled(true);
			//			} else {
			//				pnlModList.removeAll();
			//				pnlRight.setVisible(false);
			//				btnUpdate.setEnabled(false);
			//				setStatus("Idle");
			//			}

			if ( Files.notExists( mcu.getInstanceRoot().resolve(selected.getServerId()) ) ) {
				InstanceManager.createInstance(selected.getServerId());
			}
			//			else {
			//				instancePath = mcu.getInstanceRoot().resolve(selected.getServerId());
			//			}
			//			try {
			//				Path MCPath = mcu.getMCFolder();
			//				if (Files.exists(MCPath)) {
			//					if (Files.isSymbolicLink(MCPath)) {
			//						Files.delete(MCPath);
			//					} else {
			//						Path instDataPath = MCPath.resolve("instance.dat");
			//						boolean instanceDataExists = Files.exists(instDataPath);
			//						if (instanceDataExists) {
			//							Properties instProp = new Properties();
			//							instProp.load(Files.newInputStream(instDataPath));
			//							Path oldInstance = mcu.getInstanceRoot().resolve(instProp.getProperty("serverID"));
			//							removeAndPrepareFolder(MCPath, oldInstance, false);
			//						} else {
			//							removeAndPrepareFolder(MCPath, mcu.getInstanceRoot().resolve("unmanaged"), true);
			//						}
			//					}
			//				}
			//				if (canCreateLinks) {
			//					InstanceManager.createLink(MCPath, instancePath);
			//				} else {
			//					copyInstanceFolder(instancePath, MCPath);
			//				}
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//				return;
			//			}
			ServerStatus status = ServerStatus.getStatus(selected.getAddress());
			if (status != null) {
				setStatus("Idle - Server status: " + status.getMOTD() + " (" + status.getPlayers() + "/" + status.getMaxPlayers() + ")");
			} else {
				setStatus("Idle - Server status: Unable to connect!");
			}
			this.frmMain.repaint();
		} catch (IOException ioe) {
			baseLogger.log(Level.SEVERE, "I/O error", ioe);
		}

	}

//	private void copyInstanceFolder(Path instancePath, Path MCPath) {
//		CopyFiles cf = new CopyFiles(instancePath, MCPath);
//		try {
//			Files.walkFileTree(instancePath, cf);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void removeAndPrepareFolder(Path MCPath, Path instancePath, boolean prepareInstance) {
//
//		if (!Files.exists(instancePath)) {
//			try {
//				Files.createDirectory(instancePath);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		PrepareFiles pf = new PrepareFiles(MCPath, instancePath, prepareInstance);
//		try {
//			Files.walkFileTree(MCPath, pf);
//			Path instanceFile = instancePath.resolve("instance.dat");
//			if (!Files.exists(instanceFile)) {
//				Files.createFile(instanceFile);
//				Properties instData = new Properties();
//				instData.setProperty("serverID", "unmanaged");
//				instData.setProperty("revision", "0");
//				instData.store(Files.newOutputStream(instanceFile), "Instance data");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}

	public void updateInstanceList()
	{
		serverList.setVisible(false);
		slModel.clear();
		List<ServerList> servers = mcu.loadServerList(Customization.getString("InitialServer.text"));
		if (servers != null)
		{
			Iterator<ServerList> it = servers.iterator();

			//boolean flag = false;
			while(it.hasNext())
			{
				ServerList entry = it.next();
				slModel.add(entry);
			}
			//slModel.add(new ServerListPacket(new ServerList("unmanaged","Unmanaged","http://www.example.org/ServerPack.xml","http://mcupdater.net46.net","","","",false,"0"), mcu));
		}
		serverList.setVisible(true);
	}

	@Override
	public void setStatus(String text) {
		lblStatus.setText(text);
	}

	@Override
	public void setProgressBar(int value) {
		progressBar.setValue(value);
	}

	public void setPlayerName(String playerName) {
		this.lblPlayerName2.setText(playerName);
	}

	@Override
	public void log(String msg) {
//		final StringBuilder str = new StringBuilder(logSDF.format(new Date()));
//		str.append(msg);
//		str.append('\n');
//		console.log(str.toString());
		baseLogger.info(msg);
	}

	private void initTray() {
		if( !SystemTray.isSupported() ) {
			baseLogger.warning("System tray is NOT supported.");
			return;
		}

		// change minimize behavior to go to tray
		frmMain.addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {
				minimize(false);	// this is a manual minimize event
			}
		});

		final String label = "MCUpdater "+Version.VERSION;

		trayIcon = new TrayIcon(mcuIcon.getImage(),label);
		trayIcon.setImageAutoSize(true);
		final SystemTray tray = SystemTray.getSystemTray();
		final PopupMenu menu = new PopupMenu();

		final MenuItem restoreItem = new MenuItem("Restore MCU");
		ActionListener restoreAL = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				restore();
			}
		};
		restoreItem.addActionListener(restoreAL);
		final MenuItem killItem = new MenuItem("Kill Minecraft");
		killItem.setEnabled(false);

		menu.add(restoreItem);
		menu.add(killItem);

		trayIcon.setPopupMenu(menu);
		trayIcon.addActionListener(restoreAL);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			trayIcon = null;
			baseLogger.log(Level.SEVERE, "Java AWT error", e);
		}
	}
	public void minimize(boolean auto) {
		if( trayIcon == null ) {
			// only minimize if we have a way to come back
			return;
		}
		if( auto && !Boolean.parseBoolean(config.getProperty("minimizeOnLaunch")) ) {
			// don't autominimize unless configured to
			return;
		}
		frmMain.setVisible(false);
		frmMain.setExtendedState(Frame.ICONIFIED);
		minimized = true;
	}
	public void restore() {
		if( !minimized ) {
			return;
		}
		frmMain.setVisible(true);
		frmMain.setExtendedState(Frame.NORMAL);
	}

	public LoginData login(String username, String password, Boolean storePassword) throws Exception {
		try {
			HashMap<String, Object> localHashMap = new HashMap<String, Object>();
			localHashMap.put("user", username);
			localHashMap.put("password", password);
			localHashMap.put("version", Integer.valueOf(13));
			String str = HTTPSUtils.executePost("https://login.minecraft.net/", localHashMap);
			if (str == null) {
				//showError("Can't connect to minecraft.net");
				throw new MCLoginException(ResponseType.NOCONNECTION);
			}
			if (!str.contains(":")) {
				if (str.trim().equals("Bad login")) {
					throw new MCLoginException(ResponseType.BADLOGIN);
				} else if (str.trim().equals("Old version")) {
					throw new MCLoginException(ResponseType.OLDVERSION);
				} else if (str.trim().equals("User not premium")) {
					throw new MCLoginException(ResponseType.OLDLAUNCHER);
				} else {
					throw new MCLoginException(str);
				}
			}
			if (!Version.isMasterBranch()) {
				this.baseLogger.info("Login response string: " + str);
			}
			String[] arrayOfString = str.split(":");

			LoginData login = new LoginData();
			login.setUserName(arrayOfString[2].trim());
			login.setLatestVersion(arrayOfString[0].trim());
			login.setSessionId(arrayOfString[3].trim());
			setLoginData(login);
			getConfig().setProperty("userName", username);
			if (storePassword == true ) {
				getConfig().setProperty("password", encrypt(password));
				getConfig().setProperty("storePassword", "true");
			} else {
				getConfig().remove("password");
				getConfig().setProperty("storePassword", "false");
			}
			writeConfig(getConfig());
			return login;

		} catch (MCLoginException mcle) {
			throw mcle;
		} catch (Exception localException) {
			baseLogger.log(Level.SEVERE, "General error", localException);
			throw localException;
		}
	}

	public void setLoginData(LoginData response) {
		this.loginData = response;
		setPlayerName(response.getUserName());
		try {
			this.lblAvatar.setIcon(new ImageIcon(new URL("http://cravatar.tomheinan.com/" + response.getUserName() + "/16")));
		} catch (MalformedURLException e) {
			baseLogger.log(Level.SEVERE, "Bad URL", e);
		}
	}

	private Cipher getCipher(int mode, String password) throws Exception {
		Random random = new Random(92845025L);
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

		SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
	}
	private void updateInstance() {
		new Thread() {
			public void run() {
				if (!mcu.checkVersionCache(selected.getVersion())) {
					JOptionPane.showMessageDialog(null, "Unable to validate Minecraft version!", "MCUpdater", JOptionPane.ERROR_MESSAGE);
					return;
				}
				btnUpdate.setEnabled(false);
				btnLaunchMinecraft.setEnabled(false);
				mcu.getMCVersion();
//				int saveConfig = JOptionPane.showConfirmDialog(null, "Do you want to save a backup of your existing configuration?", "MCUpdater", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
//				if(saveConfig == JOptionPane.YES_OPTION){
//					setStatus("Creating backup");
//					setProgressBar(10);
//					Calendar cal = Calendar.getInstance();
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String backDesc = (String) JOptionPane.showInputDialog(null,"Enter description for backup:", "MCUpdater", JOptionPane.QUESTION_MESSAGE, null, null, ("Automatic backup: " + sdf.format(cal.getTime())));
//					log("Creating backup ("+backDesc+") ...");
//					mcu.saveConfig(backDesc);
//					log("Backup complete.");
//				} else if(saveConfig == JOptionPane.CANCEL_OPTION){
//					btnUpdate.setEnabled(true);
//					btnLaunchMinecraft.setEnabled(true);
//					return;
//				}
				tabs.setSelectedIndex(tabs.getTabCount()-1);
				Properties instData = new Properties();
				Path instanceFile = mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("instance.dat");
				try {
					instData.load(Files.newInputStream(instanceFile));
				} catch (IOException e1) {
					baseLogger.log(Level.SEVERE, "I/O error", e1);
				}

				List<Module> toInstall = new ArrayList<Module>();
				List<Component> selects = new ArrayList<Component>(Arrays.asList(pnlModList.getComponents()));
				Iterator<Component> it = selects.iterator();
				setStatus("Preparing module list");
				log("Preparing module list for instance (" + selected.getName() + ")...");
				setProgressBar(20);
				while(it.hasNext()) {
					Component baseEntry = it.next();
					if(baseEntry.getClass().toString().equals("class org.smbarbour.mcu.JModuleCheckBox")) {
						JModuleCheckBox entry = (JModuleCheckBox) baseEntry;
						if(entry.isSelected()){
							toInstall.add(entry.getModule());
						}
					}
				}
				boolean result = false;
				try {
					setStatus("Installing mods");
					log("Installing mods...");
					setProgressBar(25);
					result = mcu.installMods(selected, toInstall, chkHardUpdate.isSelected(), instData);
					if (selected.isGenerateList()) {
						setStatus("Writing servers.dat");
						log("Writing servers.dat");
						setProgressBar(90);
						mcu.writeMCServerFile(selected.getName(), selected.getAddress(), selected.getServerId());
					}
					setStatus("Finished");
					setProgressBar(100);
				} catch (FileNotFoundException fnf) {
					log("! Error: "+fnf.getMessage());
					JOptionPane.showMessageDialog(null, fnf.getMessage(), "MCUpdater", JOptionPane.ERROR_MESSAGE);
				}
				instData.setProperty("serverID", selected.getServerId());
				instData.setProperty("revision", selected.getRevision());
				try {
					instData.store(Files.newOutputStream(instanceFile), "Instance Data");
				} catch (IOException e) {
					baseLogger.log(Level.SEVERE, "I/O error", e);
				}
				if (result) {
					log("Update complete.");
					JOptionPane.showMessageDialog(frmMain, "Your update is complete.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
				} else {
					log("Update Failed!");
					setStatus("Update Failed!");
					setProgressBar(0);
					JOptionPane.showMessageDialog(frmMain, "The update failed to complete successfully.", "Update Failed", JOptionPane.ERROR_MESSAGE);
				}
				btnUpdate.setEnabled(true);
				btnLaunchMinecraft.setEnabled(true);
			}
		}.start();
	}
	private void launchMinecraft() {
		if (serverList.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(null,"You must select an instance first.","MCUpdater",JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			// make sure the selected server actually -exists- first
			final Path instancePath = MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId());
			final File dir = new File(instancePath.toString());
			boolean fail = false;
			if( !dir.exists() ) {
				fail = true;
			} else {
				final Path binPath = instancePath.resolve("bin");
				final File binDir = new File(binPath.toString());
				if( !binDir.exists() ) {
					fail = true;
				} else {
					final File mcJar = new File(binPath.resolve("minecraft.jar").toString());
					if( !mcJar.exists() ) {
						fail = true;
					}
				}
			}
			if( fail ) {
				JOptionPane.showMessageDialog(null, "Selected instance not found. Have you run 'update' yet?", "MCUpdater", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		//if (!System.getProperty("os.name").startsWith("Mac")){
			if (!requestLogin()) {
				if (loginData.getUserName().isEmpty()) {
					JOptionPane.showMessageDialog(null,"You must login first.","MCUpdater",JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		//}
		File outFile = mcu.getArchiveFolder().resolve("client-log.txt").toFile();
		outFile.delete();
		btnLaunchMinecraft.setEnabled(false);
		tabs.setSelectedIndex(tabs.getTabCount()-1);
		MenuItem killItem = null;
		if (SystemTray.isSupported()) {
			final PopupMenu menu = trayIcon.getPopupMenu();
			for( int k = 0; k < menu.getItemCount(); ++k ) {
				final MenuItem item = menu.getItem(k);
				if( item.getLabel().equals("Kill Minecraft") ) {
					killItem = item;
					break;
				}
			}
		}

		GenericLauncherThread thread;
//		if (System.getProperty("os.name").startsWith("Mac")) {
//
//			File launcher = mcu.getMCFolder().resolve("minecraft.jar").toFile();
//			if(!launcher.exists())
//			{
//				try {
//					URL launcherURL = new URL("http://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar");
//					ReadableByteChannel rbc = Channels.newChannel(launcherURL.openStream());
//					FileOutputStream fos = new FileOutputStream(launcher);
//					fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//					fos.close();
//				} catch (MalformedURLException mue) {
//					mue.printStackTrace();
//
//				} catch (IOException ioe) {
//					ioe.printStackTrace();
//				}
//			}
//			thread = LauncherThread.launch(launcher, config.getProperty("jrePath",System.getProperty("java.home")), config.getProperty("minimumMemory"), config.getProperty("maximumMemory"), Boolean.parseBoolean(config.getProperty("suppressUpdates")), outFile, console);
//
//		} else {
			//thread = NativeLauncherThread.launch(window, loginData, config.getProperty("jrePath",System.getProperty("java.home")), config.getProperty("minimumMemory"), config.getProperty("maximumMemory"), outFile, console);
			thread = AppletLauncherThread.launch(window, loginData,
					config.getProperty("jrePath",System.getProperty("java.home")),
					config.getProperty("minimumMemory"),
					config.getProperty("maximumMemory"),
					config.getProperty("permGen"),
					outFile, console, selected);
//		}
		thread.register(window, btnLaunchMinecraft, killItem );
		thread.start();
	}
	private void showLoginForm() {
		LoginForm lf = new LoginForm(window);
		lf.pack();
		lf.setLocationRelativeTo(frmMain);
		lf.setVisible(true);
	}
//	private void showBackupManager() {
//		BackupManager bm = new BackupManager(window);
//		bm.setVisible(true);
//	}
	private void showClientConfig() {
		ClientConfig cc = new ClientConfig(window);
		cc.pack();
		cc.setLocationRelativeTo(frmMain);
		cc.setVisible(true);
	}
	private void showServerManager() {
		ServerManager sm = new ServerManager(window);
		sm.pack();
		sm.setLocationRelativeTo(frmMain);
		sm.setVisible(true);
	}

	@Override
	public void addServer(ServerList entry) {
		// TODO Auto-generated method stub
		
	}

	private final class NewsPaneListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent he) {
			if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
				try {
					MCUpdater.openLink(he.getURL().toURI());
				} catch (Exception e) {
					baseLogger.log(Level.SEVERE, "General error", e);
				}
			}
		}
	}

	private final class InstanceListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting())
			{
				changeSelectedServer((ServerList) serverList.getSelectedValue());
				// check for server version update
				Properties instData = new Properties();
				try {
					instData.load(Files.newInputStream(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("instance.dat")));
				} catch (IOException e1) {
					baseLogger.log(Level.SEVERE, "I/O error", e1);
				}
				final boolean needUpdate = !selected.getRevision().equals(instData.getProperty("revision"));
				// check for mcu version update
				final boolean needMCUUpgrade = Version.isVersionOld(selected.getMCUVersion());

				String warningMessage = null;
				if( needUpdate ) {
					warningMessage = "Your configuration is out of sync with the server. Updating is necessary.";
				} else if( needMCUUpgrade ) {
					warningMessage = "The server requires a newer version of MCUpdater than you currently have installed.\nPlease upgrade as soon as possible, things are not likely to update correctly otherwise.";
				}

				if ( warningMessage != null ) {
					JOptionPane.showMessageDialog(null, warningMessage, "MCUpdater", JOptionPane.WARNING_MESSAGE);
				}

			}
		}
	}
}

class JModuleCheckBox extends JCheckBox
{
	/**
	 *
	 */
	private static final long serialVersionUID = 8124564072878896685L;
	private Module entry;

	public JModuleCheckBox(String name) {
		super(name);
	}

	public void setModule(Module entry)
	{
		this.entry=entry;
	}

	public Module getModule()
	{
		return entry;
	}
}

class SLListModel extends AbstractListModel
{
	/**
	 *
	 */
	private static final long serialVersionUID = -6829288390151952427L;
	List<ServerList> model;

	public SLListModel()
	{
		model = new ArrayList<ServerList>();
	}

	public int getEntryIdByTag(String tag) {
		int foundId = 0;
		Iterator<ServerList> it = model.iterator();
		int searchId = 0;
		while (it.hasNext()) {
			ServerList entry = it.next();
			if (tag.equals(entry.getServerId())) {
				foundId = searchId;
				break;
			}
			searchId++;
		}
		return foundId;
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public ServerList getElementAt(int index) {
		return model.get(index);
	}

	public void add(ServerList element)
	{
		model.add(element);
		fireContentsChanged(this, 0, getSize());
	}

	public Iterator<ServerList> iterator()
	{
		return model.iterator();
	}

	public boolean removeElement(ServerList element)
	{
		boolean removed = model.remove(element);
		if (removed) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}

	public void clear()
	{
		model.clear();
		fireContentsChanged(this, 0, getSize());
	}

}

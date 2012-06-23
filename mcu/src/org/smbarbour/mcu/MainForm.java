package org.smbarbour.mcu;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.smbarbour.mcu.MCUApp;
import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.Module;
import org.smbarbour.mcu.util.ServerList;
import org.smbarbour.mcu.util.ServerListPacket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.swing.JProgressBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.ResourceBundle;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;

public class MainForm extends MCUApp {
	private static final ResourceBundle Customization = ResourceBundle.getBundle("customization"); //$NON-NLS-1$
	private static final String VERSION = "v1.00";
	private static MainForm window;
	private JFrame frmMain;
	final MCUpdater mcu = new MCUpdater();
	private JMenu mnList = new JMenu("List");
	private final JTextPane browser = new JTextPane();
	private ServerList selected;
	//private final JPanel pnlServerList = new JPanel();
	private final JPanel pnlModList = new JPanel();
	private JLabel lblStatus;
	private JProgressBar progressBar;

	private JList serverList;
	private SLListModel slModel;
	
	/**
	 * Create the application.
	 */
	public MainForm() {
		initialize();
		window = this;
		window.frmMain.setVisible(true);
		mcu.setParent(window);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	void initialize() {
		frmMain = new JFrame();
		frmMain.setTitle("[No Server Selected] - Minecraft Updater " + MainForm.VERSION);
		frmMain.setResizable(false);
		frmMain.setBounds(100, 100, 1175, 592);
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel pnlFooter = new JPanel();
		frmMain.getContentPane().add(pnlFooter, BorderLayout.SOUTH);
		pnlFooter.setLayout(new BorderLayout(0, 0));

		JPanel pnlButtons = new JPanel();
		pnlFooter.add(pnlButtons, BorderLayout.EAST);

		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						 mcu.getMCVersion();
						int saveConfig = JOptionPane.showConfirmDialog(null, "Do you want to save a backup of your existing configuration?", "MCUpdater", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(saveConfig == JOptionPane.YES_OPTION){
							setLblStatus("Creating backup");
							setProgressBar(10);
							Calendar cal = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String backDesc = (String) JOptionPane.showInputDialog(null,"Enter description for backup:", "MCUpdater", JOptionPane.QUESTION_MESSAGE, null, null, ("Automatic backup: " + sdf.format(cal.getTime()))); 
							mcu.saveConfig(backDesc);
						} else if(saveConfig == JOptionPane.CANCEL_OPTION){
							return;
						}
						List<Module> toInstall = new ArrayList<Module>();
						List<Component> selects = new ArrayList<Component>(Arrays.asList(pnlModList.getComponents()));
						Iterator<Component> it = selects.iterator();
						setLblStatus("Preparing module list");
						setProgressBar(20);
						while(it.hasNext()) {
							Component baseEntry = it.next();
							System.out.println(baseEntry.getClass().toString());
							if(baseEntry.getClass().toString().equals("class org.smbarbour.mcu.JModuleCheckBox")) {
								JModuleCheckBox entry = (JModuleCheckBox) baseEntry;
								if(entry.isSelected()){
									toInstall.add(entry.getModule());
								}
							}
						}
						try {
							setLblStatus("Installing mods");
							setProgressBar(25);
							mcu.installMods(selected, toInstall);
							if (selected.isGenerateList()) {
								setLblStatus("Writing servers.dat");
								setProgressBar(90);
								mcu.writeMCServerFile(selected.getName(), selected.getAddress());
							}
							setLblStatus("Finished");
							setProgressBar(100);
						} catch (FileNotFoundException fnf) {
							JOptionPane.showMessageDialog(null, fnf.getMessage(), "MCUpdater", JOptionPane.ERROR_MESSAGE);
						}
					}						
				}.start();
			}
		});
		pnlButtons.add(btnUpdate);

		JButton btnLaunchMinecraft = new JButton("Launch Minecraft");
		btnLaunchMinecraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File launcher = new File(mcu.getMCFolder() + MCUpdater.sep + "minecraft.jar");
				if(!launcher.exists())
				{
					try {
						URL launcherURL = new URL("http://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar");
						ReadableByteChannel rbc = Channels.newChannel(launcherURL.openStream());
						FileOutputStream fos = new FileOutputStream(launcher);
						fos.getChannel().transferFrom(rbc, 0, 1 << 24);
					} catch (MalformedURLException mue) {
						mue.printStackTrace();

					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
				LauncherThread.launch(launcher);
			}
		});
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
		
		JLabel lblServers = new JLabel("Servers");
		lblServers.setHorizontalAlignment(SwingConstants.CENTER);
		lblServers.setFont(new Font("Dialog", Font.BOLD, 14));
		pnlLeft.add(lblServers, BorderLayout.NORTH);
		
		slModel = new SLListModel();
		serverList = new JList();
		serverList.setModel(slModel);
		serverList.setCellRenderer(new ServerListCellRenderer());
		serverList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
				{
					changeSelectedServer(((ServerListPacket)serverList.getSelectedValue()).getEntry());
				}
			}
			
		});
				
		JScrollPane serverScroller = new JScrollPane(serverList);
		pnlLeft.add(serverScroller, BorderLayout.CENTER);
						
		JPanel pnlRight = new JPanel();
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
		browser.setEditable(false);
		browser.setContentType("text/html");
		browser.addHyperlinkListener(new HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent he) {
				if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
					try {
						MCUpdater.openLink(he.getURL().toURI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		});

		browser.setText("<HTML><BODY>There are no servers currently defined.</BODY></HTML>");
		JScrollPane scrollPane = new JScrollPane(browser);
		scrollPane.setViewportBorder(null);
		browser.setBorder(null);
		frmMain.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		frmMain.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JButton btnManageServers = new JButton("");
		btnManageServers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerManager sm = new ServerManager(window);
				sm.setVisible(true);
			}
		});
		btnManageServers.setToolTipText("Manage Servers");
		btnManageServers.setIcon(new ImageIcon(MainForm.class.getResource("/icons/server_database.png")));
		toolBar.add(btnManageServers);
		
		JButton btnOptions = new JButton("");
		btnOptions.setToolTipText("Options");
		btnOptions.setIcon(new ImageIcon(MainForm.class.getResource("/icons/application_edit.png")));
		toolBar.add(btnOptions);
		
		JButton btnBackups = new JButton("");
		btnBackups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BackupManager bm = new BackupManager(window);
				bm.setVisible(true);
			}
		});
		btnBackups.setIcon(new ImageIcon(MainForm.class.getResource("/icons/folder_database.png")));
		btnBackups.setToolTipText("Backups");
		toolBar.add(btnBackups);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue);
		
		JLabel lblNewLabel = new JLabel("minecraft.jar version: " + mcu.getMCVersion());
		toolBar.add(lblNewLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(5);
		toolBar.add(horizontalStrut_1);

		File serverFile = new File(mcu.getArchiveFolder() + MCUpdater.sep + "mcuServers.dat");
		while(!serverFile.exists() && !(serverFile.length() > 0)){
			String packUrl = (String) JOptionPane.showInputDialog(null, "No servers defined.\nPlease enter URL to ServerPack.xml: ", "MCUpdater", JOptionPane.INFORMATION_MESSAGE, null, null, Customization.getString("InitialServer.text"));
			if(packUrl.isEmpty()) {
				System.exit(0);
			}
			try {
				Document serverHeader = MCUpdater.readXmlFromUrl(packUrl);
				Element docEle = serverHeader.getDocumentElement();
				ServerList sl = new ServerList(docEle.getAttribute("name"), packUrl, docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), mcu.parseBoolean(docEle.getAttribute("generateList")), docEle.getAttribute("revision"));
				List<ServerList> servers = new ArrayList<ServerList>();
				servers.add(sl);
				mcu.writeServerList(servers);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}

		updateServerList();
		/*
		JMenuBar menuBar = new JMenuBar();
		//frmMain.setJMenuBar(menuBar);

		JMenu mnServers = new JMenu("Servers");
		menuBar.add(mnServers);

		JMenuItem mntmManage = new JMenuItem("Manage");
		mntmManage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerManager sm = new ServerManager(window);
				sm.setVisible(true);
			}
		});
		mnServers.add(mntmManage);

		mnServers.add(mnList);

		JMenu mnBackups = new JMenu("Backups");
		menuBar.add(mnBackups);

		JMenuItem mntmManageBackups = new JMenuItem("Manage");
		mntmManageBackups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BackupManager bm = new BackupManager(window);
				bm.setVisible(true);
			}
		});
		mnBackups.add(mntmManageBackups);

		JMenu mnInfo = new JMenu("Info");
		menuBar.add(mnInfo);

		final JMenuItem mntmMinecraftjarVersion = new JMenuItem("minecraft.jar version: " + mcu.getMCVersion());
		mnInfo.add(mntmMinecraftjarVersion);
		
		JMenuItem mntmRecheckVersion = new JMenuItem("Recheck Version");
		mntmRecheckVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmMinecraftjarVersion.setText("minecraft.jar version: " + mcu.getMCVersion());
			}
		});
		mnInfo.add(mntmRecheckVersion);
		*/
	}

	protected void changeSelectedServer(ServerList entry) {
		try {
			browser.setPage(entry.getNewsUrl());
			frmMain.setTitle(entry.getName() + " - Minecraft Updater " + MainForm.VERSION);
			List<Module> modules = mcu.loadFromURL(entry.getPackUrl());
			Iterator<Module> itMods = modules.iterator();
			pnlModList.setVisible(false);
			pnlModList.removeAll();
			while(itMods.hasNext())
			{
				Module modEntry = itMods.next();
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
			pnlModList.setVisible(true);
			this.frmMain.repaint();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public void updateServerList()
	{
		serverList.setVisible(false);
		slModel.clear();
		List<ServerList> servers = mcu.loadServerList();
		if (servers != null)
		{
			Iterator<ServerList> it = servers.iterator();
			
			//boolean flag = false;
			while(it.hasNext())
			{
				ServerList entry = it.next();
				slModel.add(new ServerListPacket(entry, mcu));
			}
		}
		serverList.setVisible(true);
	}
	
	public void updateServerList_old()
	{
		mnList.removeAll();
		List<ServerList> servers = mcu.loadServerList();
		if(servers != null)
		{
			Iterator<ServerList> it = servers.iterator();

			boolean flag = false;
			while(it.hasNext())
			{
				ServerList entry = it.next();
				final JServerMenuItem mnuServerEntry = new JServerMenuItem(entry.getName());
				mnuServerEntry.setServerEntry(entry);
				mnList.add(mnuServerEntry);
				mnuServerEntry.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						try {
							selected = mnuServerEntry.getServerEntry();
							browser.setPage(mnuServerEntry.getServerEntry().getNewsUrl());
							frmMain.setTitle(mnuServerEntry.getServerEntry().getName() + " - Minecraft Updater " + MainForm.VERSION);
							List<Module> modules = mcu.loadFromURL(mnuServerEntry.getServerEntry().getPackUrl());
							Iterator<Module> itMods = modules.iterator();
							pnlModList.setVisible(false);
							pnlModList.removeAll();
							while(itMods.hasNext())
							{
								Module modEntry = itMods.next();
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
							pnlModList.setVisible(true);
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}

					}

				});
				if(flag==false)
				{
					mnuServerEntry.doClick();
					flag=true;
				}
			}
		}

	}
	
	@Override
	public void setLblStatus(String text) {
		lblStatus.setText(text);
	}
	
	@Override
	public void setProgressBar(int value) {
		progressBar.setValue(value);
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

class JServerMenuItem extends JMenuItem
{
	private ServerList entry;
	/**
	 * 
	 */
	private static final long serialVersionUID = -6374548651338922992L;

	public JServerMenuItem(String name) {
		super(name);
	}

	public void setServerEntry(ServerList entry)
	{
		this.entry = entry;
	}

	public ServerList getServerEntry()
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
	List<ServerListPacket> model;
	
	public SLListModel()
	{
		model = new ArrayList<ServerListPacket>();
	}
	
	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public Object getElementAt(int index) {
		return model.toArray()[index];
	}

	public void add(ServerListPacket element)
	{
		model.add(element);
		fireContentsChanged(this, 0, getSize());
	}
	
	public Iterator<ServerListPacket> iterator()
	{
		return model.iterator();
	}
	
	public boolean removeElement(ServerListPacket element)
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
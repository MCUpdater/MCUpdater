package org.smbarbour.mcu;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import argo.jdom.JsonNode;
import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class PackHelper {

	private JFrame frame;
	private JTextField txtBaseUrl;
	private JTextArea txtAreaModule;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PackHelper window = new PackHelper();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PackHelper() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 50);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		JMenuBar menuBar = new JMenuBar();
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(false);
				fc.showOpenDialog(frame);

				File openFile = fc.getSelectedFile();
				processFile(openFile);
			}
		});
		mnFile.add(mntmOpen);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		{
		int row = 0;
		Component rigidAreaHead = Box.createRigidArea(new Dimension(5, 5));
		GridBagConstraints gbc_rigidAreaHead = new GridBagConstraints();
		gbc_rigidAreaHead.insets = new Insets(0, 0, 0, 5);
		gbc_rigidAreaHead.gridx = 0;
		gbc_rigidAreaHead.gridy = row;
		panel.add(rigidAreaHead, gbc_rigidAreaHead);
		
		row++;
		
		JLabel lblBaseUrl = new JLabel("Base URL:");
		lblBaseUrl.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblBaseUrl = new GridBagConstraints();
		gbc_lblBaseUrl.anchor = GridBagConstraints.EAST;
		gbc_lblBaseUrl.insets = new Insets(0, 0, 5, 5);
		gbc_lblBaseUrl.gridx = 1;
		gbc_lblBaseUrl.gridy = row;
		panel.add(lblBaseUrl, gbc_lblBaseUrl);
		
		txtBaseUrl = new JTextField();
		GridBagConstraints gbc_txtBaseUrl = new GridBagConstraints();
		gbc_txtBaseUrl.insets = new Insets(0, 0, 5, 0);
		gbc_txtBaseUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBaseUrl.gridx = 2;
		gbc_txtBaseUrl.gridy = row;
		panel.add(txtBaseUrl, gbc_txtBaseUrl);
		txtBaseUrl.setColumns(10);
		
		row++;
		txtAreaModule = new JTextArea(15,80);
		txtAreaModule.setWrapStyleWord(true);
		txtAreaModule.setLineWrap(true);
		GridBagConstraints gbc_txtAreaModule = new GridBagConstraints();
		gbc_txtAreaModule.fill = GridBagConstraints.BOTH;
		gbc_txtAreaModule.anchor = GridBagConstraints.EAST;
		gbc_txtAreaModule.gridwidth = 2;
		gbc_txtAreaModule.gridx = 1;
		gbc_txtAreaModule.gridy = row;
		txtAreaModule.setMinimumSize(new Dimension(panel.getWidth()-10,200));
		panel.add(txtAreaModule, gbc_txtAreaModule);

		row++;
		Component rigidAreaFoot = Box.createRigidArea(new Dimension(5, 5));
		GridBagConstraints gbc_rigidAreaFoot = new GridBagConstraints();
		gbc_rigidAreaFoot.insets = new Insets(0, 0, 0, 5);
		gbc_rigidAreaFoot.gridx = 3;
		gbc_rigidAreaFoot.gridy = row;
		panel.add(rigidAreaFoot, gbc_rigidAreaFoot);
		}

		frame.setSize(frame.getWidth(),frame.getHeight()+(int)frame.getContentPane().getMinimumSize().getHeight());
	}

	protected void processFile(File file) {
		String modID = file.toPath().getFileName().toString();
		String name = file.toPath().getFileName().toString();
		String md5 = "";
		try {
			InputStream is = new FileInputStream(file);
			byte[] hash = DigestUtils.md5(is);
			md5 = new String(Hex.encodeHex(hash));
			ZipFile zf = new ZipFile(file);
			System.out.println(zf.size() + " entries in file.");
			JdomParser parser = new JdomParser();
			JsonRootNode modInfo = parser.parse(new InputStreamReader(zf.getInputStream(zf.getEntry("mcmod.info"))));
			JsonNode subnode;
			if (modInfo.hasElements()) {
				subnode = modInfo.getElements().get(0);
			} else {
				subnode = modInfo.getNode("modlist").getElements().get(0);
			}
			modID = subnode.getStringValue("modid");
			name = subnode.getStringValue("name");
			zf.close();
		} catch (NullPointerException e) {
		} catch (ZipException e) {
			System.out.println("Not an archive.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("id:" + modID + " - name:" + name);
			StringBuilder modText = new StringBuilder();
			modText.append("<Module name=\"" + name + "\" id=\"" + modID + "\">\n");
			modText.append("\t<URL>" + txtBaseUrl.getText() + file.getName() + "</URL>\n");
			modText.append("\t<MD5>" + md5 + "</MD5>\n");
			modText.append("\t<Required>true</Required>\n");
			modText.append("\t<InJar>false</InJar>\n");
			modText.append("\t<CoreMod>false</CoreMod>\n");
			modText.append("\t<Extract>false</Extract>\n");
			modText.append("\t<InRoot>false</InRoot>\n");
			modText.append("</Module>\n");
			txtAreaModule.setText(modText.toString());
		}
	}

}

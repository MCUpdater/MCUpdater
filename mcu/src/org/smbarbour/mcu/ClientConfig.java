package org.smbarbour.mcu;

import j7compat.Path;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JCheckBox;

public class ClientConfig extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 6628969370506234011L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtMinimum;
	private JTextField txtMaximum;
	private JTextField txtJavaPath;
	private JCheckBox chckbxMinimize;
	//private JCheckBox chckbxSuppressVanillaUpdate;
	private JTextField txtInstanceRoot;
	private JTextField txtJVMOpts;
	private JTextField txtResWidth;
	private JTextField txtResHeight;
	private JTextField txtJVMContainer;

	/**
	 * Create the dialog.
	 */
	public ClientConfig(final MainForm parent) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setTitle("Client Configuration");
		setResizable(false);
		setSize(480, 75);	// height is amount of padding reserved for button bar
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 20, 217, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 20, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};

		int row = 0;
		contentPanel.setLayout(gbl_contentPanel);
		{
			Component rigidArea = Box.createRigidArea(new Dimension(5, 5));
			GridBagConstraints gbc_rigidArea = new GridBagConstraints();
			gbc_rigidArea.insets = new Insets(0, 0, 5, 5);
			gbc_rigidArea.gridx = 0;
			gbc_rigidArea.gridy = row;
			contentPanel.add(rigidArea, gbc_rigidArea);
		}

		// memory
		++row;
		{
			JLabel lblMinimumMemory = new JLabel("Minimum memory:");
			GridBagConstraints gbc_lblMinimumMemory = new GridBagConstraints();
			gbc_lblMinimumMemory.anchor = GridBagConstraints.EAST;
			gbc_lblMinimumMemory.insets = new Insets(0, 0, 5, 5);
			gbc_lblMinimumMemory.gridx = 1;
			gbc_lblMinimumMemory.gridy = row;
			contentPanel.add(lblMinimumMemory, gbc_lblMinimumMemory);
		}
		{
			txtMinimum = new JTextField();
			txtMinimum.setText(parent.getConfig().getProperty("minimumMemory"));
			GridBagConstraints gbc_txtMinimum = new GridBagConstraints();
			gbc_txtMinimum.insets = new Insets(0, 0, 5, 5);
			gbc_txtMinimum.anchor = GridBagConstraints.WEST;
			gbc_txtMinimum.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtMinimum.gridx = 2;
			gbc_txtMinimum.gridy = row;
			gbc_txtMinimum.gridwidth = 2;
			contentPanel.add(txtMinimum, gbc_txtMinimum);
			txtMinimum.setColumns(10);
		}
		{
			Component horizontalStrut = Box.createHorizontalStrut(5);
			GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
			gbc_horizontalStrut.insets = new Insets(0, 0, 5, 0);
			gbc_horizontalStrut.gridx = 3;
			gbc_horizontalStrut.gridy = row;
			contentPanel.add(horizontalStrut, gbc_horizontalStrut);
		}
		++row;
		{
			JLabel lblMaximumMemory = new JLabel("Maximum memory:");
			GridBagConstraints gbc_lblMaximumMemory = new GridBagConstraints();
			gbc_lblMaximumMemory.anchor = GridBagConstraints.EAST;
			gbc_lblMaximumMemory.insets = new Insets(0, 0, 5, 5);
			gbc_lblMaximumMemory.gridx = 1;
			gbc_lblMaximumMemory.gridy = row;
			contentPanel.add(lblMaximumMemory, gbc_lblMaximumMemory);
		}
		{
			txtMaximum = new JTextField();
			txtMaximum.setText(parent.getConfig().getProperty("maximumMemory"));
			GridBagConstraints gbc_txtMaximum = new GridBagConstraints();
			gbc_txtMaximum.anchor = GridBagConstraints.WEST;
			gbc_txtMaximum.insets = new Insets(0, 0, 5, 5);
			gbc_txtMaximum.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtMaximum.gridx = 2;
			gbc_txtMaximum.gridy = row;
			gbc_txtMaximum.gridwidth = 2;
			contentPanel.add(txtMaximum, gbc_txtMaximum);
			txtMaximum.setColumns(10);
		}
		++row;
		{
			JLabel lblMemoryCanBe = new JLabel("Memory can be specified in MB or GB (i.e. 512M or 1G)");
			GridBagConstraints gbc_lblMemoryCanBe = new GridBagConstraints();
			gbc_lblMemoryCanBe.gridwidth = 3;
			gbc_lblMemoryCanBe.insets = new Insets(0, 0, 5, 5);
			gbc_lblMemoryCanBe.gridx = 1;
			gbc_lblMemoryCanBe.gridy = row;
			contentPanel.add(lblMemoryCanBe, gbc_lblMemoryCanBe);
		}
		// Resolution
		++row;
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
			gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
			gbc_verticalStrut.gridx = 1;
			gbc_verticalStrut.gridy = row;
			contentPanel.add(verticalStrut, gbc_verticalStrut);
		}
		++row;
		{
			JLabel lblResolution = new JLabel("Resolution:");
			GridBagConstraints gbc_lblResolution = new GridBagConstraints();
			gbc_lblResolution.anchor = GridBagConstraints.EAST;
			gbc_lblResolution.insets = new Insets(0, 0, 5, 5);
			gbc_lblResolution.gridx = 1;
			gbc_lblResolution.gridy = row;
			contentPanel.add(lblResolution, gbc_lblResolution);

			JPanel pnlResolution = new JPanel();
			pnlResolution.setLayout(new BorderLayout());
			GridBagConstraints gbc_pnlResolution = new GridBagConstraints();
			gbc_pnlResolution.anchor = GridBagConstraints.WEST;
			gbc_pnlResolution.gridwidth = 2;
			gbc_pnlResolution.insets = new Insets(0, 0, 5, 5);
			gbc_pnlResolution.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlResolution.gridx=2;
			gbc_pnlResolution.gridy=row;
			{
				txtResWidth = new JTextField();
				txtResWidth.setText(parent.getConfig().getProperty("width"));
				txtResWidth.setColumns(10);
				pnlResolution.add(txtResWidth, BorderLayout.WEST);

				JLabel lblResX = new JLabel("X");
				lblResX.setHorizontalAlignment(SwingConstants.CENTER);
				pnlResolution.add(lblResX, BorderLayout.CENTER);


				txtResHeight = new JTextField();
				txtResHeight.setText(parent.getConfig().getProperty("height"));
				txtResHeight.setColumns(10);
				pnlResolution.add(txtResHeight, BorderLayout.EAST);

			}
			contentPanel.add(pnlResolution, gbc_pnlResolution);
		}
		// JRE
		++row;
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
			gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
			gbc_verticalStrut.gridx = 1;
			gbc_verticalStrut.gridy = row;
			contentPanel.add(verticalStrut, gbc_verticalStrut);
		}
		++row;
		{
			JLabel lblJavaPath = new JLabel("Java home path:");
			GridBagConstraints gbc_lblJavaPath = new GridBagConstraints();
			gbc_lblJavaPath.anchor = GridBagConstraints.EAST;
			gbc_lblJavaPath.insets = new Insets(0, 0, 5, 5);
			gbc_lblJavaPath.gridx = 1;
			gbc_lblJavaPath.gridy = row;
			contentPanel.add(lblJavaPath, gbc_lblJavaPath);
		}
		{
			txtJavaPath = new JTextField();
			txtJavaPath.setText(parent.getConfig().getProperty("jrePath",System.getProperty("java.home")));
			GridBagConstraints gbc_txtJavaPath = new GridBagConstraints();
			gbc_txtJavaPath.anchor = GridBagConstraints.WEST;
			gbc_txtJavaPath.insets = new Insets(0, 0, 5, 5);
			gbc_txtJavaPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtJavaPath.gridx = 2;
			gbc_txtJavaPath.gridy = row;
			contentPanel.add(txtJavaPath, gbc_txtJavaPath);
		}
		{
			JButton btnJavaPath = new JButton("Browse");
			GridBagConstraints gbc_btnJavaPath = new GridBagConstraints();
			gbc_btnJavaPath.anchor = GridBagConstraints.WEST;
			gbc_btnJavaPath.insets = new Insets(0, 0, 5, 5);
			gbc_btnJavaPath.gridx = 3;
			gbc_btnJavaPath.gridy = row;
			contentPanel.add(btnJavaPath, gbc_btnJavaPath);
			btnJavaPath.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setCurrentDirectory(new File(txtJavaPath.getText()));
					int choice = fc.showOpenDialog(null);
					if( choice == JFileChooser.APPROVE_OPTION ) {
						try {
							txtJavaPath.setText(fc.getSelectedFile().getCanonicalPath());
						} catch (IOException e) {
							parent.log("[Error]" +e.getMessage()+"\n");
						}
					}
				}
			});
		}
		// JVM Options
		++row;
		{
			JLabel lblJVMOpts = new JLabel("JVM Opts:");
			GridBagConstraints gbc_lblJVMOpts = new GridBagConstraints();
			gbc_lblJVMOpts.anchor = GridBagConstraints.EAST;
			gbc_lblJVMOpts.insets = new Insets(0, 0, 5, 5);
			gbc_lblJVMOpts.gridx = 1;
			gbc_lblJVMOpts.gridy = row;
			contentPanel.add(lblJVMOpts, gbc_lblJVMOpts);

			txtJVMOpts = new JTextField();
			txtJVMOpts.setText(parent.getConfig().getProperty("jvmOpts"));
			GridBagConstraints gbc_txtJVMOpts = new GridBagConstraints();
			gbc_txtJVMOpts.insets = new Insets(0, 0, 5, 5);
			gbc_txtJVMOpts.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtJVMOpts.gridx = 2;
			gbc_txtJVMOpts.gridy = row;
			gbc_txtJVMOpts.gridwidth = 2;
			contentPanel.add(txtJVMOpts, gbc_txtJVMOpts);
			txtJVMOpts.setColumns(10);

		}
		// Instance
		++row;
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
			gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
			gbc_verticalStrut.gridx = 1;
			gbc_verticalStrut.gridy = row;
			contentPanel.add(verticalStrut, gbc_verticalStrut);
		}
		++row;
		{
			JLabel lblInstanceFolder = new JLabel("Instance folder:");
			GridBagConstraints gbc_lblInstanceFolder = new GridBagConstraints();
			gbc_lblInstanceFolder.anchor = GridBagConstraints.EAST;
			gbc_lblInstanceFolder.insets = new Insets(0, 0, 5, 5);
			gbc_lblInstanceFolder.gridx = 1;
			gbc_lblInstanceFolder.gridy = row;
			contentPanel.add(lblInstanceFolder, gbc_lblInstanceFolder);
		}
		{
			txtInstanceRoot = new JTextField();
			txtInstanceRoot.setText(parent.getConfig().getProperty("instanceRoot"));
			GridBagConstraints gbc_txtInstanceRoot = new GridBagConstraints();
			gbc_txtInstanceRoot.insets = new Insets(0, 0, 5, 5);
			gbc_txtInstanceRoot.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtInstanceRoot.gridx = 2;
			gbc_txtInstanceRoot.gridy = row;
			contentPanel.add(txtInstanceRoot, gbc_txtInstanceRoot);
			txtInstanceRoot.setColumns(10);
		}
		{
			JButton btnInstanceRoot = new JButton("Browse");
			btnInstanceRoot.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser jfc = new JFileChooser();
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					jfc.showOpenDialog(contentPanel);

					txtInstanceRoot.setText(jfc.getSelectedFile().getAbsolutePath());
				}

			});
			GridBagConstraints gbc_btnInstanceRoot = new GridBagConstraints();
			gbc_btnInstanceRoot.anchor = GridBagConstraints.WEST;
			gbc_btnInstanceRoot.insets = new Insets(0, 0, 5, 5);
			gbc_btnInstanceRoot.gridx = 3;
			gbc_btnInstanceRoot.gridy = row;
			contentPanel.add(btnInstanceRoot, gbc_btnInstanceRoot);
		}

		++row;
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
			gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
			gbc_verticalStrut.gridx = 1;
			gbc_verticalStrut.gridy = row;
			contentPanel.add(verticalStrut, gbc_verticalStrut);
		}

		// Minimize preference
		++row;
		{
			chckbxMinimize = new JCheckBox("Minimize MCU on launch");
			GridBagConstraints gbc_chckbxMinimize = new GridBagConstraints();
			gbc_chckbxMinimize.anchor = GridBagConstraints.WEST;
			gbc_chckbxMinimize.gridwidth = 3;
			gbc_chckbxMinimize.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxMinimize.gridx = 1;
			gbc_chckbxMinimize.gridy = row;
			chckbxMinimize.setSelected(Boolean.parseBoolean(parent.getConfig().getProperty("minimizeOnLaunch")));
			contentPanel.add(chckbxMinimize, gbc_chckbxMinimize);
		}

		// JVM Container
		++row;
		{
			JLabel lblJVMContainer = new JLabel("Run Minecraft in Container:");
			GridBagConstraints gbc_lblJVMContainer = new GridBagConstraints();
			gbc_lblJVMContainer.anchor = GridBagConstraints.EAST;
			gbc_lblJVMContainer.insets = new Insets(0, 0, 5, 5);
			gbc_lblJVMContainer.gridx = 1;
			gbc_lblJVMContainer.gridy = row;
			contentPanel.add(lblJVMContainer, gbc_lblJVMContainer);

			txtJVMContainer = new JTextField();
			txtJVMContainer.setText(parent.getConfig().getProperty("jvmContainer"));
			GridBagConstraints gbc_txtJVMContainer = new GridBagConstraints();
			gbc_txtJVMContainer.insets = new Insets(0, 0, 5, 5);
			gbc_txtJVMContainer.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtJVMContainer.gridx = 2;
			gbc_txtJVMContainer.gridy = row;
			gbc_txtJVMContainer.gridwidth = 2;
			contentPanel.add(txtJVMContainer, gbc_txtJVMContainer);
			txtJVMContainer.setColumns(10);

		}

		/* This option is obsolete with native launcher */
		/* Suppress update
		if (System.getProperty("os.name").startsWith("Mac")) {
			++row;
			{
				chckbxSuppressVanillaUpdate = new JCheckBox("Suppress vanilla update check");
				GridBagConstraints gbc_chckbxSuppressVanillaUpdate = new GridBagConstraints();
				gbc_chckbxSuppressVanillaUpdate.anchor = GridBagConstraints.WEST;
				gbc_chckbxSuppressVanillaUpdate.gridwidth = 3;
				gbc_chckbxSuppressVanillaUpdate.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxSuppressVanillaUpdate.gridx = 1;
				gbc_chckbxSuppressVanillaUpdate.gridy = row;
				chckbxSuppressVanillaUpdate.setSelected(Boolean.parseBoolean(parent.getConfig().getProperty("suppressUpdates")));
				contentPanel.add(chckbxSuppressVanillaUpdate, gbc_chckbxSuppressVanillaUpdate);
			}
		}
		 --- */

		// Stretch to make room for the content panel
		setSize(this.getWidth(), this.getHeight() + (int)contentPanel.getMinimumSize().getHeight());

		// Buttons
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Properties newConfig = parent.getConfig();
						newConfig.setProperty("minimumMemory", txtMinimum.getText());
						newConfig.setProperty("maximumMemory", txtMaximum.getText());
						newConfig.setProperty("jrePath", txtJavaPath.getText());
						newConfig.setProperty("minimizeOnLaunch", Boolean.toString(chckbxMinimize.isSelected()));
						//newConfig.setProperty("suppressUpdates", Boolean.toString(chckbxSuppressVanillaUpdate.isSelected()));
						newConfig.setProperty("instanceRoot", txtInstanceRoot.getText());
						newConfig.setProperty("jvmOpts", txtJVMOpts.getText());
						newConfig.setProperty("width", txtResWidth.getText());
						newConfig.setProperty("height", txtResHeight.getText());
                        newConfig.setProperty("jvmContainer", txtJVMContainer.getText());
						parent.writeConfig(newConfig);
						parent.mcu.setInstanceRoot(new Path(new File(txtInstanceRoot.getText())));
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}

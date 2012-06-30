package org.smbarbour.mcu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
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
import java.util.Properties;

public class ClientConfig extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6628969370506234011L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtMinimum;
	private JTextField txtMaximum;

	/**
	 * Create the dialog.
	 */
	public ClientConfig(final MainForm parent) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setTitle("Client Configuration");
		setResizable(false);
		setBounds(100, 100, 354, 152);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 20, 217, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 20, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			Component rigidArea = Box.createRigidArea(new Dimension(5, 5));
			GridBagConstraints gbc_rigidArea = new GridBagConstraints();
			gbc_rigidArea.insets = new Insets(0, 0, 5, 5);
			gbc_rigidArea.gridx = 0;
			gbc_rigidArea.gridy = 0;
			contentPanel.add(rigidArea, gbc_rigidArea);
		}
		{
			JLabel lblMinimumMemory = new JLabel("Minimum memory:");
			GridBagConstraints gbc_lblMinimumMemory = new GridBagConstraints();
			gbc_lblMinimumMemory.anchor = GridBagConstraints.WEST;
			gbc_lblMinimumMemory.insets = new Insets(0, 0, 5, 5);
			gbc_lblMinimumMemory.gridx = 1;
			gbc_lblMinimumMemory.gridy = 1;
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
			gbc_txtMinimum.gridy = 1;
			contentPanel.add(txtMinimum, gbc_txtMinimum);
			txtMinimum.setColumns(10);
		}
		{
			Component horizontalStrut = Box.createHorizontalStrut(5);
			GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
			gbc_horizontalStrut.insets = new Insets(0, 0, 5, 0);
			gbc_horizontalStrut.gridx = 3;
			gbc_horizontalStrut.gridy = 1;
			contentPanel.add(horizontalStrut, gbc_horizontalStrut);
		}
		{
			JLabel lblMaximumMemory = new JLabel("Maximum memory:");
			GridBagConstraints gbc_lblMaximumMemory = new GridBagConstraints();
			gbc_lblMaximumMemory.anchor = GridBagConstraints.EAST;
			gbc_lblMaximumMemory.insets = new Insets(0, 0, 5, 5);
			gbc_lblMaximumMemory.gridx = 1;
			gbc_lblMaximumMemory.gridy = 2;
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
			gbc_txtMaximum.gridy = 2;
			contentPanel.add(txtMaximum, gbc_txtMaximum);
			txtMaximum.setColumns(10);
		}
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
			gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
			gbc_verticalStrut.gridx = 1;
			gbc_verticalStrut.gridy = 3;
			contentPanel.add(verticalStrut, gbc_verticalStrut);
		}
		{
			JLabel lblMemoryCanBe = new JLabel("Memory can be specified in MB or GB (i.e. 512M or 1G)");
			GridBagConstraints gbc_lblMemoryCanBe = new GridBagConstraints();
			gbc_lblMemoryCanBe.gridwidth = 2;
			gbc_lblMemoryCanBe.insets = new Insets(0, 0, 0, 5);
			gbc_lblMemoryCanBe.gridx = 1;
			gbc_lblMemoryCanBe.gridy = 4;
			contentPanel.add(lblMemoryCanBe, gbc_lblMemoryCanBe);
		}
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
						parent.writeConfig(newConfig);
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

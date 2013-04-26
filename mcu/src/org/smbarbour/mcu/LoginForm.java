package org.smbarbour.mcu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.smbarbour.mcu.util.Localization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.Color;
import java.awt.Toolkit;

public class LoginForm extends JDialog {

	private static final long serialVersionUID = 494448102754040724L;
	private final JPanel contentPanel = new JPanel();
	private final JPanel buttonPane = new JPanel();
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JCheckBox chkStorePassword;
	private JLabel lblStatus;
	private JButton cancelButton;
	/**
	 * Create the dialog.
	 */
	public LoginForm(final MainForm parent) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(LoginForm.class.getResource("/art/mcu-icon.png")));
		setTitle("Minecraft Login");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final LoginForm window = this;
		setBounds(100, 100, 0, 75);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0, 60, 0, 0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		
				{
					//Username
					JLabel lblUsername = new JLabel("Username:");
					GridBagConstraints gbc_lblUsername = new GridBagConstraints();
					gbc_lblUsername.anchor = GridBagConstraints.WEST;
					gbc_lblUsername.fill = GridBagConstraints.VERTICAL;
					gbc_lblUsername.insets = new Insets(0, 0, 5, 0);
					gbc_lblUsername.gridx = 1;
					gbc_lblUsername.gridy = 1;
					contentPanel.add(lblUsername, gbc_lblUsername);
				}
		{
			Component rigidArea = Box.createRigidArea(new Dimension(5, 5));
			GridBagConstraints gbc_rigidArea = new GridBagConstraints();
			gbc_rigidArea.anchor = GridBagConstraints.WEST;
			gbc_rigidArea.fill = GridBagConstraints.VERTICAL;
			gbc_rigidArea.insets = new Insets(0, 0, 5, 0);
			gbc_rigidArea.gridx = 0;
			gbc_rigidArea.gridy = 0;
			contentPanel.add(rigidArea, gbc_rigidArea);
		}
		{
			Component horizontalStrut = Box.createHorizontalStrut(5);
			GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
			gbc_horizontalStrut.anchor = GridBagConstraints.NORTHWEST;
			gbc_horizontalStrut.insets = new Insets(0, 0, 5, 0);
			gbc_horizontalStrut.gridx = 0;
			gbc_horizontalStrut.gridy = 3;
			contentPanel.add(horizontalStrut, gbc_horizontalStrut);
		}
		
		{
			JLabel lblPassword = new JLabel("Password:");
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.anchor = GridBagConstraints.WEST;
			gbc_lblPassword.fill = GridBagConstraints.VERTICAL;
			gbc_lblPassword.insets = new Insets(0, 0, 5, 0);
			gbc_lblPassword.gridx = 1;
			gbc_lblPassword.gridy = 2;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			txtUsername = new JTextField(parent.getConfig().getProperty("userName",""));
			GridBagConstraints gbc_txtUsername = new GridBagConstraints();
			gbc_txtUsername.anchor = GridBagConstraints.NORTH;
			gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtUsername.insets = new Insets(0, 0, 5, 0);
			gbc_txtUsername.gridx = 2;
			gbc_txtUsername.gridy = 1;
			contentPanel.add(txtUsername, gbc_txtUsername);
		}
				{
					txtPassword = new JPasswordField();
					GridBagConstraints gbc_txtPassword = new GridBagConstraints();
					gbc_txtPassword.anchor = GridBagConstraints.NORTH;
					gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtPassword.gridx = 2;
					gbc_txtPassword.gridy = 2;
					contentPanel.add(txtPassword, gbc_txtPassword);
				}
				{
					chkStorePassword = new JCheckBox("Store password", false);
					chkStorePassword.setToolTipText(Localization.getText("tip_StorePassword"));
					GridBagConstraints gbc_chkStorePassword = new GridBagConstraints();
					gbc_chkStorePassword.anchor = GridBagConstraints.NORTH;
					gbc_chkStorePassword.fill = GridBagConstraints.HORIZONTAL;
					gbc_chkStorePassword.gridx = 2;
					gbc_chkStorePassword.gridy = 3;
					contentPanel.add(chkStorePassword, gbc_chkStorePassword);
				}
		{
			buttonPane.setBorder(new EmptyBorder(0, 5, 5, 5));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				GridBagLayout gbl_buttonPane = new GridBagLayout();
				gbl_buttonPane.columnWidths = new int[]{78, 78, 78, 0, 0};
				gbl_buttonPane.rowHeights = new int[]{23, 0};
				gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				buttonPane.setLayout(gbl_buttonPane);
				{
					lblStatus = new JLabel("");
					lblStatus.setForeground(Color.RED);
					lblStatus.setFont(new Font("Tahoma", Font.BOLD, 12));
					GridBagConstraints gbc_lblStatus = new GridBagConstraints();
					gbc_lblStatus.anchor = GridBagConstraints.WEST;
					gbc_lblStatus.gridwidth = 2;
					gbc_lblStatus.fill = GridBagConstraints.BOTH;
					gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
					gbc_lblStatus.gridx = 0;
					gbc_lblStatus.gridy = 0;
					buttonPane.add(lblStatus, gbc_lblStatus);
				}
				{
					cancelButton = new JButton("Cancel");
					cancelButton.setActionCommand("Cancel");
					cancelButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							onCancel(window);
						}
						
					});
					JButton okButton = new JButton("OK");
					okButton.setActionCommand("OK");
					okButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							try {
								getContentPane().setEnabled(false);
								parent.login(txtUsername.getText(), String.valueOf(txtPassword.getPassword()), chkStorePassword.isSelected());
								window.dispose();
								
							} catch (MCLoginException e) {
								lblStatus.setText(e.getMessage());
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								getContentPane().setEnabled(true);
							}
							
						}
						
					});
					GridBagConstraints gbc_okButton = new GridBagConstraints();
					gbc_okButton.anchor = GridBagConstraints.EAST;
					gbc_okButton.fill = GridBagConstraints.BOTH;
					gbc_okButton.insets = new Insets(0, 0, 0, 5);
					gbc_okButton.gridx = 2;
					gbc_okButton.gridy = 0;
					buttonPane.add(okButton, gbc_okButton);
					getRootPane().setDefaultButton(okButton);
					GridBagConstraints gbc_cancelButton = new GridBagConstraints();
					gbc_cancelButton.anchor = GridBagConstraints.EAST;
					gbc_cancelButton.fill = GridBagConstraints.BOTH;
					gbc_cancelButton.gridx = 3;
					gbc_cancelButton.gridy = 0;
					buttonPane.add(cancelButton, gbc_cancelButton);				
				}
			}
		}
		setSize(this.getWidth() + (int)buttonPane.getMinimumSize().getWidth(), this.getHeight() + (int)contentPanel.getMinimumSize().getHeight());

		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL");
		getRootPane().getActionMap().put("CANCEL", new AbstractAction(){

			private static final long serialVersionUID = -5137929722752752866L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				onCancel(window);
			}
		});
		
		this.addWindowListener( new WindowAdapter() {
			public void windowOpened( WindowEvent e ){
				if (!txtUsername.getText().isEmpty()) {
					txtPassword.requestFocus();
				}				
			}
		});
	}

	protected void onCancel(LoginForm window) {
		window.dispose();		
	}
}


package org.mcupdater;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class JURLSelector extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6110970422198815156L;
	private final JPanel contentPanel = new JPanel();
	private URL result;
	private JTextField txtUrl;
	private JURLSelector self = this;

	/**
	 * Create the dialog.
	 */
	public JURLSelector() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent ev) {
				result = null;
				dispose();
			}
		});
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setTitle("MCU-ServerUtility");
		setResizable(false);
		setSize(350, 35);
		getContentPane().setLayout(new BorderLayout());
		{
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			GridBagLayout gbl_contentPanel = new GridBagLayout();
			gbl_contentPanel.columnWidths = new int[]{0,40,200,40,0};
			gbl_contentPanel.rowHeights = new int[]{0,0,0};
			gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			contentPanel.setLayout(gbl_contentPanel);
			
			int row = 0;
			
			Component rigidTopLeft = Box.createRigidArea(new Dimension(3,3));
			GridBagConstraints gbc_rigidTopLeft = new GridBagConstraints();
			gbc_rigidTopLeft.insets=new Insets(0, 0, 5, 5);
			gbc_rigidTopLeft.gridx=0;
			gbc_rigidTopLeft.gridy=row;
			contentPanel.add(rigidTopLeft, gbc_rigidTopLeft);

			row++;
			
			JLabel lblUrl = new JLabel("URL:");
			GridBagConstraints gbc_lblUrl = new GridBagConstraints();
			gbc_lblUrl.anchor = GridBagConstraints.EAST;
			gbc_lblUrl.insets = new Insets(0, 0, 5, 5);
			gbc_lblUrl.gridx = 1;
			gbc_lblUrl.gridy = row;
			contentPanel.add(lblUrl, gbc_lblUrl);
			
			txtUrl = new JTextField("");
			txtUrl.setColumns(10);
			GridBagConstraints gbc_txtUrl = new GridBagConstraints();
			gbc_txtUrl.insets = new Insets(0, 0, 5, 5);
			gbc_txtUrl.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtUrl.gridx=2;
			gbc_txtUrl.gridy=row;
			contentPanel.add(txtUrl, gbc_txtUrl);
			
			JButton btnBrowse = new JButton("Browse");
			btnBrowse.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser jfc = new JFileChooser();
					int result = jfc.showOpenDialog(self);
					if (!(result == JFileChooser.APPROVE_OPTION)) { return; }				
					try {
						txtUrl.setText(jfc.getSelectedFile().toURI().toURL().toString());
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					}
				}
				
			});
			GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
			gbc_btnBrowse.insets = new Insets(0, 0, 5, 5);
			gbc_btnBrowse.gridx=3;
			gbc_btnBrowse.gridy=row;
			gbc_btnBrowse.anchor = GridBagConstraints.WEST;
			contentPanel.add(btnBrowse, gbc_btnBrowse);
			
			row++;
			
			Component rigidBottomRight = Box.createRigidArea(new Dimension(3,3));
			GridBagConstraints gbc_rigidBottomRight = new GridBagConstraints();
			gbc_rigidBottomRight.insets=new Insets(0, 0, 5, 5);
			gbc_rigidBottomRight.gridx=4;
			gbc_rigidBottomRight.gridy=row;
			contentPanel.add(rigidBottomRight, gbc_rigidBottomRight);
			
			getContentPane().add(contentPanel, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						result = new URL(txtUrl.getText());
						setVisible(false);
						dispose();
					} catch (MalformedURLException e1) {
						JOptionPane.showMessageDialog(null, "The URL entered is not valid!","MCU-ServerUtility",JOptionPane.ERROR_MESSAGE);
					}
				}
				
			});
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					result = null;
					setVisible(false);
					dispose();
				}
			});
			buttonPane.add(cancelButton);

			getContentPane().add(buttonPane, BorderLayout.SOUTH);
		}
		setSize(this.getWidth(), this.getHeight() + (int)contentPanel.getMinimumSize().getHeight());
	}

	public URL getResult() {
		return result;
	}

}

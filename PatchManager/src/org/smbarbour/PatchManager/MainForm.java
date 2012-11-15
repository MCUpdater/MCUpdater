package org.smbarbour.PatchManager;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;

import org.smbarbour.mcu.util.Transmogrify;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainForm {

	private JFrame frame;
	private JTextField txtCreateSource;
	private JTextField txtCreateTarget;
	private JTextField txtCreatePatch;
	private JTextField txtPatchSource;
	private JTextField txtPatchTarget;
	private JTextField txtPatchPatch;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainForm window = new MainForm();
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
	public MainForm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 286);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		Component rigidTopLeft = Box.createRigidArea(new Dimension(5, 5));
		GridBagConstraints gbc_rigidTopLeft = new GridBagConstraints();
		gbc_rigidTopLeft.insets = new Insets(0, 0, 5, 5);
		gbc_rigidTopLeft.gridx = 0;
		gbc_rigidTopLeft.gridy = 0;
		frame.getContentPane().add(rigidTopLeft, gbc_rigidTopLeft);
		
		JLabel lblSource1 = new JLabel("Source file:");
		lblSource1.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblSource1 = new GridBagConstraints();
		gbc_lblSource1.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSource1.anchor = GridBagConstraints.EAST;
		gbc_lblSource1.insets = new Insets(0, 0, 5, 5);
		gbc_lblSource1.gridx = 1;
		gbc_lblSource1.gridy = 1;
		frame.getContentPane().add(lblSource1, gbc_lblSource1);
		
		txtCreateSource = new JTextField();
		GridBagConstraints gbc_txtCreateSource = new GridBagConstraints();
		gbc_txtCreateSource.insets = new Insets(0, 0, 5, 5);
		gbc_txtCreateSource.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtCreateSource.gridx = 2;
		gbc_txtCreateSource.gridy = 1;
		frame.getContentPane().add(txtCreateSource, gbc_txtCreateSource);
		txtCreateSource.setColumns(10);
		
		Component rigidRight = Box.createHorizontalStrut(5);
		GridBagConstraints gbc_rigidRight = new GridBagConstraints();
		gbc_rigidRight.insets = new Insets(0, 0, 5, 0);
		gbc_rigidRight.gridx = 3;
		gbc_rigidRight.gridy = 1;
		frame.getContentPane().add(rigidRight, gbc_rigidRight);
		
		JLabel lblTarget1 = new JLabel("Target file:");
		lblTarget1.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblTarget1 = new GridBagConstraints();
		gbc_lblTarget1.anchor = GridBagConstraints.EAST;
		gbc_lblTarget1.insets = new Insets(0, 0, 5, 5);
		gbc_lblTarget1.gridx = 1;
		gbc_lblTarget1.gridy = 2;
		frame.getContentPane().add(lblTarget1, gbc_lblTarget1);
		
		txtCreateTarget = new JTextField();
		GridBagConstraints gbc_txtCreateTarget = new GridBagConstraints();
		gbc_txtCreateTarget.insets = new Insets(0, 0, 5, 5);
		gbc_txtCreateTarget.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtCreateTarget.gridx = 2;
		gbc_txtCreateTarget.gridy = 2;
		frame.getContentPane().add(txtCreateTarget, gbc_txtCreateTarget);
		txtCreateTarget.setColumns(10);
		
		JLabel lblPatch1 = new JLabel("Patch file:");
		lblPatch1.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPatch1 = new GridBagConstraints();
		gbc_lblPatch1.anchor = GridBagConstraints.EAST;
		gbc_lblPatch1.insets = new Insets(0, 0, 5, 5);
		gbc_lblPatch1.gridx = 1;
		gbc_lblPatch1.gridy = 3;
		frame.getContentPane().add(lblPatch1, gbc_lblPatch1);
		
		txtCreatePatch = new JTextField();
		GridBagConstraints gbc_txtCreatePatch = new GridBagConstraints();
		gbc_txtCreatePatch.insets = new Insets(0, 0, 5, 5);
		gbc_txtCreatePatch.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtCreatePatch.gridx = 2;
		gbc_txtCreatePatch.gridy = 3;
		frame.getContentPane().add(txtCreatePatch, gbc_txtCreatePatch);
		txtCreatePatch.setColumns(10);
		
		Component spacer1 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_spacer1 = new GridBagConstraints();
		gbc_spacer1.insets = new Insets(0, 0, 5, 5);
		gbc_spacer1.gridx = 1;
		gbc_spacer1.gridy = 4;
		frame.getContentPane().add(spacer1, gbc_spacer1);
		
		JButton btnCreate = new JButton("Create patch file");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Path sourcePath = (new File(txtCreateSource.getText())).toPath();
				Path targetPath = (new File(txtCreateTarget.getText())).toPath();
				Path patchPath = (new File(txtCreatePatch.getText())).toPath();
				try {
					Transmogrify.createPatch(sourcePath, targetPath, patchPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		GridBagConstraints gbc_btnCreate = new GridBagConstraints();
		gbc_btnCreate.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCreate.gridwidth = 2;
		gbc_btnCreate.insets = new Insets(0, 0, 5, 5);
		gbc_btnCreate.gridx = 1;
		gbc_btnCreate.gridy = 5;
		frame.getContentPane().add(btnCreate, gbc_btnCreate);
		
		Component spacer2 = Box.createVerticalStrut(10);
		GridBagConstraints gbc_spacer2 = new GridBagConstraints();
		gbc_spacer2.insets = new Insets(0, 0, 5, 5);
		gbc_spacer2.gridx = 1;
		gbc_spacer2.gridy = 6;
		frame.getContentPane().add(spacer2, gbc_spacer2);
		
		JLabel lblSource2 = new JLabel("Source file:");
		lblSource2.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblSource2 = new GridBagConstraints();
		gbc_lblSource2.anchor = GridBagConstraints.EAST;
		gbc_lblSource2.insets = new Insets(0, 0, 5, 5);
		gbc_lblSource2.gridx = 1;
		gbc_lblSource2.gridy = 7;
		frame.getContentPane().add(lblSource2, gbc_lblSource2);
		
		txtPatchSource = new JTextField();
		GridBagConstraints gbc_txtPatchSource = new GridBagConstraints();
		gbc_txtPatchSource.insets = new Insets(0, 0, 5, 5);
		gbc_txtPatchSource.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPatchSource.gridx = 2;
		gbc_txtPatchSource.gridy = 7;
		frame.getContentPane().add(txtPatchSource, gbc_txtPatchSource);
		txtPatchSource.setColumns(10);
		
		JLabel lblTarget2 = new JLabel("Target file:");
		lblTarget2.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblTarget2 = new GridBagConstraints();
		gbc_lblTarget2.anchor = GridBagConstraints.EAST;
		gbc_lblTarget2.insets = new Insets(0, 0, 5, 5);
		gbc_lblTarget2.gridx = 1;
		gbc_lblTarget2.gridy = 8;
		frame.getContentPane().add(lblTarget2, gbc_lblTarget2);
		
		txtPatchTarget = new JTextField();
		GridBagConstraints gbc_txtPatchTarget = new GridBagConstraints();
		gbc_txtPatchTarget.insets = new Insets(0, 0, 5, 5);
		gbc_txtPatchTarget.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPatchTarget.gridx = 2;
		gbc_txtPatchTarget.gridy = 8;
		frame.getContentPane().add(txtPatchTarget, gbc_txtPatchTarget);
		txtPatchTarget.setColumns(10);
		
		JLabel lblPatch2 = new JLabel("Patch file:");
		lblPatch2.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPatch2 = new GridBagConstraints();
		gbc_lblPatch2.anchor = GridBagConstraints.EAST;
		gbc_lblPatch2.insets = new Insets(0, 0, 5, 5);
		gbc_lblPatch2.gridx = 1;
		gbc_lblPatch2.gridy = 9;
		frame.getContentPane().add(lblPatch2, gbc_lblPatch2);
		
		txtPatchPatch = new JTextField();
		GridBagConstraints gbc_txtPatchPatch = new GridBagConstraints();
		gbc_txtPatchPatch.insets = new Insets(0, 0, 5, 5);
		gbc_txtPatchPatch.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPatchPatch.gridx = 2;
		gbc_txtPatchPatch.gridy = 9;
		frame.getContentPane().add(txtPatchPatch, gbc_txtPatchPatch);
		txtPatchPatch.setColumns(10);
		
		Component spacer3 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_spacer3 = new GridBagConstraints();
		gbc_spacer3.insets = new Insets(0, 0, 5, 5);
		gbc_spacer3.gridx = 1;
		gbc_spacer3.gridy = 10;
		frame.getContentPane().add(spacer3, gbc_spacer3);
		
		JButton btnApplyPatch = new JButton("Apply Patch");
		btnApplyPatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Path sourcePath = (new File(txtPatchSource.getText())).toPath();
					Path targetPath = (new File(txtPatchTarget.getText())).toPath();
					Path patchPath = (new File(txtPatchPatch.getText())).toPath();
					Transmogrify.applyPatch(sourcePath, targetPath, patchPath);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		});
		GridBagConstraints gbc_btnApplyPatch = new GridBagConstraints();
		gbc_btnApplyPatch.gridwidth = 2;
		gbc_btnApplyPatch.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnApplyPatch.insets = new Insets(0, 0, 0, 5);
		gbc_btnApplyPatch.gridx = 1;
		gbc_btnApplyPatch.gridy = 11;
		frame.getContentPane().add(btnApplyPatch, gbc_btnApplyPatch);
	}

}

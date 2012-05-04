package org.smbarbour.mcu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.smbarbour.mcu.util.Backup;
import org.smbarbour.mcu.util.MCUpdater;


public class BackupManager extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5904510489295772326L;
	private JPanel contentPane;
	private JTable table;
	final MCUpdater mcu = new MCUpdater();

	public BackupManager(final MainForm parent)  {
		setResizable(false);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setTitle("Manage Backups");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 562, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane gridScroller = new JScrollPane();
		contentPane.add(gridScroller, BorderLayout.CENTER);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final BLTableModel model = new BLTableModel(mcu.loadBackupList());
		model.addTableModelListener(model);
		table.setModel(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(350);
		gridScroller.setViewportView(table);

		JPanel pnlRight = new JPanel();
		contentPane.add(pnlRight, BorderLayout.EAST);
		pnlRight.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel pnlButtons = new JPanel();
		pnlRight.add(pnlButtons);
		pnlButtons.setLayout(new GridLayout(0, 1, 0, 5));
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//JOptionPane.showMessageDialog(null,"This function is not yet implemented.\n\nBackups must currently be restored manually.", "MCUpdater", JOptionPane.WARNING_MESSAGE);
				int saveConfig = JOptionPane.showConfirmDialog(null, "Do you want to save a backup of your existing configuration?", "MCUpdater", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(saveConfig == JOptionPane.YES_OPTION){
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String backDesc = (String) JOptionPane.showInputDialog(null,"Enter description for backup:", "MCUpdater", JOptionPane.QUESTION_MESSAGE, null, null, ("Automatic backup: " + sdf.format(cal.getTime()))); 
					mcu.saveConfig(backDesc);
				} else if(saveConfig == JOptionPane.CANCEL_OPTION){
					return;
				}
				mcu.restoreBackup(new File(mcu.getArchiveFolder().getPath() + MCUpdater.sep + model.getEntry(table.getSelectedRow()).getFilename()));
			}
		});
		pnlButtons.add(btnLoad);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this backup?", "MCUpdater", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(response == JOptionPane.NO_OPTION){
					return;
				}
				File backup = new File(mcu.getArchiveFolder().getPath() + MCUpdater.sep + model.getEntry(table.getSelectedRow()).getFilename());
				backup.delete();
				model.remove(table.getSelectedRow());
			}
		});
		pnlButtons.add(btnRemove);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				mcu.writeBackupList(model.getList());
			}
		});

	}
}

class BLTableModel extends AbstractTableModel implements TableModelListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Backup> bTable = new ArrayList<Backup>();
		
	public BLTableModel(List<Backup> list)
	{
		bTable = list;
	}
	
	public Backup getEntry(int selectedRow) {
		return bTable.get(selectedRow);
	}

	@Override
	public int getRowCount() {
		int rowcount = 0;
		try
		{
			rowcount = bTable.size();
		} catch(NullPointerException npe)
		{
			npe.printStackTrace();
		}
		return rowcount;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	public String getColumnName(int col)
	{
		if(col == 0)
		{
			return "Filename";
		} else if(col == 1)
		{
			return "Description";
		} else
		{
			return "Undefined";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
		{
			return bTable.get(rowIndex).getFilename();
		} else if(columnIndex == 1)
		{
			return bTable.get(rowIndex).getDescription();
		} else
		{
			return null;
		}
	}
	
	public void add(Backup entry)
	{
		bTable.add(entry);
		tableChanged(new TableModelEvent(this));		
	}
	
	public void remove(int rowIndex) {
		bTable.remove(rowIndex);
		tableChanged(new TableModelEvent(this));
	}
	
	public List<Backup> getList()
	{
		return bTable;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}
}
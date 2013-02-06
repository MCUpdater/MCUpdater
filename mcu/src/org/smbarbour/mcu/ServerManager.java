package org.smbarbour.mcu;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;

import org.smbarbour.mcu.util.MCUpdater;
import org.smbarbour.mcu.util.ServerList;
import org.smbarbour.mcu.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalExclusionType;

public class ServerManager extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7843463650576741698L;
	private JPanel contentPane;
	private JTable table;
	final MCUpdater mcu = MCUpdater.getInstance();

	/**
	 * Create the frame.
	 */
	public ServerManager(final MainForm parent) {
		setResizable(false);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setTitle("Manage Servers");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 562, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane gridScroller = new JScrollPane();
		contentPane.add(gridScroller, BorderLayout.CENTER);
		
		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final SLTableModel model = new SLTableModel(mcu.loadServerList(parent.getCustomization().getString("InitialServer.text")));
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
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String packURL = JOptionPane.showInputDialog(null,"Enter server pack URL: ", "Add Server", JOptionPane.PLAIN_MESSAGE);
				try {
					Document serverHeader = ServerPackParser.readXmlFromUrl(packURL);
					Element docEle = serverHeader.getDocumentElement();
					ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), packURL, docEle.getAttribute("newsUrl"), docEle.getAttribute("logoUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), ServerPackParser.parseBoolean(docEle.getAttribute("generateList")), docEle.getAttribute("revision"));
					model.add(sl);
				} catch (Exception x) {
					x.printStackTrace();
				}

			}
		});
		pnlButtons.add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this server?", "MCUpdater", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(response == JOptionPane.NO_OPTION){
					return;
				}
				model.remove(table.getSelectedRow());
			}
		});
		pnlButtons.add(btnRemove);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				mcu.writeServerList(model.getList());
				parent.updateServerList();
			}
		});

	}

}

class SLTableModel extends AbstractTableModel implements TableModelListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ServerList> slTable = new ArrayList<ServerList>();
		
	public SLTableModel(List<ServerList> list)
	{
		slTable = list;
	}
	
	@Override
	public int getRowCount() {
		int rowcount = 0;
		try
		{
			rowcount = slTable.size();
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
			return "Server Name";
		} else if(col == 1)
		{
			return "Pack URL";
		} else
		{
			return "Undefined";
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
		{
			return slTable.get(rowIndex).getName();
		} else if(columnIndex == 1)
		{
			return slTable.get(rowIndex).getPackUrl();
		} else
		{
			return null;
		}
	}
	
	public void add(ServerList entry)
	{
		slTable.add(entry);
		tableChanged(new TableModelEvent(this));		
	}
	
	public void remove(int rowIndex) {
		slTable.remove(rowIndex);
		tableChanged(new TableModelEvent(this));
	}
	
	public List<ServerList> getList()
	{
		return slTable;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}
}
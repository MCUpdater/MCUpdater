package cah.melonar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

public class ServerManagerSWT extends Dialog {

	protected Object result;
	protected Shell shell;
	private Table table;
	private List<ServerList> slModify;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ServerManagerSWT(Shell parent, int style) {
		super(parent, SWT.DIALOG_TRIM);
		setText("Manage Servers");
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		final MCUpdater mcu = new MCUpdater();
		
		slModify = mcu.loadServerList();
		
		shell = new Shell(getParent(), getStyle());
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				mcu.writeServerList(slModify);
				MainSWT.updateLists((Shell) shell.getParent());
			}
		});
		shell.setSize(562, 300);
		shell.setText(getText());
		shell.setLayout(new BorderLayout(0, 0));
		
		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(BorderLayout.CENTER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnServerName = new TableColumn(table, SWT.NONE);
		tblclmnServerName.setWidth(190);
		tblclmnServerName.setText("Server Name");
		
		TableColumn tblclmnPackUrl = new TableColumn(table, SWT.NONE);
		tblclmnPackUrl.setWidth(100);
		tblclmnPackUrl.setText("Pack URL");
		
		Iterator<ServerList> it = slModify.iterator();
		while(it.hasNext())
		{
			ServerList entry = it.next();
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(entry.getName());
			tableItem.setText(1, entry.getPackUrl());
			tableItem.setData(entry);
		}
				
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(BorderLayout.EAST);
		RowLayout rl_composite_1 = new RowLayout(SWT.VERTICAL);
		rl_composite_1.fill = true;
		composite_1.setLayout(rl_composite_1);
		
		Button btnAdd = new Button(composite_1, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog in = new InputDialog(Display.getCurrent().getActiveShell(), "Add Server", "Enter URL for Server Pack", "", new URLValidator() );
				if(in.open() == Window.OK)
				{
					try {
						Document serverHeader = MCUpdater.readXmlFromUrl(in.getValue());
						Element docEle = serverHeader.getDocumentElement();
						ServerList sl = new ServerList(docEle.getAttribute("name"), in.getValue(), docEle.getAttribute("newsUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"));
						TableItem tableItem = new TableItem(table, SWT.NONE);
						tableItem.setText(sl.getName());
						tableItem.setText(1, sl.getPackUrl());
						tableItem.setData(sl);
						slModify.add(sl);
						//slList.add(new ServerList(docEle.getAttribute("name"), entry, docEle.getAttribute("newsUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress")));
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
		});
		btnAdd.setText("&Add");
		
		Button btnRemove = new Button(composite_1, SWT.NONE);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				slModify.remove(table.getSelection()[0].getData());
				table.getSelection()[0].dispose();
			}
		});
		btnRemove.setText("&Remove");

	}

	class URLValidator implements IInputValidator
	{
	    public String isValid(String newText)
	    {
	        try
	        {
	            new URL( newText );  // Throws exception is not valid...
	            return null;
	        } catch( MalformedURLException e )
	        {
	            return "Invalid URL.";
	        }
	    }
	}
}

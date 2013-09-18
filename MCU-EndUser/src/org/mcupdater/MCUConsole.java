package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class MCUConsole extends Composite {

	private StyledText console;

	public MCUConsole(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());
		console = new StyledText(this, SWT.V_SCROLL | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		FontData[] fdConsole = console.getFont().getFontData();
		fdConsole[0].setHeight(8);
		console.setFont(new Font(Display.getCurrent(), fdConsole));
		console.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.stateMask == SWT.CONTROL && arg0.keyCode == 'a') {
					console.selectAll();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {}
			
		});
		console.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent arg0) {
				console.setTopIndex(console.getLineCount()-1);
			}
		});
	}

	public void appendLine(final String text, final LineStyle style) {
		console.append(text + "\n");
		console.setLineBackground(console.getLineCount()-2, 1, new Color(Display.getCurrent(), style.getColor()));
	}
	
	public enum LineStyle {
		NORMAL(new RGB(255,255,255)),
		WARNING(new RGB(255,255,0)),
		ERROR(new RGB(255,0,0));
		
		private RGB color;

		LineStyle(RGB color) {
			this.setColor(color);
		}

		public RGB getColor() {
			return color;
		}

		private void setColor(RGB color) {
			this.color = color;
		}
	}
}

package classes;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class AppScrollBarRenderer extends BasicScrollBarUI {
	
	@Override
	protected JButton createDecreaseButton(int orientation) {
		return createZeroButton(orientation);
	}
	
	@Override
	protected JButton createIncreaseButton(int orientation) {
		return createZeroButton(orientation);
	}
	
	@Override
	protected void configureScrollBarColors() {
		this.thumbColor = Color.decode("#d9d9d9");
		this.scrollBarWidth = 10;
	}
	
	protected JButton createZeroButton(int orientation) {
		JButton btn = new JButton();
		btn.setPreferredSize(new Dimension(0,0));
		btn.setMaximumSize(new Dimension(0,0));
		btn.setMinimumSize(new Dimension(0,0));
		return btn;
	}
}

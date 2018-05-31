package cre.test.ui.statusbar;

import java.util.Date;

public interface StatusBarUI {

	public void printInfo (String info);
	
	public void print (String label, long percent, Date d);

	
}

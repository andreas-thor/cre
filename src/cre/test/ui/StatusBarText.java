package cre.test.ui;

import java.util.Date;


public class StatusBarText implements StatusBarUI {

	public void printInfo (String info) {
		System.out.println(info);
	}
	
	public void print (String label, long percent, Date d) {

		StringBuffer out = new StringBuffer(String.format("%s: %s", d.toString(), label));
		if (percent > 0) {
			 out.append(String.format(" [%d%%]", percent));
		} 
		System.out.println(out.toString());
	}

	
	

	
}

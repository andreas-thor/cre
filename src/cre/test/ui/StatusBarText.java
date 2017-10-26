package cre.test.ui;

import java.util.Date;


public class StatusBarText extends StatusBar {


	@Override
	protected void setInfo (String info) {
		System.out.println(info);
	}
	
	@Override

	protected void setValue (String label, long value, String info, Date d) {
		System.out.println(String.format("%s: %s [%d%%]", d.toString(), label, (maxSize==0) ? 0 : 100*value/maxSize));
	}

	
	

	
}

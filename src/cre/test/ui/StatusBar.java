package cre.test.ui;

import java.util.Date;

import cre.test.Main.EventStatusBar;

public class StatusBar {

	private long maxSize;
	private long blockSize;
	private long blockCount;
	private Date date;
	private String label;
	private EventStatusBar onUpdate;
	

	
	public StatusBar(EventStatusBar onUpdate) {
		super();
		this.onUpdate = onUpdate;
		initProgressbar(1, "CRE started");
	}
	

	public void setValue (String label, long value) {
		setValue(label, value, null, new Date()); 
	}
	public void setValue (String label, long value, String info) {
		setValue(label, value, info, new Date());
	}
	public void setValue (String label) {
		setValue(label, 0L, null, new Date());
	}
	public void setValue (String label, String info) {
		setValue(label, 0L, info, new Date());
	}

		
	public void setValue (String label, long value, String info, Date d) {
		this.onUpdate.onUpdate(String.format("   %1$s: %2$s       ", d, label), 1.0*value/maxSize, info==null?null:String.format("       %s   ", info));
	}
	
	
	public void initProgressbar (long maxSize, String label) {
		
		System.out.println("MaxSize = " + maxSize);
		this.maxSize = maxSize;
		this.blockSize = maxSize/20;
		this.blockCount = 0;
		this.date = new Date();
		this.label = label;
		setValue(label, 0, "", this.date);
		
		
	}
	
	public void updateProgressbar (long count) {
		if (blockCount*blockSize<count) {
			System.out.println("Count = " + count);
			setValue(label, count, "", date);
			blockCount++;
		}
	}
	

	
}

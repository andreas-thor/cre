package cre.test.ui;

import java.util.Date;
import java.util.function.Function;

import cre.test.Main.onUpdateStatusBar;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class StatusBar {

	private Label sblabel;
	private ProgressBar sbpb;
	private Label sbinfo;
	
	private long maxSize;
	private long blockSize;
	private long blockCount;
	private Date date;
	private String label;
	private onUpdateStatusBar onUpdate;
	

	
	public StatusBar(Label sblabel, ProgressBar sbpb, Label sbinfo, onUpdateStatusBar onUpdate) {
		super();
		this.sblabel = sblabel;
		this.sbpb = sbpb;
		this.sbinfo = sbinfo;
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
		
//		Runnable runnable = new Runnable() {
//			@Override
//			public void run() {

		this.onUpdate.onUpdate(String.format("   %1$s: %2$s       ", d, label), 1.0*value/maxSize, info==null?null:String.format("       %s   ", info));
		
//		Platform.runLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				sblabel.setText(String.format("   %1$s: %2$s       ", d, label));
////				sbpb.setProgress(value);
//				sbpb.setProgress(1.0*value/maxSize);
//				if (info != null) {
//					sbinfo.setText(String.format("       %s   ", info));
//				}
//				
//			}
//		});
		
//			}
//		};
//		
//		Thread t = new Thread(runnable);
//		t.start();
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

package cre.test.ui;

import java.util.Date;
import java.util.function.Consumer;

import cre.test.data.CRStats;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class StatusBar extends GridPane {

	private static StatusBar stat = null;
	private static boolean ISTEXT = false;
	
	private Label sbdate;
	private Label sblabel;
	private ProgressBar sbpb;
	private Label sbinfo;
	
	protected long maxSize;
	private long blockSize;
	private long blockCount;
	private Date date;
	private String label;
	
	private long count;

	private Consumer<Void> onUpdateInfo;

	public static StatusBar get() {
		if (stat == null) {
			if (!ISTEXT) {
				stat  = new StatusBar();
			} else {
				stat = new StatusBarText();
			}
		}
		return stat;
	}
	
	
	public void setOnUpdateInfo (Consumer<Void> onUpdateInfo) {
		this.onUpdateInfo = onUpdateInfo;
	}
	
	
	protected StatusBar() {
		
		super();
		this.onUpdateInfo = null;

		if (!ISTEXT) {
		
			GridPane.setColumnIndex(this,  0);
			GridPane.setRowIndex(this,  0);
			GridPane.setHgrow(this, Priority.ALWAYS);
			setPadding(new Insets(5, 10, 5, 10));
	
			this.sbdate = new Label();
			this.sbdate.setPadding(new Insets (0, 10, 0, 0));
			this.sbdate.setTextFill(Color.GRAY);
			add(this.sbdate, 0, 0);
			
			this.sblabel = new Label();
			this.sblabel.setPadding(new Insets (0, 10, 0, 0));
			add(this.sblabel, 1, 0);
			
			this.sbpb = new ProgressBar(0);
			GridPane.setHgrow(this.sbpb, Priority.ALWAYS);
			this.sbpb.setMaxWidth(999999);
			this.sbpb.setPadding(new Insets (0, 10, 0, 0));
			add(this.sbpb, 2, 0);
			
			this.sbinfo = new Label();
			this.sbinfo.setPadding(new Insets (0, 0, 0, 0));
	
			add(this.sbinfo, 3, 0);
		}
		
		initProgressbar(1, "CRE started");
	}

	

//	public void setValue (String label, long value) {
//		setValue(label, value, null, new Date()); 
//	}
//	public void setValue (String label, long value, String info) {
//		setValue(label, value, info, new Date());
//	}
	public void setValue (String label) {
		setValue(label, 0L, null, new Date());
	}
//	public void setValue (String label, String info) {
//		setValue(label, 0L, info, new Date());
//	}

	public void updateInfo () {
		int[] yearsRPY = CRStats.getMaxRangeYear();
		int[] yearsRPYVisible = CRStats.getMaxRangeYear(true);
		int[] yearsPY  = CRStats.getMaxRangeCitingYear();

		setInfo (String.format("#CRs: %d (%d shown), #Clusters: %d, RPY: %d-%d (%d-%d shown), PY: %d-%d",
			CRStats.getSize(),
			CRStats.getNumberByVisibility(true),
			CRStats.getNoOfClusters(), 
			yearsRPY[0], 
			yearsRPY[1],
			yearsRPYVisible[0], 
			yearsRPYVisible[1],
			yearsPY[0], 
			yearsPY[1]
			));
		
		if (this.onUpdateInfo!=null) {
			this.onUpdateInfo.accept(null);
		}
	}
		
	
	protected void setInfo (String info) {
		sbinfo.setText (info);
	}
	
	protected void setValue (String label, long value, String info, Date d) {
		this.label = label;
		Platform.runLater( () -> {
			sbdate.setText(d.toString());
			sblabel.setText(label);
			sbpb.setProgress(1.0*value/maxSize);
			if (info!= null) {
				sbinfo.setText(info);
			}
		});		
	}
	

	public void initProgressbar (long maxSize) {
		this.initProgressbar(maxSize, this.label);
	}

	public void initProgressbar (long maxSize, String label) {
		
//		System.out.println("MaxSize = " + maxSize);
		this.maxSize = maxSize;
		this.blockSize = maxSize/20;
		this.blockCount = 0;
		this.date = new Date();
		this.label = label;
		this.count = 0;
		setValue(label, 0, "", this.date);
	}
	
	public void incProgressbar () {
		updateProgressbar(this.count+1);
	}
	
	public void incProgressbar (long inc) {
		updateProgressbar(this.count+inc);
	}
	
	public void updateProgressbar (long count) {
		this.count = count;
		if (blockCount*blockSize<count) {
//			System.out.println("Count = " + count);
			setValue(label, count, "", date);
			blockCount++;
		}
	}
	

	
}

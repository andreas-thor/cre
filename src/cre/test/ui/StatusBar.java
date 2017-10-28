package cre.test.ui;

import java.util.Date;
import java.util.function.Consumer;

import cre.test.data.CRStats;

public class StatusBar {

	// Singleton Pattern
	private static StatusBar stat = null;

	// UI element 
	private StatusBarUI statUI = null;
	
	private long count;
	protected long maxSize;
	private long blockSize;
	private long blockCount;
	
	private Date date;
	private String label;

	protected Consumer<Void> onUpdateInfo;

	
	public void setUI(StatusBarUI statUI) {
		this.statUI = statUI;
		initProgressbar(0, "CRE started");
	}
	
	public static StatusBar get() {
		if (stat==null) {
			stat = new StatusBar();
			stat.onUpdateInfo = null;
		}
		return stat;
	}
	
	
	public void setOnUpdateInfo (Consumer<Void> onUpdateInfo) {
		this.onUpdateInfo = onUpdateInfo;
	}
	
	

	public void setValue (String label) {
		setValue(label, 0L, new Date());
	}

	
	
	public void updateInfo () {
		int[] yearsRPY = CRStats.getMaxRangeYear();
		int[] yearsRPYVisible = CRStats.getMaxRangeYear(true);
		int[] yearsPY  = CRStats.getMaxRangeCitingYear();

		if (this.statUI != null) {
			statUI.printInfo(String.format("#CRs: %d (%d shown), #Clusters: %d, RPY: %d-%d (%d-%d shown), PY: %d-%d",
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
		}
		
		if (this.onUpdateInfo!=null) {
			this.onUpdateInfo.accept(null);
		}
	}
		
	

	protected void setValue (String label, long percent, Date d) {
		this.label = label;
		this.date = d;
		if (this.statUI != null) {
			this.statUI.print(label, percent, d);
		}
	}
	


	public void initProgressbar (long maxSize) {
		this.initProgressbar(maxSize, this.label);
	}

	public void initProgressbar (long maxSize, String label) {
		
		this.maxSize = maxSize;
		this.blockSize = maxSize/20;
		this.blockCount = 0;
		this.date = new Date();
		this.label = label;
		this.count = 0;
		setValue(label, 0L, this.date);
	}
	
	
	public void incProgressbar () {
		updateProgressbar(this.count+1);
	}
	
	public void incProgressbar (long inc) {
		updateProgressbar(this.count+inc);
	}
	
	public void updateProgressbar (long count) {
		this.count = count;
		if (count > blockCount*blockSize) {
			setValue(label, (this.maxSize==0) ? 0L : 100*count/maxSize, this.date);
			blockCount++;
		}
	}


	
}

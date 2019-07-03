package main.cre.ui.statusbar;

import java.util.Date;
import java.util.function.Consumer;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.Statistics.IntRange;

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
		IntRange yearsRPY = CRTable.get().getStatistics().getMaxRangeRPY();
		IntRange yearsRPYVisible = CRTable.get().getStatistics().getMaxRangeRPY(true);
		IntRange yearsPY  = CRTable.get().getStatistics().getMaxRangePY();

		
		if (this.statUI != null) {
			statUI.printInfo(String.format("#CRs: %d (%d shown), #Clusters: %d, RPY: %d-%d (%d-%d shown), PY: %d-%d",
				CRTable.get().getStatistics().getNumberOfCRs(),
				CRTable.get().getStatistics().getNumberOfCRsByVisibility(true),
				CRTable.get().getClustering().getNumberOfClusters(), 
				yearsRPY.getMin(), 
				yearsRPY.getMax(),
				yearsRPYVisible.getMin(), 
				yearsRPYVisible.getMax(),
				yearsPY.getMin(), 
				yearsPY.getMax()
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

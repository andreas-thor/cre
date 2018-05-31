package cre.test.ui;

import cre.test.data.CRChartData;
import cre.test.data.UserSettings;
import javafx.scene.Node;

public abstract class CRChart {

	public static String xAxisLabel = "Reference Publication Year";
	public static String yAxisLabel = "Cited References";
	
	
	
	public CRChart () {
		super();
	}
	
	public abstract Node getNode ();
	
	public abstract void setVisible (boolean value);

	public abstract boolean isVisible ();
	
	public void setDomainRange (int[] range) {
		if ((range==null) || !isVisible()) return;
		setChartDomainRange(range);
	};
	
	protected abstract void setChartDomainRange (int[] range);
	
	public abstract void updateData (CRChartData data);
	
	public abstract void setFontSize ();
	
	public abstract void autoRange ();
	
	protected String getSeriesLabel (int idx) {
		switch (idx) {
		case 0: return "Number of Cited References";
		case 1: return String.format("Deviation from the %1$d-Year-Median", 2*UserSettings.get().getMedianRange()+1);
		default: return "";
		}
	}
	
	protected abstract void onSelectYear (int year);
	
	protected abstract void onYearRangeFilter (double min, double max);
	

	
	
}

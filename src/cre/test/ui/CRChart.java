package cre.test.ui;

import java.util.Optional;

import cre.test.data.CRTable;
import cre.test.data.CRType;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;

public abstract class CRChart {

	public static String xAxisLabel = "Cited Reference Year";
	public static String yAxisLabel = "Cited References";
	
	protected CRTableView tabView;
	protected CRTable crTab;
	
	public CRChart (CRTable crTab, CRTableView tabView) {
		this.crTab = crTab;
		this.tabView = tabView;
	}
	
	public abstract Node getNode ();
	
	public abstract void setVisible (boolean value);

	public abstract boolean isVisible ();
	
	public void setDomainRange (int[] range) {
		if ((range==null) || !isVisible()) return;
		setChartDomainRange(range);
	};
	
	protected abstract void setChartDomainRange (int[] range);
	
	public abstract void updateData (int[][] data);
	
	
	protected String getSeriesLabel (int idx) {
		switch (idx) {
		case 0: return "Number of Cited References";
		case 1: return String.format("Deviation from the %1$d-Year-Median", 2*UserSettings.get().getMedianRange()+1);
		default: return "";
		}
	}
	
	protected void onSelectYear (int year) {
		
		/* sort by year ASC, n_cr desc */
		Platform.runLater( () -> {
			tabView.getColumnByName("RPY").setSortType(TableColumn.SortType.ASCENDING);
			tabView.getColumnByName("N_CR").setSortType(TableColumn.SortType.DESCENDING);
			tabView.getSortOrder().clear();
			tabView.getSortOrder().add(tabView.getColumnByName("RPY"));
			tabView.getSortOrder().add(tabView.getColumnByName("N_CR"));
			Optional<CRType> first = tabView.getItems().stream().filter(cr -> cr.getRPY() == year).findFirst();
			if (first.isPresent()) {
				tabView.getSelectionModel().select(first.get());
				tabView.scrollTo(first.get());
			}
		});
	}
	
	protected void onYearRangeFilter (double min, double max) {
		if (! crTab.duringUpdate) {	// ignore updates during data update
			crTab.filterByYear (new int[] {(int)Math.ceil(min), (int)Math.floor(max)});
		}
	}
	
}

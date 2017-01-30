package cre.test.ui;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.w3c.dom.Document;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public abstract class CRChart_HighCharts extends CRChart {

	
	private WebView browser;
	private boolean loaded;
	private boolean duringUpdate;

	public class ChartCallBack  {
		
		public void onRedraw(double min, double max) {
			if (!duringUpdate) onYearRangeFilter(min, max);
		}
		public void onClick(double x) {
			onSelectYear((int)Math.round(x));
		}
	}

	
	public CRChart_HighCharts () {

		super();
		
		browser = new WebView();
		loaded = false;
		duringUpdate = false;
		
		WebEngine webEngine = browser.getEngine();
		browser.setContextMenuEnabled(false);

		
		webEngine.load(CRChart_HighCharts.class.getResource("highcharts/CRChart.html").toString());
		
		webEngine.documentProperty().addListener(new ChangeListener<Document>() {
			@Override
			public void changed(ObservableValue<? extends Document> observableValue, Document document,	Document newDoc) {
				if (newDoc != null) {
					webEngine.documentProperty().removeListener(this);
					loaded = true;
					JSObject jsobj = (JSObject) webEngine.executeScript("window");
					jsobj.setMember("java", new ChartCallBack());
					updateData(new int[][] { { 0 }, { 0 }, { 0 } });
				}
			}
		});
		
	}
	
	@Override
	public Node getNode() {
		return browser;
	}

	@Override
	public void setVisible(boolean value) {
		browser.setVisible(value);
	}
	
	@Override
	public boolean isVisible() {
		return browser.isVisible();
	}

	
	@Override
	public void setChartDomainRange(int[] range) {
		if (loaded) {
			duringUpdate = true;	// make sure that setting x-axis range does not trigger the onRedraw -> onYearRangeFilter
			browser.getEngine().executeScript(String.format("c.xAxis[0].setExtremes(%d, %d, true);", range[0], range[1]));
			duringUpdate = false;
		}
	}

	@Override
	public void updateData(int[][] data) {

		duringUpdate = true;
		
		// series as JSON data
		String[] json = IntStream.range(0, 2).mapToObj(s ->
				IntStream.range(0, data[0].length).mapToObj(it -> "["+data[0][it]+","+data[s+1][it]+"]").collect(Collectors.joining(", "))
			).toArray(size -> new String[size]);
		
		// minimum[..,0] and maximum[..,1] for x-Axis[0] and both y-Axes [1] and [2]
		int[][] extremes = IntStream.range(0, 3).mapToObj(k -> {
			IntSummaryStatistics stats = Arrays.stream(data[k]).summaryStatistics();
			return (stats.getCount()==0) ? new int[] {0, 1} : new int[] { stats.getMin(), stats.getMax() };
		}).toArray(int[][]::new);
		
		// call Javascript to render chart
		if (loaded) {
			try {
				browser.getEngine().executeScript(String.format("updateData($.parseJSON('[%s]'), $.parseJSON('[%s]'), '%s', '%s', ['%s', '%s']);", json[0] ,json[1], CRChart.xAxisLabel, CRChart.yAxisLabel, getSeriesLabel(0), getSeriesLabel(1)));
				browser.getEngine().executeScript(String.format("c.xAxis[0].setExtremes(%d, %d, false);", extremes[0][0], extremes[0][1]));
				browser.getEngine().executeScript(String.format("c.yAxis[0].setExtremes(%d, %d, true);", Math.min(extremes[1][0], extremes[2][0]), Math.max(extremes[1][1], extremes[2][1])));
			} catch (JSException e) {}
		}
		
		duringUpdate = false;
		
	}

}

package cre.test.ui;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public abstract class CRChart_HighCharts extends CRChart {

	
	private WebView browser;
	private boolean loaded;
	private ChartCallBack cb;

	public class ChartCallBack  {
		
		public void onRedraw(String e, double min, double max) {
			System.out.println("HighChart On Redraw " + min + "/" + max);
			onYearRangeFilter(min, max);
		}
		
		public void onClick(double x) {
			onSelectYear((int)Math.round(x));
		}
	}

	
	public CRChart_HighCharts () {

		super();
		
		browser = new WebView();
		setVisible(false);
		loaded = false;
		cb = new ChartCallBack();
		
		WebEngine webEngine = browser.getEngine();
		browser.setContextMenuEnabled(false);

		
		
		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				if ((newValue == Worker.State.SUCCEEDED) && (isVisible())) {
					loaded = true;
					Platform.runLater( () -> {
						JSObject jsobj = (JSObject) webEngine.executeScript("window");
						jsobj.setMember("crejava", cb);
						updateData(new int[][] { { 0 }, { 0 }, { 0 } });
					});
				}
				
			}
		});

		
		webEngine.load(CRChart_HighCharts.class.getResource("highcharts/CRChart.html").toExternalForm());
		
     
		
//		webEngine.documentProperty().addListener(new ChangeListener<Document>() {
//			@Override
//			public void changed(ObservableValue<? extends Document> observableValue, Document document,	Document newDoc) {
//				if (newDoc != null) {
//					webEngine.documentProperty().removeListener(this);
//					loaded = true;
//					JSObject jsobj = (JSObject) webEngine.executeScript("window");
//					jsobj.setMember("java", new ChartCallBack());
//					updateData(new int[][] { { 0 }, { 0 }, { 0 } });
//				}
//			}
//		});
		
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
		
		
		WebEngine webEngine = browser.getEngine();

		JSObject jsobj = (JSObject) webEngine.executeScript("window");
		if ((jsobj.getMember("crejava")==null) || (jsobj.getMember("crejava").equals("undefined"))) {
			jsobj.setMember("crejava", cb);
		}

		if (loaded) {
			webEngine.executeScript(String.format("c.xAxis[0].setExtremes(%d, %d, true);", range[0], range[1]));
		}
	}

	@Override
	public void updateData(int[][] data) {

		
		// series as JSON data
		String[] json = IntStream.range(0, 2).mapToObj(s ->
				IntStream.range(0, data[0].length).mapToObj(it -> "["+data[0][it]+","+data[s+1][it]+"]").collect(Collectors.joining(", "))
			).toArray(size -> new String[size]);
		
		// call Javascript to render chart
		if (loaded) {
			try {
				WebEngine webEngine = browser.getEngine();
				
				JSObject jsobj = (JSObject) webEngine.executeScript("window");
				if ((jsobj.getMember("crejava")==null) || (jsobj.getMember("crejava").equals("undefined"))) {
					jsobj.setMember("crejava", cb);
				}

				webEngine.executeScript(String.format("updateData($.parseJSON('[%s]'), $.parseJSON('[%s]'), '%s', '%s', ['%s', '%s']);", json[0] ,json[1], CRChart.xAxisLabel, CRChart.yAxisLabel, getSeriesLabel(0), getSeriesLabel(1)));
				
			} catch (JSException e) {
				e.printStackTrace();
				
				
			}
		}
	}

}

package cre.test.ui;

import org.w3c.dom.Document;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class CRChart_HighCharts implements CRChart {

	public class ChartCallBack  {
		
		public void callMe(double min, double max) {
			System.out.println("Called me!: " + min + "/" + max);
		}
	}
	
	private WebView browser;
	private boolean loaded;
	
	public CRChart_HighCharts() {
		
		browser = new WebView();
		loaded = false;
		
		WebEngine webEngine = browser.getEngine();
		JSObject jsobj = (JSObject) webEngine.executeScript("window");
		jsobj.setMember("java", new ChartCallBack());
		webEngine.load("file:///E:/Dev/CRE/src/cre/test/ui/highcharts/CRChart.html");
		
		webEngine.documentProperty().addListener(new ChangeListener<Document>() {
			  @Override public void changed(ObservableValue<? extends Document> observableValue, Document document, Document newDoc) {
			    if (newDoc != null) {
			    	webEngine.documentProperty().removeListener(this);
			    	loaded = true;
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
	public void setDomainRange(Integer min, Integer max) {
		if (loaded) {
			browser.getEngine().executeScript(String.format("c.xAxis[0].setExtremes(%d, %d, true);", min, max));

		}
	}

	@Override
	public void updateData(int[][] data) {
		// TODO Auto-generated method stub
		
		StringBuffer json = new StringBuffer("");
		boolean first = true;
		
		int[][] extremes = new int[][] { {0, 1}, {0, 1}, {0, 1} };
		
		for (int i=0; i<data[0].length; i++) {
			if (!first) json.append(", ");
			json.append("[");
			json.append(data[0][i]);
			json.append(",");
			json.append(data[1][i]);
			json.append("]");
			
			for (int k=0; k<3; k++) {
				if (first) {
					extremes[k][0] = data[k][i];
					extremes[k][1] = data[k][i];
				}
				if (extremes[k][0]>data[k][i]) extremes[k][0] = data[k][i]; 
				if (extremes[k][1]<data[k][i]) extremes[k][1] = data[k][i]; 
			}
			first = false;
		}
		
		
		if (loaded) {
			System.out.println(json.toString());
			browser.getEngine().executeScript(String.format("updateData($.parseJSON('[%s]'));", json.toString()));
			browser.getEngine().executeScript(String.format("c.xAxis[0].setExtremes(%d, %d, false);", extremes[0][0], extremes[0][1]));
			browser.getEngine().executeScript(String.format("c.yAxis[0].setExtremes(%d, %d, true);", extremes[1][0], extremes[1][1]));

		}

		
		 //xxx($.parseJSON('[[1,10.7695], [2,20.7648], [3,5.7645]]'));
		
		
	}

}

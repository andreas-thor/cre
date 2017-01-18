package cre.test.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import cre.test.data.CRTable;
import javafx.scene.Node;

public class CRChart_JFreeChart extends CRChart {

	private JFreeChart chart;
	private ChartViewer chView;
	 
	private DefaultXYDataset ds;

	private boolean duringRangeSet;

	
	public CRChart_JFreeChart (CRTable crTab, CRTableView tabView) {

		super(crTab, tabView);
		duringRangeSet = false;
		
		ds = new DefaultXYDataset();
		chart = ChartFactory.createXYLineChart("", CRChart.xAxisLabel, CRChart.yAxisLabel, ds);

		chart.getLegend().setFrame(BlockBorder.NONE);
		
		XYPlot plot = chart.getXYPlot();
		plot.setRangeZeroBaselineVisible(true);
		plot.setRangeZeroBaselinePaint(Color.black);
		plot.setRangeZeroBaselineStroke(new BasicStroke(2));
		plot.setRenderer(new XYSplineRenderer());
		
		// domain=year -> show year number always with 4 digits
		NumberAxis dAxis = new NumberAxis();
		dAxis.setNumberFormatOverride(new DecimalFormat("0000.#"));
		plot.setDomainAxis (dAxis);
		
		// general chart layout
		XYPlot rPlot = plot.getRenderer().getPlot();
		rPlot.setBackgroundPaint(Color.white);
		rPlot.setOutlinePaint(Color.black);
		rPlot.setDomainGridlinePaint(Color.gray);
		rPlot.setRangeGridlinePaint(Color.gray);
		
		// layout for data rows
		XYItemRenderer rend = plot.getRenderer();
		double shapeSize = 6;	// 6
		float strokeSize = 3;	// 3
		rend.setSeriesShape(0, new Rectangle2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));
		rend.setSeriesStroke(0, new BasicStroke(strokeSize));
		rend.setSeriesShape(1, new Ellipse2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));
		rend.setSeriesStroke(1, new BasicStroke(strokeSize));
		
		// tooltip = CR year with sum of all NCR of this year
		
		rend.setSeriesToolTipGenerator(0, new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				return String.format("Year=%d, %s=%d", dataset.getX(0, item).intValue(), series==0 ? "N_CR" : "Diff", dataset.getY(series, item).intValue());
			}
		});
		
		// update table when zoom changes in chart
		plot.addChangeListener(pcevent -> {
			if (!duringRangeSet) {
				System.out.println("onChange");
				System.out.println(dAxis.getLowerBound());
				System.out.println(dAxis.getUpperBound());
				onYearRangeFilter(dAxis.getLowerBound(), dAxis.getUpperBound());
			}
		});
		

		chView = new ChartViewer(chart);
		chView.addChartMouseListener(new ChartMouseListenerFX() {
			
			@Override
			public void chartMouseMoved(ChartMouseEventFX arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEventFX cmevent) {
				if (cmevent.getEntity() instanceof XYItemEntity) {
					/* get year (domain value) of clicked data item */
					XYItemEntity a = (XYItemEntity) cmevent.getEntity();
					int year = a.getDataset().getX(a.getSeriesIndex(), a.getItem()).intValue();
					onSelectYear (year);
				}	
			}
		});
	}


	
	@Override
	public Node getNode() {
		return chView;
	}

	@Override
	public void setVisible (boolean value) {
		chView.setVisible(value);
	}

	@Override
	public boolean isVisible () {
		return chView.isVisible();
	}

	@Override
	protected void setChartDomainRange(int min, int max) {
		org.jfree.data.Range dAxisRange = chart.getXYPlot().getDomainAxis().getRange();
		if ((((int)Math.ceil (dAxisRange.getLowerBound())) != min) || (((int)Math.floor(dAxisRange.getUpperBound())) != max)) { 
			System.out.println("Adjusting");
			System.out.println("Axis = " + dAxisRange.toString());
			System.out.println("Year = " + min + ", " + max);
			duringRangeSet = true;
			if (min==max) {
				chart.getXYPlot().getDomainAxis().setRange(min-0.5, max+0.5);
			} else {
				chart.getXYPlot().getDomainAxis().setRange(min, max);
			}
			duringRangeSet = false;

		}
	}

	@Override
	public void updateData(int[][] data) {
		
		// delete previous chart lines
		while (ds.getSeriesCount()>0) {
			ds.removeSeries(ds.getSeriesKey(ds.getSeriesCount()-1));
		}
		
		// generate chart lines
		double[][] series = Arrays.stream(data).map(it -> Arrays.stream(it).asDoubleStream().toArray() ).toArray(double[][]::new);
		ds.addSeries(getSeriesLabel(0), new double[][] { series[0], series[1] });
		ds.addSeries(getSeriesLabel(1), new double[][] { series[0], series[2] });
	}


	
}

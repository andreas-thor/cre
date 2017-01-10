package cre.test.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Optional;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;

import cre.test.data.CRTable;
import cre.test.data.CRType;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ChartPanelFactory {

	
	
	public static ChartViewer create(CRTable crTable, TableView<CRType> tab, TableColumn<CRType, Number> colRPY, TableColumn<CRType, Number> colN_CR) {

		JFreeChart chart = ChartFactory.createXYLineChart("", "Cited Reference Year", "Cited References", crTable.ds);

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
		rend.setToolTipGenerator(new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				return crTable.getTooltip(dataset.getX(series, item).intValue());
			}
		});
		
		// update table when zoom changes in chart
		plot.addChangeListener(new PlotChangeListener() {
			
			@Override
			public void plotChanged(PlotChangeEvent pcevent) {
				if (! crTable.duringUpdate) {	// ignore updates during data update
					crTable.filterByYear ((int)Math.ceil(dAxis.getLowerBound()), (int)Math.floor(dAxis.getUpperBound()));
				}
			}
		});
		

		ChartViewer chView = new ChartViewer(chart);
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
					
					/* sort by year ASC, n_cr desc */
					Platform.runLater( new Runnable() {
						@Override
						public void run() {
							colRPY.setSortType(TableColumn.SortType.ASCENDING);
							colN_CR.setSortType(TableColumn.SortType.DESCENDING);
							tab.getSortOrder().clear();
							tab.getSortOrder().add(colRPY);
							tab.getSortOrder().add(colN_CR);
							Optional<CRType> first = tab.getItems().stream().filter(cr -> cr.getRPY() == year).findFirst();
							tab.getSelectionModel().select(first.get());
							tab.scrollTo(first.get());
						}
					}); 
				}	
				
			}
		});

		return chView;

	}
}

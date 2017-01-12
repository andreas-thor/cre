package cre.test.ui 


import groovy.transform.CompileStatic

import java.awt.BasicStroke
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.text.DecimalFormat

import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.filechooser.FileFilter

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartMouseEvent
import org.jfree.chart.ChartMouseListener
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.block.BlockBorder
import org.jfree.chart.entity.XYItemEntity
import org.jfree.chart.event.PlotChangeEvent
import org.jfree.chart.event.PlotChangeListener
import org.jfree.chart.labels.XYToolTipGenerator
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYSplineRenderer
import org.jfree.data.xy.XYDataset

import cre.test.data.CRTable



/**
 * Factory class to create panel with chart 
 * @author thor
 *
 */
@CompileStatic
class UIChartPanelFactory {

	
	static ChartPanel create(CRTable crTable, JTable tab, JMenuItem saveAsCSV) {

		JFreeChart chart = ChartFactory.createXYLineChart("", "Cited Reference Year", "Cited References", crTable.ds )
		
		chart.getLegend().setFrame(BlockBorder.NONE)
		chart.getXYPlot().with {
		
			setRangeZeroBaselineVisible(true)
			setRangeZeroBaselinePaint(Color.black)
			setRangeZeroBaselineStroke(new BasicStroke(2))
			
			// domain=year -> show year number always with 4 digits
			setRenderer(new XYSplineRenderer())
			((NumberAxis) getDomainAxis()).with {
				setNumberFormatOverride(new DecimalFormat("0000.#"))
			}
		
			// general chart layout
			getRenderer().getPlot().with {
				setBackgroundPaint(Color.white)
				setOutlinePaint(Color.black)
				setDomainGridlinePaint(Color.gray)
				setRangeGridlinePaint(Color.gray)
			}
		
			// layout for data rows
			getRenderer().with {
				double shapeSize = 0	// 6
				float strokeSize = 1	// 3
				setSeriesShape(0, new Rectangle2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize))
				setSeriesStroke(0, new BasicStroke(strokeSize))
				setSeriesShape(1, new Ellipse2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize))
				setSeriesStroke(1, new BasicStroke(strokeSize))
			}
			
			
			// tooltip = CR year with sum of all NCR of this year
			getRenderer().setToolTipGenerator([ generateToolTip: { XYDataset dataset, int series, int item ->
				return crTable.getTooltip(dataset.getX(series, item).intValue())
			}] as XYToolTipGenerator)
			
			
			// update table when zoom changes in chart
			addChangeListener([plotChanged: { PlotChangeEvent pcevent ->
				if (! crTable.duringUpdate) {	// ignore updates during data update
					((XYPlot)pcevent.getPlot()).getDomainAxis().with { crTable.filterByYear ((int)getLowerBound(), (int)getUpperBound()) }
					((AbstractTableModel)tab.getModel()).fireTableDataChanged()	// automatically triggers UI refresh
//					crTable.stat.setValue("", crTable.getInfoString())
//					sb.tmodel.fireTableDataChanged()
				}
			}] as PlotChangeListener )
		
		}
		 
		
		ChartPanel chpan = new ChartPanel( chart)
		
		// vertical blue line at mouse position
//		chpan.setHorizontalAxisTrace(true)
		
		/* on click event: get CR year, sort and jump to first row of the year */
		chpan.addChartMouseListener([
			chartMouseClicked: { ChartMouseEvent cmevent ->
				if (cmevent.getEntity() instanceof XYItemEntity) {
		
					/* get year (domain value) of clicked data item */
					XYItemEntity a = (XYItemEntity) cmevent.getEntity()
					int year = a.getDataset().getX(a.getSeriesIndex(), a.getItem()).intValue()
					
					/* sort by year ASC, n_cr desc */
					tab.getRowSorter().setSortKeys([new RowSorter.SortKey (TableFactory.columns['RPY'], SortOrder.ASCENDING), new RowSorter.SortKey (TableFactory.columns['N_CR'], SortOrder.DESCENDING)])
					
					/* find first row of the selected year; select and scroll to make it visible */
					int firstRow=0
					while (tab.getValueAt(firstRow, TableFactory.columns['RPY']) != year) { firstRow++ }
					tab.setRowSelectionInterval(firstRow, firstRow)
					tab.scrollRectToVisible(tab.getCellRect(firstRow,0, true))
					
				}
			},
			chartMouseMoved: { ChartMouseEvent cmevent ->
			}] as ChartMouseListener)
		
		

		/*
		 * Adjust "Save As"	popup menu
		 * remove "save as -> pdf" menu item because it prints "Evaluation version of OrsonPDF" on the pdf 
		 * add "save as -> csv"
		 */
		JPopupMenu saveAs = (JPopupMenu) chpan.getPopupMenu().getSubElements()[2].getSubElements()[0]
		saveAs.remove (2)
		saveAs.add(saveAsCSV) 
		
		return chpan
		
	}
	
}

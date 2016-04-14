package cre.ui 

import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic

import java.awt.Color
import java.awt.Component
import java.awt.Dimension;
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.text.DecimalFormat

import javax.swing.*
import javax.swing.RowFilter.ComparisonType
import javax.swing.event.RowSorterEvent
import javax.swing.event.RowSorterListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.JTableHeader
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter

import cre.data.CRCluster;
import cre.data.CRTable;
import cre.data.CRType;


/**
 * Factory class for UI table 
 * @author thor
 */

class TableFactory {

	private static String clickedCol 
	
	public static final Map<String, Integer> columns = ['VI':0, 'CO':1, 'RPY':4, 'N_CR':5, 'PERC_YR':6, 'PERC_ALL':7]
	private static DecimalFormat formatter = new DecimalFormat( "##0.0000%" );
	
	private static JTable init(CRTable crTable) {
		JTable tab = new SwingBuilder().table (id:'tab') {
			tableModel(  id:'tmodel',  list :  crTable.crData  ) {
	
				// first two columns are ALWAYS invisible; used to specify what rows are displayed (VI) and what background color they have (CO)
				propertyColumn(header:'VI', 					propertyName:'VI', 			type: Integer, maxWidth:0 , minWidth:0, preferredWidth:0, editable: false)
				propertyColumn(header:'CO', 					propertyName:'CO', 			type: Integer, maxWidth:0 , minWidth:0, preferredWidth:0, editable: false)
	
				propertyColumn(header:CRType.attr.ID,  			propertyName:'ID', 			type: Integer, 	editable: false)
				propertyColumn(header:CRType.attr.CR,  			propertyName:'CR', 			type: String, 	editable: false)
				propertyColumn(header:CRType.attr.RPY, 			propertyName:'RPY',			type: Integer, 	editable: false)
				propertyColumn(header:CRType.attr.N_CR, 		propertyName:'N_CR', 		type: Integer, 	editable: false)
				propertyColumn(header:CRType.attr.PERC_YR,		propertyName:'PERC_YR', 	type: Double, 	editable: false)
				propertyColumn(header:CRType.attr.PERC_ALL, 	propertyName:'PERC_ALL', 	type: Double, 	editable: false)
				propertyColumn(header:CRType.attr.AU, 			propertyName:'AU', 			type: String, 	editable: false)
				propertyColumn(header:CRType.attr.AU_L, 		propertyName:'AU_L', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.AU_F, 		propertyName:'AU_F', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.AU_A, 		propertyName:'AU_A', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.TI, 			propertyName:'TI',	 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.J, 			propertyName:'J', 			type: String, 	editable: false)
				propertyColumn(header:CRType.attr.J_N, 			propertyName:'J_N', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.J_S, 			propertyName:'J_S', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.VOL, 			propertyName:'VOL', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.PAG, 			propertyName:'PAG', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.DOI, 			propertyName:'DOI', 		type: String, 	editable: false)
				propertyColumn(header:CRType.attr.CID2, 		propertyName:'CID2', 		type: CRCluster,editable: false)
				propertyColumn(header:CRType.attr.CID_S, 		propertyName:'CID_S', 		type: Integer, 	editable: false)
				
			}
		}
		
		
		
		
		return tab
	}
	
	

	
	
	@CompileStatic
	static JTable create(CRTable crTable) {
			
		JTable tab = init(crTable)
		tab.getTableHeader().setVisible(true)
		tab.getTableHeader().setReorderingAllowed(false)

		/* show only visible rows (VI property == 1) */
		TableRowSorter tsort = new TableRowSorter<TableModel>(tab.getModel())
		tsort.setRowFilter(RowFilter.numberFilter(ComparisonType.EQUAL, 1, columns['VI'].intValue()))	// VI column defines visibility
		tab.setRowSorter(tsort);

		
		/* Recompute background color for all visible rows after re-sorting
		 * Alternating background color based on groups of equivalent values */		
		tsort.addRowSorterListener(new RowSorterListener() {
			public void sorterChanged(RowSorterEvent e) {
		
				if (e.type != RowSorterEvent.Type.SORTED) return
				
				int sortColumn = (tab.getRowSorter().getSortKeys().size()>0) ? tab.getRowSorter().getSortKeys().get(0).getColumn() : -1
				int currColor = 0;
				
				for (int r=0; r<tsort.getViewRowCount(); r++) {
					if ((r>0) && (tab.getModel().getValueAt(tab.convertRowIndexToModel(r-1), sortColumn) != tab.getModel().getValueAt(tab.convertRowIndexToModel(r), sortColumn)) ) {
						currColor = 1-currColor
					}
					crTable.crData[tab.convertRowIndexToModel(r)].CO = currColor
				}
			}
		})
		
		
		/* Set background color based on "CO" property 
		 * Format percent values */
		DefaultTableCellRenderer groupRowRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				if (((column==columns['PERC_YR']) || (column==columns['PERC_ALL'])) && (value instanceof Double)) {
					value = formatter.format((Number)value);
				}
				
				Color gColor = ((TableModel)table.getModel()).getValueAt(table.convertRowIndexToModel(row), columns['CO']) == 0 ? UIManager.getColor("Table.background") : Color.LIGHT_GRAY
				final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				c.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : gColor);
//				c.setForeground(isSelected ? UIManager.getColor("Table.selectionForeground") : UIManager.getColor("Table.foreground"))
				return c;
			}
		}


		tab.setDefaultRenderer(String.class, groupRowRenderer)
		tab.setDefaultRenderer(Integer.class, groupRowRenderer)
		tab.setDefaultRenderer(Double.class, groupRowRenderer)
		tab.setDefaultRenderer(Long.class, groupRowRenderer)
		tab.setDefaultRenderer(CRCluster.class, groupRowRenderer)
//		tab.getRowSorter().setSortKeys([new RowSorter.SortKey (0, SortOrder.ASCENDING)])
//		tab.getColumnModel().getColumn(6).setCellRenderer(new PercentFormatRenderer());
		
		return tab	
	}
	
}

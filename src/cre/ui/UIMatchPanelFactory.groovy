package cre.ui 

import groovy.swing.SwingBuilder

import java.awt.event.ItemEvent
import java.awt.event.ItemListener

import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.table.AbstractTableModel

import cre.data.*
import cre.ui.UIBind.UIMatchConfig


/**
 * Factory class for match panel
 * @author thor
 *
 */
class UIMatchPanelFactory {

	public static int matchSame = 0
	public static int matchDifferent = 1
	public static int matchExtract = 2
	public static int matchUndo = 3
	
	
	
	
	public static JPanel create (UIMatchConfig uiMC, CRMatch crMatch, JTable tab, StatusBar stat ) {
		
		JSlider framesPerSecond
		List useBoxes = [null, null, null]
		
		
		// TODO: manual in eigenen Thread
		Closure manualButtonAction = { int matchType -> 
			
			if (matchType == matchUndo) {
				crMatch.matchUndo(uiMC.threshold, uiMC.useVol, uiMC.usePag, uiMC.useDOI)
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
			} else {	// matchSame/Different/Extract
				if (tab.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "No Cited References selected")
				} else {
					crMatch.matchManual (tab.getSelectedRows().collect { tab.convertRowIndexToModel (it) }, matchType, uiMC.threshold, uiMC.useVol, uiMC.usePag, uiMC.useDOI)
					(tab.getModel() as AbstractTableModel).fireTableDataChanged()
				}
			}
		}
		
		
		
		SwingBuilder sb = new SwingBuilder()
		JPanel match = sb.panel(border: BorderFactory.createEmptyBorder(0, 10, 0, 10)) {
			
			tableLayout(id:'m', cellpadding:10) {
				
				tr {
					td (align:'left') {
						framesPerSecond = slider(id:'threshSlider', minimum: 50, maximum:100,  value:75, 
                        orientation: SwingConstants.HORIZONTAL, 
                        paintLabels:true,
                        paintTicks:true,
                        majorTickSpacing: 10,
                        minorTickSpacing: 1,
                        snapToTicks:true,
                        paintTrack:true)
					}
					td (align:'left') {
						tableLayout(cellpadding:0) { 
							tr { 
								td { useBoxes[0] = checkBox(text: 'Volume', selected: bind ('useVol', source:uiMC, mutual:true)) } 
								td { useBoxes[1] = checkBox(text: 'Page', selected: bind ('usePag', source:uiMC, mutual:true)) }
							}
 							tr { td { useBoxes[2] = checkBox(text: 'DOI', selected: bind ('useDOI', source:uiMC, mutual:true)) } }
						}
					}
					
					
					td (align:'right') {
						button(preferredSize:[100, 25], text:'Same', actionPerformed: { manualButtonAction (matchSame) })
					}
					td (align:'right') {
						button(preferredSize:[100, 25], text:'Different', actionPerformed: { manualButtonAction (matchDifferent) })
					}
					td (align:'right') {
						button(preferredSize:[100, 25], text:'Extract', actionPerformed: { manualButtonAction (matchExtract) })
					}
					td (align:'right') {
						button(preferredSize:[100, 25], text:'Undo', actionPerformed: { manualButtonAction (matchUndo) })
					}
				}
			}

		}
		
		Runnable runnable = new Runnable() {
			public void run() {
//				try {
					crMatch.updateClusterId(framesPerSecond.getValue()/100.0, true, uiMC.useVol, uiMC.usePag, uiMC.useDOI)
					(tab.getModel() as AbstractTableModel).fireTableDataChanged()
//					tab.getRowSorter().setSortKeys (null)
//					tab.getRowSorter().setSortKeys([new RowSorter.SortKey (17, SortOrder.ASCENDING)])
//					tab.repaint()
//				} catch (Exception intex) {
//					println "Exception"
//					println intex
//				}
			 }
		}
		Thread t = new Thread(runnable)
		
		
		useBoxes.each { it.addItemListener([itemStateChanged: { ItemEvent e -> match.updateClustering() } ] as ItemListener) }
			
		framesPerSecond.addChangeListener([stateChanged: { ChangeEvent e ->
			
			JSlider source = (JSlider)e.getSource()
			if (!source.getValueIsAdjusting()) match.updateClustering()
			
			} ] as ChangeListener)
		
		match.visible = false
		
		
		match.metaClass.updateClustering = {
			t.interrupt()
			t = new Thread(runnable)
			t.start()
		}
		
		return match
		
	} 
}

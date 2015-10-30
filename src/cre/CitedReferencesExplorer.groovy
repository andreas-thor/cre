package cre 

/**
 * UPDATES 10/20/2015
 * Some classes are annotated as @CompileStatic for performance improvements
 * Generated ant file and jnlp file for web starter deployment
 * Removed matcher dialog
 * Dialog windows centered relative to main window 
 * 
 * UPDATES 10/15/2015
 * 
 * Window position after start: center
 * Name changed to CitedReferencesExplorer
 * Dialog to save data before opening WoS / CSV file
 * Hide clustering buttons after loading
 * Remove vertical blue line at mouse position
 * Clustering starts immediately after clicking "Cluster equivalent ..." (no dialog)
 * Renaming: "source" for "medium" and "source title" for "title" 
 * Make clustering insensitive to lower / upper case
 *  
 * Explanation: Each ClusterId has two components x/y. 
 * If two CRs have the same clusterId (x/y) they are considered the same. 
 * If two CRs share the first component but have different second components (i.e., x/y' and x/y'') they are somewhat similar but no the same. 
 * In other words: The first component x reflects the result of a coarse-grained clustering.
 * When sorting by clusterId, CRs with different clusterIds sharing the same first component are close together which is helpful for manual inspection.
 *
 *
 */

import groovy.swing.SwingBuilder

import java.awt.*
import java.util.prefs.Preferences

import javax.swing.*
import javax.swing.filechooser.FileFilter
import javax.swing.table.AbstractTableModel

import org.jfree.chart.ChartPanel

import cre.CRTable.AbortedException
import cre.CRTable.FileTooLargeException


UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
Locale.setDefault(new Locale("en"))


SwingBuilder sb = new groovy.swing.SwingBuilder()
UIStatusBar stat = new UIStatusBar()
CRTable crTable = new CRTable(stat)
JTable tab = UITableFactory.create(crTable)
ChartPanel chpan = UIChartPanelFactory.create(crTable, tab)
UISettings uisetting
UIBind uibind = new UIBind()

JPanel matchpan = UIMatchPanelFactory.create(uibind.uiMatchConfig, crTable, tab, stat)
JFrame mainFrame



mainFrame = sb.frame(
	title:"CitedReferencesExplorer (Version 2015/10/20)",  
	size:[800,600],
	windowClosing: { sb.menuExit.doClick() },
	defaultCloseOperation:JFrame.DO_NOTHING_ON_CLOSE  // WindowConstants.EXIT_ON_CLOSE
	) {
		
	menuBar{ 
		menu(text: "File", mnemonic: 'F') {
			
			menuItem(text: "Import WoS files...", mnemonic: 'I', accelerator: KeyStroke.getKeyStroke("ctrl I"), actionPerformed: {
				
				if (crTable.crData.size()>0) {
					int answer = JOptionPane.showConfirmDialog (null, "Save changes before opening WoS files?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
					if (answer == JOptionPane.YES_OPTION) sb.menuSave.doClick();
				}

				JFileChooser dlg = new JFileChooser(dialogTitle: "Import WoS files", multiSelectionEnabled: true, fileSelectionMode: JFileChooser.FILES_ONLY)
				dlg.setCurrentDirectory(uisetting.getLastDirectory())
				
				if (dlg.showOpenDialog()==JFileChooser.APPROVE_OPTION) {
					
					JDialog wait = UIDialogFactory.createWaitDlg(mainFrame, { crTable.abort = true })
					Runnable runnable = new Runnable() { 
						public void run() {
							matchpan.visible = false
							
							try {
								crTable.loadDataFiles (dlg.getSelectedFiles(), uisetting.getMaxCR())
								wait.dispose()
							} catch (FileTooLargeException e1) {
								wait.dispose()
								JOptionPane.showMessageDialog(null, "WoS file is too large; imported ${e1.numberOfCRs} CRs only.\nYou can change the maximum number of CRs for import in the File > Settings menu." );
							} catch (AbortedException e2) {
								wait.dispose()
							} catch (OutOfMemoryError mem) {
								wait.dispose()
								crTable.init()
								JOptionPane.showMessageDialog(null, "Out Of Memory Errror" );
							} catch (Exception e3) {
								wait.dispose()
								JOptionPane.showMessageDialog(null, "Error while loading WoS file.\n(${e3.toString()})" );
							}
							
							UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
							(tab.getModel() as AbstractTableModel).fireTableDataChanged()
							uisetting.setLastDirectory(dlg.getSelectedFiles()[0].getParentFile())
						 }
					}
					
					Thread t = new Thread(runnable)
					t.start()
					wait.visible = true
					
					
				}
			})
			
			menuItem(text: "Open  CSV file...", mnemonic: 'O', accelerator: KeyStroke.getKeyStroke("ctrl O"), actionPerformed: {
				
				if (crTable.crData.size()>0) {
					int answer = JOptionPane.showConfirmDialog (null, "Save changes before opening another CSV file?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
					if (answer == JOptionPane.YES_OPTION) sb.menuSave.doClick();
				}
				
				JFileChooser dlg = new JFileChooser(dialogTitle: "Open CSV files", multiSelectionEnabled: false, fileSelectionMode: JFileChooser.FILES_ONLY)
				dlg.setFileFilter([getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*?\.csv/ || f.isDirectory() }] as FileFilter)
				dlg.setCurrentDirectory(uisetting.getLastDirectory())
				
				if (dlg.showOpenDialog()==JFileChooser.APPROVE_OPTION) {

					JDialog wait = UIDialogFactory.createWaitDlg(mainFrame, { crTable.abort = true })
					
					Runnable runnable = new Runnable() {
						public void run() {
							matchpan.visible = false
							try {
								crTable.loadCSV(dlg.getSelectedFile())
								wait.dispose()
							} catch (AbortedException e) { 
								wait.dispose()
							}
							UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
							(tab.getModel() as AbstractTableModel).fireTableDataChanged()
							uisetting.setLastDirectory(dlg.getSelectedFile().getParentFile())
						}
					}
					Thread t = new Thread(runnable)
					t.start()
					wait.visible = true

				}
			})
			
			menuItem(id: "menuSave", text: "Save as CSV file...", mnemonic: 'S', accelerator: KeyStroke.getKeyStroke("ctrl S"), actionPerformed: {
				JFileChooser dlg = new JFileChooser(dialogTitle: "Save as CSV file", multiSelectionEnabled: false, fileSelectionMode: JFileChooser.FILES_ONLY)
				dlg.setFileFilter([getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*?\.csv/ || f.isDirectory() }] as FileFilter)
				dlg.setCurrentDirectory(uisetting.getLastDirectory())

				if (dlg.showSaveDialog() ==JFileChooser.APPROVE_OPTION) {
					
					Runnable runnable = new Runnable() {
						public void run() {
							crTable.save2CSV (dlg.getSelectedFile())
							uisetting.setLastDirectory(dlg.getSelectedFile().getParentFile())
						}
					}
					Thread t = new Thread(runnable)
					t.start()

					
				}
			})

			separator()
			
			menuItem(id:'settingsDlg', text: "Settings...", mnemonic: 'T', actionPerformed: {
				UIDialogFactory.createSettingsDlg(mainFrame, 
					uisetting.getAttributes(), uisetting.getLines(), uisetting.getMaxCR(), 
					{ byte[] attributes, byte[] lines, int maxCR -> 
						uisetting.setAttributes (attributes)
						uisetting.setLines (lines)
						uisetting.setMaxCR (maxCR)
					}
				).visible = true
			})
			
			separator()
			
			menuItem(id: "menuExit", text: "Exit", mnemonic: 'X', accelerator: KeyStroke.getKeyStroke("alt F4"), actionPerformed: { 
				int answer = JOptionPane.showConfirmDialog (null, "Save changes before exit?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION) 
				switch (answer) {
					case JOptionPane.YES_OPTION: sb.menuSave.doClick(); 
					case JOptionPane.NO_OPTION: System.exit(0); 
				}
			})
		}
		
		menu(text: "Data", mnemonic: 'D') {
			
			menuItem(id:'infoDlg', text: "Info", mnemonic: 'I', actionPerformed: {
					UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
			})

			
			menuItem(text: "Filter by Cited Reference Year ...", mnemonic: 'Y', actionPerformed: {
				UIDialogFactory.createIntRangeDlg(mainFrame, uisetting.uibind[2], "Filter by Cited Reference Year", "Cited Reference Years", crTable.getMaxRangeYear(), { min, max -> chpan.getChart().getXYPlot()?.getDomainAxis()?.setRange(min-0.5, max+0.5) }).visible = true
			})

			separator()
			
			
			menuItem(text: "Remove selected Cited References...", mnemonic: 'S', actionPerformed: {

				if (tab.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "No Cited References selected");
				} else {
					if (JOptionPane.showConfirmDialog (null, "Would you like to remove the selected ${tab.getSelectedRowCount()} Cited References?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						crTable.remove (tab.getSelectedRows().collect { tab.convertRowIndexToModel (it) })
						(tab.getModel() as AbstractTableModel).fireTableDataChanged()
					}
				}
			})
			
			menuItem(text: "Remove by Cited Reference Year ...", mnemonic: 'Y', actionPerformed: {
				UIDialogFactory.createIntRangeDlg(mainFrame, uibind.uiRanges[0], "Remove by Cited Reference Year", "Cited Reference Years", crTable.getMaxRangeYear(), { min, max -> crTable.removeByYear(min, max) }).visible = true
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
			})
			
			menuItem(text: "Remove by Number of cited references...", mnemonic: 'N', actionPerformed: {
				UIDialogFactory.createIntRangeDlg(mainFrame, uibind.uiRanges[1], "Remove by Number of cited references", "Number of Cited References", crTable.getMaxRangeNCR(), { min, max -> crTable.removeByNCR(min, max) }).visible = true
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
			})

			separator()
			
			menuItem(text: "Cluster equivalent Cited References...", mnemonic: 'C', actionPerformed: {
					
				Runnable runnable = new Runnable() {
					public void run() {
						crTable.doBlocking()
						matchpan.visible = true
						matchpan.updateClustering() 
					 }
				}
				Thread t = new Thread(runnable)
				t.start()
					
			})
			
			menuItem(text: "Merge Cited References of the same cluster...", mnemonic: 'M', actionPerformed: {
				
				long toDelete = crTable.crData.size()-crTable.clusterId2Objects.keySet().size()
				if (toDelete == 0) {
					JOptionPane.showMessageDialog(null, "No Clusters to merge!");
				} else {
					if (JOptionPane.showConfirmDialog (null, "Merging will aggregate ${toDelete} Cited References! Are you sure?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						
						Runnable runnable = new Runnable() {
							public void run() {
								crTable.merge ()
								(tab.getModel() as AbstractTableModel).fireTableDataChanged()
							}
						}
						Thread t = new Thread(runnable)
						t.start()
	
					}
				}
			})

		}
//		menu(text: "View", mnemonic: 'V') {
//			
//			menuItem(text: "Filter by Cited Reference Year ...", mnemonic: 'Y', actionPerformed: {
//				UIDialogFactory.createIntRangeDlg(mainFrame, uibind.uiRanges[2], "Filter by Cited Reference Year", "Cited Reference Years", crTable.getMaxRangeYear(), { min, max -> chpan.getChart().getXYPlot()?.getDomainAxis()?.setRange(min-0.5, max+0.5) }).visible = true
//			})
//
//			separator()
//			
//			menuItem(id:'infoDlg', text: "Info", mnemonic: 'I', actionPerformed: {
//					UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
//			})
//
//			menuItem(text: "Show columns...", mnemonic: 'C', actionPerformed: {
//				
//				UIDialogFactory.createShowColDlg(mainFrame, uibind.showCol, "Show columns", CRType.attr, { 
//					
//					int prefSize = tab.getWidth()/(CRType.attr.inject (0) { res, name, label -> res += uibind.showCol."$name" ? 1 : 0 })
//					
//					(2..tab.getTableHeader().getColumnModel().getColumnCount()-1).each {  // 2 = offset to ignore first two columns (VI, CO)
//						tab.getTableHeader().getColumnModel().getColumn(it).with {
//							if (uibind.showCol."${getIdentifier()}") {
//								setMaxWidth(800)
//								setMinWidth(30)
//								setPreferredWidth(prefSize)
//								setResizable(true)
//							} else {
//								setMaxWidth(0)
//								setMinWidth(0)
//								setPreferredWidth(0)
//								setResizable(false)
//							}
//						}
//					}
//				}).visible = true
//				
//			})
//			
//			menuItem(text: "Show lines in graph...", mnemonic: 'L', actionPerformed: {
//				
//				UIDialogFactory.createShowColDlg(mainFrame, uibind.showCol, "Show lines in graph", crTable.line, {
//					
//					chpan.getChart().getXYPlot().getRenderer().with {
//						crTable.line.eachWithIndex { name, label, idx ->
//							setSeriesVisible (idx, new Boolean (uibind.showCol."${name}"))
//						}
//					}
//					
//				}).visible = true
//				
//			})
//			
//		}
	}	
	
	

	
	
	panel() {

		gridBagLayout()
		widget( new JSplitPane(),
			id:'mainSplitPane',
			constraints: gbc(gridx:0,gridy:0,fill:java.awt.GridBagConstraints.BOTH,anchor:java.awt.GridBagConstraints.NORTHWEST,weightx:1, weighty:1),
			dividerLocation: 500,
			leftComponent:   chpan ,
			rightComponent: panel() {
		
				gridBagLayout()
				widget ( matchpan , 
					constraints: gbc(gridx:0,gridy:0,fill:java.awt.GridBagConstraints.BOTH,anchor:java.awt.GridBagConstraints.NORTHWEST,weightx:1, weighty:0)
					
				)
				widget (new JScrollPane(tab), 
					constraints: gbc(gridx:0,gridy:1,fill:java.awt.GridBagConstraints.BOTH,anchor:java.awt.GridBagConstraints.NORTHWEST,weightx:1, weighty:1)
					
				)
			}
			
		)
		
		widget (
			constraints: gbc(gridx:0,gridy:1,fill:java.awt.GridBagConstraints.HORIZONTAL,anchor:java.awt.GridBagConstraints.NORTHWEST,weightx:1, weighty:0),
			stat.pan
			
		)
	}
		
		
	
}
	
mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("CRE32.png"));
mainFrame.pack()
mainFrame.setLocationRelativeTo(null)
uisetting = new UISettings(tab, chpan)
mainFrame.visible = true



	


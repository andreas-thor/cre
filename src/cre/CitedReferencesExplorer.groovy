package cre

import groovy.swing.SwingBuilder

import java.awt.Toolkit
import java.awt.event.KeyEvent

import javax.swing.*
import javax.swing.filechooser.FileFilter
import javax.swing.table.AbstractTableModel

import org.jfree.chart.ChartPanel

import cre.Exceptions.AbortedException
import cre.Exceptions.FileTooLargeException
import cre.Exceptions.UnsupportedFileFormatException
import cre.data.CRTable
import cre.data.CRTable.*
import cre.data.source.*
import cre.ui.StatusBar
import cre.ui.TableFactory
import cre.ui.UIBind
import cre.ui.UIChartPanelFactory
import cre.ui.UIDialogFactory
import cre.ui.UIMatchPanelFactory
import cre.ui.UISettings


UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
Locale.setDefault(new Locale("en"))


SwingBuilder sb = new groovy.swing.SwingBuilder()
StatusBar stat = new StatusBar()
CRTable crTable = new CRTable(stat)
JTable tab = TableFactory.create(crTable)
UISettings uisetting
UIBind uibind = new UIBind()

JPanel matchpan = UIMatchPanelFactory.create(uibind.uiMatchConfig, crTable.crMatch, tab, stat)
JFrame mainFrame



 
/* Workflow for saving / exporting files */
Closure doExportFile = { String source, String dlgTitle, FileFilter filter ->
	JFileChooser dlg = new JFileChooser(dialogTitle: dlgTitle, multiSelectionEnabled: false, fileSelectionMode: JFileChooser.FILES_ONLY)
	dlg.setFileFilter(filter)
	dlg.setCurrentDirectory(uisetting.getLastDirectory())

	int answer = JOptionPane.NO_OPTION
	while (answer == JOptionPane.NO_OPTION) {
		
		if (dlg.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
			
			answer = JOptionPane.YES_OPTION
			if (dlg.getSelectedFile().exists()) {
				answer = JOptionPane.showConfirmDialog (null, "File exists! Overwrite?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
			}
			if (answer == JOptionPane.YES_OPTION) {
				Runnable runnable = new Runnable() {
					public void run() {
						try {
							switch (source) {
								case "CRE_json": CRE_json.save(dlg.getSelectedFile(), crTable, stat); break;
								case "WoS_txt": WoS_txt.save(dlg.getSelectedFile(), crTable, stat); break;
								case "Scopus_csv": Scopus_csv.save(dlg.getSelectedFile(), crTable, stat); break;
								case "CRE_csv": CRE_csv.save(dlg.getSelectedFile(), crTable, stat); break;
								case "CRE_csv_Graph": CRE_csv.saveGraph(dlg.getSelectedFile(), crTable, stat); break;
								case "CRE_csv_Ruediger": CRE_csv.saveRuediger(dlg.getSelectedFile(), crTable, stat); break;
								default: JOptionPane.showMessageDialog(null, "Unknown file format." );
							}
							uisetting.setLastDirectory(dlg.getSelectedFile().getParentFile())
						} catch (Exception e) {
							e.printStackTrace()
							JOptionPane.showMessageDialog(null, "Error while saving file.\n(${e.toString()})" );
						}
					}
				}
				Thread t = new Thread(runnable)
				t.start()
			}
		} else {
			break
		}
	}
}

/* Workflow for loading / importing files */
Closure doImportFiles = { String source, String dlgTitle, boolean multipleFiles, FileFilter filter ->
	
	if (crTable.crData.size()>0) {
		int answer = JOptionPane.showConfirmDialog (null, "Save changes before opening another file?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
		if (answer == JOptionPane.CANCEL_OPTION) return
		if (answer == JOptionPane.YES_OPTION) { 
			doExportFile (
				"CRE_csv", "Export CRE file",
				[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*\.csv/ || f.isDirectory() }] as FileFilter)
		}
	}
	
	JFileChooser dlg = new JFileChooser(dialogTitle: dlgTitle, multiSelectionEnabled: multipleFiles, fileSelectionMode: JFileChooser.FILES_ONLY)
	dlg.setFileFilter(filter)
	
	dlg.setCurrentDirectory(uisetting.getLastDirectory())
	
	if (dlg.showOpenDialog()==JFileChooser.APPROVE_OPTION) {
		
		JDialog wait = UIDialogFactory.createWaitDlg(mainFrame, { crTable.abort = true })
		
		Runnable runnable = new Runnable() {
			public void run() {
				matchpan.visible = false
				
				try { 
					switch (source) {
						case "WoS_txt": FileImportExport.load(crTable, stat, source, dlg.getSelectedFiles(), uisetting.getMaxCR(), uisetting.getYearRange()); break;
						case "Scopus_csv": FileImportExport.load(crTable, stat, source, dlg.getSelectedFiles(), uisetting.getMaxCR(), uisetting.getYearRange()); break;
						case "CRE_csv": CRE_csv.load (dlg.getSelectedFile(), crTable, stat); break;
						default: JOptionPane.showMessageDialog(null, "Unknown file format." );
					}
					wait.dispose()
				} catch (FileTooLargeException e1) {
					wait.dispose()
					JOptionPane.showMessageDialog(null, "You try to import too many cited references.\nImport was aborted after loading ${e1.numberOfCRs} Cited References.\nYou can change the maximum number in the File > Settings > Miscellaneous menu. " );
				} catch (UnsupportedFileFormatException e4) {
					wait.dispose()
					JOptionPane.showMessageDialog(null, "Unknown file format." );
				} catch (AbortedException e2) {
					wait.dispose()
				} catch (OutOfMemoryError mem) {
					wait.dispose()
					crTable.init()
					JOptionPane.showMessageDialog(null, "Out Of Memory Error" );
				} catch (Exception e3) {
					wait.dispose()
					e3.printStackTrace()
					JOptionPane.showMessageDialog(null, "Error while loading file.\n(${e3.toString()})" );
				}
				
				sb.showNull.selected = true
				UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
				uisetting.setLastDirectory((multipleFiles ? dlg.getSelectedFiles()[0] : dlg.getSelectedFile()).getParentFile() )
			 }
		}
		
		Thread t = new Thread(runnable)
		t.start()
		wait.visible = true
	}
	
}



ChartPanel chpan = UIChartPanelFactory.create(crTable, tab,
	sb.menuItem(id: "menuSave", text: "CSV...", actionPerformed: {
		doExportFile (
			"CRE_csv_Graph", "Export Graph Data as CSV",
			[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*\.csv/ || f.isDirectory() }] as FileFilter)
	})
)




mainFrame = sb.frame(
	title:"CRExplorer (CitedReferencesExplorer by Andreas Thor et al., Version 2016/05/27 **DEV**++)",  
	size:[800,600],
	windowClosing: { sb.menuExit.doClick() },
	defaultCloseOperation:JFrame.DO_NOTHING_ON_CLOSE  // WindowConstants.EXIT_ON_CLOSE
	) {
		
	menuBar{ 
		menu(text: "File", mnemonic: 'F') {
			
			menu(text: "Import") {
				menuItem(text: "Web of Science...", mnemonic: 'W', actionPerformed: {
					doImportFiles (
						"WoS_txt", "Import Web of Science files", true,
						[getDescription: {"TXT files (*.txt)"}, accept:{File f -> f ==~ ".*\\.txt\$" || f.isDirectory() }] as FileFilter)					
				})
				menuItem(text: "Scopus...", mnemonic: 'S', actionPerformed: {
					doImportFiles (
						"Scopus_csv", "Import Scopus files", true, 
						[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ ".*\\.csv\$" || f.isDirectory() }] as FileFilter)
				})
			}
			
//			menuItem(text: "Open...", mnemonic: 'O', accelerator: KeyStroke.getKeyStroke("ctrl O"), actionPerformed: {
//				doImportFiles (
//					"CRE_json", "Open CRE file", false, 
//					[getDescription: {"CRE files (*.cre)"}, accept:{File f -> f ==~ /.*?\.cre/ || f.isDirectory() }] as FileFilter)
//			})

			menuItem(text: "DEPRECATED Open CSV ...", actionPerformed: {
				doImportFiles (
					"CRE_csv", "Open CSV file", false,
					[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*?\.csv/ || f.isDirectory() }] as FileFilter)
			})

			separator()
			
			
			menu(text: "Export") {
				menuItem(id: "menuSaveWoS", text: "Web of Science...", mnemonic: 'W', actionPerformed: {
					doExportFile (
						"WoS_txt", "Export Web of Science file",
						[getDescription: {"TXT files (*.txt)"}, accept:{File f -> f ==~ /.*\.txt/ || f.isDirectory() }] as FileFilter)
				})
				menuItem(id: "menuSaveScopus", text: "Scopus...", mnemonic: 'S', actionPerformed: {
					doExportFile (
						"Scopus_csv", "Export Scopus file",
						[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*\.csv/ || f.isDirectory() }] as FileFilter)
				})
				
				separator()
				
				menuItem(id: "menuSaveRuediger", text: "Ruediger...", mnemonic: 'R', accelerator: KeyStroke.getKeyStroke("ctrl R"), actionPerformed: {
					doExportFile (
						"CRE_csv_Ruediger", "Export CRE file for Ruediger",
						[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*\.csv/ || f.isDirectory() }] as FileFilter)
				})
			}
			
			menuItem(id: "menuSaveCRE", text: "Save...", mnemonic: 'S', accelerator: KeyStroke.getKeyStroke("ctrl S"), actionPerformed: {
				doExportFile (
					"CRE_json", "Export CRE file",
					[getDescription: {"CRE files (*.cre)"}, accept:{File f -> f ==~ /.*\.cre/ || f.isDirectory() }] as FileFilter)
			})
			
			menuItem(id: "menuSaveCSV", text: "DEPERCATED Save...", actionPerformed: {
				doExportFile (
					"CRE_csv", "Export CRE file",
					[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*\.csv/ || f.isDirectory() }] as FileFilter)
			})

			separator()
			
			menuItem(id:'settingsDlg', text: "Settings...", mnemonic: 'T', actionPerformed: {
				uisetting.createSettingsDlg(mainFrame).visible = true 
			})
			
			separator()
			
			menuItem(id: "menuExit", text: "Exit", mnemonic: 'X', accelerator: KeyStroke.getKeyStroke("alt F4"), actionPerformed: { 
				int answer = JOptionPane.showConfirmDialog (null, "Save changes before exit?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION) 
				switch (answer) {
					case JOptionPane.YES_OPTION: 
						doExportFile (
							"CRE_csv", "Export CRE file",
							[getDescription: {"CSV files (*.csv)"}, accept:{File f -> f ==~ /.*\.csv/ || f.isDirectory() }] as FileFilter); // no break!
					case JOptionPane.NO_OPTION: uisetting.setWindowPos (); System.exit(0); 
				}
			})
		}
		
		menu(text: "Data", mnemonic: 'D') {
			
			menuItem(id:'infoDlg', text: "Info", mnemonic: 'I', actionPerformed: {
				UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
			})

			checkBoxMenuItem (id:'showNull', text: "Show CRs without Year", actionPerformed: { 
				crTable.setShowNull(sb.showNull.selected)
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
				crTable.stat.setValue("", 0, crTable.getInfoString())
			})
			
			menuItem(text: "Filter by Cited Reference Year ...", mnemonic: 'Y', actionPerformed: {
				UIDialogFactory.createIntRangeDlg(mainFrame, uibind.uiRanges[2], "Filter by Cited Reference Year", "Cited Reference Years", crTable.getMaxRangeYear(), { min, max -> chpan.getChart().getXYPlot()?.getDomainAxis()?.setRange(min-0.5, max+0.5) }).visible = true
			})

			separator()
			
			
			menuItem(text: "Remove Selected Cited References...", mnemonic: 'S', actionPerformed: {

				if (tab.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(null, "No Cited References selected");
				} else {
					if (JOptionPane.showConfirmDialog (null, "Would you like to remove the selected ${tab.getSelectedRowCount()} Cited References?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						crTable.remove (tab.getSelectedRows().collect { tab.convertRowIndexToModel (it) })
						(tab.getModel() as AbstractTableModel).fireTableDataChanged()
					}
				}
			})

			menuItem(text: "Remove Cited References without Year...", actionPerformed: {
				
				int n = crTable.getNumberWithoutYear()
				if (n == 0) {
					JOptionPane.showMessageDialog(null, "No Cited References without Year");
				} else {
					if (JOptionPane.showConfirmDialog (null, "Would you like to remove all ${n} Cited References without year?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						crTable.removeWithoutYear()
						(tab.getModel() as AbstractTableModel).fireTableDataChanged()
					}
				}
			})
				
						
			menuItem(text: "Remove by Cited Reference Year ...", mnemonic: 'Y', actionPerformed: {
				UIDialogFactory.createIntRangeDlg(mainFrame, uibind.uiRanges[0], "Remove by Cited Reference Year", "Cited Reference Years", crTable.getMaxRangeYear(), { min, max -> crTable.removeByYear(min, max) }).visible = true
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
			})
			
			menuItem(text: "Remove by Number of Cited References...", mnemonic: 'N', actionPerformed: {
				UIDialogFactory.createIntRangeDlg(mainFrame, uibind.uiRanges[1], "Remove by Number of cited references", "Number of Cited References", crTable.getMaxRangeNCR(), { min, max -> crTable.removeByNCR(min, max) }).visible = true
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
			})

			menuItem(text: "Remove by Percent in Year...", mnemonic: 'P', actionPerformed: {
				UIDialogFactory.createThresholdDlg(mainFrame, "Remove by Percent in Year", "Percent in Year", { comp, threshold -> crTable.removeByPercentYear (comp, threshold) }).visible = true
				(tab.getModel() as AbstractTableModel).fireTableDataChanged()
			})

		}
		
		menu(text: "Standardization", mnemonic: 'S') {
			
			menuItem(text: "Cluster Equivalent Cited References...", mnemonic: 'C', actionPerformed: {
					
				Runnable runnable = new Runnable() {
					public void run() {
						crTable.crMatch.doBlocking()
						matchpan.visible = true
						matchpan.updateClustering() 
					 }
				}
				Thread t = new Thread(runnable)
				t.start()
					
			})
			
			menuItem(text: "Merge Clustered Cited References...", mnemonic: 'M', actionPerformed: {
				
				long toDelete = crTable.crData.size()-crTable.crMatch.getNoOfClusters()
				if (toDelete == 0) {
					JOptionPane.showMessageDialog(null, "No Clusters to merge!");
				} else {
					if (JOptionPane.showConfirmDialog (null, "Merging will aggregate ${toDelete} Cited References! Are you sure?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						
						Runnable runnable = new Runnable() {
							public void run() {
								crTable.crMatch.merge ()
								(tab.getModel() as AbstractTableModel).fireTableDataChanged()
							}
						}
						Thread t = new Thread(runnable)
						t.start()
	
					}
				}
			})

		}
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
uisetting = new UISettings(tab, chpan, mainFrame, crTable)

// Pressing space key shows CR Details Dialog
tab.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,  0), "showCRDetails");
tab.getActionMap().put("showCRDetails", UIDialogFactory.showCRDetails(mainFrame, crTable));

mainFrame.visible = true


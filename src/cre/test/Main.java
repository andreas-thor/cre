package cre.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.data.match.CRMatch2;
import cre.test.data.match.CRMatch2.ManualMatchType2;
import cre.test.data.source.CRE_csv;
import cre.test.data.source.CRE_json;
import cre.test.data.source.Scopus_csv;
import cre.test.data.source.WoS_txt;
import cre.test.data.type.CRType;
import cre.test.ui.CRChart;
import cre.test.ui.CRChart_HighCharts;
import cre.test.ui.CRChart_JFreeChart;
import cre.test.ui.CRTableView;
import cre.test.ui.MatchPanel;
import cre.test.ui.StatusBar;
import cre.test.ui.dialog.About;
import cre.test.ui.dialog.CRInfo;
import cre.test.ui.dialog.CRPubInfo;
import cre.test.ui.dialog.ConfirmAlert;
import cre.test.ui.dialog.ExceptionStacktrace;
import cre.test.ui.dialog.Info;
import cre.test.ui.dialog.Range;
import cre.test.ui.dialog.Settings;
import cre.test.ui.dialog.TextInput;
import cre.test.ui.dialog.Threshold;
import cre.test.ui.dialog.Wait;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.WindowEvent;

public class Main {

	
	public static enum ImportFormat { 
		CRE_JSON ("Open CRE File", false, new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[]{"*.cre"}))), 
		WOS_TXT ("Import Web of Science Files", true, new ExtensionFilter("Web of Science", Arrays.asList(new String[]{"*.txt"}))),
		SCOPUS_CSV ("Import Scopus Files", true, new ExtensionFilter("Scopus", Arrays.asList(new String[]{"*.csv"})));
		
		public final String label;
		public boolean multiple;
		public ExtensionFilter filter;
		ImportFormat(String label, boolean multiple, ExtensionFilter filter) {
			this.label = label;
			this.multiple = multiple;
			this.filter = filter;
		}
	};
	
	public static enum ExportFormat { 
		CRE_JSON ("Save CRE File", new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[]{"*.cre"}))), 
		WOS_TXT ("Export Web of Science File", new ExtensionFilter("Web of Science", Arrays.asList(new String[]{"*.txt"}))),
		SCOPUS_CSV ("Export Scopus File", new ExtensionFilter("Scopus", Arrays.asList(new String[]{"*.csv"}))),
		CRE_CSV_CR ("Export Cited References", new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[]{"*.csv"}))),
		CRE_CSV_PUB ("Export Citing Publications", new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[]{"*.csv"}))),
		CRE_CSV_CR_PUB ("Export Cited References + Citing Publications", new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[]{"*.csv"}))),
		CRE_CSV_GRAPH ("Export Graph", new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[]{"*.csv"})));
		
		public final String label;
		public ExtensionFilter filter;
		ExportFormat(String label, ExtensionFilter filter) {
			this.label = label;
			this.filter = filter;
		}
	};
	
	
	
	CRTable crTable;
	CRChart crChart[] = new CRChart[2];
	
	@FXML Label lab;
	@FXML GridPane mainPane;
	@FXML ScrollPane scrollTab;

	@FXML GridPane chartPane;
	@FXML GridPane tablePane;
	@FXML GridPane statPane;

	private CRTableView tableView;

	private MatchPanel matchView;
	
	private Thread t;
	
	private File creFile;
	


	
	
	
	@FXML public void initialize() throws IOException {
	
		creFile = null;
		crTable = CRTable.get();
		statPane.add(StatusBar.get(), 0, 0);

		tableView = new CRTableView();
		tablePane.add (tableView, 0, 1);
		tableView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				OnMenuDataRemoveSelected();
			}
			if (e.getCode() == KeyCode.SPACE) {
				List<CRType> sel = getSelectedCRs(); 
				if (sel.size()==1) {
					new CRInfo(sel.get(0)).showAndWait();	
				}
			}
		});



		matchView = new MatchPanel() {

			@Override
			public void onUpdateClustering(double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI) {
				if (t!=null) t.interrupt();
				t = new Thread( () -> { 
					CRMatch2.get().updateClustering(CRMatch2.ClusteringType2.REFRESH, null, threshold, useVol, usePag, useDOI);
					refreshTable();
				});
				t.start();
			}

			@Override
			public void onMatchManual(ManualMatchType2 type, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
				
				List<CRType> toMatch = getSelectedCRs();
				if ((toMatch.size()==0) || ((toMatch.size()==1) && (type != ManualMatchType2.EXTRACT))) {
					new ConfirmAlert("Error during clustering!", true, new String[] {"Too few Cited References selected!"}).showAndWait();
				} else {
					if ((toMatch.size()>5) && (type != ManualMatchType2.EXTRACT)) {
						new ConfirmAlert("Error during clustering!", true, new String[] {"Too many Cited References selected (at most 5)!"}).showAndWait();
					} else {
						CRMatch2.get().addManuMatching (toMatch, type, threshold, useVol, usePag, useDOI);
						refreshTable();
					}
				}
			}

			@Override
			public void onMatchUnDo(double threshold, boolean useVol, boolean usePag, boolean useDOI) {
				CRMatch2.get().undoManuMatching (threshold, useVol, usePag, useDOI);

//				crTable.matchUndo(threshold, useVol, usePag, useDOI);
				refreshTable();
			}
			
		};
		matchView.setVisible(false);
		tablePane.add(matchView, 0, 0);		
		
		crChart = new CRChart[] {
			new CRChart_JFreeChart () {
				@Override protected void onSelectYear(int year) { tableView.orderByYearAndSelect(year); }
				@Override protected void onYearRangeFilter(double min, double max) { filterData (new int[] {(int)Math.ceil(min), (int)Math.floor(max)}); }
			}, 
			new CRChart_HighCharts () {
				@Override protected void onSelectYear(int year) { tableView.orderByYearAndSelect(year); }
				@Override protected void onYearRangeFilter(double min, double max) { filterData (new int[] {(int)Math.ceil(min), (int)Math.floor(max)}); }
			} 
		};
		for (int i=0; i<crChart.length; i++) {
			chartPane.add(crChart[i].getNode(), 0, 0);
			crChart[i].setVisible(UserSettings.get().getChartEngine()==i);
		}
		
		
		// save user settings when exit
		CitedReferencesExplorer.stage.setOnCloseRequest(event -> {  
			
			event.consume();
			
			Alert exit = new Alert(AlertType.CONFIRMATION);
			exit.setTitle ("Warning");
			exit.setHeaderText("Save before exit?");
			exit.getDialogPane().getButtonTypes().clear();
			exit.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
//			((Button) exit.getDialogPane().lookupButton(ButtonType.YES)).setDefaultButton (false);
//			((Button) exit.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton (true);
			
			exit.showAndWait().ifPresent(button -> {

				if (button == ButtonType.CANCEL) return;

				if (button == ButtonType.YES) {
					try {
						if (!saveFile(ExportFormat.CRE_JSON, false)) return;
					} catch (Exception e) {
						Platform.runLater( () -> { new ExceptionStacktrace("Error during file export!", e).showAndWait(); });
						return;
					}
				}
				
				UserSettings.get().saveUserPrefs(CitedReferencesExplorer.stage.getWidth(), CitedReferencesExplorer.stage.getHeight(), CitedReferencesExplorer.stage.getX(), CitedReferencesExplorer.stage.getY());
				CitedReferencesExplorer.stage.close();
			});
		});

		if (CitedReferencesExplorer.loadOnOpen!=null) {
			openFile(ImportFormat.CRE_JSON, CitedReferencesExplorer.loadOnOpen);
		}

		
	}
	

	private void updateData (int[] range) {
		Platform.runLater(() -> { 
			Stream.of(crChart).forEach (it -> { it.updateData(crTable.getChartData()); } );
		});
		filterData(range);
	}
	
	private void filterData (int[] range) {
		
		if (range != null) {
			UserSettings.get().setRange(RangeType.CurrentYearRange, range);
		}
		
		Platform.runLater( () -> {
			crTable.filterByYear(UserSettings.get().getRange(RangeType.CurrentYearRange));
			
			// save sort order ...
			List<TableColumn<CRType, ?>> oldSort = new ArrayList<TableColumn<CRType, ?>>();
			tableView.getSortOrder().forEach(it -> oldSort.add(it));

			// ... update rows ...
			tableView.setItems(FXCollections.observableArrayList(crTable.getCR().filter(cr -> cr.getVI()).collect(Collectors.toList())));

			// ... reset old sort order
			for (TableColumn<CRType, ?> x: oldSort) {
				tableView.getSortOrder().add(x);
			}
			
			Stream.of(crChart).forEach (it -> { it.setDomainRange (UserSettings.get().getRange(RangeType.CurrentYearRange)); });
			refreshTable();
		});
	}

	
	private void refreshTable () {
		Platform.runLater( () -> {
			System.out.println("SortOrderSize = " + tableView.getSortOrder().size());
			
			if (tableView.getSortOrder().size() > 0) {
				tableView.sort();
			}
			tableView.getColumns().get(0).setVisible(false);
			tableView.getColumns().get(0).setVisible(true);
			
			StatusBar.get().updateInfo();
		});
	}
	
    
		
/*
 * http://stackoverflow.com/questions/36353518/javafx-table-view-multiple-selection-sometimes-skips-one-of-the-items/38207246#38207246
 * Apparently it's a bug, already fixed for version 9 (and also 8u112, if I understand correctly): https://bugs.openjdk.java.net/browse/JDK-8144501
 * A workaround for now is to use getSelectedIndices(), then get the items corresponding to these instances from table.getItems()
 * return tableView.getSelectionModel().getSelectedIndices().stream().map(idx -> tableView.getItems().get(idx)).collect(Collectors.toList());
 * ==> GEHT AUCH NICHT
*/
	private List<CRType> getSelectedCRs () {
		return tableView.getSelectionModel().getSelectedItems().stream().filter(cr -> cr != null).collect(Collectors.toList());
	}
	
	
	private void openFile (ImportFormat source) throws IOException {
		openFile(source, null);
	}
	
    private void openFile (ImportFormat source, String fileName) throws IOException {
    	
    	final List<File> files = new ArrayList<File>();

    	if (fileName != null) {	// load specific file (e.g., during appilcation start)
    		files.add(new File(fileName));
    	} else {
    		
    		FileChooser fileChooser = new FileChooser();
        	fileChooser.setTitle(source.label);
       		fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
        	fileChooser.getExtensionFilters().add(source.filter); 
        	fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[]{"*.*"})));
    		
	    	if (source.multiple) {
	    		List<File> selFiles = fileChooser.showOpenMultipleDialog(CitedReferencesExplorer.stage);
	    		if (selFiles != null) files.addAll(selFiles);
	    	} else {
	    		File selFile = fileChooser.showOpenDialog(CitedReferencesExplorer.stage);
	    		if (selFile != null) files.add(selFile);
	    	}
    	}
    	
    	if (files.size()>0) {
    		this.creFile = null;
    		UserSettings.get().setLastFileDir(files.get(0).getParentFile());	// save last directory to be used as initial directory
    		Wait wait = new Wait();
    		new Thread( () -> {
				try { 
					matchView.setVisible(false);
					switch (source) {
						case CRE_JSON: 
							CRE_json.load (files.get(0), crTable); 
							creFile = files.get(0); 		// save file name for window title and save operation
							if ((CRMatch2.get().getSize(true)+CRMatch2.get().getSize(false))>0) {		// show match panel if applicable
								matchView.setVisible(true);
								matchView.updateClustering();
							}
							break;
						case WOS_TXT: 
							WoS_txt.load(files, crTable, UserSettings.get().getMaxCR(), UserSettings.get().getRange(RangeType.ImportYearRange)); 
							break;
						case SCOPUS_CSV: 
							Scopus_csv.load(files, crTable, UserSettings.get().getMaxCR(), UserSettings.get().getRange(RangeType.ImportYearRange)); 
							break;
					}	
				} catch (FileTooLargeException e1) {
					Platform.runLater( () -> { new ConfirmAlert("Error during file import!", true, new String[] {String.format("You try to import too many cited references. Import was aborted after loading %d Cited References. You can change the maximum number in the File > Settings > Import menu. ", e1.numberOfCRs)}).showAndWait(); });
				} catch (UnsupportedFileFormatException e4) {
					Platform.runLater( () -> { new ConfirmAlert("Error during file import!", true, new String[] {"Unsupported File Format."}).showAndWait(); });
				} catch (AbortedException e2) {
					Platform.runLater( () -> { new ConfirmAlert("Error during file import!", true, new String[] {"File Import aborted."}).showAndWait(); }); 
				} catch (OutOfMemoryError mem) {
					crTable.init();
					Platform.runLater( () -> { new ConfirmAlert("Error during file import!", true, new String[] {"Out of Memory Error."}).showAndWait(); });							
				} catch (Exception e3) {
//					e3.printStackTrace();
					Platform.runLater( () -> { new ExceptionStacktrace("Error during file import!", e3).showAndWait(); });
				}
				
				Platform.runLater(() -> { 
					wait.close(); 
					if (!crTable.isAborted()) {
						OnMenuDataInfo();
						updateData(CRStats.getMaxRangeYear());
						CitedReferencesExplorer.stage.setTitle(CitedReferencesExplorer.title + ((creFile == null) ? "" : " - " + creFile.getAbsolutePath()));
					}
				});
			}).start();
    		
    		wait.showAndWait().ifPresent(cancel -> { crTable.setAborted(cancel); }); 
		
		}
    }
    
    private boolean saveFile (ExportFormat source) throws IOException {
    	return saveFile(source, true);
    }
    
    private boolean saveFile (ExportFormat source, boolean saveAs) throws IOException {

    	File selFile;
    	if ((source == ExportFormat.CRE_JSON) && (creFile != null) && (!saveAs)) {
    		selFile = creFile;
    	} else {
	    	FileChooser fileChooser = new FileChooser();
	    	fileChooser.setTitle(source.label);
	   		fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
	    	fileChooser.getExtensionFilters().add(source.filter); 
	    	fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[]{"*.*"})));
	   		selFile = fileChooser.showSaveDialog(CitedReferencesExplorer.stage);
    	}
    	
   		if (selFile == null) return false;
   			
		new Thread( () -> {
			try {
	   			switch (source) {
		   			case CRE_JSON: CRE_json.save(selFile, crTable); creFile = selFile; break;
					case WOS_TXT: WoS_txt.save(selFile, crTable); break;
					case SCOPUS_CSV: Scopus_csv.save(selFile, crTable); break;
					case CRE_CSV_CR: CRE_csv.saveCR(selFile, crTable); break;
					case CRE_CSV_PUB: CRE_csv.savePub(selFile, crTable); break;
					case CRE_CSV_CR_PUB: CRE_csv.saveCRPub(selFile, crTable); break;
					case CRE_CSV_GRAPH: CRE_csv.saveGraph(selFile, crTable); break;
	   			}
	   			
	   			Platform.runLater( () -> {
					CitedReferencesExplorer.stage.setTitle(CitedReferencesExplorer.title + ((creFile == null) ? "" : " - " + creFile.getAbsolutePath()));
	   			});
				
			} catch (Exception e) {
				Platform.runLater( () -> { new ExceptionStacktrace("Error during file export!", e).showAndWait(); });
			}
		}).start();

		return true;
    }
    
    
    
    /*
     * FILE Menu
     */
    
	@FXML public void OnMenuFileOpen() throws IOException {
		 openFile(ImportFormat.CRE_JSON);
	}

	@FXML public void OnMenuFileImportWoS(ActionEvent event) throws IOException {
		 openFile(ImportFormat.WOS_TXT);
	}

	@FXML public void OnMenuFileImportScopus(ActionEvent event) throws IOException {
		 openFile(ImportFormat.SCOPUS_CSV);
	}

	@FXML public void OnMenuFileSave() throws IOException {
		saveFile(ExportFormat.CRE_JSON, false);
	}

	@FXML public void OnMenuFileSaveAs() throws IOException {
		saveFile(ExportFormat.CRE_JSON, true);
	}

	@FXML public void OnMenuFileExportWoS() throws IOException {
		saveFile(ExportFormat.WOS_TXT);
	}

	@FXML public void OnMenuFileExportScopus() throws IOException {
		saveFile(ExportFormat.SCOPUS_CSV);
	}

	@FXML public void OnMenuFileExportCSVGraph() throws IOException {
		saveFile(ExportFormat.CRE_CSV_GRAPH);
	}

	@FXML public void OnMenuFileExportCSVCR() throws IOException {
		saveFile(ExportFormat.CRE_CSV_CR);
	}

	@FXML public void OnMenuFileExportCSVPub() throws IOException {
		saveFile(ExportFormat.CRE_CSV_PUB);
	}

	@FXML public void OnMenuFileExportCSVAll() throws IOException {
		saveFile(ExportFormat.CRE_CSV_CR_PUB);
	}
	
	
	
	
	@FXML public void OnMenuFileSettings() throws IOException {
		
		new Settings()
			.showAndWait()
			.ifPresent( noOfErrors -> {
				for (int i=0; i<crChart.length; i++) {
					crChart[i].setVisible(UserSettings.get().getChartEngine()==i);
				}
				crTable.updateData(false);
				updateData(CRStats.getMaxRangeYear());
				
				
				// TODO: Apply settings changes
			}
		);
	}
	
	@FXML public void OnMenuFileExit() {
		CitedReferencesExplorer.stage.fireEvent(new WindowEvent(CitedReferencesExplorer.stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}


	
	/*
	 * DATA Menu
	 */
	
	@FXML public void OnMenuDataInfo() {
		
		
		System.out.println("So viele Pubs haben doppelte CRs");
		System.out.println(crTable.getPub().filter(p -> p.getCR().count() != p.getCR().distinct().count()).count());
		
		System.out.println("So viele CRs haben doppelte Pubs");
//		System.out.println(crTable.getCR().filter(cr -> cr.pubList.size() != cr.pubList.stream().distinct().count()).count());
		
		
		
//		crTable.getPub().filter(p -> p.getCR().count() != p.getCR().distinct().count()).forEach(p -> {
//			System.out.print("[");
//			p.getCR().forEach(cr -> { System.out.print(cr.getID() + ",");} );
//			System.out.println("]");
//		});
		
		new Info().showAndWait();	
	}
	
	@FXML public void OnMenuViewCR() {
		List<CRType> sel = getSelectedCRs();
		if (sel.size()==1) {
			new CRInfo(sel.get(0)).showAndWait();	
		} else {
			new ConfirmAlert("Remove Cited References",true, new String[] { String.format("%s Cited References selected.", (sel.size()==0) ? "No" : "Too many") }).showAndWait();
		}
	}


	@FXML public void OnMenuViewPub() {
		List<CRType> sel = getSelectedCRs();
		if (sel.size()==1) {
			new CRPubInfo(sel.get(0)).showAndWait();	
		} else {
			new ConfirmAlert("Remove Cited References",true, new String[] { String.format("%s Cited References selected.", (sel.size()==0) ? "No" : "Too many") }).showAndWait();
		}
	}

	
	@FXML public void OnMenuDataShowCRswoYears(ActionEvent event) {
		crTable.setShowNull(((CheckMenuItem)event.getSource()).isSelected());
	}

	
	@FXML public void OnMenuDataFilterByRPY(ActionEvent event) {
		new Range("Filter Cited References", "Select Range of Cited References Years", UserSettings.RangeType.FilterByRPYRange, CRStats.getMaxRangeYear())
			.showAndWait()
			.ifPresent( range -> { 
				filterData(range);
			}
		);
	}
	
	
	@FXML public void OnMenuDataRemoveSelected() {
		
		List<CRType> toDelete = getSelectedCRs();
		int n = toDelete.size();
		new ConfirmAlert("Remove Cited References", n==0, new String[] {"No Cited References selected.", String.format("Would you like to remove all %d selected Cited References?", n)})
			.showAndWait()
			.ifPresent( btn -> {
				if (btn==ButtonType.YES) {
					crTable.removeCR(toDelete);
					updateData(null);
				}
			}
		);
	}

	@FXML public void OnMenuDataCopySelected() {

		StringBuffer buf = new StringBuffer();

		tableView.getSelectionModel().getSelectedItems().stream().forEach((CRType cr) -> {
			tableView.getColumns().filtered(c -> c.isVisible()).forEach(c -> {
				buf.append("\"");
				buf.append(c.getCellData(cr));
				buf.append("\"\t");
			});
			buf.append("\n");
		});
		
		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(buf.toString());			
        Clipboard.getSystemClipboard().setContent(clipboardContent);
	}
	
	
	@FXML public void OnMenuDataRemovewoYears() {
		
		int n = CRStats.getNumberWithoutYear();
		new ConfirmAlert("Remove Cited References", n==0, new String[] {"No Cited References w/o Year.", String.format("Would you like to remove all %d Cited References w/o Year?", n)})
			.showAndWait()
			.ifPresent( btn -> {
				if (btn==ButtonType.YES) {
					crTable.removeCRWithoutYear();
					updateData(null);
				}
			}
		);
	}

	
	@FXML public void OnMenuDataRemoveByRPY() {
		
		final String header = "Remove Cited References";  
		new Range(header, "Select Range of Cited References Years", UserSettings.RangeType.RemoveByRPYRange, CRStats.getMaxRangeYear())
			.showAndWait()
			.ifPresent( range -> { 
				long n =  CRStats.getNumberByYear(range);
				new ConfirmAlert(header, n==0, new String[] {String.format ("No Cited References with Cited Reference Year between %d and %d.", range[0], range[1]), String.format("Would you like to remove all %d Cited References with Cited Reference Year between %d and %d?", n, range[0], range[1])})
					.showAndWait()
					.ifPresent( btn -> {
						if (btn==ButtonType.YES) {
							crTable.removeCRByYear(range);
							updateData(null);
						}
					}
				);
			}
		);
	}

	
	@FXML public void OnMenuDataRemoveByNCR() {

		final String header = "Remove Cited References";  
		new Range(header, "Select Number of Cited References", UserSettings.RangeType.RemoveByNCRRange, CRStats.getMaxRangeNCR())
			.showAndWait()
			.ifPresent( range -> { 
				long n =  CRStats.getNumberByNCR(range);
				new ConfirmAlert(header, n==0, new String[] {String.format ("No Cited References with Number of Cited References between %d and %d.", range[0], range[1]), String.format("Would you like to remove all %d Cited References with Number of Cited References between %d and %d?", n, range[0], range[1])})
					.showAndWait()
					.ifPresent( btn -> {
						if (btn==ButtonType.YES) {
							crTable.removeCRByN_CR(range);
							updateData(null);
						}
					}
				);
			}
		);
	}


	@FXML public void OnMenuDataRemoveByPERC_YEAR() {
		
		final String header = "Remove Cited References";  
		new Threshold(header, "Select Threshold for Percent in Year", "<", 0.0)
			.showAndWait()
			.ifPresent( cond -> {
				String comp = cond.getKey();
				double threshold = cond.getValue().doubleValue();
				long n =  CRStats.getNumberByPercentYear(comp, threshold);
				new ConfirmAlert(header, n==0, new String[] {String.format ("No Cited References with Percent in Year %s %.1f%%.", comp, 100*threshold), String.format("Would you like to remove all %d Cited References with Percent in Year %s %.1f%%?", n, comp, 100*threshold)})
					.showAndWait()
					.ifPresent( btn -> {
						if (btn==ButtonType.YES) {
							crTable.removeCRByPERC_YR(comp, threshold);
							updateData(null);
						}
					}
				);
			}
		);
	}

	
	@FXML public void OnMenuDataRetainCRById() {
		
		new TextInput("Retain Cited References By Id", "Specify list if CR Ids")
		.showAndWait()
		.ifPresent( list -> {
			if (list != null) {
				int[] id = Arrays.stream(list.split("\\D")).mapToInt(Integer::valueOf).toArray();
				List<CRType> toRetain = crTable.getCR().filter(cr -> 
					IntStream.of(id).anyMatch(it -> cr.getID()==it)
				).collect(Collectors.toList());
				crTable.retainCR (toRetain);
			}});
	}


	

	@FXML public void OnMenuDataRetainSelected() {
		
		List<CRType> toDelete = getSelectedCRs(); 
		int n = toDelete.size();
		new ConfirmAlert("Remove Publications", n==0, new String[] {"No Cited References selected.", String.format("Would you like to remove all citing publications that do not cite any of the selected %d Cited References?", n)})
			.showAndWait()
			.ifPresent( btn -> {
				if (btn==ButtonType.YES) {
					crTable.removePubByCR(toDelete);
					updateData(null);
				}
			}
		);
	}

	
	@FXML public void OnMenuDataRetainByRPY() {
		
		new Range("Retain Publications", "Select Range of Citing Publication Years", UserSettings.RangeType.RetainByRPYRange, CRStats.getMaxRangeCitingYear())
			.showAndWait()
			.ifPresent( range -> { 
				long n =  CRStats.getNumberOfPubs() - CRStats.getNumberOfPubsByCitingYear(range);
				new ConfirmAlert("Remove Publications", n==0, new String[] {String.format ("All Citing Publication Years are between between %d and %d.", range[0], range[1]), String.format("Would you like to remove all %d citing publications with publication year lower than %d or higher than %d?", n, range[0], range[1])})
					.showAndWait()
					.ifPresent( btn -> {
						if (btn==ButtonType.YES) {
							crTable.removePubByCitingYear(range);
							updateData(null);
						}
					}
				);
			}
		);
	}




	/*
	 * Standardization Menu
	 */

	@FXML public void OnMenuStdCluster() {
		
		new Thread( () -> {
			CRMatch2.get().generateAutoMatching();
			matchView.setVisible(true);
			tablePane.requestLayout();
			matchView.updateClustering(); 
		}).start();
	}




	@FXML public void OnMenuStdMerge() {

		
		long n = CRStats.getSize()-CRStats.getNoOfClusters();
		new ConfirmAlert("Merge clusters", n==0, new String[] {"No Clusters to merge.", String.format("Merging will aggregate %d Cited References! Are you sure?", n)})
			.showAndWait()
			.ifPresent( btn -> {
				if (btn==ButtonType.YES) {
					new Thread( () -> {
						crTable.merge();
						matchView.setVisible(false);
						updateData(null);
//						tablePane.requestLayout();
					}).start();
				}
			});
	}


	/*
	 * Help Menu
	 */
	
	@FXML public void OnMenuHelpManual() {
		HostServicesFactory.getInstance(CitedReferencesExplorer.app).showDocument(CitedReferencesExplorer.url);
	}


	@FXML public void OnMenuHelpAbout() {
		new About().showAndWait();
	}




	




	
}

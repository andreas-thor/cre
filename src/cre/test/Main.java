package cre.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.CRTableEvent;
import cre.test.data.CRType;
import cre.test.data.source.CRE_csv;
import cre.test.data.source.CRE_json;
import cre.test.data.source.Scopus_csv;
import cre.test.data.source.WoS_txt;
import cre.test.ui.CRChart;
import cre.test.ui.CRChart_HighCharts;
import cre.test.ui.CRChart_JFreeChart;
import cre.test.ui.CRTableView;
import cre.test.ui.MatchPanel;
import cre.test.ui.StatusBar;
import cre.test.ui.UserSettings;
import cre.test.ui.UserSettings.RangeType;
import cre.test.ui.dialog.ConfirmAlert;
import cre.test.ui.dialog.ExceptionStacktrace;
import cre.test.ui.dialog.Info;
import cre.test.ui.dialog.Range;
import cre.test.ui.dialog.Settings;
import cre.test.ui.dialog.Threshold;
import cre.test.ui.dialog.Wait;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
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
	@FXML CRTableView tableView;

	@FXML GridPane chartPane;
	@FXML GridPane tablePane;
	@FXML GridPane statPane;
	@FXML GridPane matchPane;
	

	
	@FXML public void updateTable () {
		
		
	}
	


	
	
	
	@FXML public void initialize() {
	
		statPane.add(StatusBar.get(), 0, 0);
		matchPane.add(new MatchPanel(), 0, 0);		

		crTable = new CRTable( new CRTableEvent() {
			
			@Override 
			public void onUpdate() {
				crTable.filterByYear(UserSettings.get().getRange(RangeType.CurrentYearRange));
				Stream.of(crChart).forEach (it -> { it.updateData(crTable.getChartData()); });
				Stream.of(crChart).forEach (it -> { it.setDomainRange (UserSettings.get().getRange(RangeType.CurrentYearRange)); });
			}
			
			@Override
			public void onFilter() {
				tableView.setItems(FXCollections.observableArrayList(crTable.crData.stream().filter(cr -> cr.getVI()).collect(Collectors.toList())));
				Stream.of(crChart).forEach (it -> { it.setDomainRange (UserSettings.get().getRange(RangeType.CurrentYearRange)); });
			}
		});
				
				
//				new CRTableEvent() {
//			
//			@Override
//			public void onUpdate() {
//				Platform.runLater(new Runnable() {
//					@Override
//					public void run() {
//						tableView.setItems(FXCollections.observableArrayList(crTable.crData.stream().filter(cr -> cr.getVI()).collect(Collectors.toList())));
//						Stream.of(crChart).forEach (it -> { it.setDomainRange (range); });
//					}
//				});
//				
//			}
//			
//			@Override
//			public void onFilter(int[] range) {
//				// TODO Auto-generated method stub
//				
//			}
//		};
		
		
		tableView = new CRTableView();
		tablePane.add (tableView, 0, 0);
		
		
		

		crChart = new CRChart[] { new CRChart_JFreeChart(crTable, tableView), new CRChart_HighCharts(crTable, tableView) };
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
			((Button) exit.getDialogPane().lookupButton(ButtonType.YES)).setDefaultButton (false);
			((Button) exit.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton (true);
			exit.showAndWait().ifPresent(button -> {
				
				if (button != ButtonType.CANCEL) {
					if (button == ButtonType.YES) {
						try {
							saveFile(ExportFormat.CRE_JSON);
						} catch (Exception e) {
							Platform.runLater( () -> { new ExceptionStacktrace("Error during file export!", e).showAndWait(); });
							return;
						}
					}
					UserSettings.get().saveUserPrefs(CitedReferencesExplorer.stage.getWidth(), CitedReferencesExplorer.stage.getHeight(), CitedReferencesExplorer.stage.getX(), CitedReferencesExplorer.stage.getY());
					CitedReferencesExplorer.stage.close();
				}
			});
		});

		

		
	}
	

    
    private void openFile (ImportFormat source) throws IOException {
    	
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(source.label);
   		fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
    	fileChooser.getExtensionFilters().add(source.filter); 
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[]{"*.*"})));
    	
    	final List<File> files = new ArrayList<File>();
    	if (source.multiple) {
    		List<File> selFiles = fileChooser.showOpenMultipleDialog(CitedReferencesExplorer.stage);
    		if (selFiles != null) files.addAll(selFiles);
    	} else {
    		File selFile = fileChooser.showOpenDialog(CitedReferencesExplorer.stage);
    		if (selFile != null) files.add(selFile);
    	}
    	
    	if (files.size()>0) {
    		UserSettings.get().setLastFileDir(files.get(0).getParentFile());	// save last directory to be used as initial directory
    		Wait wait = new Wait();
    		new Thread( () -> {
				try { 
					switch (source) {
						case CRE_JSON: CRE_json.load (files.get(0), crTable); break;
						case WOS_TXT: WoS_txt.load(files, crTable, UserSettings.get().getMaxCR(), UserSettings.get().getRange(RangeType.ImportYearRange)); break;
						case SCOPUS_CSV: Scopus_csv.load(files, crTable, UserSettings.get().getMaxCR(), UserSettings.get().getRange(RangeType.ImportYearRange)); break;
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
					Platform.runLater( () -> { new ExceptionStacktrace("Error during file import!", e3).showAndWait(); });
				}
				
//				crTable.filterByYear();
				
				
				Platform.runLater(() -> { 
					wait.close(); 
					if (!crTable.abort) {
						OnMenuDataInfo(); 
//						Stream.of(crChart).forEach (it -> { it.updateData(crTable.getChartData()); });
					}
				});
			}).start();
    		
    		wait.showAndWait().ifPresent(cancel -> { crTable.abort=cancel; }); 
		
		}
    }
    
    
    private void saveFile (ExportFormat source) throws IOException {

    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(source.label);
   		fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
    	fileChooser.getExtensionFilters().add(source.filter); 
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[]{"*.*"})));

   		File selFile = fileChooser.showSaveDialog(CitedReferencesExplorer.stage);
   		if (selFile != null) {
   			
   			new Thread( () -> {
   				try {
		   			switch (source) {
			   			case CRE_JSON: CRE_json.save(selFile, crTable); break;
						case WOS_TXT: WoS_txt.save(selFile, crTable); break;
						case SCOPUS_CSV: Scopus_csv.save(selFile, crTable); break;
						case CRE_CSV_CR: CRE_csv.saveCR(selFile, crTable); break;
						case CRE_CSV_PUB: CRE_csv.savePub(selFile, crTable); break;
						case CRE_CSV_CR_PUB: CRE_csv.saveCRPub(selFile, crTable); break;
						case CRE_CSV_GRAPH: CRE_csv.saveGraph(selFile, crTable); break;
		   			}
   				} catch (Exception e) {
					Platform.runLater( () -> { new ExceptionStacktrace("Error during file export!", e).showAndWait(); });
				}
   			}).start();
   		}
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
		saveFile(ExportFormat.CRE_JSON);
	}

	@FXML public void OnMenuFileSaveAs() throws IOException {
		saveFile(ExportFormat.CRE_JSON);
	}

	@FXML public void OnMenuFileExportWoS() throws IOException {
		saveFile(ExportFormat.WOS_TXT);
	}

	@FXML public void OnMenuFileExportScopus() throws IOException {
		saveFile(ExportFormat.SCOPUS_CSV);
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
		
		int a=0;

		for (TableColumn<CRType, ?> x: tableView.getColumns()) { 
			System.out.println((++a) + " " + x.getText());
		}
		
		new Settings()
			.showAndWait()
			.ifPresent( noOfErrors -> {
				
				for (int i=0; i<crChart.length; i++) {
					crChart[i].setVisible(UserSettings.get().getChartEngine()==i);
				}
				
				
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
		new Info(crTable.getInfo()).showAndWait();	
	}
	
	
	@FXML public void OnMenuDataShowCRswoYears(ActionEvent event) {
		crTable.setShowNull(((CheckMenuItem)event.getSource()).isSelected());
	}

	
	@FXML public void OnMenuDataFilterByRPY(ActionEvent event) {
		new Range("Filter Cited References", "Select Range of Cited References Years", UserSettings.RangeType.FilterByRPYRange, crTable.getMaxRangeYear())
			.showAndWait()
			.ifPresent( range -> { 
				crTable.filterByYear(range); 
			}
		);
	}
	
	
	@FXML public void OnMenuDataRemoveSelected() {
		
		List<CRType> toDelete = tableView.getSelectionModel().getSelectedItems();
		int n = toDelete.size();
		new ConfirmAlert("Remove Cited References", n==0, new String[] {"No Cited References selected.", String.format("Would you like to remove all %d selected Cited References?", n)})
			.showAndWait()
			.ifPresent( confirmed -> {
				if (confirmed) crTable.remove(toDelete);
			}
		);
	}

	
	@FXML public void OnMenuDataRemovewoYears() {
		
		int n = crTable.getNumberWithoutYear();
		new ConfirmAlert("Remove Cited References", n==0, new String[] {"No Cited References w/o Year.", String.format("Would you like to remove all %d Cited References w/o Year?", n)})
			.showAndWait()
			.ifPresent( confirmed -> {
				if (confirmed) crTable.removeWithoutYear();
			}
		);
	}

	
	@FXML public void OnMenuDataRemoveByRPY() {
		
		final String header = "Remove Cited References";  
		new Range(header, "Select Range of Cited References Years", UserSettings.RangeType.RemoveByRPYRange, crTable.getMaxRangeYear())
			.showAndWait()
			.ifPresent( range -> { 
				long n =  crTable.getNumberByYear(range);
				new ConfirmAlert(header, n==0, new String[] {String.format ("No Cited References with Cited Reference Year between %d and %d.", range[0], range[1]), String.format("Would you like to remove all %1$d Cited References w/o Year?", n, range[0], range[1])})
					.showAndWait()
					.ifPresent( confirmed -> {
						if (confirmed) crTable.removeByYear(range[0], range[1]);
					}
				);
			}
		);
	}

	
	@FXML public void OnMenuDataRemoveByNCR() {

		final String header = "Remove Cited References";  
		new Range(header, "Select Number of Cited References", UserSettings.RangeType.RemoveByNCRRange, crTable.getMaxRangeNCR())
			.showAndWait()
			.ifPresent( range -> { 
				long n =  crTable.getNumberByNCR(range);
				new ConfirmAlert(header, n==0, new String[] {String.format ("No Cited References with Number of Cited References between %d and %d.", range[0], range[1]), String.format("Would you like to remove all %d Cited References with Number of Cited References between %d and %d?", n, range[0], range[1])})
					.showAndWait()
					.ifPresent( confirmed -> {
						if (confirmed) crTable.removeByNCR(range);
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
				long n =  crTable.getNumberByPercentYear(comp, threshold);
				new ConfirmAlert(header, n==0, new String[] {String.format ("No Cited References with Percent in Year %s %.1f%%.", comp, 100*threshold), String.format("Would you like to remove all %d Cited References with Percent in Year %s %.1f%%?", n, comp, 100*threshold)})
					.showAndWait()
					.ifPresent( confirmed -> {
						if (confirmed) crTable.removeByPercentYear(comp, threshold);
					}
				);
			}
		);
	}


	@FXML public void OnMenuDataRetainSelected() {
		
		List<CRType> toDelete = tableView.getSelectionModel().getSelectedItems();
		int n = toDelete.size();
		new ConfirmAlert("Remove Publications", n==0, new String[] {"No Cited References selected.", String.format("Would you like to remove all citing publications that do not cite any of the selected %d Cited References?", n)})
			.showAndWait()
			.ifPresent( confirmed -> {
				if (confirmed) crTable.removeByCR(toDelete);
			}
		);
	}

	
	@FXML public void OnMenuDataRetainByRPY() {
		
		new Range("Retain Publications", "Select Range of Citing Publication Years", UserSettings.RangeType.RetainByRPYRange, crTable.getMaxRangeCitingYear())
			.showAndWait()
			.ifPresent( range -> { 
				long n =  crTable.getNumberOfPubs() - crTable.getNumberOfPubsByCitingYear(range);
				new ConfirmAlert("Remove Publications", n==0, new String[] {String.format ("All Citing Publication Years are between between %d and %d.", range[0], range[1]), String.format("Would you like to remove all %d citing publications with publication year lower than %d or higher than %d?", n, range[0], range[1])})
					.showAndWait()
					.ifPresent( confirmed -> {
						if (confirmed) crTable.removeByCitingYear(range);
					}
				);
			}
		);
	}






	





	
	
	
}

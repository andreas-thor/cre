package cre.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileFilter;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.CRType;
import cre.test.data.source.CRE_json;
import cre.test.data.source.Scopus_csv;
import cre.test.data.source.WoS_txt;
import cre.test.ui.CRChart;
import cre.test.ui.CRTableView;
import cre.test.ui.StatusBar;
import cre.test.ui.UserSettings;
import cre.test.ui.UserSettings.RangeType;
import cre.test.ui.dialog.ConfirmAlert;
import cre.test.ui.dialog.Info;
import cre.test.ui.dialog.Range;
import cre.test.ui.dialog.Settings;
import cre.test.ui.dialog.Threshold;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

public class Main {

	StatusBar stat;
	CRTable crTable;
	CRChart crChart;
	
	
	@FXML Label lab;
	@FXML GridPane mainPane;
	@FXML Label sblabel;
	@FXML ProgressBar sbpb;
	@FXML Label sbinfo;
	@FXML ScrollPane scrollTab;
	@FXML CRTableView tableView;

	@FXML GridPane chartPane;
	@FXML GridPane tablePane;
	
	
	
	@FXML public void updateTable () {
		
		
	}
	

	
	public interface EventCRFilter {
		public void onUpdate (Integer yearMin, Integer yearMax);
	}
	
	@FXML public void initialize() {
	
		stat = new StatusBar(sblabel, sbpb, sbinfo);
				

		crTable = new CRTable(stat, new EventCRFilter() {
			@Override
			public void onUpdate(Integer yearMin, Integer yearMax) {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						tableView.setItems(FXCollections.observableArrayList(crTable.crData.stream().filter(cr -> cr.getVI()).collect(Collectors.toList())));
						crChart.adjustDomainRange (yearMin, yearMax);
											}
				});
			}
		});
		
		
		tableView = new CRTableView();
		
		tablePane.add (tableView, 0, 0);
		

		crChart = new CRChart(crTable, tableView);
		chartPane.add(crChart.getViewer(), 0, 0);
		

		
	}
	

    
    private void openFile (String source, String title, boolean multipleFiles, FileFilter filter) throws IOException {
    	
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(title);
    	
    	// set initial directory
    	if (UserSettings.get().getLastFileDir().exists()) {
    		fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
    	}
    	
    	final List<File> files = new ArrayList<File>();
    	if (multipleFiles) {
    		List<File> selFiles = fileChooser.showOpenMultipleDialog(CitedReferencesExplorer.stage);
    		if (selFiles != null) files.addAll(selFiles);
    	} else {
    		File selFile = fileChooser.showOpenDialog(CitedReferencesExplorer.stage);
    		if (selFile != null) files.add(selFile);
    	}
    	
    	
    	if (files.size()>0) {
    		
    		// save last directory to be used as initial directory
    		UserSettings.get().setLastFileDir(files.get(0).getParentFile());
    		
			Runnable runnable = new Runnable() {
				public void run() {
					try { 
						switch (source) {
							case "CRE_json": CRE_json.load (files.get(0), crTable, stat); break;
							case "WoS_txt": WoS_txt.load(files, crTable, stat, UserSettings.get().getMaxCR(), UserSettings.get().getRange(RangeType.ImportYearRange)); break;
							case "Scopus_csv": Scopus_csv.load(files, crTable, stat, UserSettings.get().getMaxCR(), UserSettings.get().getRange(RangeType.ImportYearRange)); break;
					}
						
					} catch (FileTooLargeException e1) {
						Platform.runLater( () -> {
							new ConfirmAlert("Error during file import!", true, new String[] {String.format("You try to import too many cited references. Import was aborted after loading %d Cited References. You can change the maximum number in the File > Settings > Import menu. ", e1.numberOfCRs)}).showAndWait();
						});
					} catch (UnsupportedFileFormatException e4) {
						Platform.runLater( () -> {
							new ConfirmAlert("Error during file import!", true, new String[] {"Unsupported File Format."}).showAndWait();
						});
					} catch (AbortedException e2) {
						Platform.runLater( () -> {
							new ConfirmAlert("Error during file import!", true, new String[] {"File Import aborted."}).showAndWait();
						});
					} catch (OutOfMemoryError mem) {
						crTable.init();
						Platform.runLater( () -> {
							new ConfirmAlert("Error during file import!", true, new String[] {"Out of Memory Error."}).showAndWait();
						});
					} catch (Exception e3) {
						Platform.runLater( () -> {

							Alert alert = new Alert(AlertType.ERROR, "Exception while loading file!");
	
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							e3.printStackTrace(pw);
	
							TextArea textArea = new TextArea(sw.toString());
							textArea.setEditable(false);
							textArea.setWrapText(true);
	
							textArea.setMaxWidth(Double.MAX_VALUE);
							textArea.setMaxHeight(Double.MAX_VALUE);
							GridPane.setVgrow(textArea, Priority.ALWAYS);
							GridPane.setHgrow(textArea, Priority.ALWAYS);
	
							GridPane expContent = new GridPane();
							expContent.setMaxWidth(Double.MAX_VALUE);
							expContent.add(new Label("The exception stacktrace was:"), 0, 0);
							expContent.add(textArea, 0, 1);
	
							// Set expandable Exception into the dialog pane.
							alert.getDialogPane().setExpandableContent(expContent);
	
							alert.showAndWait();
						});
					}
					
//    						sb.showNull.selected = true
//    						UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
//    						(tab.getModel() as AbstractTableModel).fireTableDataChanged()
//    						uisetting.setLastDirectory((multipleFiles ? dlg.getSelectedFiles()[0] : dlg.getSelectedFile()).getParentFile() )
				 
					 crTable.filterByYear();
					 Platform.runLater(() -> { OnMenuDataInfo(); });
				}
			};
			
			Thread t = new Thread(runnable);
			t.start();
//    				wait.visible = true
		
		}
    	
    		
    }
    
    
    /*
     * FILE Menu
     */
    
	@FXML public void OnMenuFileOpen() throws IOException {
		 openFile("CRE_json", "Open CRE File", false, null);
	}

	@FXML public void OnMenuFileImportWoS(ActionEvent event) throws IOException {
		 openFile("WoS_txt", "Import Web of Science Files", true, null);
	}

	@FXML public void OnMenuFileImportScopus(ActionEvent event) throws IOException {
		 openFile("Scopus_csv", "Import Scopus Files", true, null);
	}
	

	@FXML public void OnMenuFileSave() {
		
	}



	@FXML public void OnMenuFileSaveAs() {
		
		
	}

	@FXML public void OnMenuFileExportWoS() {}



	@FXML public void OnMenuFileExportScopus() {}



	@FXML public void OnMenuFileExportCSVCR() {}



	@FXML public void OnMenuFileExportCSVPub() {}



	@FXML public void OnMenuFileExportCSVAll() {}
	
	
	
	
	@FXML public void OnMenuFileSettings() throws IOException {
		new Settings()
			.showAndWait()
			.ifPresent( noOfErrors -> {
				// TODO: Apply settings changes
			}
		);
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
				crTable.filterByYear(range[0], range[1]); 
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

package cre.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.print.attribute.standard.DialogTypeSelection;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.fx.ChartViewer;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRCluster;
import cre.test.data.CRTable;
import cre.test.data.CRType;
import cre.test.data.source.CRE_json;
import cre.test.data.source.Scopus_csv;
import cre.test.data.source.WoS_txt;
import cre.test.ui.CRChart;
import cre.test.ui.StatusBar;
import cre.test.ui.UserSettings;
import cre.test.ui.dialog.ConfirmAlert;
import cre.test.ui.dialog.Info;
import cre.test.ui.dialog.Range;
import cre.test.ui.dialog.Settings;
import cre.test.ui.dialog.Threshold;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main {

	StatusBar stat;
	CRTable crTable;
	CRChart crChart;
	
	FilteredList<CRType> x;
	
	@FXML Text actiontarget;
	@FXML Label lab;
	@FXML GridPane mainPane;
	@FXML Label sblabel;
	@FXML ProgressBar sbpb;
	@FXML Label sbinfo;
	@FXML ScrollPane scrollTab;
	@FXML TableView<CRType> tableView;


	@FXML TableColumn<CRType, Number> colVI;
	@FXML TableColumn<CRType, Number> colCO;
	@FXML TableColumn<CRType, Number> colID;
	@FXML TableColumn<CRType, String> colCR;
	@FXML TableColumn<CRType, Number> colRPY;
	@FXML TableColumn<CRType, Number> colN_CR;
	
	@FXML TableColumn<CRType, Number> colPERC_YR;
	@FXML TableColumn<CRType, Number> colPERC_ALL;
	
	@FXML TableColumn<CRType, String> colAU;
	@FXML TableColumn<CRType, String> colAU_L;
	@FXML TableColumn<CRType, String> colAU_F;
	@FXML TableColumn<CRType, String> colAU_A;
	@FXML TableColumn<CRType, String> colTI;
	@FXML TableColumn<CRType, String> colJ;
	@FXML TableColumn<CRType, String> colJ_N;
	@FXML TableColumn<CRType, String> colJ_S;
	@FXML TableColumn<CRType, String> colVOL;
	@FXML TableColumn<CRType, String> colPAG;
	@FXML TableColumn<CRType, String> colDOI;
	
	@FXML TableColumn<CRType, CRCluster> colCID2;
	@FXML TableColumn<CRType, Number> colCID_S;
	
	
	/* TODO */
	@FXML TableColumn<CRType, Integer> colN_PYEARS;
	@FXML TableColumn<CRType, Double> colPYEAR_PERC;
	@FXML TableColumn<CRType, Integer> colN_PCT50;
	@FXML TableColumn<CRType, Integer> colN_PCT75;
	@FXML TableColumn<CRType, Integer> colN_PCT90;
	@FXML TableColumn<CRType, Integer> colN_PYEARS2;
//	@FXML SwingNode swingChart;
//	@FXML ChartViewer chartViewer;
	@FXML GridPane chartPane;
	
	
	
	@FXML public void updateTable () {
		
		
	}
	

	
	public interface EventCRFilter {
		public void onUpdate (Integer yearMin, Integer yearMax);
	}
	
	@FXML public void initialize() {
	
		
		colAU.setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

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
		
		
/*		
		x = new FilteredList<CRType>(crTable.crDataObserved);
//		x.setPredicate(t -> t.getVI());
		
//		x = new FilteredList<>(crTable.crData, t -> ! t.getVI());
		
//		FilteredList<Item> list = new FilteredList<>(baseList, t -> ! t.filteredProperty().get());
		
		
		x.setPredicate(new Predicate<CRType>() {
			@Override
			public boolean test(CRType t) {
				return t.getVI();
			}
		});
		SortedList<CRType> y = new SortedList<CRType>(x);
//		
		y.comparatorProperty().bind(tableView.comparatorProperty());
		tableView.setItems(y);
		*/
		
//		colVI.setCellValueFactory(cellData -> cellData.getValue().getVIProp());
//		colCO.setCellValueFactory(cellData -> cellData.getValue().getCOProp());
		colID.setCellValueFactory(cellData -> cellData.getValue().getIDProp());
		
		
		colCR.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCR()));
		colRPY.setCellValueFactory(cellData -> cellData.getValue().getRPYProp());
		colN_CR.setCellValueFactory(cellData -> cellData.getValue().getN_CRProp());
		 
		colPERC_YR.setCellValueFactory(cellData -> cellData.getValue().getPERC_YRProp());
		colPERC_ALL.setCellValueFactory(cellData -> cellData.getValue().getPERC_ALLProp());
		
		colAU.setCellValueFactory(cellData -> cellData.getValue().getAUProp());
		colAU_L.setCellValueFactory(cellData -> cellData.getValue().getAU_LProp());
		colAU_F.setCellValueFactory(cellData -> cellData.getValue().getAU_FProp());
		colAU_A.setCellValueFactory(cellData -> cellData.getValue().getAU_AProp());
		colTI.setCellValueFactory(cellData -> cellData.getValue().getTIProp());
		colJ.setCellValueFactory(cellData -> cellData.getValue().getJProp());
		colJ_N.setCellValueFactory(cellData -> cellData.getValue().getJ_NProp());
		colJ_S.setCellValueFactory(cellData -> cellData.getValue().getJ_SProp());
		
		
		colVOL.setCellValueFactory(cellData -> cellData.getValue().getVOLProp());
		colPAG.setCellValueFactory(cellData -> cellData.getValue().getPAGProp());
		colDOI.setCellValueFactory(cellData -> cellData.getValue().getDOIProp());	

		colCID2.setCellValueFactory(cellData -> cellData.getValue().getCID2());
		colCID_S.setCellValueFactory(cellData -> cellData.getValue().getCID_SProp());
		
//		colN_PYEARS.setCellValueFactory(cellData -> cellData.getValue().propN_PYEARS);
//		colPYEAR_PERC.setCellValueFactory(cellData -> cellData.getValue().propPYEAR_PERC);
		
		
//		colN_PCT50.setCellValueFactory(cellData -> cellData.getValue().propN_PCT50);
//		colN_PCT75.setCellValueFactory(cellData -> cellData.getValue().propN_PCT75);
//		colN_PCT90.setCellValueFactory(cellData -> cellData.getValue().propN_PCT90);
//		colN_PYEARS2.setCellValueFactory(cellData -> cellData.getValue().propN_PYEARS2);
		
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


		crChart = new CRChart(crTable, tableView, colRPY, colN_CR);
		chartPane.add(crChart.getViewer(), 0, 0);
		

		
	}
	
    @FXML protected void handleAboutAction(ActionEvent event) {
        actiontarget.setText("Sign in button pressed");
    }

    
    private void openFile (String source, String title, boolean multipleFiles, FileFilter filter) throws IOException {
    	
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(title);
    	final List<File> files = new ArrayList<File>();
    	if (multipleFiles) {
    		List<File> selFiles = fileChooser.showOpenMultipleDialog(CitedReferencesExplorer.stage);
    		if (selFiles != null) files.addAll(selFiles);
    	} else {
    		File selFile = fileChooser.showOpenDialog(CitedReferencesExplorer.stage);
    		if (selFile != null) files.add(selFile);
    	}
    	
    	
    	if (files.size()>0) {
    		
			Runnable runnable = new Runnable() {
				public void run() {
					try { 
						switch (source) {
							case "CRE_json": CRE_json.load (files.get(0), crTable, stat); break;
							case "WoS_txt": WoS_txt.load(files, crTable, stat, 10, new int[] { 0, 0 }); break;
							case "Scopus_csv": Scopus_csv.load(files, crTable, stat, 0, new int[] { 0, 0 }); break;
					}
						
					} catch (FileTooLargeException e1) {
						Platform.runLater( () -> {
							Alert alert = new Alert(AlertType.ERROR, String.format("You try to import too many cited references. Import was aborted after loading %d Cited References. You can change the maximum number in the File > Settings > Miscellaneous menu. ", e1.numberOfCRs)); 
							alert.setHeaderText("Error during file import!");
							alert.showAndWait();
						});
					} catch (UnsupportedFileFormatException e4) {
						Platform.runLater( () -> {
							Alert alert = new Alert(AlertType.ERROR, "Unsupported File Format."); 
							alert.setHeaderText("Error during file import!");
							alert.showAndWait();
						});
					} catch (AbortedException e2) {
						Platform.runLater( () -> {
							Alert alert = new Alert(AlertType.ERROR, "File Import aborted."); 
							alert.setHeaderText("Error during file import!");
							alert.showAndWait();
						});
					} catch (OutOfMemoryError mem) {
						crTable.init();
						Platform.runLater( () -> {
							Alert alert = new Alert(AlertType.ERROR, "Out of Memory Error."); 
							alert.setHeaderText("Error during file import!");
							alert.showAndWait();
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
	
	@FXML public void OnMenuFileSettings() throws IOException {
		

		new Settings().showAndWait();
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
		new Range("Filter Cited References", "Select Range of Cited References Years", UserSettings.get().filterByRPYRange, crTable.getMaxRangeYear())
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
		new Range(header, "Select Range of Cited References Years", UserSettings.get().removeByRPYRange, crTable.getMaxRangeYear())
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
		new Range(header, "Select Number of Cited References", UserSettings.get().removeByNCRRange, crTable.getMaxRangeNCR())
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
		
		new Range("Retain Publications", "Select Range of Citing Publication Years", UserSettings.get().retainByRPYRange, crTable.getMaxRangeCitingYear())
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

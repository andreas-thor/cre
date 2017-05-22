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
import cre.test.data.CRSearch;
import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.data.match.CRMatch2;
import cre.test.data.match.CRMatch2.ManualMatchType2;
import cre.test.data.source.ExportFormat;
import cre.test.data.source.ImportFormat;
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
import cre.test.ui.dialog.Search;
import cre.test.ui.dialog.Settings;
import cre.test.ui.dialog.TextInput;
import cre.test.ui.dialog.Threshold;
import cre.test.ui.dialog.Wait;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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

	@FXML MenuItem noOfVisibleCRs;
	@FXML CheckMenuItem showWOYear;


	@FXML
	public void initialize() throws IOException {

		
		
		creFile = null;
		crTable = CRTable.get();
		statPane.add(StatusBar.get(), 0, 0);

		StatusBar.get().setOnUpdateInfo(x -> {
			noOfVisibleCRs.setDisable(CRStats.getSize() == CRStats.getNumberByVisibility(true));
			noOfVisibleCRs.setText(String.format("Show all Cited References (currently %d of %d)",
					CRStats.getNumberByVisibility(true), CRStats.getSize()));
		});

		tableView = new CRTableView();
		tablePane.add(tableView, 0, 1);
		tableView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				OnMenuDataRemoveSelected();
			}
			if (e.getCode() == KeyCode.SPACE) {
				List<CRType> sel = getSelectedCRs();
				if (sel.size() == 1) {
					new CRInfo(sel.get(0)).showAndWait();
				}
			}
		});

		matchView = new MatchPanel() {

			@Override
			public void onUpdateClustering(double threshold, boolean useClustering, boolean useVol, boolean usePag,
					boolean useDOI) {
				if (t != null)
					t.interrupt();
				t = new Thread(() -> {
					CRMatch2.get().updateClustering(CRMatch2.ClusteringType2.REFRESH, null, threshold, useVol, usePag,
							useDOI);
					refreshTableValues();
				});
				t.start();
			}

			@Override
			public void onMatchManual(ManualMatchType2 type, double threshold, boolean useVol, boolean usePag,
					boolean useDOI) {

				List<CRType> toMatch = getSelectedCRs();
				if ((toMatch.size() == 0) || ((toMatch.size() == 1) && (type != ManualMatchType2.EXTRACT))) {
					new ConfirmAlert("Error during clustering!", true,
							new String[] { "Too few Cited References selected!" }).showAndWait();
				} else {
					if ((toMatch.size() > 5) && (type != ManualMatchType2.EXTRACT)) {
						new ConfirmAlert("Error during clustering!", true,
								new String[] { "Too many Cited References selected (at most 5)!" }).showAndWait();
					} else {
						CRMatch2.get().addManuMatching(toMatch, type, threshold, useVol, usePag, useDOI);
						refreshTableValues();
					}
				}
			}

			@Override
			public void onMatchUnDo(double threshold, boolean useVol, boolean usePag, boolean useDOI) {
				CRMatch2.get().undoManuMatching(threshold, useVol, usePag, useDOI);
				refreshTableValues();
			}

		};
		matchView.setVisible(false);
		tablePane.add(matchView, 0, 0);

		crChart = new CRChart[] { new CRChart_JFreeChart() {
			@Override
			protected void onSelectYear(int year) {
				tableView.orderByYearAndSelect(year);
			}

			@Override
			protected void onYearRangeFilter(double min, double max) {
				filterByRPY(new int[] { (int) Math.ceil(min), (int) Math.floor(max) }, true);
			}
		}, new CRChart_HighCharts() {
			@Override
			protected void onSelectYear(int year) {
				tableView.orderByYearAndSelect(year);
			}

			@Override
			protected void onYearRangeFilter(double min, double max) {
				filterByRPY(new int[] { (int) Math.ceil(min), (int) Math.floor(max) }, true);
			}
		} };
		for (int i = 0; i < crChart.length; i++) {
			chartPane.add(crChart[i].getNode(), 0, 0);
			crChart[i].setVisible(UserSettings.get().getChartEngine() == i);
		}

		// save user settings when exit
		CitedReferencesExplorer.stage.setOnCloseRequest(event -> {

			event.consume();

			Alert exit = new Alert(AlertType.CONFIRMATION);
			exit.setTitle("Warning");
			exit.setHeaderText("Save before exit?");
			exit.getDialogPane().getButtonTypes().clear();
			exit.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

			exit.showAndWait().ifPresent(button -> {

				if (button == ButtonType.CANCEL)
					return;

				if (button == ButtonType.YES) {
					try {
						if (!saveFile(ExportFormat.CRE_JSON, false))
							return;
					} catch (Exception e) {
						Platform.runLater(() -> {
							new ExceptionStacktrace("Error during file export!", e).showAndWait();
						});
						return;
					}
				}

				UserSettings.get().saveUserPrefs(CitedReferencesExplorer.stage.getWidth(),
						CitedReferencesExplorer.stage.getHeight(), CitedReferencesExplorer.stage.getX(),
						CitedReferencesExplorer.stage.getY());
				CitedReferencesExplorer.stage.close();
			});
		});

		if (CitedReferencesExplorer.loadOnOpen != null) {
			openFile(ImportFormat.CRE_JSON, CitedReferencesExplorer.loadOnOpen);
		}

	}


	private void filterByRPY(int[] range, boolean fromChart) {
		UserSettings.get().setRange(RangeType.FilterByRPYRange, range);
		crTable.filterByYear(range);
		updateTableCRList(fromChart);
	}
	
	private void updateTableCRList() {
		updateTableCRList(false);
	}	

	private void updateTableCRList(boolean triggeredByChartZoom) {
		Platform.runLater(() -> {

			// save sort order ...
			List<TableColumn<CRType, ?>> oldSort = new ArrayList<TableColumn<CRType, ?>>();
			tableView.getSortOrder().forEach(it -> oldSort.add(it));
	
			// ... update rows ...
			tableView.setItems(FXCollections.observableArrayList(crTable.getCR().filter(cr -> cr.getVI()).collect(Collectors.toList())));
	
			// ... reset old sort order
			for (TableColumn<CRType, ?> x : oldSort) {
				tableView.getSortOrder().add(x);
			}
	
			// set Domain Range for charts
			if (!triggeredByChartZoom) {
				Stream.of(crChart).forEach(it -> {
					if (it.isVisible()) {
						it.updateData(crTable.getChartData());
						it.setDomainRange(CRStats.getMaxRangeYear(true));
					}
				});
			}
	
			refreshTableValues();
		});
	}

	private void refreshTableValues() {
		Platform.runLater(() -> {
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
	 * http://stackoverflow.com/questions/36353518/javafx-table-view-multiple-
	 * selection-sometimes-skips-one-of-the-items/38207246#38207246 Apparently
	 * it's a bug, already fixed for version 9 (and also 8u112, if I understand
	 * correctly): https://bugs.openjdk.java.net/browse/JDK-8144501 A workaround
	 * for now is to use getSelectedIndices(), then get the items corresponding
	 * to these instances from table.getItems() return
	 * tableView.getSelectionModel().getSelectedIndices().stream().map(idx ->
	 * tableView.getItems().get(idx)).collect(Collectors.toList()); ==> GEHT
	 * AUCH NICHT
	 */
	private List<CRType> getSelectedCRs() {
		return tableView.getSelectionModel().getSelectedItems().stream().filter(cr -> cr != null).collect(Collectors.toList());
	}

	private void openFile(ImportFormat source) throws IOException {
		openFile(source, null);
	}

	private void openFile(ImportFormat source, String fileName) throws IOException {

		final List<File> files = new ArrayList<File>();

		if (fileName != null) { // load specific file (e.g., during appilcation start)
			files.add(new File(fileName));
		} else {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(source.label);
			fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
			fileChooser.getExtensionFilters().add(source.filter);
			fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[] { "*.*" })));

			if (source.multiple) {
				List<File> selFiles = fileChooser.showOpenMultipleDialog(CitedReferencesExplorer.stage);
				if (selFiles != null) files.addAll(selFiles);
			} else {
				File selFile = fileChooser.showOpenDialog(CitedReferencesExplorer.stage);
				if (selFile != null) files.add(selFile);
			}
		}

		if (files.size() > 0) {
			this.creFile = null;
			UserSettings.get().setLastFileDir(files.get(0).getParentFile()); // save last directory to be uses as initial directory 
			
			Wait wait = new Wait();
			Service<Void> serv = new Service<Void>() {

				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {

						@Override
						protected Void call() throws Exception {
							source.load(files);
							if (source == ImportFormat.CRE_JSON) creFile = files.get(0); 
								
							// show match panel if applicable
							matchView.setVisible((CRMatch2.get().getSize(true) + CRMatch2.get().getSize(false)) > 0);
							matchView.updateClustering();
							
							return null;
						}
					};
				}
			};
			

			serv.setOnSucceeded((WorkerStateEvent t) -> {
				OnMenuViewInfo();
				updateTableCRList();
				CitedReferencesExplorer.stage.setTitle(CitedReferencesExplorer.title + ((creFile == null) ? "" : " - " + creFile.getAbsolutePath()));
				wait.close();
			});

			
			serv.setOnFailed((WorkerStateEvent t) -> {
				Throwable e = t.getSource().getException(); 
				if (e instanceof FileTooLargeException) {
					new ConfirmAlert("Error during file import!", true, new String[] { String.format(
						"You try to import too many cited references. Import was aborted after loading %d Cited References and %d Citing Publications. You can change the maximum number in the File > Settings > Import menu. ",
						((FileTooLargeException)e).numberOfCRs, ((FileTooLargeException)e).numberOfPubs) }).showAndWait();
				} else if (e instanceof UnsupportedFileFormatException) {
					new ConfirmAlert("Error during file import!", true, new String[] { "Unsupported File Format." }).showAndWait();
				} else if (e instanceof AbortedException) {
					new ConfirmAlert("Error during file import!", true, new String[] { "File Import aborted." }).showAndWait();
				} else if (e instanceof OutOfMemoryError) {
					crTable.init();
					new ConfirmAlert("Error during file import!", true, new String[] { "Out of Memory Error." }).showAndWait();
				} else {
					new ExceptionStacktrace("Error during file import!", e).showAndWait();
				}
				wait.close();
			});
			
			serv.start();
			
			wait.showAndWait().ifPresent(cancel -> {
				crTable.setAborted(cancel);
			});

		}
	}

	private boolean saveFile(ExportFormat source) throws IOException {
		return saveFile(source, true);
	}

	private boolean saveFile(ExportFormat source, boolean saveAs) throws IOException {

		File selFile;
		if ((source == ExportFormat.CRE_JSON) && (creFile != null) && (!saveAs)) {
			selFile = creFile;
		} else {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(source.label);
			fileChooser.setInitialDirectory(UserSettings.get().getLastFileDir());
			fileChooser.getExtensionFilters().add(source.filter);
			fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[] { "*.*" })));
			selFile = fileChooser.showSaveDialog(CitedReferencesExplorer.stage);
		}

		if (selFile == null)
			return false;

		// save last directory to be uses as initial directory
		UserSettings.get().setLastFileDir(selFile.getParentFile()); 

		Service<Void> serv = new Service<Void>() {
			@Override 
			protected Task<Void> createTask() {
				return new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						source.save(selFile);
						if (source == ExportFormat.CRE_JSON) creFile = selFile;
						return null;
					}
				};
			}
		};

		serv.setOnSucceeded((WorkerStateEvent t) -> {
			CitedReferencesExplorer.stage.setTitle(CitedReferencesExplorer.title + ((creFile == null) ? "" : " - " + creFile.getAbsolutePath()));
		});

		serv.setOnFailed((WorkerStateEvent t) -> {
			Throwable e = t.getSource().getException(); 
			new ExceptionStacktrace("Error during file import!", e).showAndWait();
		});
		
		serv.start();		

		return true;
	}

	/*
	 * FILE Menu
	 */

	@FXML
	public void OnMenuFileOpen() throws IOException {
		openFile(ImportFormat.CRE_JSON);
	}

	@FXML
	public void OnMenuFileImportWoS(ActionEvent event) throws IOException {
		openFile(ImportFormat.WOS_TXT);
	}

	@FXML
	public void OnMenuFileImportScopus(ActionEvent event) throws IOException {
		openFile(ImportFormat.SCOPUS_CSV);
	}

	@FXML
	public void OnMenuFileSave() throws IOException {
		saveFile(ExportFormat.CRE_JSON, false);
	}

	@FXML
	public void OnMenuFileSaveAs() throws IOException {
		saveFile(ExportFormat.CRE_JSON, true);
	}

	@FXML
	public void OnMenuFileExportWoS() throws IOException {
		saveFile(ExportFormat.WOS_TXT);
	}

	@FXML
	public void OnMenuFileExportScopus() throws IOException {
		saveFile(ExportFormat.SCOPUS_CSV);
	}

	@FXML
	public void OnMenuFileExportCSVGraph() throws IOException {
		saveFile(ExportFormat.CRE_CSV_GRAPH);
	}

	@FXML
	public void OnMenuFileExportCSVCR() throws IOException {
		saveFile(ExportFormat.CRE_CSV_CR);
	}

	@FXML
	public void OnMenuFileExportCSVPub() throws IOException {
		saveFile(ExportFormat.CRE_CSV_PUB);
	}

	@FXML
	public void OnMenuFileExportCSVAll() throws IOException {
		saveFile(ExportFormat.CRE_CSV_CR_PUB);
	}

	@FXML
	public void OnMenuFileSettings() throws IOException {

		new Settings().showAndWait().ifPresent(noOfErrors -> {
			for (int i = 0; i < crChart.length; i++) {
				crChart[i].setVisible(UserSettings.get().getChartEngine() == i);
			}
			updateTableCRList();
			// TODO: Apply settings changes
		});
	}

	@FXML
	public void OnMenuFileExit() {
		CitedReferencesExplorer.stage.fireEvent(new WindowEvent(CitedReferencesExplorer.stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	/*
	 * VIEW Menu
	 */

	@FXML
	public void OnMenuViewInfo() {
		new Info().showAndWait();
	}

	@FXML
	public void OnMenuViewCR() {
		List<CRType> sel = getSelectedCRs();
		if (sel.size() == 1) {
			new CRInfo(sel.get(0)).showAndWait();
		} else {
			new ConfirmAlert("Remove Cited References", true, new String[] { String.format("%s Cited References selected.", (sel.size() == 0) ? "No" : "Too many") }).showAndWait();
		}
	}

	@FXML
	public void OnMenuViewPub() {
		List<CRType> sel = getSelectedCRs();
		if (sel.size() == 1) {
			new CRPubInfo(sel.get(0)).showAndWait();
		} else {
			new ConfirmAlert("Remove Cited References", true, new String[] { String.format("%s Cited References selected.", (sel.size() == 0) ? "No" : "Too many") }).showAndWait();
		}
	}

	@FXML
	public void OnMenuViewShowCRsWOYears(ActionEvent event) {
		crTable.setShowNull(((CheckMenuItem) event.getSource()).isSelected());
		updateTableCRList();
	}

	@FXML
	public void OnMenuViewFilterByRPY(ActionEvent event) {
		new Range("Filter Cited References", "Select Range of Cited References Years",
				UserSettings.RangeType.FilterByRPYRange, CRStats.getMaxRangeYear()).showAndWait().ifPresent(range -> {
					filterByRPY(range, false);
				});
	}


	@FXML
	public void OnMenuViewShowCluster() {
		List<CRType> sel = getSelectedCRs();
		if (sel.size() > 0) {

			crTable.filterByCluster (sel);
			updateTableCRList();
			
		} else {
			new ConfirmAlert("Filter Cited References by Cluster", true,
					new String[] { "No Cited References selected." }).showAndWait();
		}
	}
	
	
	@FXML
	public void OnMenuViewSearch() {
		
		try {
		
			new Search().showAndWait().ifPresent(query -> {
			
				try {
					
					
					CRSearch.get().search(query);
					tableView.orderBySearchResult();
					
//					Arrays.stream(id).forEach(it -> System.out.println(it));
//					List<CRType> show = crTable.getCR().filter(cr -> IntStream.of(id).anyMatch(it -> cr.getID() == it)).collect(Collectors.toList());
//					
//					System.out.println("Size = " + show.size());
					
//					crTable.filterByCR(null); // show);
					updateTableCRList();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	@FXML
	public void OnMenuViewShowAll() {
		showWOYear.setSelected(true);
		crTable.showAll();
		updateTableCRList();
	}
	

	/*
	 * DATA Menu
	 */
	
	@FXML
	public void OnMenuDataRemoveSelected() {

		List<CRType> toDelete = getSelectedCRs();
		int n = toDelete.size();
		new ConfirmAlert("Remove Cited References", n == 0,
				new String[] { "No Cited References selected.",
						String.format("Would you like to remove all %d selected Cited References?", n) }).showAndWait()
								.ifPresent(btn -> {
									if (btn == ButtonType.YES) {
										crTable.removeCR(toDelete);
										updateTableCRList();
									}
								});
	}

	@FXML
	public void OnMenuDataCopySelected() {

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

	@FXML
	public void OnMenuDataRemovewoYears() {

		int n = CRStats.getNumberWithoutYear();
		new ConfirmAlert("Remove Cited References", n == 0,
				new String[] { "No Cited References w/o Year.",
						String.format("Would you like to remove all %d Cited References w/o Year?", n) }).showAndWait()
								.ifPresent(btn -> {
									if (btn == ButtonType.YES) {
										crTable.removeCRWithoutYear();
										updateTableCRList();
									}
								});
	}

	@FXML
	public void OnMenuDataRemoveByRPY() {

		final String header = "Remove Cited References";
		new Range(header, "Select Range of Cited References Years", UserSettings.RangeType.RemoveByRPYRange,
				CRStats.getMaxRangeYear()).showAndWait().ifPresent(range -> {
					long n = CRStats.getNumberByYear(range);
					new ConfirmAlert(header, n == 0,
							new String[] {
									String.format("No Cited References with Cited Reference Year between %d and %d.",
											range[0], range[1]),
									String.format(
											"Would you like to remove all %d Cited References with Cited Reference Year between %d and %d?",
											n, range[0], range[1]) }).showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.YES) {
							crTable.removeCRByYear(range);
							updateTableCRList();
						}
					});
				});
	}

	@FXML
	public void OnMenuDataRemoveByNCR() {

		final String header = "Remove Cited References";
		new Range(header, "Select Number of Cited References", UserSettings.RangeType.RemoveByNCRRange,
				CRStats.getMaxRangeNCR()).showAndWait().ifPresent(range -> {
					long n = CRStats.getNumberByNCR(range);
					new ConfirmAlert(header, n == 0, new String[] {
							String.format("No Cited References with Number of Cited References between %d and %d.",
									range[0], range[1]),
							String.format(
									"Would you like to remove all %d Cited References with Number of Cited References between %d and %d?",
									n, range[0], range[1]) }).showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.YES) {
							crTable.removeCRByN_CR(range);
							updateTableCRList();
						}
					});
				});
	}

	@FXML
	public void OnMenuDataRemoveByPERC_YEAR() {

		final String header = "Remove Cited References";
		new Threshold(header, "Select Threshold for Percent in Year", "<", 0.0).showAndWait().ifPresent(cond -> {
			String comp = cond.getKey();
			double threshold = cond.getValue().doubleValue();
			long n = CRStats.getNumberByPercentYear(comp, threshold);
			new ConfirmAlert(header, n == 0,
					new String[] {
							String.format("No Cited References with Percent in Year %s %.1f%%.", comp, 100 * threshold),
							String.format(
									"Would you like to remove all %d Cited References with Percent in Year %s %.1f%%?",
									n, comp, 100 * threshold) }).showAndWait().ifPresent(btn -> {
				if (btn == ButtonType.YES) {
					crTable.removeCRByPERC_YR(comp, threshold);
					updateTableCRList();
				}
			});
		});
	}

	@FXML
	public void OnMenuDataRetainCRById() {

		new TextInput("Retain Cited References By Id", "Specify list if CR Ids").showAndWait().ifPresent(list -> {
			if (list != null) {
				int[] id = Arrays.stream(list.split("\\D")).mapToInt(Integer::valueOf).toArray();
				List<CRType> toRetain = crTable.getCR().filter(cr -> IntStream.of(id).anyMatch(it -> cr.getID() == it))
						.collect(Collectors.toList());
				crTable.retainCR(toRetain);
			}
		});
	}

	@FXML
	public void OnMenuDataRetainSelected() {

		List<CRType> toDelete = getSelectedCRs();
		int n = toDelete.size();
		new ConfirmAlert("Remove Publications", n == 0,
				new String[] { "No Cited References selected.",
						String.format(
								"Would you like to remove all citing publications that do not cite any of the selected %d Cited References?",
								n) }).showAndWait().ifPresent(btn -> {
									if (btn == ButtonType.YES) {
										crTable.removePubByCR(toDelete);
										updateTableCRList();
									}
								});
	}

	@FXML
	public void OnMenuDataRetainByRPY() {

		new Range("Retain Publications", "Select Range of Citing Publication Years",
				UserSettings.RangeType.RetainByRPYRange, CRStats.getMaxRangeCitingYear()).showAndWait()
						.ifPresent(range -> {
							long n = CRStats.getNumberOfPubs() - CRStats.getNumberOfPubsByCitingYear(range);
							new ConfirmAlert("Remove Publications", n == 0,
									new String[] {
											String.format("All Citing Publication Years are between between %d and %d.",
													range[0], range[1]),
											String.format(
													"Would you like to remove all %d citing publications with publication year lower than %d or higher than %d?",
													n, range[0], range[1]) }).showAndWait().ifPresent(btn -> {
								if (btn == ButtonType.YES) {
									crTable.removePubByCitingYear(range);
									updateTableCRList();
								}
							});
						});
	}

	/*
	 * Standardization Menu
	 */

	@FXML
	public void OnMenuStdCluster() {

		new Thread(() -> {
			CRMatch2.get().generateAutoMatching();
			matchView.setVisible(true);
			tablePane.requestLayout();
			matchView.updateClustering();
		}).start();
	}

	@FXML
	public void OnMenuStdMerge() {

		long n = CRStats.getSize() - CRStats.getNoOfClusters();
		new ConfirmAlert("Merge clusters", n == 0, new String[] { "No Clusters to merge.", String.format("Merging will aggregate %d Cited References! Are you sure?", n) }).showAndWait()
			.ifPresent(btn -> {
				if (btn == ButtonType.YES) {
					new Thread(() -> {
						crTable.merge();
						matchView.setVisible(false);
						updateTableCRList();
						// tablePane.requestLayout();
					}).start();
				}
			});
	}

	/*
	 * Help Menu
	 */

	@FXML
	public void OnMenuHelpManual() {
		HostServicesFactory.getInstance(CitedReferencesExplorer.app).showDocument(CitedReferencesExplorer.manual_url);
	}

	@FXML
	public void OnMenuHelpAbout() {
		new About().showAndWait();
	}


	
}

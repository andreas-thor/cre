package cre.test;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRCluster;
import cre.test.data.CRTable;
import cre.test.data.CRType;
import cre.test.data.source.CRE_json;
import cre.test.ui.ChartPanelFactory;
import cre.test.ui.StatusBar;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.Range;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class Main {

	StatusBar stat;
	CRTable crTable;
	ChartViewer chart;
	
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
	
	public interface EventStatusBar {
		public void onUpdate (String label, Double value, String info); 
	}
	
	public interface EventCRFilter {
		public void onUpdate (Integer yearMin, Integer yearMax);
	}
	
	@FXML public void initialize() {
	
		
		colAU.setComparator(new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		
		stat = new StatusBar(new EventStatusBar() {
			@Override
			public void onUpdate(String label, Double value, String info) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						sblabel.setText(label);
						sbpb.setProgress(value);
						if (info!= null) {
							sbinfo.setText(info);
						}
						
					}
				});
				
			}
		}); 
				

		crTable = new CRTable(stat, new EventCRFilter() {
			@Override
			public void onUpdate(Integer yearMin, Integer yearMax) {
				// no lambda function because we always need a new (copy of the) predicate
//				x.setPredicate(new Predicate<CRType>() {
//					@Override
//					public boolean test(CRType t) {
//						return t.getVI();
//					}
//				});
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						tableView.setItems(FXCollections.observableArrayList(crTable.crData.stream().filter(cr -> cr.getVI()).collect(Collectors.toList())));
						if ((yearMin!=null) && (yearMax!=null)) {
							Range dAxisRange = chart.getChart().getXYPlot().getDomainAxis().getRange();
							if ((((int)Math.ceil (dAxisRange.getLowerBound())) != yearMin.intValue()) || (((int)Math.floor(dAxisRange.getUpperBound())) != yearMax.intValue())) { 
								
								System.out.println("Adjusting");
								System.out.println("Axis = " + dAxisRange.toString());
								System.out.println("Year = " + yearMin + ", " + yearMax);
								crTable.duringUpdate = true;
								chart.getChart().getXYPlot().getDomainAxis().setRange(yearMin, yearMax);
								crTable.duringUpdate = false;

							}
						}
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
		chart = ChartPanelFactory.create(crTable, tableView, colRPY, colN_CR);
		chartPane.add(chart, 0, 0);
		

		
		
	}
	
    @FXML protected void handleAboutAction(ActionEvent event) {
        actiontarget.setText("Sign in button pressed");
    }

    
    private void openFile (String source, String title, boolean multipleFiles, FileFilter filter) throws IOException {
    	
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle(title);
    	File file = fileChooser.showOpenDialog(CitedReferencesExplorer.stage);
    	if (file != null) {
    		
			Runnable runnable = new Runnable() {
				public void run() {
					try { 
						CRE_json.load (file, crTable, stat);
//					} catch (FileTooLargeException e1) {
//						JOptionPane.showMessageDialog(null, "You try to import too many cited references.\nImport was aborted after loading ${e1.numberOfCRs} Cited References.\nYou can change the maximum number in the File > Settings > Miscellaneous menu. " );
//					} catch (UnsupportedFileFormatException e4) {
//						JOptionPane.showMessageDialog(null, "Unknown file format." );
//					} catch (AbortedException e2) {
					} catch (OutOfMemoryError mem) {
						crTable.init();
						JOptionPane.showMessageDialog(null, "Out Of Memory Error" );
					} catch (Exception e3) {
						e3.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error while loading file.\n(${e3.toString()})" );
					}
					
//    						sb.showNull.selected = true
//    						UIDialogFactory.createInfoDlg(mainFrame, crTable.getInfo()).visible = true
//    						(tab.getModel() as AbstractTableModel).fireTableDataChanged()
//    						uisetting.setLastDirectory((multipleFiles ? dlg.getSelectedFiles()[0] : dlg.getSelectedFile()).getParentFile() )
				 
					 crTable.filterByYear();

				}
			};
			
			Thread t = new Thread(runnable);
			t.start();
//    				wait.visible = true
		
		}
    	
    		
    }
    
    
	@FXML public void OnMenuOpen() throws IOException {
//		stat.initProgressbar(100, "hjhj");
		 openFile("CRE_json", "Open CRE File", false, null);
		
	}

	@FXML public void OnMenuImportWoS(ActionEvent event) {
		
		crTable.crData.get(0).setRPY(999);
		tableView.sort();
		
	}

	@FXML public void OnMenuDataShowCRswoYears(ActionEvent event) {
		crTable.setShowNull(((CheckMenuItem)event.getSource()).isSelected());
	}

	@FXML public void OnMenuDataFilterByRPY(ActionEvent event) {
//		crTable.filterByYear(2000, 3000);
		
		
		Optional<Integer[]> result = new cre.test.ui.dialog.Range(0, 2016, crTable.getMaxRangeCitingYear()).showAndWait();
		result.ifPresent( range -> { crTable.filterByYear(range[0], range[1]); });
		
		
	}
	
}

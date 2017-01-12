package cre.test.ui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import cre.test.data.CRCluster;
import cre.test.data.CRType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

public class CRTableView extends TableView<CRType> {

	// Mapping of internal attribute names to labels
	public static final Map<String, String> attr;
    static {
        Map<String, String> aMap = new LinkedHashMap<String, String>();
		aMap.put("ID", "ID");
		aMap.put("CR", "Cited Reference");
		aMap.put("RPY", "Cited Reference Year");
		aMap.put("N_CR", "Number of Cited References");
		aMap.put("PERC_YR", "Percent in Year");
		aMap.put("PERC_ALL", "Percent over all Years");
		aMap.put("AU", "Author");
		aMap.put("AU_L", "Last Name");
		aMap.put("AU_F", "First Name Initial");
		aMap.put("AU_A", "Authors");
		aMap.put("TI", "Title");
		aMap.put("J", "Source");
		aMap.put("J_N", "Source Title");
		aMap.put("J_S", "Title Short");
		aMap.put("VOL", "Volume");
		aMap.put("PAG", "Page");
		aMap.put("DOI", "DOI");
		aMap.put("CID2", "ClusterID");
		aMap.put("CID_S", "Cluster Size");
//		aMap.put("N_PYEARS", "N_PYEARS");
//		aMap.put("PYEAR_PERC", "PYEAR_PERC");
//		aMap.put("N_PCT50","N_PCT50");
//		aMap.put("N_PCT75","N_PCT75");
//		aMap.put("N_PCT90","N_PCT90");
//		aMap.put("N_PYEARS2","N_PYEARS2");
        attr = Collections.unmodifiableMap(aMap);
    }
	
	private TableColumn<CRType, ?>[] columns;
	
	
	
	
	@SuppressWarnings("unchecked")
	public CRTableView() {
	
		
		setMinHeight(100);
		setMinWidth(100);
		GridPane.setVgrow(this, Priority.ALWAYS);
		GridPane.setHgrow(this, Priority.ALWAYS);
		
		Callback<TableColumn<CRType, Number>, TableCell<CRType, Number>> doubleNumberFormat = column -> { 
			return new TableCell<CRType, Number>() {
		        @Override
		        protected void updateItem(Number value , boolean empty) {
		            super.updateItem(value, empty);

		            if ((value == null) || empty) {
		                setText(null);
//		                setStyle("");
		            } else {
		            	setText (UserSettings.get().getFormat().format(value.doubleValue()));
		            }
		        }
			}; 
		};

		columns = new TableColumn[CRTableView.attr.size()];

		columns[ 0] = new TableColumn<CRType, Number>("ID");
		((TableColumn<CRType, Number>) columns[ 0]).setCellValueFactory(cellData -> cellData.getValue().getIDProp());
		
		columns[ 1] = new TableColumn<CRType, String>("CR");
		((TableColumn<CRType, String>) columns[ 1]).setCellValueFactory(cellData -> cellData.getValue().getCRProp());
		((TableColumn<CRType, String>) columns[ 1]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[ 2] = new TableColumn<CRType, Number>("RPY");
		((TableColumn<CRType, Number>) columns[ 2]).setCellValueFactory(cellData -> cellData.getValue().getRPYProp());

		columns[ 3] = new TableColumn<CRType, Number>("N_CR");
		((TableColumn<CRType, Number>) columns[ 3]).setCellValueFactory(cellData -> cellData.getValue().getN_CRProp());

		columns[ 4] = new TableColumn<CRType, Number>("PERC_YR");
		((TableColumn<CRType, Number>) columns[ 4]).setCellValueFactory(cellData -> cellData.getValue().getPERC_YRProp());
		((TableColumn<CRType, Number>) columns[ 4]).setCellFactory(doubleNumberFormat); 

		columns[ 5] = new TableColumn<CRType, Number>("PERC_ALL");
		((TableColumn<CRType, Number>) columns[ 5]).setCellValueFactory(cellData -> cellData.getValue().getPERC_ALLProp());
		((TableColumn<CRType, Number>) columns[ 5]).setCellFactory(doubleNumberFormat); 

		columns[ 6] = new TableColumn<CRType, String>("AU");
		((TableColumn<CRType, String>) columns[ 6]).setCellValueFactory(cellData -> cellData.getValue().getAUProp());
		((TableColumn<CRType, String>) columns[ 6]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[ 7] = new TableColumn<CRType, String>("AU_L");
		((TableColumn<CRType, String>) columns[ 7]).setCellValueFactory(cellData -> cellData.getValue().getAU_LProp());
		((TableColumn<CRType, String>) columns[ 7]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[ 8] = new TableColumn<CRType, String>("AU_F");
		((TableColumn<CRType, String>) columns[ 8]).setCellValueFactory(cellData -> cellData.getValue().getAU_FProp());
		((TableColumn<CRType, String>) columns[ 8]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[ 9] = new TableColumn<CRType, String>("AU_A");
		((TableColumn<CRType, String>) columns[ 9]).setCellValueFactory(cellData -> cellData.getValue().getAU_AProp());
		((TableColumn<CRType, String>) columns[ 9]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });
		
		columns[10] = new TableColumn<CRType, String>("TI");
		((TableColumn<CRType, String>) columns[10]).setCellValueFactory(cellData -> cellData.getValue().getTIProp());
		((TableColumn<CRType, String>) columns[10]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[11] = new TableColumn<CRType, String>("J");
		((TableColumn<CRType, String>) columns[11]).setCellValueFactory(cellData -> cellData.getValue().getJProp());
		((TableColumn<CRType, String>) columns[11]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[12] = new TableColumn<CRType, String>("J_N");
		((TableColumn<CRType, String>) columns[12]).setCellValueFactory(cellData -> cellData.getValue().getJ_NProp());
		((TableColumn<CRType, String>) columns[12]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[13] = new TableColumn<CRType, String>("J_S");
		((TableColumn<CRType, String>) columns[13]).setCellValueFactory(cellData -> cellData.getValue().getJ_SProp());
		((TableColumn<CRType, String>) columns[13]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[14] = new TableColumn<CRType, String>("VOL");
		((TableColumn<CRType, String>) columns[14]).setCellValueFactory(cellData -> cellData.getValue().getVOLProp());
		((TableColumn<CRType, String>) columns[14]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[15] = new TableColumn<CRType, String>("PAG");
		((TableColumn<CRType, String>) columns[15]).setCellValueFactory(cellData -> cellData.getValue().getPAGProp());
		((TableColumn<CRType, String>) columns[15]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });

		columns[16] = new TableColumn<CRType, String>("DOI");
		((TableColumn<CRType, String>) columns[16]).setCellValueFactory(cellData -> cellData.getValue().getDOIProp());	
		((TableColumn<CRType, String>) columns[16]).setComparator((o1, o2) -> { return o1.compareToIgnoreCase(o2); });	

		columns[17] = new TableColumn<CRType, CRCluster>("CID2");
		((TableColumn<CRType, CRCluster>) columns[17]).setCellValueFactory(cellData -> cellData.getValue().getCID2());

		columns[18] = new TableColumn<CRType, Number>("CID_S");
		((TableColumn<CRType, Number>) columns[18]).setCellValueFactory(cellData -> cellData.getValue().getCID_SProp());		
		
		
//		columns[19] = new TableColumn<CRType, Integer>(); //N_PYEARS;
//		columns[20] = new TableColumn<CRType, Double>(); //PYEAR_PERC;
//		columns[21] = new TableColumn<CRType, Integer>(); //N_PCT50;
//		columns[22] = new TableColumn<CRType, Integer>(); //N_PCT75;
//		columns[23] = new TableColumn<CRType, Integer>(); //N_PCT90;
//		columns[24] = new TableColumn<CRType, Integer>(); //N_PYEARS2;		
		
		for (int idx=0; idx<CRTableView.attr.size(); idx++) {
			columns[idx].visibleProperty().bindBidirectional(UserSettings.get().getColumnVisibleProperty(idx));
		}
		
		getColumns().addAll(columns);
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		
		
	}
	
	
	public TableColumn<CRType, ?> getColumnByName (String name) {
		
		int idx = 0;
		for (Entry<String, String> e: CRTableView.attr.entrySet()) {
			if (name.equalsIgnoreCase(e.getKey())) return columns[idx];
			idx++;
		}
		
		return null;
	}
	
}

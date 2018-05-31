package cre.test.ui;

import java.util.Optional;
import java.util.function.Function;

import cre.test.data.match.CRCluster;
import cre.test.data.type.CRType;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class CRTableView extends TableView<CRType> {

	// Column Information
	public static enum ColGroup { CR, INDICATOR, CLUSTER, SEARCH }
	public static enum ColDataType { INT, DOUBLE, STRING, CRCLUSTER } 
	public static enum CRColumn {
		
		ID 	("ID", "ID", ColGroup.CR, ColDataType.INT, CRType::getIDProp),
		CR 	("CR", "Cited Reference", ColGroup.CR, ColDataType.STRING, CRType::getCRProp),
		RPY ("RPY", "Reference Publication Year", ColGroup.CR, ColDataType.INT, CRType::getRPYProp),
		N_CR ("N_CR", "Number of Cited References", ColGroup.CR, ColDataType.INT, CRType::getN_CRProp),
		PERC_YR ("PERC_YR", "Percent in Year", ColGroup.INDICATOR, ColDataType.DOUBLE, CRType::getPERC_YRProp),
		PERC_ALL ("PERC_ALL", "Percent over all Years", ColGroup.INDICATOR, ColDataType.DOUBLE, CRType::getPERC_ALLProp),
		AU ("AU", "Author", ColGroup.CR, ColDataType.STRING, CRType::getAUProp),
		AU_L ("AU_L", "Last Name", ColGroup.CR, ColDataType.STRING, CRType::getAU_LProp),
		AU_F ("AU_F", "First Name Initial", ColGroup.CR, ColDataType.STRING, CRType::getAU_FProp),
		AU_A ("AU_A", "Authors", ColGroup.CR, ColDataType.STRING, CRType::getAU_AProp),
		TI ("TI", "Title", ColGroup.CR, ColDataType.STRING, CRType::getTIProp),
		J ("J", "Source", ColGroup.CR, ColDataType.STRING, CRType::getJProp),
		J_N ("J_N", "Source Title", ColGroup.CR, ColDataType.STRING, CRType::getJ_NProp),
		J_S ("J_S", "Title short", ColGroup.CR, ColDataType.STRING, CRType::getJ_SProp),
		VOL ("VOL", "Volume", ColGroup.CR, ColDataType.STRING, CRType::getVOLProp),
		PAG ("PAG", "Page", ColGroup.CR, ColDataType.STRING, CRType::getPAGProp),
		DOI ("DOI", "DOI", ColGroup.CR, ColDataType.STRING, CRType::getDOIProp),
		CID2 ("CID2", "ClusterID", ColGroup.CLUSTER, ColDataType.CRCLUSTER, CRType::getCID2),
		CID_S ("CID_S", "ClusterSize", ColGroup.CLUSTER, ColDataType.INT, CRType::getCID_SProp),
		N_PYEARS ("N_PYEARS", "Number of Citing Years", ColGroup.INDICATOR, ColDataType.INT, CRType::getN_PYEARSProp),
		PYEAR_PERC ("PERC_PYEARS", "Percentage of Citing Years", ColGroup.INDICATOR, ColDataType.DOUBLE, CRType::getPYEAR_PERCProp),
		N_PCT50 ("N_TOP50", "Top 50% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, CRType::getN_PCT50Prop),
		N_PCT75 ("N_TOP25", "Top 25% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, CRType::getN_PCT75Prop),
		N_PCT90 ("N_TOP10", "Top 10% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, CRType::getN_PCT90Prop),

		SEQUENCE  ("SEQUENCE", "Sequence", ColGroup.INDICATOR, ColDataType.STRING, CRType::getSEQUENCEProp),
		TYPE  ("TYPE", "Type", ColGroup.INDICATOR, ColDataType.STRING, CRType::getTYPEProp),
		SEARCH_SCORE  ("SEARCH_SCORE", "Score from Search Process", ColGroup.SEARCH, ColDataType.INT, CRType::getSEARCH_SCOREProp)
		
		
		
//		CO  ("CO", "CO", ColGroup.INVISIBLE, ColDataType.INT, CRType::getCOProp), 

		;
//		N_PYEARS2 ("N_PYEARS2", "N_PYEARS2", ColGroup.INDICATOR, ColDataType.INT, CRType::getN_PYEARS2Prop);
		
		public String id;
		public String title;
		public ColGroup group;	
		public ColDataType type;
		public Function<CRType, ObservableValue<?>> prop;
		
		CRColumn(String id, String title, ColGroup group, ColDataType type, Function<CRType, ObservableValue<?>> prop) {
			this.id = id;
			this.title = title;
			this.group = group;
			this.type = type;
			this.prop = prop;
		}
	}
	

	private TableColumn<CRType, ?>[] columns;

	
	@SuppressWarnings("unchecked")
	public CRTableView() {
	
		
		
		setMinHeight(100);
		setMinWidth(100);
		GridPane.setVgrow(this, Priority.ALWAYS);
		GridPane.setHgrow(this, Priority.ALWAYS);
		
		CRColumn[] colInfo = CRColumn.values();
		columns = new TableColumn[colInfo.length];
		for (int i=0; i<colInfo.length; i++) {
			
			CRColumn col = colInfo[i];
			switch (col.type) {
				case INT:  
					columns[i] = new TableColumn<CRType, Number>(col.id); 
					((TableColumn<CRType, Number>) columns[i]).setCellValueFactory(cellData -> (ObservableValue<Number>) col.prop.apply (cellData.getValue()));
					break;
				case DOUBLE: 
					columns[i] = new TableColumn<CRType, Number>(col.id); 
					((TableColumn<CRType, Number>) columns[i]).setCellValueFactory(cellData -> (ObservableValue<Number>) col.prop.apply (cellData.getValue()));
					((TableColumn<CRType, Number>) columns[i]).setCellFactory(column -> { 
						return new TableCell<CRType, Number>() {
					        @Override
					        protected void updateItem(Number value , boolean empty) {
					            super.updateItem(value, empty);
				            	setText (((value == null) || empty) ? null : UISettings.get().getFormat().format(value.doubleValue()));
					        }
						}; 
					}); 
					break;
				case STRING: 
					columns[i] = new TableColumn<CRType, String>(col.id); 
					((TableColumn<CRType, String>) columns[i]).setCellValueFactory(cellData -> (ObservableValue<String>) col.prop.apply (cellData.getValue()));
					((TableColumn<CRType, String>) columns[i]).setComparator((o1, o2) -> { 
						if (o1==null) return 1;
						if (o2==null) return -1;
						return o1.compareToIgnoreCase(o2); 
					});
					break;
				case CRCLUSTER: 
					columns[i] = new TableColumn<CRType, CRCluster>(col.id); 
					((TableColumn<CRType, CRCluster>) columns[i]).setCellValueFactory(cellData -> (ObservableValue<CRCluster>) col.prop.apply (cellData.getValue()));
					break;
				default: assert false;
			}
			
			columns[i].visibleProperty().bindBidirectional(UISettings.get().getColumnVisibleProperty(i));
			
			if (i==0) {
				((TableColumn<CRType, Number>) columns[ 0]).setCellValueFactory(cellData -> (ObservableValue<Number>) col.prop.apply (cellData.getValue()));
			}


		}
		
		
		
//		setRowFactory(x -> {
//			TableRow<CRType> row = new TableRow<CRType>() {
//				
//				@Override
//			    public void updateItem(CRType cr, boolean empty){
//					
//					if (cr==null) return;
//					if (cr.getCO()==0) {
//						this.setStyle("-fx-background-color:lightcoral");
//					} else {
//						this.setStyle("-fx-background-color:lightgreen");
//					}
//				}
//				
//			};
//			
//			return row;
//		});
		
		getColumns().addAll(columns);
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	
	


//	public TableColumn<CRType, ?> getColumnByName (String name) {
//		
//		int idx = 0;
//		for (CRColumn e: CRColumn.values()) {
//			if (name.equalsIgnoreCase(e.id)) return columns[idx];
//			idx++;
//		}
//		
//		return null;
//	}
	
//	
//	private void setRowFactory(Callback<TableView<CRType>, TableRow<CRType>> value) {
//		// TODO Auto-generated method stub
//		
//	}



	public void orderBySearchResult () {

		/* remove SEARCH_SCORE as order criteria */
		for (int i=getSortOrder().size()-1; i>=0; i--) {
			if (getSortOrder().get(i).getText().equals(CRColumn.SEARCH_SCORE.id)) {
				getSortOrder().remove(i);
			}
		}
		
		/* sort by search first; remains other (if existing) search criteria */
		columns[CRColumn.SEARCH_SCORE.ordinal()].setSortType(TableColumn.SortType.DESCENDING);
		getSortOrder().add(0, columns[CRColumn.SEARCH_SCORE.ordinal()]);
		sort();
		
		Optional<CRType> first = getItems().stream().findFirst();
		if (first.isPresent()) {
			getSelectionModel().clearSelection();
			getSelectionModel().select(first.get());
			scrollTo(first.get());
		}
	}


	public void orderByYearAndSelect (int year) {
		/* sort by year ASC, n_cr desc */
		columns[CRColumn.RPY.ordinal()].setSortType(TableColumn.SortType.ASCENDING);
		columns[CRColumn.N_CR.ordinal()].setSortType(TableColumn.SortType.DESCENDING);
		getSortOrder().clear();
		getSortOrder().add(columns[CRColumn.RPY.ordinal()]);
		getSortOrder().add(columns[CRColumn.N_CR.ordinal()]);
		sort();
		Optional<CRType> first = getItems().stream().filter(cr -> (cr.getRPY()!=null) && (cr.getRPY().intValue() == year)).findFirst();
		if (first.isPresent()) {
			getSelectionModel().clearSelection();
			getSelectionModel().select(first.get());
			scrollTo(first.get());
		}
	}
	
}

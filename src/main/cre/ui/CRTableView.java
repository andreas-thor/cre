package main.cre.ui;

import java.util.Optional;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.CRType_ColumnView;

public class CRTableView extends TableView<CRType> {

	private TableColumn<CRType, ?>[] columns;

	
	@SuppressWarnings("unchecked")
	public CRTableView() {
	
		
		
		setMinHeight(100);
		setMinWidth(100);
		GridPane.setVgrow(this, Priority.ALWAYS);
		GridPane.setHgrow(this, Priority.ALWAYS);
		
		CRType_ColumnView.CRColumn[] colInfo = CRType_ColumnView.CRColumn.values();
		columns = new TableColumn[colInfo.length];
		for (int i=0; i<colInfo.length; i++) {
			
			CRType_ColumnView.CRColumn col = colInfo[i];
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
//				case CRCLUSTER: 
//					columns[i] = new TableColumn<CRType, CRCluster>(col.id); 
//					((TableColumn<CRType, CRCluster>) columns[i]).setCellValueFactory(cellData -> (ObservableValue<CRCluster>) col.prop.apply (cellData.getValue()));
//					break;
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
			if (getSortOrder().get(i).getText().equals(CRType_ColumnView.CRColumn.SEARCH_SCORE.id)) {
				getSortOrder().remove(i);
			}
		}
		
		/* sort by search first; remains other (if existing) search criteria */
		columns[CRType_ColumnView.CRColumn.SEARCH_SCORE.ordinal()].setSortType(TableColumn.SortType.DESCENDING);
		getSortOrder().add(0, columns[CRType_ColumnView.CRColumn.SEARCH_SCORE.ordinal()]);
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
		columns[CRType_ColumnView.CRColumn.RPY.ordinal()].setSortType(TableColumn.SortType.ASCENDING);
		columns[CRType_ColumnView.CRColumn.N_CR.ordinal()].setSortType(TableColumn.SortType.DESCENDING);
		getSortOrder().clear();
		getSortOrder().add(columns[CRType_ColumnView.CRColumn.RPY.ordinal()]);
		getSortOrder().add(columns[CRType_ColumnView.CRColumn.N_CR.ordinal()]);
		sort();
		Optional<CRType> first = getItems().stream().filter(cr -> (cr.getRPY()!=null) && (cr.getRPY().intValue() == year)).findFirst();
		if (first.isPresent()) {
			getSelectionModel().clearSelection();
			getSelectionModel().select(first.get());
			scrollTo(first.get());
		}
	}
	
}

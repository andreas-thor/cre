package main.cre.ui.dialog;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.CRType_ColumnView;
import main.cre.ui.UISettings;

public class CRInfo extends Dialog<Void> {

	
	public CRInfo(CRType<?> cr) {
		super();
		
		setTitle("Info");
		setHeaderText("Cited Reference");
		setResizable(true);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));

		int row = 0;
		for (CRType_ColumnView.CRColumn col: CRType_ColumnView.CRColumn.values()) {
			
			ObservableValue<?> prop = col.prop.apply(cr);
			String value = null;
			
			
			switch (col.type) {
			case INT:
				value = ((ObservableValue<Integer>)prop).getValue().toString();
				break;
			case DOUBLE: 
				value = UISettings.get().getFormat().format(((ObservableValue<Double>)prop).getValue().doubleValue());
				break;
			case STRING: 
				value = ((ObservableValue<String>)prop).getValue();
				break;
//			case CRCLUSTER: 
//				value = ((ObservableValue<CRCluster>)prop).getValue().toString();
//				break;
			default: assert false;
			}			
			
			addProperty (grid, row, col.title, prop.getValue()==null ? "" : value);
			row++;
		}

//		addProperty (grid, row, "text", String.valueOf(cr.pubList.size()));

		
		getDialogPane().setContent(grid);
		
		// Request focus on first field by default.

		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());

			
	}
	
	private void addProperty (GridPane grid, int row, String name, String value) {
		grid.add(new Label(name), 0, row);
		TextField tf = new TextField(String.valueOf(value));
		tf.setEditable(false);
		grid.add(tf, 1, row);
		GridPane.setHgrow(tf, Priority.ALWAYS);
	}
	
}

package cre.test.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class Range extends Dialog<int[]> {

	
	public Range(String title, String header, int[] range, int[] maxRange) {
		super();
		
		setTitle(title);
		setHeaderText(header);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		TextField[] tf = new TextField[] { new TextField(String.valueOf(range[0])), new TextField(String.valueOf(range[1])) }; 
		CheckBox[] cb = new CheckBox[] { new CheckBox("Minimum"), new CheckBox("Maximum") }; 

		cb[0].setOnAction((event) -> {
		    tf[0].setDisable(cb[0].isSelected());
		    tf[0].setText(cb[0].isSelected() ? String.valueOf(maxRange[0]) : tf[0].getText());
		});
		cb[1].setOnAction((event) -> {
		    tf[1].setDisable(cb[1].isSelected());
		    tf[1].setText(cb[1].isSelected() ? String.valueOf(maxRange[1]) : tf[1].getText());
		});
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		grid.addRow(0, new Label("From:"), tf[0], cb[0]);
		grid.addRow(1, new Label("To:"  ), tf[1], cb[1]);
		getDialogPane().setContent(grid);
		
		// Request focus on first field by default.
		Platform.runLater(() -> tf[0].requestFocus());

		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		        return new int[] {Integer.parseInt(tf[0].getText()), Integer.parseInt(tf[1].getText()) };
		    }
		    return null;
		});		
	}
	
	
}

package main.cre.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class Search extends Dialog<String> {

	
	public Search() {
		super();
		
		setTitle("Search");
		setHeaderText("Cited Reference");
		setResizable(true);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));

		grid.add(new Label("Search"), 0, 0);
		TextField tf = new TextField("");
		tf.setEditable(true);
		grid.add(tf, 1, 0);
		GridPane.setHgrow(tf, Priority.ALWAYS);
		
		getDialogPane().setContent(grid);
		
		// Request focus on first field by default.
		Platform.runLater(() -> tf.requestFocus());
		
		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
	    		return tf.getText();
		    }
		    return null;	// CANCEL
		});	
			
	}
	

	
}

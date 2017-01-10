package cre.test.ui.dialog;

import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class Info extends Dialog<Void> {

	
	public Info(Map<String, Integer> info) {
		super();
		
		setTitle("Info");
		setHeaderText("Cited References Dataset");
		getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		
		int i = 0;
		for (Entry<String, Integer> e: info.entrySet()) {
			grid.add(new Label(e.getKey()), 0, i);
			TextField tf = new TextField(String.valueOf(e.getValue()));
			tf.setEditable(false);
			grid.add(tf, 1, i);
			i++;
		};
		
		getDialogPane().setContent(grid);
		
		// Request focus on first field by default.

		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());

			
	}
	
	
}

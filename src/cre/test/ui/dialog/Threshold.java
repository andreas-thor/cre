package cre.test.ui.dialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class Threshold extends Dialog<Pair<String, Double>> {

	
	public Threshold(String title, String header, String comp, double threshold) {
		super();
		
		setTitle(title);
		setHeaderText(header);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		TextField tf = new TextField(String.valueOf(threshold)); 
		ComboBox<String> cb = new ComboBox<String>(FXCollections.observableArrayList("<", "<=", "=", ">=", ">"));
		cb.setValue(comp);
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		grid.addRow(0, new Label("Percent in Year:"), cb, tf);
		getDialogPane().setContent(grid);
		
		// Request focus on first field by default.
		Platform.runLater(() -> tf.requestFocus());

		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		        return new Pair<String, Double>(cb.getValue(), Double.valueOf(tf.getText())/100);
		    }
		    return null;
		});		
	}
	
	
}

package main.cre.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import main.cre.data.Statistics;

public class Info extends Dialog<Void> {

	
	public Info() {
		super();
		
		setTitle("Info");
		setHeaderText("Cited References Dataset");
		getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		
		grid.addRow(0, 
			new Label("Number of Cited References"),
			createTF (Statistics.getNumberOfCRs()));
		
		grid.addRow(1, 
			new Label("Number of Cited References (shown)"),
			createTF (Statistics.getNumberOfCRsByVisibility(true)));
		
		grid.addRow(2, 
			new Label("Number of Cited References Clusters"), 
			createTF (Statistics.getNumberOfClusters()));
		
		int[] r = Statistics.getMaxRangeRPY();
		grid.addRow(3, 
			new Label("Range of Cited References Years"), 
			createTF (r[0], 1),
			new Label("-"),
			createTF (r[1], 1));
		
		grid.addRow(4, 
			new Label("Number of different Cited References Years"), 
			createTF (Statistics.getNumberOfDistinctRPY()));
		
		grid.addRow(5, 
			new Label("Number of Publications"), 
			createTF (Statistics.getNumberOfPubs(true)));

		grid.addRow(6, 
				new Label("Number of Citing Publications"), 
				createTF (Statistics.getNumberOfPubs()));
		
		r = Statistics.getMaxRangePY();
		grid.addRow(7, 
			new Label("Range of Citing Publications Years"),
			createTF (r[0], 1),
			new Label("-"),
			createTF (r[1], 1));
		
		grid.addRow(8,
			new Label("Number of different Citing Publications Years"),
			createTF (Statistics.getNumberOfDistinctPY()));
		
		
		getDialogPane().setContent(grid);
		
		// Request focus on first field by default.

		getDialogPane().lookupButton(ButtonType.OK).requestFocus();
	}
	
	
	TextField createTF (long value) {
		return createTF(value, 3);
	}
	
	
	TextField createTF (long value, int colspan) {
		TextField tf = new TextField(String.valueOf(value));
		tf.setEditable(false);
		tf.setMaxWidth(colspan==1 ? 50 : 125);
		GridPane.setColumnSpan(tf, colspan);
		return tf;
	}
	
	
}
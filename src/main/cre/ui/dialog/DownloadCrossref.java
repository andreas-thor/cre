package main.cre.ui.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import main.cre.ui.UISettings;

public class DownloadCrossref extends Dialog<DownloadCrossrefData> {

	



	public DownloadCrossref() {
		super();
		
		
		setTitle("Search Crossref");
		setHeaderText("Search Crossref");
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		
		DownloadCrossrefData d = UISettings.get().getDownloadCrossrefData();

		TextField tfTitle = createTF (d.getTitle());
		tfTitle.setMaxWidth(170);
		grid.add(new Label("Title"), 0, 0);
		grid.add(tfTitle, 1, 0, 3, 1);		
		
		
		TextField tfISSN = createTF (d.getISSN());
		tfISSN.setMaxWidth(170);
		grid.add(new Label("ISSN"), 0, 1);
		grid.add(tfISSN, 1, 1, 3, 1);		
		

		TextField tfRangeFrom = createTF (d.getRange()[0] == -1 ? "" : String.valueOf(d.getRange()[0]));
		TextField tfRangeTo = createTF (d.getRange()[1] == -1 ? "" : String.valueOf(d.getRange()[1]));
		grid.addRow(2, 
			new Label("Range of Cited References Years"), 
			tfRangeFrom,
			new Label("-"),
			tfRangeTo);
		
		TextArea tfDOI = new TextArea();
		tfDOI.setMaxWidth(165);
		tfDOI.setEditable(true);
		tfDOI.setMinHeight(50);
		tfDOI.setText(String.join("\n", d.getDOI()));

		grid.add(new Label("DOIs"), 0, 3);
		grid.add(tfDOI, 1, 3, 3, 1);
		
		getDialogPane().setContent(grid);
		
		

		getDialogPane().lookupButton(ButtonType.OK).requestFocus();
		
		setResultConverter(dialogButton -> {
		   DownloadCrossrefData data = new DownloadCrossrefData(tfTitle.getText(), tfISSN.getText(), new String[] {tfRangeFrom.getText(), tfRangeTo.getText()}, tfDOI.getText());
		   UISettings.get().setDownloadCrossrefData(data);
		   return data;
		});		
	}

	private TextField createTF(String value) {
		TextField tf = new TextField(value);
		tf.setAlignment(Pos.CENTER_RIGHT);
		tf.setEditable(true);
		tf.setMaxWidth(70);
		return tf;
	}

}

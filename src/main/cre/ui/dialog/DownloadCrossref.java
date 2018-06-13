package main.cre.ui.dialog;

import java.util.Arrays;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import main.cre.ui.dialog.DownloadCrossref.DownloadCrossrefData;

public class DownloadCrossref extends Dialog<DownloadCrossrefData> {

	public class DownloadCrossrefData {
		
		private String ISSN = null;
		private int[] range = { -1, -1 };
		private String[] DOI = null;
	
		public DownloadCrossrefData(String ISSN, String[] range, String DOI) {
			super();

			this.ISSN = ISSN.trim();
			this.range = Arrays.stream(range).mapToInt(s -> {
				try {
					return Integer.valueOf(s).intValue();
				} catch (Exception e) {
					return -1;
				}
			}).toArray();
			this.DOI = Arrays.stream(DOI.split("[,;\\s]")).map(s -> s.trim()).filter(s -> s.length()>0).toArray(String[]::new);
		}
		
		public String getISSN() {
			return ISSN;
		}

		public int[] getRange() {
			return range;
		}

		public String[] getDOI() {
			return DOI;
		}
	}



	public DownloadCrossref() {
		super();
		
		
		setTitle("Search Crossref");
		setHeaderText("Search Crossref");
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		
		TextField tfISSN = createTF ("");
		tfISSN.setMaxWidth(170);
		grid.add(new Label("ISSN"), 0, 0);
		grid.add(tfISSN, 1, 0, 3, 1);		
		
		TextField tfRangeFrom = createTF ("");
		TextField tfRangeTo = createTF ("");
		grid.addRow(1, 
			new Label("Range of Cited References Years"), 
			tfRangeFrom,
			new Label("-"),
			tfRangeTo);
		
		TextArea tfDOI = new TextArea();
		tfDOI.setMaxWidth(165);
		tfDOI.setEditable(true);
		tfDOI.setMinHeight(50);

		grid.add(new Label("DOIs"), 0, 2);
		grid.add(tfDOI, 1, 2, 3, 1);
		
		getDialogPane().setContent(grid);
		
		

		getDialogPane().lookupButton(ButtonType.OK).requestFocus();
		
		setResultConverter(dialogButton -> {
		   return new DownloadCrossrefData(tfISSN.getText(), new String[] {tfRangeFrom.getText(), tfRangeTo.getText()}, tfDOI.getText()); 
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

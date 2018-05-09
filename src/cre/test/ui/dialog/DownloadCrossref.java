package cre.test.ui.dialog;

import java.util.Arrays;

import cre.test.ui.dialog.DownloadCrossref.DownloadCrossrefData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class DownloadCrossref extends Dialog<DownloadCrossrefData> {

	public class DownloadCrossrefData {
		
		private String ISSN = null;
		private int[] range = { -1, -1 };
		private String[] DOI = null;
	
		public DownloadCrossrefData(String ISSN, String[] range, String DOI) {
			super();

			this.ISSN = ISSN;
			this.range = Arrays.stream(range).mapToInt(s -> {
				try {
					return Integer.valueOf(s).intValue();
				} catch (Exception e) {
					return -1;
				}
			}).toArray();
			this.DOI = Arrays.stream(DOI.split("[,;]")).map(s -> s.trim()).toArray(String[]::new);
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
		grid.addRow(0, 
			new Label("ISSN"),
			tfISSN);
		
		TextField tfRangeFrom = createTF ("");
		TextField tfRangeTo = createTF ("");
		grid.addRow(1, 
			new Label("Range of Cited References Years"), 
			tfRangeFrom,
			new Label("-"),
			tfRangeTo);
		
		TextField tfDOI = createTF ("");
		grid.addRow(2, 
			new Label("DOIs"), 
			tfDOI);
		
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

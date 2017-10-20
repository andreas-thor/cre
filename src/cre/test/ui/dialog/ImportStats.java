package cre.test.ui.dialog;

import cre.test.data.CRStatsInfo;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class ImportStats extends Dialog<Integer> {

	private CRStatsInfo crStatsInfo;
	
	private TextField tfNumber[] = new TextField[2];
	private TextField tfYear[] = new TextField[4];
	private CheckBox cbWithout[] = new CheckBox[2];
	private Button btnImport; 
	
	public ImportStats(CRStatsInfo crStatsInfo) {
		super();
		
		this.crStatsInfo = crStatsInfo;
		
		setTitle("Info");
		setHeaderText("Cited References Dataset");
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		btnImport = (Button) getDialogPane().lookupButton(ButtonType.OK);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		
		tfNumber[0] = createTF (UserSettings.get().getMaxCR(), true);
		tfNumber[1] = createTF (-1, false);
		grid.addRow(0, 
			new Label("Number of non-distinct Cited References"),
			tfNumber[0],
			new Label("of"),
			tfNumber[1]);

		
		long allocatedMemory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
		grid.addRow(1, 
				new Label("(Approx. maximum number based in memory)"),
				createTF (300*presumableFreeMemory/1024/1024, false));

		int[] rangeRPY = crStatsInfo.getRangeRPY();
		tfYear[0] = createTF (rangeRPY[0], true);
		grid.addRow(2, 
			new Label("Minimum Reference Publication Year"), 
			tfYear[0],
			new Label(""),
			createTF (rangeRPY[0], false));

		tfYear[1] = createTF (rangeRPY[1], true);
		grid.addRow(3, 
				new Label("Maximum Reference Publication Year"), 
				tfYear[1],
				new Label(""),
				createTF (rangeRPY[1], false));

		cbWithout[0] = new CheckBox("Include");
		cbWithout[0].setSelected(UserSettings.get().getImportCRsWithoutYear());
		grid.addRow(4, 
				new Label("CRs w/o Reference Publication Year"), 
				cbWithout[0],
				new Label(""),
				createTF (crStatsInfo.getNumberOfCRsWithoutRPY(), false));

		grid.addRow(5, 
				new Label("Number of Publications"),
				new Label(""),
				new Label(""),
				createTF (crStatsInfo.getNumberOfPubs(), false));
		
		int[] rangePY = crStatsInfo.getRangePY();

		tfYear[2] = createTF (rangePY[0], true);
		grid.addRow(6, 
			new Label("Minimum Publication Year"), 
			tfYear[2],
			new Label(""),
			createTF (rangePY[0], false));

		tfYear[3] = createTF (rangePY[1], true);
		grid.addRow(7, 
				new Label("Maximum Publication Year"), 
				tfYear[3],
				new Label(""),
				createTF (rangePY[1], false));

		cbWithout[1] = new CheckBox("Include");
		cbWithout[1].setSelected(UserSettings.get().getImportPubsWithoutYear());
		grid.addRow(8, 
				new Label("Publications w/o Publication Year"), 
				cbWithout[1],
				new Label(""),
				createTF (crStatsInfo.getNumberOfCRsWithoutPY(), false));		
		
		
		tfNumber[0].textProperty().addListener((obs, oldText, newText) -> {
			updateNumberOfCRs();
		});
		
		for (TextField x: tfYear) {
			x.textProperty().addListener((obs, oldText, newText) -> {
				updateNumberOfCRs();
			});
		}
		for (CheckBox x: cbWithout) {
			x.selectedProperty().addListener((obs, oldSel, newSel) -> {
					updateNumberOfCRs();
			});
		}
		
		getDialogPane().setContent(grid);
		updateNumberOfCRs();
		// Request focus on first field by default.

		getDialogPane().lookupButton(ButtonType.OK).requestFocus();
		
		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		    	int noOfErrors = 0;
		    	noOfErrors += UserSettings.get().setMaxCR(tfNumber[0].getText());
		    	noOfErrors += UserSettings.get().setRange(UserSettings.RangeType.ImportRPYRange, new String[] { tfYear[0].getText(), tfYear[1].getText()} );
		    	noOfErrors += UserSettings.get().setRange(UserSettings.RangeType.ImportPYRange, new String[] { tfYear[2].getText(), tfYear[3].getText()} );
		    	noOfErrors += UserSettings.get().setImportCRsWithoutYear(cbWithout[0].isSelected());
		    	noOfErrors += UserSettings.get().setImportPubsWithoutYear(cbWithout[1].isSelected());
		    	
		    	if (noOfErrors>0) {	// if result == 0 --> no adjustments, otherwise errors (parseInt) or invalid values (e.g., <0)

		    		// INVALID range
			        Alert alert = new Alert(AlertType.WARNING);
			        alert.setTitle("Warning");
			        alert.setHeaderText("Invalid Values");
			        alert.setContentText("Some values have been adjusted!");
			        alert.showAndWait();
		    	}
		    	
		    	return noOfErrors;	
		    }
		    return null;
		});		
	}
	
	
	
	private void updateNumberOfCRs () {
		 
		 
		 try {
			 tfNumber[1].setText("???");
			 long n = crStatsInfo.getNumberOfCRs(
					 new int[] { Integer.valueOf (tfYear[0].getText()).intValue(), Integer.valueOf (tfYear[1].getText()).intValue() },
					 cbWithout[0].isSelected(),
					 new int[] { Integer.valueOf (tfYear[2].getText()).intValue(), Integer.valueOf (tfYear[3].getText()).intValue() },
					 cbWithout[1].isSelected());
			 tfNumber[1].setText(String.valueOf(n));
			 
			 long toImport = Integer.valueOf(tfNumber[0].getText()).intValue();
			 
			 btnImport.setText((toImport>0) && (toImport<n) ? "Random Import" : "Import All");
			 
		 } catch (NumberFormatException e) { }
		 

		 
		 
		 
	}
	
	TextField createTF (long value, boolean editable) {
		TextField tf = new TextField(String.valueOf(value));
		tf.setEditable(editable);
		if (!editable) {
			tf.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
		}
		tf.setMaxWidth(85);
		return tf;
	}
	
	
}

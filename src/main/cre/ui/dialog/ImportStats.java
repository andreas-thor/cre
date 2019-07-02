package main.cre.ui.dialog;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.cre.data.CRStatsInfo;
import main.cre.ui.UISettings;

public class ImportStats extends Dialog<Integer> {

	private CRStatsInfo crStatsInfo;
	
	private TextField tfNumber[] = new TextField[3];
	private TextField tfYear[] = new TextField[4];
	private CheckBox cbWithout[] = new CheckBox[2];
	private Button btnImport; 
	private ComboBox<String> comboSampling;
	
	public ImportStats(CRStatsInfo crStatsInfo) {
		super();
		
		this.crStatsInfo = crStatsInfo;
		
		setTitle("Info");
		setHeaderText("Cited References Dataset");
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		btnImport = (Button) getDialogPane().lookupButton(ButtonType.OK);
		btnImport.setMaxWidth(130);
		btnImport.setPrefWidth(130);
		btnImport.setMinWidth(130);
		
		
		VBox tabChart = new VBox(10);
		tabChart.setPadding (new Insets(20, 20, 20, 20));
		tabChart.getChildren().add(new TitledPane("Sampling", createSamplingPane()));
		tabChart.getChildren().add(new TitledPane("Restrictions", createRestrictionsPane()));
		getDialogPane().setContent(tabChart);
		
		updateNumberOfCRs();
		// Request focus on first field by default.

		getDialogPane().lookupButton(ButtonType.OK).requestFocus();
		
		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		    	int noOfErrors = 0;
		    	noOfErrors += UISettings.get().setMaxCR(tfNumber[0].getText());
		    	noOfErrors += UISettings.get().setSampling(comboSampling.getValue());
		    	noOfErrors += UISettings.get().setRange(UISettings.RangeType.ImportRPYRange, new String[] { tfYear[0].getText(), tfYear[1].getText()} );
		    	noOfErrors += UISettings.get().setRange(UISettings.RangeType.ImportPYRange, new String[] { tfYear[2].getText(), tfYear[3].getText()} );
		    	noOfErrors += UISettings.get().setImportCRsWithoutYear(cbWithout[0].isSelected());
		    	noOfErrors += UISettings.get().setImportPubsWithoutYear(cbWithout[1].isSelected());
		    	
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
	

	private GridPane createSamplingPane () {
	
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));


		tfNumber[1] = createTF (-1, false);
		grid.addRow(0, 
			new Label("Number of non-distinct Cited References"),
			tfNumber[1]);

		long allocatedMemory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
		tfNumber[2] = createTF (300*presumableFreeMemory/1024/1024, false);
		grid.addRow(1, 
				new Label("Estimated maximal Number which can be loaded in Memory"),
				tfNumber[2]);

		
		ObservableList<String> options = FXCollections.observableArrayList(Sampling.NONE.label, Sampling.RANDOM.label, Sampling.SYSTEMATIC.label, Sampling.CLUSTER.label);
		comboSampling = new ComboBox<String>(options);
		comboSampling.setEditable(false);
		
		
			
		tfNumber[0] = createTF (crStatsInfo.getNumberOfCRs() /*UserSettings.get().getMaxCR()*/, true);
		grid.addRow(2, 
				comboSampling,
				tfNumber[0]);
		
		comboSampling.valueProperty().addListener(new ChangeListener<String>() {
	        @Override public void changed(ObservableValue ov, String t, String t1) {
	        	tfNumber[0].setVisible(t1.equals(options.get(1)) || t1.equals(options.get(2))); 
	        }    
	    });
		comboSampling.setValue(options.get(0));
		
		return grid;
	}

	private GridPane createRestrictionsPane () {
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		
		int[] rangeRPY = crStatsInfo.getRangeRPY();
		tfYear[0] = createTF (rangeRPY[0], true);
		grid.addRow(0, 
			new Label("Minimum Reference Publication Year"), 
			tfYear[0],
			new Label(""),
			createTF (rangeRPY[0], false));

		tfYear[1] = createTF (rangeRPY[1], true);
		grid.addRow(1, 
				new Label("Maximum Reference Publication Year"), 
				tfYear[1],
				new Label(""),
				createTF (rangeRPY[1], false));

		cbWithout[0] = new CheckBox("Include");
		cbWithout[0].setSelected(UISettings.get().getImportCRsWithoutYear());
		grid.addRow(2, 
				new Label("Number of Cited References without Publication Year"), 
				cbWithout[0],
				new Label(""),
				createTF (crStatsInfo.getNumberOfCRsWithoutRPY(), false));

		grid.addRow(3, 
				new Label("Number of Publications"),
				new Label(""),
				new Label(""),
				createTF (crStatsInfo.getNumberOfPubs(), false));
		
		int[] rangePY = crStatsInfo.getRangePY();

		tfYear[2] = createTF (rangePY[0], true);
		grid.addRow(4, 
			new Label("Minimum Publication Year"), 
			tfYear[2],
			new Label(""),
			createTF (rangePY[0], false));

		tfYear[3] = createTF (rangePY[1], true);
		grid.addRow(5, 
				new Label("Maximum Publication Year"), 
				tfYear[3],
				new Label(""),
				createTF (rangePY[1], false));

		cbWithout[1] = new CheckBox("Include");
		cbWithout[1].setSelected(UISettings.get().getImportPubsWithoutYear());
		grid.addRow(6, 
				new Label("Number of Publications without Publication Year"), 
				cbWithout[1],
				new Label(""),
				createTF (crStatsInfo.getNumberOfPubsWithoutPY(), false));		
		
		
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
		
		return grid;


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
			 
			 btnImport.setText("Import");
//			 btnImport.setText((toImport>0) && (toImport<n) ? "Random Import" : "Import All");
			 
		 } catch (NumberFormatException e) { }
		 

		 
		 
		 
	}
	
	private TextField createTF (long value, boolean editable) {
		TextField tf = new TextField(String.valueOf(value));
		tf.setAlignment(Pos.CENTER_RIGHT);
		tf.setEditable(editable);
		if (!editable) {
			tf.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
		}
		tf.setMaxWidth(70);
		return tf;
	}
	
	
}

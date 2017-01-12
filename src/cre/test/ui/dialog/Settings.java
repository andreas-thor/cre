package cre.test.ui.dialog;

import java.io.IOException;
import java.util.Map.Entry;

import cre.test.ui.CRTableView;
import cre.test.ui.UserSettings;
import cre.test.ui.UserSettings.RangeType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class Settings extends Dialog<Integer> {

	
	private final CheckBox[] cbCol = new CheckBox[CRTableView.attr.size()];
	private final TextField tfDigits = new TextField();
	private final CheckBox[] cbLine = new CheckBox[2];
	private final TextField[] tfLine = new TextField[2];
	private final TextField tfMedian = new TextField();
	private final TextField[] tfImport = new TextField[3];
	
	public Settings() throws IOException {
		// TODO Auto-generated constructor stub
		
		super();

		TabPane tpane = new TabPane();
		tpane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		VBox tabTable = new VBox(10);
		tabTable.setPadding (new Insets(20, 20, 20, 20));
		tabTable.getChildren().add(new TitledPane("Visible Columns", createTableColPane()));
		tabTable.getChildren().add(new TitledPane("Value Format", createTableValPane()));
		tpane.getTabs().add(new Tab("Table", tabTable));
		
		VBox tabChart = new VBox(10);
		tabChart.setPadding (new Insets(20, 20, 20, 20));
		tabChart.getChildren().add(new TitledPane("Chart Layout", createChartLayoutPane()));
		tpane.getTabs().add(new Tab("Chart", tabChart));

		VBox tabImport = new VBox(10);
		tabImport.setPadding (new Insets(20, 20, 20, 20));
		tabImport.getChildren().add(new TitledPane("Restrict Import of Cited References", createImportRestrictionPane()));
		tpane.getTabs().add(new Tab("Import", tabImport));
		
		
		// set the dialog
		setTitle("Settings");
		getDialogPane().setContent(tpane);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());

		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		    	int noOfErrors = 0;
		    	for (int i=0; i<cbCol.length; i++) {
		    		UserSettings.get().getColumnVisibleProperty(i).set(cbCol[i].isSelected());
		    	}
		    	noOfErrors += UserSettings.get().setFormatDigits(tfDigits.getText());
		    	noOfErrors += UserSettings.get().setChartLine(new boolean[] { cbLine[0].isSelected(), cbLine[1].isSelected() });
		    	noOfErrors += UserSettings.get().setChartSize(new String[] { tfLine[0].getText(), tfLine[1].getText() });
		    	noOfErrors += UserSettings.get().setMedianRange(tfMedian.getText());
		    	noOfErrors += UserSettings.get().setMaxCR(tfImport[0].getText());
		    	noOfErrors += UserSettings.get().setRange(UserSettings.RangeType.ImportYearRange, new String[] { tfImport[1].getText(), tfImport[2].getText()} );
		    	
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
	
	private GridPane createChartLayoutPane() {
		
		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		String[] label = new String[] { "Number of Cited References", "Deviation from the Median +/-"};
		for (int i=0; i<label.length; i++) {
			cbLine[i] = new CheckBox(label[i]);
			cbLine[i].setSelected(UserSettings.get().getChartLine()[i]);
			result.add(cbLine[i], 0, i);
		}
		
		tfMedian.setText(String.valueOf(UserSettings.get().getMedianRange()));
		tfMedian.setMaxWidth(50);
		tfMedian.setDisable(!cbLine[1].isSelected());
		result.add(tfMedian, 1, 1);
		cbLine[1].setOnAction( event -> { tfMedian.setDisable(!cbLine[1].isSelected());} );
		result.add(new Label("Years"), 2, 1);
		
		label = new String[] { "Stroke Size", "Shape Size"};
		for (int i=0; i<label.length; i++) {
			result.add(new Label(label[i]), 0, i+2);
			tfLine[i] = new TextField(String.valueOf (UserSettings.get().getChartSize()[i]));
			tfLine[i].setMaxWidth(50);
			result.add(tfLine[i], 1, i+2);
		}

		return result;
		
	}
	
	
	private GridPane createImportRestrictionPane() {

		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		String[] label = new String[] { "Maximum Number", "Minimum Publication Year", "Maximum Publication Year" };
		for (int i=0; i<label.length; i++) {
			result.add(new Label(label[i]), 0, i);
			tfImport[i] = new TextField((i==0) ? String.valueOf(UserSettings.get().getMaxCR()) : String.valueOf(UserSettings.get().getRange(RangeType.ImportYearRange)[i-1]));
			tfImport[i].setMaxWidth(50);
			result.add(tfImport[i], 1, i);
		}
		
		return result;
		
		
	}
	
	private GridPane createTableValPane() {
		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		result.add(new Label("Number of Digits"), 0, 0);
		tfDigits.setMaxWidth(50);
		tfDigits.setText(UserSettings.get().getFormatDigits());
		result.add(tfDigits, 1, 0);
		return result;
		
	}
	
	private GridPane createTableColPane() {
		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.3);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.3);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.3);
        result.getColumnConstraints().addAll(col1,col2,col3);
        
		
		
		int col = 0;
		int row = 0;
		int idx = 0;
		for (Entry<String, String> e: CRTableView.attr.entrySet()) {
			cbCol[idx] = new CheckBox(e.getValue());
			cbCol[idx].setSelected(UserSettings.get().getColumnVisibleProperty(idx).get());
			result.add(cbCol[idx], col, row);
			if (col<2) {
				col++;
			} else {
				row++;
				col=0;
			}
			idx++;
		}
		return result;
	}
}

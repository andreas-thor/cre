package cre.test.ui.dialog;

import java.io.IOException;
import java.util.Arrays;

import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.ui.CRTableView;
import cre.test.ui.CRTableView.CRColumn;
import cre.test.ui.CRTableView.ColGroup;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class Settings extends Dialog<Integer> {

	
	private final CheckBox[] cbCol = new CheckBox[CRTableView.CRColumn.values().length];
	private final TextField tfDigits = new TextField();
	private final TextField tfNPCT = new TextField();
	private final CheckBox[] cbLine = new CheckBox[2];
	private final TextField[] tfLine = new TextField[4];
	private final TextField tfMedian = new TextField();
	private final TextField[] tfImport = new TextField[4];
	private final CheckBox cbIncludePubsWithoutCRs = new CheckBox();
	private final CheckBox cbImportRandom = new CheckBox();
	
	private final RadioButton[] rbChart = new RadioButton[2];
	
	private final CheckBox cbSelectAll = new CheckBox("Select/Deselect All");
	private boolean duringSelectAll = new Boolean(false);
	
	public Settings() throws IOException {
		
		super();

		this.setResizable(true);
		
		TabPane tpane = new TabPane();
		tpane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		
		VBox tabTable = new VBox(10);
		tabTable.setPadding (new Insets(20, 20, 20, 20));
		
		tabTable.getChildren().add(cbSelectAll);
		
		
		cbSelectAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	if (duringSelectAll) return;
		    	duringSelectAll = true;
		    	for (CheckBox cb: cbCol) cb.setSelected(newValue);
		    	duringSelectAll = false;
		    }
		});
		
		tabTable.getChildren().add(new TitledPane("Cited References", createTableColPane(ColGroup.CR)));
		tabTable.getChildren().add(new TitledPane("Indicators", createTableColPane(ColGroup.INDICATOR)));
		tabTable.getChildren().add(new TitledPane("Clustering", createTableColPane(ColGroup.CLUSTER)));
		tabTable.getChildren().add(new TitledPane("Searching", createTableColPane(ColGroup.SEARCH)));
		tabTable.getChildren().add(new TitledPane("Value Settings", createTableDataPane()));
		tpane.getTabs().add(new Tab("Table", tabTable));

		
//		tabTable.getChildren().filtered(it -> it instanceof TitledPane).forEach(t -> {
//
//			TitledPane a = (TitledPane) t; 
//			a.heightProperty().addListener((obs, oldHeight, newHeight) -> {
//				this.setHeight(163 + tabTable.getChildren().stream().filter(c -> c instanceof TitledPane).mapToInt(n -> 10 + (int) ((TitledPane) n).getHeight()).sum());
//			});
//			a.setAnimated(false);
//		});
				
		
		
		VBox tabChart = new VBox(10);
		tabChart.setPadding (new Insets(20, 20, 20, 20));
		tabChart.getChildren().add(new TitledPane("Chart Layout", createChartLayoutPane()));
		tabChart.getChildren().add(new TitledPane("Chart Engine (Switching chart engines requires re-start of CRExplorer!)", createChartEnginePane()));
		tpane.getTabs().add(new Tab("Chart", tabChart));

		VBox tabImport = new VBox(10);
		tabImport.setPadding (new Insets(20, 20, 20, 20));
		tabImport.getChildren().add(new TitledPane("Restrict Import of Cited References", createImportRestrictionPane()));
		tabImport.getChildren().add(new TitledPane("Advanced Import/Export Options", createAdvancedExportPane()));
		tpane.getTabs().add(new Tab("Import/Export", tabImport));
		
		VBox[] q = {tabTable, tabChart, tabImport};
		Arrays.asList(q).stream().forEach(it -> it.getChildren().filtered(t -> t instanceof TitledPane).forEach(t -> {
			TitledPane a = (TitledPane) t; 
			a.heightProperty().addListener((obs, oldHeight, newHeight) -> {
				this.setHeight(183 + it.getChildren().stream().filter(c -> c instanceof TitledPane).mapToInt(n -> 10 + (int) ((TitledPane) n).getHeight()).sum());
			});
			a.setAnimated(false);
		}));
		
		
		setSelectAll();

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
		    	noOfErrors += UserSettings.get().setChartSize(new String[] { tfLine[0].getText(), tfLine[1].getText(), tfLine[2].getText(), tfLine[3].getText() });
		    	noOfErrors += UserSettings.get().setMedianRange(tfMedian.getText());
		    	noOfErrors += UserSettings.get().setMaxCR(tfImport[0].getText());
		    	noOfErrors += UserSettings.get().setMaxPub(tfImport[1].getText());
		    	noOfErrors += UserSettings.get().setIncludePubsWithoutCRs(cbIncludePubsWithoutCRs.isSelected());
		    	noOfErrors += UserSettings.get().setImportRandom(cbImportRandom.isSelected());
		    	noOfErrors += UserSettings.get().setRange(UserSettings.RangeType.ImportYearRange, new String[] { tfImport[2].getText(), tfImport[3].getText()} );
		    	noOfErrors += UserSettings.get().setNPCTRange(tfNPCT.getText());
		    	UserSettings.get().setChartEngine(rbChart[0].isSelected() ? 0 : 1);
		    	
		    	
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
	
	
	private GridPane createChartEnginePane() {
		
		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		final ToggleGroup group = new ToggleGroup();
		String[] label = new String[] { "JFreeChart", "Highcharts"};
		for (int i=0; i<label.length; i++) {
			rbChart[i] = new RadioButton(label[i]);
			rbChart[i].setSelected(UserSettings.get().getChartEngine()==i);
			rbChart[i].setToggleGroup(group);
			result.add(rbChart[i], 0, i);			
		}

		return result;
		
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
		
		label = new String[] { "Stroke Size", "Shape Size", "Label Font Size", "Tick Font Size"};
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
		
		String[] label = new String[] { "Maximum Number of CRs", "Maximum Number of Publications ", "Minimum Publication Year", "Maximum Publication Year" };
		long[] value = new long[] { UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange)[0], UserSettings.get().getRange(RangeType.ImportYearRange)[1] };
		for (int i=0; i<label.length; i++) {
			result.add(new Label(label[i]), 0, i);
			tfImport[i] = new TextField(String.valueOf(value[i]));
			tfImport[i].setMaxWidth(50);
			result.add(tfImport[i], 1, i);
		}
		
		cbImportRandom.setText("Import Random");
		cbImportRandom.setSelected(UserSettings.get().getImportRandom());
		result.add(cbImportRandom, 0, label.length);
		
		return result;
		
		
	}
	
	private GridPane createAdvancedExportPane() {

		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		cbIncludePubsWithoutCRs.setText("Include Publications without CRs in export");
		cbIncludePubsWithoutCRs.setSelected(UserSettings.get().getIncludePubsWithoutCRs());
		result.add(cbIncludePubsWithoutCRs, 0, 0);
		return result;
	}
	
	
	private GridPane createTableDataPane() {
		GridPane result =  new GridPane();
		result.setHgap(10);
		result.setVgap(10);
		result.setPadding(new Insets(20, 20, 20, 20));
		
		result.add(new Label("Number of Digits"), 0, 0);
		tfDigits.setMaxWidth(50);
		tfDigits.setText(UserSettings.get().getFormatDigits());
		result.add(tfDigits, 1, 0);
		
		result.add(new Label("N_PCT Range"), 0, 1);
		tfNPCT.setMaxWidth(50);
		tfNPCT.setText(String.valueOf(UserSettings.get().getNPCTRange()));
		result.add(tfNPCT, 1, 1);
		
		return result;
		
	}
	
	private GridPane createTableColPane(ColGroup group) {
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
		for (CRColumn e: CRTableView.CRColumn.values()) {
			
			if (e.group == group) {
				cbCol[idx] = new CheckBox(String.format("%s (%s)", e.id, e.title));
				cbCol[idx].setMnemonicParsing(false);
				cbCol[idx].setSelected(UserSettings.get().getColumnVisibleProperty(idx).get());
				
				cbCol[idx].selectedProperty().addListener(new ChangeListener<Boolean>() {
				    @Override
				    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				    	setSelectAll();
				    }
				});
				
				result.add(cbCol[idx], col, row);
				if (col<2) {
					col++;
				} else {
					row++;
					col=0;
				}
			}
			idx++;
		}
		return result;
	}
	
	
	private void setSelectAll () {
		if (duringSelectAll) return;
		duringSelectAll = true;
		int sum = 0;
    	for (CheckBox cb: cbCol) sum += cb.isSelected() ? 1 : 0;
    	cbSelectAll.setSelected(sum==cbCol.length);
    	cbSelectAll.setIndeterminate((sum!=0) && (sum!=cbCol.length));
    	duringSelectAll = false;
	}
}

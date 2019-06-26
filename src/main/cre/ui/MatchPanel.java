package main.cre.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import main.cre.data.type.abs.Clustering;

public abstract class MatchPanel extends TitledPane {

	Button[] matchManual = new Button[3];	// buttons "same", "different", and "extract"
	Button matchUndo;						// UnDo button
	CheckBox[] volPagDOI;					// checkboxes to use VOL, PAG, and DOI for clustering
	Slider threshold = new Slider(50,  100,  75);	// Slider for similariy threshold
	
	
	public abstract void onUpdateClustering(double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI); 
	public abstract void onMatchManual(Clustering.ManualMatchType type, double threshold, boolean useVol, boolean usePag, boolean useDOI); 
	public abstract void onMatchUnDo (double threshold, boolean useVol, boolean usePag, boolean useDOI); 
	
	
	public MatchPanel() {
		super();

		// ensures that setVisible(false) hides entire panel
		managedProperty().bind(visibleProperty());
		setCollapsible(true);
		setText("Matching");

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 20, 10, 20));
		
		// Threshold slider
		threshold.setMajorTickUnit(10);
		threshold.setMinorTickCount(9);
		threshold.setShowTickLabels(true);
		threshold.setShowTickMarks(true);
		threshold.setSnapToTicks(true);
		threshold.setPrefWidth(300);
		threshold.valueProperty().addListener((ov, old_val, new_val) -> { if (!threshold.isValueChanging()) { updateClustering(); } });
		grid.add (threshold, 0, 0);

		
		// Volume CheckBoxes
		volPagDOI = new CheckBox[] { new CheckBox("Volume"), new CheckBox("Page"), new CheckBox("DOI") };
		GridPane g = new GridPane();
		g.setPrefWidth(100);
		for (int i=0; i<volPagDOI.length; i++) {
			g.add(volPagDOI[i], 0, i);
			volPagDOI[i].selectedProperty().addListener( (observable, oldValue, newValue) -> { updateClustering(); });
		}
		grid.add (g, 1, 0);
		GridPane.setMargin(g, new Insets(0, 0, 0, 30));
	
		// Match Buttons
		for (int i=0; i<Clustering.ManualMatchType.values().length; i++) {
			Clustering.ManualMatchType type = Clustering.ManualMatchType.values()[i];
			matchManual[i] = new Button (type.toString().substring(0, 1) + type.toString().substring(1).toLowerCase());	// "SAME" --> "Same"
			matchManual[i].setPrefSize(100, 25);
			matchManual[i].setOnAction(e -> { onMatchManual(type, 0.01d*threshold.getValue(), volPagDOI[0].isSelected(), volPagDOI[1].isSelected(), volPagDOI[2].isSelected()); });
			grid.add (matchManual[i], i+2, 0);
			GridPane.setMargin(matchManual[i], new Insets(0, 0 , 0, (i==0)?30:10));
		}
		
		matchUndo = new Button("Undo");
		matchUndo.setPrefSize(100, 25);
		matchUndo.setOnAction(e -> { onMatchUnDo(0.01d*threshold.getValue(), volPagDOI[0].isSelected(), volPagDOI[1].isSelected(), volPagDOI[2].isSelected()); });
		grid.add (matchUndo, 5, 0);
		GridPane.setMargin(matchUndo, new Insets(0, 0 , 0, 30));
		
		setContent(grid);
		
	}


	public void updateClustering() {
		onUpdateClustering(0.01d*threshold.getValue(), true, volPagDOI[0].isSelected(), volPagDOI[1].isSelected(), volPagDOI[2].isSelected());
	}
	


	
}

package cre.test.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class MatchPanel extends GridPane {

	Button[] manual = new Button[] { new Button("Same"), new Button("Different"), new Button ("Extract"), new Button ("Undo") };
	Slider threshold = new Slider(50,  100,  75);
	
	public MatchPanel() {
		super();
		
		GridPane.setColumnIndex(this,  0);
		GridPane.setRowIndex(this,  0);
		GridPane.setHgrow(this, Priority.ALWAYS);
		setPadding(new Insets(5, 20, 5, 20));

//		Label sbdate = new Label("jklh");
//		sbdate.setPadding(new Insets (0, 10, 0, 0));
//		sbdate.setTextFill(Color.GRAY);
//		add(sbdate, 0, 0);
//		
//		Label sblabel = new Label("lökjlkjlk kl");
//		sblabel.setPadding(new Insets (0, 10, 0, 0));
//		add(sblabel, 1, 0);
//		
//		Label sbinfo = new Label(">>>");
//		sbinfo.setPadding(new Insets (0, 0, 0, 0));
//
//		add(sbinfo, 3, 0);
		
		
		CheckBox[] VolPagDOI = new CheckBox[] { new CheckBox("Volume"), new CheckBox("Page"), new CheckBox("DOI") };
		GridPane g = new GridPane();
		g.setPrefWidth(100);
		for (int i=0; i<3; i++) {
			g.add(VolPagDOI[i], 0, i);
		}
		
		add (g, 1, 0);
		setMargin(g, new Insets(0, 0, 0, 30));
	
		threshold.setMajorTickUnit(10);
		threshold.setMinorTickCount(9);
		threshold.setShowTickLabels(true);
		threshold.setShowTickMarks(true);
		threshold.setSnapToTicks(true);
		threshold.setPrefWidth(300);
		add (threshold, 0, 0);
		
		for (int i=0; i<4; i++) {
			manual[i].setPrefSize(100, 25);
			add (manual[i], i+2, 0);
			setMargin(manual[i], new Insets(0, 0 , 0, ((i==0) || (i==3))?30:10));
		}
		
		
		
		
	}
	


	
}

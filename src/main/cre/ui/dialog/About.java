package main.cre.ui.dialog;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import main.cre.CitedReferencesExplorer;

public class About extends Alert {

	
	public About() {
		
		super(AlertType.INFORMATION);
		
		setTitle("Info");
		setHeaderText("About " + CitedReferencesExplorer.title);
		setResizable(true);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));

		grid.add(new Label("Software Development:"), 0, 0);
		grid.add(new Label("Andreas Thor <thor@hft-leipzig.de>"), 1, 0);
		grid.add(new Label("Content Development:"), 0, 1);
		grid.add(new Label("Lutz Bornmann and Werner Marx"), 1, 1);
		grid.add(new Label("with further support of:"), 0, 2);
		grid.add(new Label("Robin Haunschild, Loet Leydesdorff, and Rüdiger Mutz"), 1, 2);
		Hyperlink hp = new Hyperlink("Project website: crexplorer.net");
		hp.setOnAction(e -> {
			HostServicesFactory.getInstance(CitedReferencesExplorer.app).showDocument(CitedReferencesExplorer.url);
		});
		grid.add(hp, 0, 3);
		
		getDialogPane().setContent(grid);
			
	}
	

	
}

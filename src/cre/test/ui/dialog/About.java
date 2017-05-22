package cre.test.ui.dialog;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;

import cre.test.CitedReferencesExplorer;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

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

		grid.add(new Label("Developed by Andreas Thor <thor@hft-leipzig.de>"), 0, 0);
		grid.add(new Label("Joint work with Werner Marx and Lutz Bornmann"), 0, 1);
		grid.add(new Label("in co-operation with and support of Robin Haunschild, Loet Leydesdorff, and R�diger Mutz."), 0, 2);
		Hyperlink hp = new Hyperlink("Project website: crexplorer.net");
		hp.setOnAction(e -> {
			HostServicesFactory.getInstance(CitedReferencesExplorer.app).showDocument(CitedReferencesExplorer.url);
		});
		grid.add(hp, 0, 3);
		
		getDialogPane().setContent(grid);
			
	}
	

	
}

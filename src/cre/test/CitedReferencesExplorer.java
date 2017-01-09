package cre.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CitedReferencesExplorer extends Application {

	
	public static Stage stage;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		CitedReferencesExplorer.stage = stage;
		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
	    
        Scene scene = new Scene(root, 800, 600);
        
        
        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
	}

}

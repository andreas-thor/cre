package cre.test;

import java.util.Locale;

import cre.test.ui.UserSettings;
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

		Locale.setDefault(new Locale("en"));

		CitedReferencesExplorer.stage = stage;
		
		
		stage.setOnCloseRequest(event -> {
		   UserSettings.get().saveUserPrefs();
		});
		
		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
	    
        Scene scene = new Scene(root, 800, 600);
        
        
        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
	}

}

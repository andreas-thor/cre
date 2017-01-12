package cre.test;

import java.util.Locale;

import cre.test.ui.UserSettings;
import javafx.application.Application;
import javafx.application.Platform;
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

		Locale.setDefault(Locale.US);
		Platform.setImplicitExit(true);
		
//		setUserAgentStylesheet(STYLESHEET_CASPIAN); // Switches to "Caspian"
		setUserAgentStylesheet(STYLESHEET_MODENA);  // Switches to "Modena"
		
		CitedReferencesExplorer.stage = stage;
		
		
		stage.setWidth(UserSettings.get().getWindowWidth());
		stage.setHeight(UserSettings.get().getWindowHeight());
		stage.setX(UserSettings.get().getWindowX());
		stage.setY(UserSettings.get().getWindowY());
		
		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
	    
        Scene scene = new Scene(root); // , 800, 600);
        
        
        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
	}

}

package cre.test;

import java.util.Locale;

import cre.test.data.UserSettings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class CitedReferencesExplorer extends Application {

	
	public static Stage stage;
	public static Application app;
	public static String url = "http://www.crexplorer.net";
	public static String title = "CRExplorer (Version 1.71 DEVELOPMENT)";
	
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
		CitedReferencesExplorer.app = this;
		
		stage.setWidth(UserSettings.get().getWindowWidth());
		stage.setHeight(UserSettings.get().getWindowHeight());
		stage.setX(UserSettings.get().getWindowX());
		stage.setY(UserSettings.get().getWindowY());
		stage.getIcons().add(new Image("file:CRE32.png"));
		
		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
	    
        Scene scene = new Scene(root); // , 800, 600);
        
        
		stage.setTitle(CitedReferencesExplorer.title);
        stage.setScene(scene);
        stage.show();
	}

}

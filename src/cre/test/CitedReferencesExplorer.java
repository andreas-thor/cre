package cre.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
	public static String manual_url = "http://andreas-thor.github.io/cre/manual.pdf";
	public static String url = "http://www.crexplorer.net";
	// public static String title = "CRExplorer (Version 1.77)";
	public static String title = "CRExplorer (DEVELOPMENT; Dec-19-2017)";
	public static String loadOnOpen = null;

	public static void main(String[] args) {

		if ((args.length > 1) && (args[0].equals("-open"))) {
			loadOnOpen = args[1];
		}

		try {
			StringBuilder result = new StringBuilder();
			URL url = new URL("https://crexplorer-186022.appspot.com/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(2000);
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
//			System.out.println(result.toString());
		
		} catch (Exception e) {
		}

		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		Locale.setDefault(Locale.US);
		Platform.setImplicitExit(true);

		// setUserAgentStylesheet(STYLESHEET_CASPIAN); // Switches to "Caspian"
		setUserAgentStylesheet(STYLESHEET_MODENA); // Switches to "Modena"

		CitedReferencesExplorer.stage = stage;
		CitedReferencesExplorer.app = this;

		stage.setWidth(UserSettings.get().getWindowWidth());
		stage.setHeight(UserSettings.get().getWindowHeight());
		stage.setX(UserSettings.get().getWindowX());
		stage.setY(UserSettings.get().getWindowY());
		// stage.getIcons().add(new Image("file:CRE32.png"));
		stage.getIcons().add(new Image(getClass().getResourceAsStream("CRE32.png")));
		// stage.getIcons().add(new Image("file:../../deploy/CRE32.png"));
		stage.setTitle(CitedReferencesExplorer.title);

		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
		Scene scene = new Scene(root); // , 800, 600);
		stage.setScene(scene);
		stage.show();
	}

}

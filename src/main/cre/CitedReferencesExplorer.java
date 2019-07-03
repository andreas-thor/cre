package main.cre;

import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;

public class CitedReferencesExplorer extends Application {

	public static Stage stage;
	public static Application app;
	public static String manual_url = "http://andreas-thor.github.io/cre/manual.pdf";
	public static String url = "http://www.crexplorer.net";
//	public static String title = "CRExplorer (Version 1.9)";
	public static String title = "CRExplorer (DEVELOPMENT; May-29-2019)";
	public static String loadOnOpen = null;

	public static void main(String[] args) {

		if ((args.length > 1) && (args[0].equals("-open"))) {
			loadOnOpen = args[1];
		}

//		title += " " + String.join(" ", args);
		
		
		for (String arg: args) {
			if (arg.toLowerCase().startsWith("-db")) {
				CRTable.type = TABLE_IMPL_TYPES.DB;
				String[] split = arg.split("=");
				CRTable.name = (split.length==2) ? split[1] : "test";
				title += String.format(" (DB=%s)", CRTable.name); 
			}
		}

		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		Locale.setDefault(Locale.US);
		Platform.setImplicitExit(true);

		setUserAgentStylesheet(STYLESHEET_MODENA); 

		CitedReferencesExplorer.stage = stage;
		CitedReferencesExplorer.app = this;
		
		stage.getIcons().add(new Image(getClass().getResourceAsStream("ui/CRE32.png")));
		stage.setTitle(CitedReferencesExplorer.title);

		Parent root = FXMLLoader.load(getClass().getResource("ui/Main.fxml"));
		Scene scene = new Scene(root); 
		stage.setScene(scene);
		stage.show();
	}

}

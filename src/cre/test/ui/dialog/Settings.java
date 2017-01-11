package cre.test.ui.dialog;

import java.io.IOException;
import java.util.Map.Entry;

import cre.test.data.CRType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;

public class Settings extends Dialog<Boolean> {

	
	@FXML GridPane colPane;

	public Settings() throws IOException {
		// TODO Auto-generated constructor stub
		
		super();
		TabPane content = new FXMLLoader(getClass().getResource("Settings.fxml")).load();  

		
		GridPane g = (GridPane) content.getTabs().get(0).getContent();
		
		int col = 0;
		int row = 0;
		for (Entry<String, String> e: CRType.attr.entrySet()) {
			g.add(new CheckBox(e.getValue()), col, row);
			if (col<2) {
				col++;
			} else {
				row++;
				col=0;
			}
		}
		
		
		
		getDialogPane().setContent(content);
		
		
		
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		
		
	}
}

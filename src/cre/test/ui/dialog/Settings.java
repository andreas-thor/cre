package cre.test.ui.dialog;

import java.io.IOException;
import java.util.Map.Entry;

import cre.test.data.CRType;
import cre.test.ui.CRTableView;
import cre.test.ui.UserSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.GridPane;

public class Settings extends Dialog<Boolean> {

	
	@FXML GridPane colPane;

	public Settings() throws IOException {
		// TODO Auto-generated constructor stub
		
		super();
		TabPane tpane = new TabPane();
		tpane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		
		
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(10);
		g.setPadding(new Insets(20, 20, 20, 20));
		
		int col = 0;
		int row = 0;
		int idx = 0;
		final CheckBox[] cb = new CheckBox[CRTableView.attr.size()];
		for (Entry<String, String> e: CRTableView.attr.entrySet()) {
			cb[idx] = new CheckBox(e.getValue());
			cb[idx].setSelected(UserSettings.get().columnVisible[idx].get());
			g.add(cb[idx], col, row);
			if (col<2) {
				col++;
			} else {
				row++;
				col=0;
			}
			idx++;
		}
		
		
		tpane.getTabs().add(new Tab("Table", g));
		
		
		
		getDialogPane().setContent(tpane);
		
		
		
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());

		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		    	for (int i=0; i<cb.length; i++) {
		    		UserSettings.get().columnVisible[i].set(cb[i].isSelected());
		    	}
		    }
		    return null;
		});
		
	}
}

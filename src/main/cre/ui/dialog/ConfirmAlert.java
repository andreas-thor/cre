package main.cre.ui.dialog;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConfirmAlert extends Alert {

	
	public ConfirmAlert(String header, boolean condition, String[] message) {
		
		super (condition ? AlertType.ERROR : AlertType.WARNING);
		
		setHeaderText(header);
		if (condition) {
			setTitle ("Error");
			setContentText(message[0]);
			getDialogPane().getButtonTypes().clear();
			getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());
		} else {
			setTitle ("Warning");
			setContentText(message[1]);
			
			DialogPane pane = getDialogPane();
			pane.getButtonTypes().clear();
			pane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
			pane.getButtonTypes().stream().map (pane::lookupButton).map (Button.class::cast).forEach( button -> {
				button.setDefaultButton(false);
				button.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
					if (KeyCode.ENTER.equals(event.getCode()) && event.getTarget() instanceof Button) {
					      ((Button) event.getTarget()).fire();
					   }
				});
			});
			
			Platform.runLater(() -> {
				((Button) pane.lookupButton(ButtonType.NO)).requestFocus();
			});
		}
		
	}
	
}

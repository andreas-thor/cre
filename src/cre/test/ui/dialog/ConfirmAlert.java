package cre.test.ui.dialog;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class ConfirmAlert extends Dialog<Boolean> {

	
	public ConfirmAlert(String header, boolean condition, String[] message) {
		
		setHeaderText(header);
		if (condition) {
			setTitle ("Error");
			setContentText(message[0]);
			getDialogPane().getButtonTypes().addAll(ButtonType.OK);
			Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());

			setResultConverter(dialogButton -> {
			    return false;
			});
		} else {
			setTitle ("Warning");
			setContentText(message[1]);
			getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
			Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.NO).requestFocus());

			setResultConverter(dialogButton -> {
			    return (dialogButton == ButtonType.YES); 
			});
		}
		
	}
	
}

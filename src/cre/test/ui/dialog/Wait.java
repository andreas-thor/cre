package cre.test.ui.dialog;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class Wait extends Dialog<Boolean> {

	
	public Wait() {
		
		setHeaderText("Wait for operation in progress.");
		setTitle ("Waiting");
		setContentText("Press Cancel button to abort current operation.");
		getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.CANCEL).requestFocus());

		
		setResultConverter(dialogButton -> {
		    return (dialogButton == ButtonType.CANCEL); 
		});
	}
	
}

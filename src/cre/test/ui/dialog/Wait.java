package cre.test.ui.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class Wait extends Dialog<Boolean> {

	
	public Wait() {
		
		setHeaderText("Wait for operation in progress.");
		setTitle ("Waiting");
		setContentText("Press Cancel button to abort current operation.");
		getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
		getDialogPane().lookupButton(ButtonType.CANCEL).requestFocus();

		
		setResultConverter(dialogButton -> {
		    return (dialogButton == ButtonType.CANCEL); 
		});
	}
	
	public void abortDueToException () {
		close();
	}
	
}

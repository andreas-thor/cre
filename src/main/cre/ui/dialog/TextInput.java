package main.cre.ui.dialog;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class TextInput extends Dialog<String> {

	
	public TextInput(String header, String message) {
		
		setHeaderText(header);
		setTitle ("Input");


		TextArea textArea = new TextArea();
		textArea.setEditable(true);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(new Label(message), 0, 0);
		expContent.add(textArea, 0, 1);
		getDialogPane().setContent(expContent);
		
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.CANCEL).requestFocus());

		setResultConverter(dialogButton -> {
		    return (dialogButton == ButtonType.OK) ? textArea.getText() : null; 
		});
		
				
		
	}
	
}

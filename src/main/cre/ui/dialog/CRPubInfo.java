package main.cre.ui.dialog;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import main.cre.data.type.abs.CRType;

public class CRPubInfo extends Dialog<Void> {

	
	public CRPubInfo(CRType cr) {
		super();
		
		setTitle ("Info");
		setHeaderText(cr.getN_CR() + " Citing Publications of Cited Reference " + cr.getID());
		getDialogPane().getButtonTypes().addAll(ButtonType.OK);

		AtomicLong idx = new AtomicLong();
		TextArea textArea = new TextArea(String.join("\n\n", cr.getPub().sorted().map(pub -> String.format("%4d) %s", idx.incrementAndGet(), pub.toLineString())).collect(Collectors.toList())));
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(textArea, 0, 0);

		getDialogPane().setContent(expContent);
		
		
		Platform.runLater(() -> getDialogPane().lookupButton(ButtonType.OK).requestFocus());


			
	}
	

	
}

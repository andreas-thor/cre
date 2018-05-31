package main.cre.ui.statusbar;

import java.util.Date;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class StatusBarFX extends GridPane implements StatusBarUI {

	private Label sbdate;
	private Label sblabel;
	private ProgressBar sbpb;
	private Label sbinfo;
	
	
	
	public StatusBarFX() {
		
		super();

		GridPane.setColumnIndex(this,  0);
		GridPane.setRowIndex(this,  0);
		GridPane.setHgrow(this, Priority.ALWAYS);
		setPadding(new Insets(5, 10, 5, 10));

		this.sbdate = new Label();
		this.sbdate.setPadding(new Insets (0, 10, 0, 0));
		this.sbdate.setTextFill(Color.GRAY);
		add(this.sbdate, 0, 0);
		
		this.sblabel = new Label();
		this.sblabel.setPadding(new Insets (0, 10, 0, 0));
		add(this.sblabel, 1, 0);
		
		this.sbpb = new ProgressBar(0);
		GridPane.setHgrow(this.sbpb, Priority.ALWAYS);
		this.sbpb.setMaxWidth(999999);
		this.sbpb.setPadding(new Insets (0, 10, 0, 0));
		add(this.sbpb, 2, 0);
		
		this.sbinfo = new Label();
		this.sbinfo.setPadding(new Insets (0, 0, 0, 0));
		add(this.sbinfo, 3, 0);
		
		
		
	}

	
	@Override
	public void printInfo(String info) {
		sbinfo.setText (info);
	}



	@Override
	public void print(String label, long percent, Date d) {
		Platform.runLater( () -> {
			sbdate.setText(d.toString());
			sblabel.setText(label);
			sbpb.setProgress(percent/100.0);
		});	
	}
	
	
}

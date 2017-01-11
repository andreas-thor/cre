package cre.test.ui;

import java.io.File;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class UserSettings {

	/* Singleton */
	private static UserSettings set = null;
	
	public SimpleIntegerProperty[] filterByRPYRange;
	public SimpleIntegerProperty[] removeByRPYRange;
	public SimpleIntegerProperty[] removeByNCRRange;
	public SimpleIntegerProperty[] retainByRPYRange;
	public File lastFileDir;
	
	public SimpleBooleanProperty[] columnVisible;
	
	private UserSettings() { 	// private avoids direct instantiation
		
		filterByRPYRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		removeByRPYRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		removeByNCRRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		retainByRPYRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		lastFileDir = new File("");
		
		columnVisible = new SimpleBooleanProperty[CRTableView.attr.size()];
		for (int i=0; i<columnVisible.length; i++) {
			columnVisible[i] = new SimpleBooleanProperty(true);
		}
	}
		
	public static UserSettings get() {
		
		if (UserSettings.set==null) {
			UserSettings.set = new UserSettings();
		}
		return UserSettings.set;
	}
	
	
	
	
	
	
}

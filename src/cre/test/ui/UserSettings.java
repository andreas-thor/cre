package cre.test.ui;

import javafx.beans.property.SimpleIntegerProperty;

public class UserSettings {

	/* Singleton */
	private static UserSettings set = null;
	
	public SimpleIntegerProperty[] filterByRPYRange;
	public SimpleIntegerProperty[] removeByRPYRange;
	public SimpleIntegerProperty[] removeByNCRRange;
	public SimpleIntegerProperty[] retainByRPYRange;
	
	
	private UserSettings() { 	// private avoids direct instantiation
		
		filterByRPYRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		removeByRPYRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		removeByNCRRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
		retainByRPYRange = new SimpleIntegerProperty[] { new SimpleIntegerProperty(-1), new SimpleIntegerProperty(-1) };
	}
		
	public static UserSettings get() {
		
		if (UserSettings.set==null) {
			UserSettings.set = new UserSettings();
		}
		return UserSettings.set;
	}
	
	
	
	
	
	
}

package cre.test.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.prefs.Preferences;

import cre.test.CitedReferencesExplorer;
import javafx.beans.property.SimpleBooleanProperty;

public class UserSettings {

	// SINGLETON
	private static UserSettings set = null;

	// store Preferences
	private Preferences userPrefs;

	// ranges
	public static enum RangeType { FilterByRPYRange, RemoveByRPYRange, RemoveByNCRRange, RetainByRPYRange, ImportYearRange }
	private int[][] range = new int[][] { { -1, -1 }, { -1, -1 }, { -1, -1 }, { -1, -1 }, {0, 0} };

	// directory for loading/saving files
	private File lastFileDir = new File("");

	// visibility of table columns and number of digits
	private SimpleBooleanProperty[] columnVisible = new SimpleBooleanProperty[CRTableView.CRColumn.values().length];
	private int formatDigits = 2;
	private DecimalFormat format;

	// chart
	private boolean[] chartLine = new boolean[] { true, true };
	private int[] chartSize = new int[] { 1, 3 };
	private int medianRange = 2;

	// import restrictions
	private int maxCR = 0;
	private double[] window = new double[] {800, 600, 0, 0};

	private int chartEngine;
	
	/**
	 * Singleton pattern
	 * 
	 * @return
	 */
	public static UserSettings get() {

		if (UserSettings.set == null) {
			UserSettings.set = new UserSettings();
		}
		return UserSettings.set;
	}

	private UserSettings() { // private avoids direct instantiation
		userPrefs = Preferences.userNodeForPackage(CitedReferencesExplorer.class);
		loadUserPrefs();
	}

	public void loadUserPrefs() {

		for (RangeType r : RangeType.values()) {
			setRange(r, new int[] { userPrefs.getInt(r.toString() + "0", -1), userPrefs.getInt(r.toString() + "1", -1) });
		}

		for (int i = 0; i < columnVisible.length; i++) {
			columnVisible[i] = new SimpleBooleanProperty();
			columnVisible[i].set(userPrefs.getBoolean("columnVisible" + i, true));
		}

		setLastFileDir(new File(userPrefs.get("lastFileDir", "")));
		setFormatDigits(userPrefs.getInt("formatDigits", formatDigits));

		for (int i = 0; i < chartLine.length; i++) {
			chartLine[i] = userPrefs.getBoolean("chartLine" + i, chartLine[i]);
		}
		for (int i = 0; i < chartSize.length; i++) {
			chartSize[i] = userPrefs.getInt("chartSize" + i, chartSize[i]);
		}
		medianRange = userPrefs.getInt("medianRange", medianRange);
		maxCR = userPrefs.getInt("maxCR", maxCR);
		chartEngine = userPrefs.getInt("chartEngine", 0);
		
		window = new double[] {
				userPrefs.getDouble("WindowWidth", 800), 
				userPrefs.getDouble("WindowHeight", 600),
				userPrefs.getDouble("WindowX", 100), 
				userPrefs.getDouble("WindowY", 100)
		};
		
	}

	public void saveUserPrefs(double windowWidth, double windowHeight, double windowX, double windowY) {

		for (RangeType r : RangeType.values()) {
			userPrefs.putInt(r.toString() + "0", getRange(r)[0]);
			userPrefs.putInt(r.toString() + "1", getRange(r)[1]);
		}

		for (int i = 0; i < columnVisible.length; i++) {
			userPrefs.putBoolean("columnVisible" + i, columnVisible[i].getValue());
		}

		userPrefs.put("lastFileDir", lastFileDir.getAbsolutePath());
		userPrefs.putInt("formatDigits", formatDigits);

		for (int i = 0; i < chartLine.length; i++) {
			userPrefs.putBoolean("chartLine" + i, chartLine[i]);
		}
		for (int i = 0; i < chartSize.length; i++) {
			userPrefs.putInt("chartSize" + i, chartSize[i]);
		}
		userPrefs.putInt("medianRange", medianRange);
		userPrefs.putInt("maxCR", maxCR);
		userPrefs.putInt("chartEngine", chartEngine);
		
		userPrefs.putDouble("WindowWidth", windowWidth);
		userPrefs.putDouble("WindowHeight", windowHeight);
		userPrefs.putDouble("WindowX", windowX); 
		userPrefs.putDouble("WindowY", windowY);
	}

	public double getWindowWidth () {
		return window[0];
	}
	public double getWindowHeight () {
		return window[1];
	}
	public double getWindowX () {
		return window[2];
	}
	public double getWindowY () {
		return window[3];
	}
	
	
	public String getFormatDigits() {
		return String.valueOf(formatDigits);
	}

	public int setFormatDigits(String formatDigits) {
		try {
			return setFormatDigits(Integer.parseInt(formatDigits));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	private int setFormatDigits(int formatDigits) {
		this.formatDigits = formatDigits;
		String zero = "";
		for (int i=1; i<=this.formatDigits; i++) zero += "0";
		this.format = new DecimalFormat( "##0." + zero + "%" );
		return 0;
	}
	
	public DecimalFormat getFormat() {
		return this.format;
	}

	public int[] getRange(RangeType r) {
		return Arrays.copyOf(this.range[r.ordinal()], this.range[r.ordinal()].length);
	}

	public int setRange(RangeType r, int[] range) {
		if (range[0] > range[1])
			return 1;
		this.range[r.ordinal()][0] = range[0];
		this.range[r.ordinal()][1] = range[1];
		return 0;
	}

	public int setRange(RangeType r, String[] range) {

		try {
			return setRange(r, new int[] { Integer.parseInt(range[0]), Integer.parseInt(range[1]) });
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	public File getLastFileDir() {
		return lastFileDir;
	}

	public void setLastFileDir(File lastFileDir) {
		this.lastFileDir = lastFileDir;
		if (!this.lastFileDir.exists()) {
			this.lastFileDir = new File(System.getProperty("user.dir"));
		}
	}

	public SimpleBooleanProperty getColumnVisibleProperty(int idx) {
		return columnVisible[idx];
	}

	public boolean[] getChartLine() {
		return Arrays.copyOf(chartLine, chartLine.length);
	}

	public int setChartLine(boolean[] chartLine) {
		this.chartLine = Arrays.copyOf(chartLine, chartLine.length);
		for (boolean c: this.chartLine) {
			if (c) return 0;
		}
		
		// all lines not visible --> set first line to true
		this.chartLine[0] = true;
		return 1;	
	}

	public int[] getChartSize() {
		return Arrays.copyOf(chartSize, chartSize.length);
	}

	// checks that chartSize is Integer and >= 1
	public int setChartSize(String[] chartSize) {

		int result = 0;
		int[] temp = new int[this.chartSize.length];
		try {
			for (int i = 0; i < temp.length; i++) {
				temp[i] = Integer.parseInt(chartSize[i]);
				if (temp[i] < 1) {
					temp[i] = 1;
					result = 1;
				}
			}
		} catch (NumberFormatException e) {
			return 1;
		} catch (NullPointerException e2) {
			return 1;
		}

		this.chartSize = Arrays.copyOf(temp, temp.length);
		return result;
	}

	public int getMedianRange() {
		return medianRange;
	}

	// checks that Median Range is Integer and >= 1
	public int setMedianRange(String medianRange) {
		try {
			this.medianRange = Integer.parseInt(medianRange);
			if (this.medianRange < 1) {
				this.medianRange = 1;
				return 1;
			}
			return 0;
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	public int getMaxCR() {
		return maxCR;
	}

	public int setMaxCR (int maxCR) {
		this.maxCR = maxCR;
		if (this.maxCR<0) {
			this.maxCR = 0;
			return 1;
		}
		return 0;
	}
	
	
	public int setMaxCR (String maxCR) {
		try {
			return setMaxCR (Integer.parseInt(maxCR));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	public int getChartEngine() {
		return this.chartEngine;
	}

	public void setChartEngine (int chartEngine) {
		this.chartEngine = chartEngine;
	}



}

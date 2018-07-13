package main.cre.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleBooleanProperty;
import main.cre.CitedReferencesExplorer;
import main.cre.data.DownloadCrossrefData;
import main.cre.data.Sampling;

public class UISettings {

	// SINGLETON
	private static UISettings set = null;

	// store Preferences
	private Preferences userPrefs;

	// ranges
	public static enum RangeType { FilterByRPYRange, RemoveByRPYRange, RemoveByNCRRange, RetainByRPYRange, ImportRPYRange, CurrentYearRange, ImportPYRange }
	private int[][] range = new int[][] { { -1, -1 }, { -1, -1 }, { -1, -1 }, { -1, -1 }, {0, 0}, {-1, -1}, {0, 0} };

	// directory for loading/saving files
	private File lastFileDir = new File("");

	// visibility of table columns and number of digits
	private SimpleBooleanProperty[] columnVisible = new SimpleBooleanProperty[CRTableView.CRColumn.values().length];
	private int formatDigits = 2;
	private DecimalFormat format;

	// chart
	private boolean[] chartLine = new boolean[] { true, true };
	private int[] chartSize = new int[] { 1, 3, 20, 12 };
	private int medianRange = 2;

	// PubYear Range (+/-) for NPCT indicators
	private int npctRange = 1;

	// import / export restrictions
	private int maxCR = 0;
	private int maxPub = 0;
	private boolean includePubsWithoutCRs = false;
	private boolean importRandom = false;
	
	private boolean importCRsWithoutYear = true;
	private boolean importPubsWithoutYear = true;
	
	
	private double[] window = new double[] {800, 600, 0, 0};
	

	private int chartEngine;
	
	private Sampling sampling;
	
	private DownloadCrossrefData downloadCrossrefData;
	
	
	/**
	 * Singleton pattern
	 * 
	 * @return
	 */
	public static UISettings get() {

		if (UISettings.set == null) {
			UISettings.set = new UISettings();
		}
		return UISettings.set;
	}

	private UISettings() { // private avoids direct instantiation
		userPrefs = Preferences.userNodeForPackage(CitedReferencesExplorer.class);
		loadUserPrefs();
	}

	public void loadUserPrefs() {

		for (RangeType r : RangeType.values()) {
			if (r != RangeType.CurrentYearRange) {
				setRange(r, new int[] { userPrefs.getInt(r.toString() + "0", -1), userPrefs.getInt(r.toString() + "1", -1) });
			}
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
		npctRange = userPrefs.getInt("npctRange", npctRange);

		maxCR = userPrefs.getInt("maxCR", maxCR);
		maxPub = userPrefs.getInt("maxPub", maxPub);
		includePubsWithoutCRs = userPrefs.getBoolean ("includePubsWithoutCRs", includePubsWithoutCRs);
		importRandom = userPrefs.getBoolean ("importRandom", importRandom);
		importCRsWithoutYear = userPrefs.getBoolean ("importCRsWithoutYear", importCRsWithoutYear);
		importPubsWithoutYear = userPrefs.getBoolean ("importPubsWithoutYear", importPubsWithoutYear);
		
		
		chartEngine = userPrefs.getInt("chartEngine", 0);
		
		window = new double[] {
				userPrefs.getDouble("WindowWidth", 800), 
				userPrefs.getDouble("WindowHeight", 600),
				userPrefs.getDouble("WindowX", 100), 
				userPrefs.getDouble("WindowY", 100)
		};
		
		
		downloadCrossrefData = new DownloadCrossrefData(
			userPrefs.get("DownloadCrossrefDataISSN", ""),
			new String[] {
				userPrefs.get("DownloadCrossrefDataPY0", "-1"),
				userPrefs.get("DownloadCrossrefDataPY1", "-1")
			},
			userPrefs.get("DownloadCrossrefDataDOI", "")
		);
				
		
	}

	public void saveUserPrefs(double windowWidth, double windowHeight, double windowX, double windowY) {

		for (RangeType r : RangeType.values()) {
			if (r != RangeType.CurrentYearRange) {
				userPrefs.putInt(r.toString() + "0", getRange(r)[0]);
				userPrefs.putInt(r.toString() + "1", getRange(r)[1]);
			}
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
		userPrefs.putInt("npctRange", npctRange);
		userPrefs.putInt("medianRange", medianRange);
		userPrefs.putInt("maxCR", maxCR);
		userPrefs.putInt("maxPub", maxPub);
		userPrefs.putBoolean ("includePubsWithoutCRs", includePubsWithoutCRs);
		userPrefs.putBoolean ("importRandom", importRandom);
		userPrefs.putBoolean ("importCRsWithoutYear", importCRsWithoutYear);
		userPrefs.putBoolean ("importPubsWithoutYear", importPubsWithoutYear);

		userPrefs.putInt("chartEngine", chartEngine);
		
		userPrefs.putDouble("WindowWidth", windowWidth);
		userPrefs.putDouble("WindowHeight", windowHeight);
		userPrefs.putDouble("WindowX", windowX); 
		userPrefs.putDouble("WindowY", windowY);
		
		userPrefs.put("DownloadCrossrefDataISSN", downloadCrossrefData.getISSN());
		userPrefs.put("DownloadCrossrefDataPY0", String.valueOf(downloadCrossrefData.getRange()[0]));
		userPrefs.put("DownloadCrossrefDataPY1", String.valueOf(downloadCrossrefData.getRange()[1]));
		userPrefs.put("DownloadCrossrefDataDOI", String.join("\n", downloadCrossrefData.getDOI()));
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

	public int getNPCTRange() {
		return npctRange;
	}
	
	
	// checks that NPCT Range is Integer and >= 0
	public int setNPCTRange(String npctRange) {
		try {
			this.npctRange = Integer.parseInt(npctRange);
			if (this.npctRange < 0) {
				this.npctRange = 0;
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
	
	public int getMaxPub() {
		return maxPub;
	}
	
	
	

	public int setMaxPub (int maxPub) {
		this.maxPub = maxPub;
		if (this.maxPub<0) {
			this.maxPub = 0;
			return 1;
		}
		return 0;
	}
	
	public int setMaxPub (String maxPub) {
		try {
			return setMaxPub (Integer.parseInt(maxPub));
		} catch (NumberFormatException e) {
			return 1;
		}
	}
	
	public boolean getIncludePubsWithoutCRs() {
		// return includePubsWithoutCRs;
		// TODO: we currently always include Pubs without CR
		return true;
	}

	public int setIncludePubsWithoutCRs (boolean includePubsWithoutCRs) {
		this.includePubsWithoutCRs = includePubsWithoutCRs;
		return 0;
	}

	public boolean getImportRandom() {
		return importRandom;
	}

	public int setImportRandom (boolean importRandom) {
		this.importRandom = importRandom;
		return 0;
	}
	

	public boolean getImportPubsWithoutYear () {
		return this.importPubsWithoutYear;
	}
	
	public int setImportPubsWithoutYear (boolean importPubsWithoutYear) {
		this.importPubsWithoutYear = importPubsWithoutYear;
		return 0;
	}

	public boolean getImportCRsWithoutYear () {
		return this.importCRsWithoutYear;
	}

	public int setImportCRsWithoutYear (boolean importCRsWithoutYear) {
		this.importCRsWithoutYear = importCRsWithoutYear;
		return 0;
	}
	
	
	public int getChartEngine() {
		return this.chartEngine;
	}

	public void setChartEngine (int chartEngine) {
		this.chartEngine = chartEngine;
	}

	public int setSampling (String label) {
		this.sampling = Sampling.NONE;
		for (Sampling s: Sampling.values()) {
			if (s.label.equals(label)) {
				this.sampling=s;
				return 0;
			}
		}
		return 1;
	}
	
	public int setSampling (Sampling s) {
		this.sampling = s;
		return 0;
	}
	
	public Sampling getSampling () {
		return this.sampling;
	}

	public int setDownloadCrossrefData (DownloadCrossrefData d) {
		this.downloadCrossrefData = d;
		return 0;
	}
	
	public DownloadCrossrefData getDownloadCrossrefData () {
		return this.downloadCrossrefData;
	}
	
	
}

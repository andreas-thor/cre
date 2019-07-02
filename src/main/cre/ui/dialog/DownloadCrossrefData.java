package main.cre.ui.dialog;

import java.util.Arrays;

public class DownloadCrossrefData {

	private String title = null;
	private String ISSN = null;
	private int[] range = { -1, -1 };
	private String[] DOI = null;

	public DownloadCrossrefData(String title, String ISSN, String[] range, String DOI) {
		super();

		this.title = title.trim();
		this.ISSN = ISSN.trim();
		this.range = Arrays.stream(range).mapToInt(s -> {
			try {
				return Integer.valueOf(s).intValue();
			} catch (Exception e) {
				return -1;
			}
		}).toArray();
		this.DOI = Arrays.stream(DOI.split("[\\s]")).map(s -> s.trim()).filter(s -> s.length() > 0).toArray(String[]::new);


	}

	public String getTitle() {
		return title;
	}
	
	public String getISSN() {
		return ISSN;
	}

	public int[] getRange() {
		return range;
	}

	public String[] getDOI() {
		return DOI;
	}
}
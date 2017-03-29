package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import javafx.stage.FileChooser.ExtensionFilter;

public enum ImportFormat {
	
	CRE_JSON("Open CRE File", false, 
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.cre" })),
			CRE_json::load), 
	
	WOS_TXT("Import Web of Science Files", true, 
			new ExtensionFilter("Web of Science", Arrays.asList(new String[] { "*.txt" })),
			WoS_txt::load), 
	
	SCOPUS_CSV("Import Scopus Files", true,	
			new ExtensionFilter("Scopus", Arrays.asList(new String[] { "*.csv" })),
			Scopus_csv::load);

	public final String label;
	public boolean multiple;
	public ExtensionFilter filter;
	private Import load;		// save function as Consumer


	ImportFormat(String label, boolean multiple, ExtensionFilter filter, Import load) {
		this.label = label;
		this.multiple = multiple;
		this.filter = filter;
		this.load = load;
	}
	
	public void load (List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {
		this.load.apply(files, CRTable.get(), UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange));
	}
};
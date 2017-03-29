package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import cre.test.data.CRTable;
import javafx.stage.FileChooser.ExtensionFilter;

public enum ExportFormat {

	CRE_JSON("Save CRE File",
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.cre" })),
			CRE_json::save),

	WOS_TXT("Export Web of Science File",
			new ExtensionFilter("Web of Science", Arrays.asList(new String[] { "*.txt" })),
			WoS_txt::save),

	SCOPUS_CSV("Export Scopus File", 
			new ExtensionFilter("Scopus", Arrays.asList(new String[] { "*.csv" })),
			Scopus_csv::save),

	CRE_CSV_CR("Export Cited References",
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.csv" })),
			CRE_csv::saveCR),

	CRE_CSV_PUB("Export Citing Publications",
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.csv" })),
			CRE_csv::savePub),

	CRE_CSV_CR_PUB("Export Cited References + Citing Publications",
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.csv" })),
			CRE_csv::saveCRPub),

	CRE_CSV_GRAPH("Export Graph",
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.csv" })),
			CRE_csv::saveGraph);

	
	public final String label;	
	public ExtensionFilter filter;
	private Export save;		// save function as BiConsumer

	ExportFormat(String label, ExtensionFilter filter, Export save) {
		this.label = label;
		this.filter = filter;
		this.save = save;
	}
	
	public void save (File file) throws IOException {
		this.save.apply(file, CRTable.get());
	}
}
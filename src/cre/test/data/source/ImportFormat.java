package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.ui.StatusBar;
import javafx.stage.FileChooser.ExtensionFilter;

public enum ImportFormat {

	
	CRE_JSON("Cited References Explorer", false, "cre" , null, CRE_json::save),

	WOS_TXT("Web of Science", true, "txt", new WoS_Reader(), WoS_Reader::save),

	SCOPUS_CSV("Scopus", true,	"csv", new Scopus_Reader(), Scopus_Reader::save),

	CRE_CSV_CR("Cited References", false, "csv", null, CRE_csv::saveCR), 
	
	CRE_CSV_PUB("Citing Publications", false, "csv", null, CRE_csv::savePub), 

	CRE_CSV_CR_PUB("Cited References + Citing Publications", false, "csv", null, CRE_csv::saveCRPub), 

	CRE_CSV_GRAPH("CRE Graph", false, "csv", null, CRE_csv::saveGraph);
	
	
	public final String label;
	public boolean importMultiple;
	public String fileExtension;
	public ExtensionFilter fileExtensionFilter;
	public ImportReader importReader;
	public Export exportSave;

	
	ImportFormat(String label, boolean importMultiple, String fileExtension, ImportReader importReader, Export exportSave) {
		this.label = label;
		this.importMultiple = importMultiple;
		this.fileExtension = fileExtension;
		this.fileExtensionFilter = new ExtensionFilter(label, Arrays.asList(new String[] { "*." + fileExtension}));
		this.importReader = importReader;
		this.exportSave = exportSave;
	}


	public void save (File file) throws IOException {
		
		// add extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith("." + this.fileExtension)) file_name += "." + this.fileExtension;
		
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s ...", file.getName(), this.label));
		this.exportSave.apply(file_name);
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s done", file.getName(), this.label));

	}
	

	public void load(List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {

		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();

		CRTable crTab = CRTable.get(); 
		crTab.init();

		int idx = 0;
		double ratioCR = 1d;
		for (File file: files) {
			
			StatusBar.get().initProgressbar(file.length(), String.format("Loading %4$s file %1$d of %2$d (%3$s) ...", (++idx), files.size(), file.getName(), this.label));

			if (this==ImportFormat.CRE_JSON) {	// load internal CRE format
				CRE_json.load(file);
			} else {	// import external data format
			
				this.importReader.init(file, UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange), ratioCR);
				StreamSupport.stream(this.importReader.getIterable().spliterator(), false)
					.filter(pub -> pub != null)
					.filter(pub -> (ratioCR==1d) || (pub.getSizeCR()>0))	// do not include pubs w/o CRs when random select	
					.forEach(pub -> {
						StatusBar.get().incProgressbar(pub.length);
						crTab.addNewPub(pub);
					});
				this.importReader.close();
			}
				
		}

		
		// Check for abort by user
		if (crTab.isAborted()) {
			crTab.init();
			StatusBar.get().setValue(String.format("Loading %1$s file%2$s aborted (due to user request)", this.label, files.size()>1 ? "s" : ""));
			throw new AbortedException();
		}
		
		System.out.println("CRTable.get().getPub().count()=" + CRTable.get().getPub(true).count());
		System.out.println("CRTable.get().getPub(true).flatMap(pub -> pub.getCR()).count()=" + CRTable.get().getPub(true).flatMap(pub -> pub.getCR()).count());
		System.out.println("CRTable.get().getCR().count()=" + CRTable.get().getCR().count());
		

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();

		System.out.println("Load time is " + ((ts2-ts1)/1000d) + " seconds");
		System.out.println("Load Memory usage " + ((ms2-ms1)/1024d/1024d) + " MBytes");
		
		crTab.updateData();

		long ts3 = System.currentTimeMillis();
		long ms3 = Runtime.getRuntime().totalMemory();

		System.out.println("Update time is " + ((ts3-ts2)/1000d) + " seconds");
		System.out.println("Update Memory usage " + ((ms3-ms2)/1024d/1024d) + " MBytes");

		
		StatusBar.get().setValue(String.format("Loading %1$s file%2$s done", this.label, files.size()>1 ? "s" : ""));
	}

};
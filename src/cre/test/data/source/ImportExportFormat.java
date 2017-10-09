package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRStatsInfo;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.ui.StatusBar;
import javafx.stage.FileChooser.ExtensionFilter;

public enum ImportExportFormat {

	
	CRE_JSON("Cited References Explorer", false, "cre" , null, CRE_json::save),

	WOS_TXT("Web of Science", true, "txt", new WoS_txt(), WoS_txt::save),

	SCOPUS_CSV("Scopus", true,	"csv", new Scopus_csv(), Scopus_csv::save),

	CRE_CSV_CR("Cited References", false, "csv", null, CRE_csv::saveCR), 
	
	CRE_CSV_PUB("Citing Publications", false, "csv", null, CRE_csv::savePub), 

	CRE_CSV_CR_PUB("Cited References + Citing Publications", false, "csv", null, CRE_csv::saveCRPub), 

	CRE_CSV_GRAPH("CRE Graph", false, "csv", null, CRE_csv::saveGraph);
	
	

	public interface Export {
	   void save(String file_name) throws IOException, RuntimeException;
	}
	
	public final String label;
	public boolean importMultiple;
	public String fileExtension;
	public ExtensionFilter fileExtensionFilter;
	public ImportReader importReader;
	public Export exportSave;

	
	ImportExportFormat(String label, boolean importMultiple, String fileExtension, ImportReader importReader, Export exportSave) {
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
		this.exportSave.save(file_name);
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s done", file.getName(), this.label));

	}
	
	
	
	public CRStatsInfo analyze(List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {
		
		CRTable crTab = CRTable.get(); 
		crTab.init();
		
		CRStatsInfo crStatsInfo = CRStatsInfo.get();
		crStatsInfo.init();

		int idx = 0;
		for (File file: files) {
			
			StatusBar.get().initProgressbar(file.length(), String.format("Analyzing %4$s file %1$d of %2$d (%3$s) ...", (++idx), files.size(), file.getName(), this.label));

			this.importReader.init(file);
			StreamSupport.stream(this.importReader.getIterable().spliterator(), false)
				.filter(pub -> pub != null)
				.forEach(pub -> {
					if (crTab.isAborted()) this.importReader.stop();
					StatusBar.get().incProgressbar(pub.length);
					crStatsInfo.updateStats(pub);
				});
			this.importReader.close();
				
		}
		
		System.out.println(crStatsInfo);
		return crStatsInfo;
	}

	public void load(List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {

		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();

		CRTable crTab = CRTable.get(); 
		crTab.init();


		final int[] rpyRange = UserSettings.get().getRange(RangeType.ImportRPYRange);
		boolean importCRsWithoutYear = UserSettings.get().getImportCRsWithoutYear();
		final int[] pyRange = UserSettings.get().getRange(RangeType.ImportPYRange);
		boolean importPubsWithoutYear = UserSettings.get().getImportPubsWithoutYear();
		
		final long noMaxCRs = UserSettings.get().getMaxCR();
		AtomicLong noAvailableCRs = new AtomicLong (CRStatsInfo.get().getNumberOfCRs (rpyRange, importCRsWithoutYear, pyRange, importPubsWithoutYear));
		AtomicLong noToImportCRs = new AtomicLong(noMaxCRs);
				
		Random rand = new Random();
		int idx = 0;
		for (File file: files) {
			
			StatusBar.get().initProgressbar(file.length(), String.format("Loading %4$s file %1$d of %2$d (%3$s) ...", (++idx), files.size(), file.getName(), this.label));

			if (this==ImportExportFormat.CRE_JSON) {	// load internal CRE format
				CRE_json.load(file);
			} else {	// import external data format
			
				this.importReader.init(file);
				StreamSupport.stream(this.importReader.getIterable().spliterator(), false)
					.filter(pub -> pub != null)
					.forEach(pub -> {
						
						if (crTab.isAborted()) {
							this.importReader.stop();
						}

						StatusBar.get().incProgressbar(pub.length);
						
						
						boolean addPub = true;
						if (pub.getPY()==null) {
							addPub = importPubsWithoutYear; 
						} else {
							int py = pub.getPY().intValue();
							if ((pyRange[0] != CRStatsInfo.NONE) && (pyRange[0]>py)) addPub = false;
							if ((pyRange[1] != CRStatsInfo.NONE) && (pyRange[1]<py)) addPub = false;
						}
						
						if (addPub) {
							pub.removeCRByYear(rpyRange, importCRsWithoutYear, true);
							
							if ((pub.getSizeCR()>0) && (noMaxCRs>0)) {		// select CRs at random
								pub.removeCRByRandom(rand, noToImportCRs, noAvailableCRs);
							}
					
							if (pub.getSizeCR()>0) {		
								crTab.addPub(pub, true);
								
//								if ((UserSettings.get().getMaxCR()>0) && (numberOfCRs.addAndGet(pub.getSizeCR()) >= UserSettings.get().getMaxCR())) {
//									this.importReader.stop(); 
//								}
							}
						}
						
						
					});
				this.importReader.close();
			}
				
		}

		System.out.println("noAvailableCRs=" + noAvailableCRs.get());
		System.out.println("noToImportCRs=" + noToImportCRs.get());
		
		
		// Check for abort by user
		if (crTab.isAborted()) {
			crTab.init();
			StatusBar.get().setValue(String.format("Loading %1$s file%2$s aborted (due to user request)", this.label, files.size()>1 ? "s" : ""));
			throw new AbortedException();
		}
		
		System.out.println("CRTable.get().getPub().count(true)=" + CRTable.get().getPub(true).count());
		System.out.println("CRTable.get().getPub().count()=" + CRTable.get().getPub().count());
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
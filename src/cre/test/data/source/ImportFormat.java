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

	
	CRE_JSON("Open CRE File", false, 
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.cre" })),CRE_json::load,null),

	WOS_TXT("Import Web of Science Files", true, 
			new ExtensionFilter("Web of Science", Arrays.asList(new String[] { "*.txt" })),null, new WoS_Reader()),

	SCOPUS_CSV("Import Scopus Files", true,	
			new ExtensionFilter("Scopus", Arrays.asList(new String[] { "*.csv" })),null, new Scopus_Reader());

	public final String label;
	public boolean multiple;
	public ExtensionFilter filter;
	private Load load; // save function as Consumer
	public ImportReader reader;

	interface Load {
		void apply(List<File> files, CRTable crTab, int maxCR, int maxPub, int[] yearRange, boolean random) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException;
	}
	
	ImportFormat(String label, boolean multiple, ExtensionFilter filter, Load load, ImportReader reader) {
		this.label = label;
		this.multiple = multiple;
		this.filter = filter;
		this.load = load;
		this.reader = reader;
	}

	public void load2(List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {
		this.load.apply(files, CRTable.get(), UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange), UserSettings.get().getImportRandom());
	}

	public void load(List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {

		// this.load.apply(files, , UserSettings.get().getMaxCR(),
		// UserSettings.get().getMaxPub(),
		// UserSettings.get().getRange(RangeType.ImportYearRange));

		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();

		CRTable crTab = CRTable.get(); 
		crTab.init();

		int idx = 0;
		double ratioCR = 1d;
		for (File file: files) {
			
			StatusBar.get().initProgressbar(file.length(), String.format("Loading file %1$d of %2$d (%3$s) ...", (++idx), files.size(), file.getName()));

			this.reader.init(file, UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange), ratioCR);
			StreamSupport.stream(this.reader.getIterable().spliterator(), false)
				.filter(pub -> pub != null)
				.filter(pub -> (ratioCR==1d) || (pub.getSizeCR()>0))	// do not include pubs w/o CRs when random select	
				.forEach(pub -> {
					StatusBar.get().incProgressbar(pub.length);
					crTab.addNewPub(pub);
				});
			this.reader.close();
				
		}

		
		System.out.println("CRTable.get().getPub().count()=" + CRTable.get().getPub(true).count());
		System.out.println("CRTable.get().getPub(true).flatMap(pub -> pub.getCR()).count()=" + CRTable.get().getPub(true).flatMap(pub -> pub.getCR()).count());
		System.out.println("CRTable.get().getCR().count()=" + CRTable.get().getCR().count());
		

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();

		System.out.println("Load time is " + ((ts2-ts1)/1000d) + " seconds");
		System.out.println("Load Memory usage " + ((ms2-ms1)/1024d/1024d) + " MBytes");
		
		CRTable.get().updateData();

		long ts3 = System.currentTimeMillis();
		long ms3 = Runtime.getRuntime().totalMemory();

		System.out.println("Update time is " + ((ts3-ts2)/1000d) + " seconds");
		System.out.println("Update Memory usage " + ((ms3-ms2)/1024d/1024d) + " MBytes");

		
		StatusBar.get().setValue("Loading WoS files done");
	}

};
package cre.test.data.source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.data.source.WoS_txt.WoS_Iterator;
import cre.test.data.type.PubType;
import cre.test.ui.StatusBar;
import javafx.stage.FileChooser.ExtensionFilter;

public enum ImportFormat {
	
	CRE_JSON("Open CRE File", false, 
			new ExtensionFilter("Cited References Explorer", Arrays.asList(new String[] { "*.cre" })),
			CRE_json::load, null), 
	
	WOS_TXT("Import Web of Science Files", true, 
			new ExtensionFilter("Web of Science", Arrays.asList(new String[] { "*.txt" })),
			WoS_txt::load, new WoS_Reader()), 
	
	SCOPUS_CSV("Import Scopus Files", true,	
			new ExtensionFilter("Scopus", Arrays.asList(new String[] { "*.csv" })),
			Scopus_csv::load, null);

	public final String label;
	public boolean multiple;
	public ExtensionFilter filter;
	private Import load;		// save function as Consumer
	public ImportReader reader;


	ImportFormat(String label, boolean multiple, ExtensionFilter filter, Import load, ImportReader reader) {
		this.label = label;
		this.multiple = multiple;
		this.filter = filter;
		this.load = load;
		this.reader = reader;
	}
	
	public void load (List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {
		this.load.apply(files, CRTable.get(), UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange));
	}
	
	public void load2 (List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {
		
//		this.load.apply(files, , UserSettings.get().getMaxCR(), UserSettings.get().getMaxPub(), UserSettings.get().getRange(RangeType.ImportYearRange));
		
		
		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();
		
		CRTable.get().init();
		
		AtomicLong countCR = new AtomicLong(0);
		AtomicLong countPub = new AtomicLong(0);
		
		this.reader.init(files);
		
//		CRTable.get().addPubsStream(
		long a = StreamSupport.stream(this.reader.getIterable().spliterator(), true).map(pub -> {
			
//			System.out.println("Ho");
			/* if user abort or maximum number of CRs reached --> do no process anymore */
			if (CRTable.get().isAborted()) return null;
			if ((UserSettings.get().getMaxCR()>0) && (countCR.get()>=UserSettings.get().getMaxCR())) return null;
			if ((UserSettings.get().getMaxPub()>0) && (countPub.get()>=UserSettings.get().getMaxPub())) return null;
			
			StatusBar.get().incProgressbar(pub.length);
//			System.out.println("#CR =" + countCR.addAndGet(pub.getSizeCR()));
			
			if (countPub.incrementAndGet() % 1000 == 0) {
				System.out.println("#Pub=" + countPub.get());
			}
			
			
			// Check for abort by user
			if (CRTable.get().isAborted()) {
				CRTable.get().init();
				StatusBar.get().setValue ("Loading WoS files aborted (due to user request)");
//				throw new AbortedException();
			}

			// Check for maximal number of CRs
			if (((UserSettings.get().getMaxCR()>0) && (countCR.get()>=UserSettings.get().getMaxCR())) || ((UserSettings.get().getMaxPub()>0) && (countPub.get()>=UserSettings.get().getMaxPub()))) {
				CRTable.get().updateData();
				StatusBar.get().setValue("Loading WoS files aborted (due to maximal number of CRs / Pubs)");
//				throw new FileTooLargeException (countCR.get(), countPub.get());
			}
			
			CRTable.get().addPub (pub);
			
			return null;
			}).count();
				
		System.out.println("a="+a);
		
	

		
		CRTable.get().updateData();

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();

		System.out.println("Load time is " + ((ts2-ts1)/1000d) + " seconds");
		System.out.println("Memory usage " + ((ms2-ms1)/1024d/1024d) + " MBytes");

		StatusBar.get().setValue("Loading WoS files done");	
	}
	
	
};
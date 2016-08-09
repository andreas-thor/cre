package cre.data.source;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;
import cre.Exceptions.AbortedException;
import cre.Exceptions.FileTooLargeException;
import cre.Exceptions.UnsupportedFileFormatException;
import cre.data.CRTable;
import cre.data.PubType;
import cre.ui.StatusBar;

public class Scopus_csv  {

	
	public static void loadBulk (CRTable crTab, StatusBar stat, String source, File[] files, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {
		
		long ts1 = System.currentTimeMillis();
		
		crTab.abort = false;	// can be changed by "wait dialog"
		
		Date startDate = new Date();
		stat.setValue(String.format("%1$s: Loading Scopus files ...", startDate), 0, "");
		
		crTab.init();
		
		AtomicLong countCR = new AtomicLong(0);
		
		for (int idx=0; idx<files.length; idx++) {


			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(files[idx]), "UTF-8"));
			CSVReader csv = new CSVReader(br);
			List<String[]> content = csv.readAll(); 
			csv.close();
			
			String[] attributes = Arrays.stream(content.get(0)).map(it ->  it.trim()).toArray(size -> new String[size]);
			content.remove(0);
			
			/*
				http://stackoverflow.com/questions/21891578/removing-bom-characters-using-java
				Java does not handle BOM properly. In fact Java handles a BOM like every other char.
				Found this:	http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html
			*/
			if (attributes[0].startsWith("\uFEFF")) attributes[0] = attributes[0].substring(1);
			

			
			int stepSize = 5;
			int modulo = content.size()*stepSize/100;
			AtomicLong countPub = new AtomicLong(0);

			int fileNr = idx+1;
			crTab.pubData.addAll(content.parallelStream().map ( it -> {
			
				/* if user abort or maximum number of CRs reached --> do no process anymore */
				if (crTab.abort) return null;
				if ((maxCR>0) && (countCR.get()>=maxCR)) return null;
				
				PubType pub = new PubType().parseScopus(it, attributes, yearRange);
				countPub.incrementAndGet();
				countCR.addAndGet(pub.crList.size());
				
				// update status bar
				if ((countPub.get()%modulo) == 0) {
					stat.setValue (String.format("%1$s: Loading Scopus file %2$d of %3$d", startDate, fileNr, files.length), (int)countPub.get()*stepSize/modulo);
				}
				
				return pub;
			}).filter ( it -> it != null).collect (Collectors.toList()));	// remove null values (abort)
			
			// Check for abort by user
			if (crTab.abort) {
				crTab.init();
				crTab.updateData(false);
				stat.setValue(String.format("%1$s: Loading Scopus files aborted (due to user request)", startDate), 0);
				crTab.abort = false;
				throw new AbortedException();
			}

			// Check for maximal number of CRs
			if ((maxCR>0) && (countCR.get()>=maxCR)) {
				stat.setValue(String.format("$1%s: Loading Scopus files aborted (due to maximal number of CRs)", startDate), 0);
				crTab.createCRList();
				crTab.updateData(false);
				throw new FileTooLargeException ((int) countCR.get());
			}

			
		}


		
		
		crTab.createCRList();
		
		

		long ts2 = System.currentTimeMillis();
		System.out.println("Load time is " + ((ts2-ts1)/1000d) + " seconds");

		crTab.updateData(false);
		stat.setValue(String.format("$1%s: Loading Scopus files done", startDate), 0, crTab.getInfoString());
	}
	
	




	public static void save (File file, CRTable crTab, StatusBar stat) {
		
		// TODO: J8 to implement

	}
	
	
	
	
} 


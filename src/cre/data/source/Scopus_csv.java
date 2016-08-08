package cre.data.source;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import cre.Exceptions.AbortedException;
import cre.Exceptions.FileTooLargeException;
import cre.Exceptions.UnsupportedFileFormatException;
import cre.data.*;
import cre.ui.StatusBar;
import groovy.transform.CompileStatic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Scopus_csv  {

	

	
	
	public static void loadBulk (CRTable crTab, StatusBar stat, String source, File[] files, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {
		
		crTab.abort = false;	// can be changed by "wait dialog"
		
		Date startDate = new Date();
		stat.setValue(String.format("%1$s: Loading files ...", startDate), 0, "");
		
		// TODO: initialize crDup  when "no init" mode  
		HashMap<Character,  HashMap<String,Integer>> crDup = new HashMap<Character,  HashMap<String,Integer>>(); // first character -> (crString -> id )
		int indexCount = 0;
		crTab.init();
		
		int stepCount = 0;
		int stepSize = 5;
		
		int idx = 0;
		for (File f2: files) {

			int fileSizeStep = (int) (f2.length()*stepSize/100);
			long fileSizeRead = 0;

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f2), "UTF-8"));
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
			
		
			
			crTab.pubData.addAll(content.stream().map ( it -> {
			
				// Check for abort by user
				if (crTab.abort) {
					crTab.init();
					crTab.updateData(false);
					stat.setValue(String.format("%1$s:Loading files aborted", startDate), 0);
					crTab.abort = false;
//					throw new AbortedException();
				}
				
				PubType pub = new PubType().parseScopus(it, attributes, yearRange);
				 
				// update status bar
//				fileSizeRead += pub.length;
				if (stepCount*fileSizeStep < fileSizeRead) {
					stat.setValue (String.format("%1$s: Loading Scopus file %2$d of %3$d", startDate, idx+1, files.length), stepCount*stepSize);
//					stepCount++;
				}
				
				return pub;
			}).collect (Collectors.toList()));
		}
				
			
		for (PubType pub: crTab.pubData) {
				
			int crPos = 0;
			for (CRType cr: pub.crList) {
					
				// if CR already in database: increase N_CR by 1; else add new record
				crDup.putIfAbsent(cr.CR.charAt(0), new HashMap<String,Integer>());
				Integer id = crDup.get(cr.CR.charAt(0)).get(cr.CR);
				if (id != null) {
					crTab.crData.get(id).N_CR++;
					pub.crList.set(crPos, crTab.crData.get(id));
				} else {
					crDup.get(cr.CR.charAt(0)).put (cr.CR, indexCount);
					
					if ((maxCR>0) && (indexCount==maxCR)) {
						crTab.updateData(false);
						stat.setValue(String.format("$1%s: Loading Scopus files aborted", startDate), 0);
//						throw new FileTooLargeException (indexCount);
					}
					
					// todo: add new CR as separate function (make clusterId2Objects private again)
					
					cr.ID = indexCount+1;
					cr.CID2 = new CRCluster (indexCount+1, 1);
					cr.CID_S = 1;
					crTab.crData.add (cr);
					
					HashSet<Integer> tmp = new HashSet<Integer>();
					tmp.add(indexCount+1);
					crTab.crMatch.clusterId2Objects.put(cr.CID2, tmp);
//						crTab.crMatch.clusterId2Objects[cr.CID2] = [indexCount+1];
					indexCount++;
				}
				crPos++;
					
			} 
				
//				this.noOfPubs++
//			this.noOfPubs += parser.noOfPubs

		};
			

		
//		long timeEnd = System.currentTimeMillis()
//		long overall = timeEnd-timeStart
//		println "Overall: ${overall}"
//		for (int i=0; i<timeDiffs.length; i++) {
//			long percent = (long) (timeDiffs[i]*100 / overall)
//			println "${i} : ${percent}%"
//		}
//
//		println indexCount
		
		
		crTab.updateData(false);
		stat.setValue(String.format("$1%s: Loading files done", startDate), 0, crTab.getInfoString());
	}
	
	
	
	public PubType getNextPub() {
		// TODO: J8 to implement
		return null;
	}




	public static void save (File file, CRTable crTab, StatusBar stat) {
		
		// TODO: J8 to implement

	}
	
	
	
	
} 


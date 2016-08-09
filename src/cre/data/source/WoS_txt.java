package cre.data.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import cre.Exceptions.AbortedException;
import cre.Exceptions.FileTooLargeException;
import cre.Exceptions.UnsupportedFileFormatException;
import cre.data.CRTable;
import cre.data.CRType;
import cre.data.PubType;
import cre.ui.StatusBar;

public class WoS_txt {

	static Pattern sWoS_matchAuthor = Pattern.compile("([^ ]*)( )?(.*)?");
	
	static Pattern[] sWoS_matchAuthorVon = new Pattern[] { 
			Pattern.compile("(von )()([^ ]*)( )([^ ]*)(.*)"), 
			Pattern.compile("(van )(der )?([^ ]*)( )([^ ]*)(.*)"), 
			Pattern.compile("(van't )()([^ ]*)( )([^ ]*)(.*)") 
		};
	
	static Pattern sWoS_matchPageVolumes = Pattern.compile ("([PV])([0-9]+)");
	static Pattern sWoS_matchDOI = Pattern.compile(".*DOI (10\\.[^/]+/ *[^ ,]+).*");
	
	
	public static class WoS_Iterator implements Iterator<List<String>> {

		BufferedReader br;
		List<String> entry = new ArrayList<>();
		
		public WoS_Iterator(File f) throws IOException {
			// TODO Auto-generated constructor stub
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			computeNext();
		}
		
		
		public void close() throws IOException {
			br.close();
		}
		
		private void computeNext () throws IOException {
			
			entry = new ArrayList<>();
			
			String line;
			while ((line = br.readLine()) != null) {
				if ((line.startsWith("ER")) || (line.startsWith("EF"))) {
					return;
				}
				entry.add(line);
			}
			
		}
		
		
		
		@Override
		public boolean hasNext() {
			return entry.size()>0;
		}

		@Override
		public List<String> next() {
			List<String> result = entry;
			
			try {
				computeNext();
			} catch (IOException e) {
				entry = new ArrayList<>();
			}
			return result;
		}
		
		public Iterable<List<String>> getIterable () { 
			return () -> this;
		}
		
		
	}
	
	
	
	public static void loadBulk (CRTable crTab, StatusBar stat, String source, File[] files, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {

		
		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();
		
		crTab.abort = false;	// can be changed by "wait dialog"
		
		Date startDate = new Date();
		stat.setValue(String.format("%1$s: Loading WoS files ...", startDate), 0, "");
		
		crTab.init();
		
		AtomicLong countCR = new AtomicLong(0);
		
		for (int idx=0; idx<files.length; idx++) {
			
			int fileNr = idx+1;
			WoS_Iterator wosIt = new WoS_Iterator(files[idx]);
			
			
			long stepSize = files[idx].length()/20;
			AtomicLong countPub = new AtomicLong(0);
			
			AtomicLong countSteps = new AtomicLong(0);
			AtomicLong countSize = new AtomicLong(0);
			

			
			crTab.pubData.addAll(StreamSupport.stream(wosIt.getIterable().spliterator(), true).map ( it -> {
			
				/* if user abort or maximum number of CRs reached --> do no process anymore */
				if (crTab.abort) return null;
				if ((maxCR>0) && (countCR.get()>=maxCR)) return null;

				String currentTag = "";
				String tagBlock = "";
				String value = "";
					
				PubType pub = new PubType();
				pub.FS = "WoS";
				pub.length = 0;
				List<String> C1 = new ArrayList<String>();
				
				for (String l: it) {
					pub.length += 1 + l.length();
					if (l.length()<2) continue;
					currentTag = l.substring(0, 2);
					if (currentTag.equals("ER")) continue;
					if (currentTag.equals("EF")) continue;
					tagBlock = currentTag.equals("  ") ? tagBlock : new String(currentTag);
					value = l.substring(3);
					
					
					switch (tagBlock) {
					
					case "PT": pub.PT = value; break;
					
					/* Concatenated Strings */
					case "TI": pub.TI = (pub.TI==null) ? value : pub.TI+" "+value; break;
					case "SO": pub.SO = (pub.SO==null) ? value : pub.SO+" "+value; break;
					case "VL": pub.VL = (pub.VL==null) ? value : pub.VL+" "+value; break;
					case "IS": pub.IS = (pub.IS==null) ? value : pub.IS+" "+value; break;
					case "AR": pub.AR = (pub.AR==null) ? value : pub.AR+" "+value; break;
					case "DI": pub.DI = (pub.DI==null) ? value : pub.DI+" "+value; break;
					case "LI": pub.LI = (pub.LI==null) ? value : pub.LI+" "+value; break;
					case "AB": pub.AB = (pub.AB==null) ? value : pub.AB+" "+value; break;
					case "DE": pub.DE = (pub.DE==null) ? value : pub.DE+" "+value; break;
					case "DT": pub.DT = (pub.DT==null) ? value : pub.DT+" "+value; break;
					case "UT": pub.UT = (pub.UT==null) ? value : pub.UT+" "+value; break;
					
					/* Integer values */
					case "PY": pub.PY = Integer.valueOf(value); break;
					case "BP": pub.BP = Integer.valueOf(value); break;
					case "EP": pub.EP = Integer.valueOf(value); break;
					case "PG": pub.PG = Integer.valueOf(value); break;
					case "TC": pub.TC = Integer.valueOf(value); break;
					
					/* Parse Cited References */
					case "CR": CRType cr = /*new CRType().*/parseWoS(value, yearRange); if (cr!=null) pub.crList.add(cr); break;
					
					/* Authors */
					case "AU": if (pub.AU==null) pub.AU=new ArrayList<String>(); pub.AU.add(value); break;
					case "AF": if (pub.AF==null) pub.AF=new ArrayList<String>(); pub.AF.add(value); break;
					case "EM": pub.EM = new ArrayList<String>(Arrays.asList(value.split("; "))); break;
					
					/* store C1 values in a separate list for further processing */
					case "C1": C1.add(value); break;
					}
				}
				
				it = null;
				if (pub.PT==null) return null;
				
				pub.C1 = new ArrayList<String[]>();
				pub.AA = new ArrayList<String>();
				for (String corr: C1) {
					int pos = corr.indexOf(']');
					if (pos>0) {
						String names = corr.substring(1, pos);
						String affiliation = corr.substring (pos+2);
						for (String name: names.split("; ")) {
							pub.C1.add (new String[] { name, affiliation });
							pub.AA.add (affiliation);
						}
					}
				}
				
				countPub.incrementAndGet();
				countSize.addAndGet(pub.length);
				
				countCR.addAndGet(pub.crList.size());
				
				// update status bar
				if (countSteps.get()*stepSize < countSize.get() ) {
					stat.setValue (String.format("%1$s: Loading WoS file %2$d of %3$d", startDate, fileNr, files.length), (int)(5*countSteps.get()));
					countSteps.incrementAndGet();
				}
				
				return pub;
			}).filter ( it -> it != null).collect (Collectors.toList()));	// remove null values (abort)
			
			wosIt.close();
			
			// Check for abort by user
			if (crTab.abort) {
				crTab.init();
				crTab.updateData(false);
				stat.setValue(String.format("%1$s: Loading WoS files aborted (due to user request)", startDate), 0);
				crTab.abort = false;
				throw new AbortedException();
			}

			// Check for maximal number of CRs
			if ((maxCR>0) && (countCR.get()>=maxCR)) {
				stat.setValue(String.format("$1%s: Loading WoS files aborted (due to maximal number of CRs)", startDate), 0);
				crTab.createCRList();
				crTab.updateData(false);
				throw new FileTooLargeException ((int) countCR.get());
			}

			
		}


		
		crTab.createCRList();
		
		

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();

		System.out.println("Load time is " + ((ts2-ts1)/1000d) + " seconds");
		System.out.println("Memory usage " + ((ms2-ms1)/1024d/1024d) + " MBytes");

		crTab.updateData(false);
		stat.setValue(String.format("$1%s: Loading WoS files done", startDate), 0, crTab.getInfoString());		
		
	}
	
	
	public static CRType parseWoS (String line, int[] yearRange) {

		CRType cr = new CRType();
		cr.CR = line; // [3..-1] // .toUpperCase()
		cr.type = CRType.TYPE_WOS;
		String[] crsplit = cr.CR.split (",", 3);
		
		String yearS = crsplit.length > 1 ? crsplit[1].trim() : "";
		try {
			
			int year = Integer.parseInt(yearS);
			if (((year < yearRange[0]) && (yearRange[0]!=0)) || ((year > yearRange[1]) && (yearRange[1]!=0))) return null;
			cr.RPY = year;
		} catch (NumberFormatException e) {
			return null;
		}

		
		cr.AU = crsplit[0].trim();
		
		// process "difficult" last names starting with "von" etc.
		if ((cr.AU.length()>0) && (cr.AU.charAt(0)=='v')) {
			
			for (Pattern p: sWoS_matchAuthorVon) {
				Matcher matchVon = p.matcher(cr.AU);
				if (matchVon.matches()) {
					cr.AU_L = (matchVon.group(1) + (matchVon.group(2)==null?"":matchVon.group(2)) + matchVon.group( ((matchVon.group(3).equals("")) ? 5 : 3) )).replaceAll(" ","").replaceAll("\\-","");
					String tmp = ((((matchVon.group(3).equals("")) ? "" : matchVon.group(5)) + matchVon.group(6)).trim());
					cr.AU_F = tmp.equals("") ? "" : tmp.substring(0,1);	// cast as List to avoid Index out of Bounds exception
					break;
				}
			}
		}
		
		// process all other authors
		if (cr.AU_L == null) {
			Matcher WoS_matchAuthor = sWoS_matchAuthor.matcher(cr.AU);
			if (WoS_matchAuthor.matches()) {
				cr.AU_L = WoS_matchAuthor.group(1).replaceAll("\\-","");
				cr.AU_F = (WoS_matchAuthor.group(3) == null ? " " : WoS_matchAuthor.group(3) + " ").substring(0, 1);
			}
		}
			
		// process all journals
		cr.J = crsplit.length > 2 ? crsplit[2].trim() : "";
		cr.J_N = cr.J.split(",")[0];
		String[] split = cr.J_N.split(" ");
		if (split.length==1) {
			cr.J_S = split[0]; 
		} else {
			cr.J_S = "";
			for (String s: split) {
				if (s.length()>0) cr.J_S += s.charAt(0);
			}
		}
		
		
		// find Volume, Pages and DOI
		for (String it: cr.J.split(",")) {
			Matcher WoS_matchPageVolumes = sWoS_matchPageVolumes.matcher(it.trim());
			if (WoS_matchPageVolumes.matches()) {
				if (WoS_matchPageVolumes.group(1).equals("P")) cr.PAG = WoS_matchPageVolumes.group(2);
				if (WoS_matchPageVolumes.group(1).equals("V")) cr.VOL = WoS_matchPageVolumes.group(2);
			}
			
			Matcher WoS_matchDOI = sWoS_matchDOI.matcher(it.trim());
			if (WoS_matchDOI.matches()) {
				cr.DOI = WoS_matchDOI.group(1).replaceAll("  ","").toUpperCase();
			}
		}
		
		return cr;
				
		
	}
	
}

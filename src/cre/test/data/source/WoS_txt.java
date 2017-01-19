package cre.test.data.source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.CRType;
import cre.test.data.PubType;
import cre.test.ui.StatusBar;
import cre.test.ui.UserSettings;

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
				if ((line.startsWith("ER")) || (line.startsWith("EF"))) return;
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
	
	
	
	public static void load (List<File> files, CRTable crTab, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {

		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();
		
		crTab.abort = false;	// can be changed by "wait dialog"
		

		
		crTab.init();
		
		AtomicLong countCR = new AtomicLong(0);
		
		int idx = 0;
		for (File file: files) {
			
			WoS_Iterator wosIt = new WoS_Iterator(file);
			
			StatusBar.get().initProgressbar(file.length(), String.format("Loading WoS file %1$d of %2$d ...", (++idx), files.size()));
			
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
					case "PY": try { pub.PY = Integer.valueOf(value); } catch (NumberFormatException e) { }; break;
					case "BP": try { pub.BP = Integer.valueOf(value); } catch (NumberFormatException e) { }; break;
					case "EP": try { pub.EP = Integer.valueOf(value); } catch (NumberFormatException e) { }; break;
					case "PG": try { pub.PG = Integer.valueOf(value); } catch (NumberFormatException e) { }; break;
					case "TC": try { pub.TC = Integer.valueOf(value); } catch (NumberFormatException e) { }; break;
					
					/* Parse Cited References */
					case "CR": CRType cr = /*new CRType().*/parseCR(value, yearRange); if (cr!=null) pub.crList.add(cr); break;
					
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
					} else {
						pub.C1.add (new String[] { "", corr });
						pub.AA.add (corr);
					}
				}
				
				StatusBar.get().updateProgressbar(countSize.addAndGet(pub.length));
				countCR.addAndGet(pub.crList.size());
				
				return pub;
			}).filter ( it -> it != null).collect (Collectors.toList()));	// remove null values (abort)
			
			wosIt.close();
			
			// Check for abort by user
			if (crTab.abort) {
				crTab.init();
				crTab.updateData(false);
				StatusBar.get().setValue("Loading WoS files aborted (due to user request)", 0);
				throw new AbortedException();
			}

			// Check for maximal number of CRs
			if ((maxCR>0) && (countCR.get()>=maxCR)) {
				StatusBar.get().setValue("Loading WoS files aborted (due to maximal number of CRs)", 0);
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
		StatusBar.get().setValue("Loading WoS files done", crTab.getInfoString());		
		
	}
	
	public static void save (File file, CRTable crTab) throws IOException {
		
		StatusBar.get().initProgressbar(crTab.getSizePub(), "Saving WoS file ...");
		
		// add txt extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".txt")) file_name += ".txt";
				
						
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		bw.write("FN Thomson Reuters Web of Science\u0153 modified by CRExplorer");
		bw.newLine();
		bw.write("VR 1.0");
		bw.newLine();
		
		for (PubType pub: crTab.pubData) {
			writeTag(bw, "PT", pub.PT == null ? "J" : pub.PT);	// TODO: Is "J" the correct default for publication type?
			writeTag(bw, "AU", pub.AU);
			writeTag(bw, "AF", pub.AF);
			if (pub.C1 != null) {
				writeTag(bw, "C1", pub.C1.stream().map(it -> { return "[" + it[0] + "] " + it[1]; }).collect(Collectors.toList()));
			}
			
			if (pub.EM != null) {
				writeTag (bw, "EM", pub.EM.stream().distinct().collect(Collectors.joining("; ")));
			}
			
			// make sure TI value is split into lines up to 70 characters (=maxLength)
			ArrayList<String> linesTI = new ArrayList<String>();
			String title = new String(pub.TI == null ? "" : pub.TI);
			int maxLength = 70;
			while (true) {
				if (title.length()<=maxLength) { 
					linesTI.add(title);
					break;
				}
				
				int pos = title.lastIndexOf(' ', maxLength);
				if (pos > 0) {
					linesTI.add (title.substring(0,  pos));
					title = title.substring(pos+1);
				} else {
					linesTI.add (title.substring(0,  maxLength));
					title = title.substring(maxLength);
				}
			} 
			writeTag(bw, "TI", linesTI);
			
			if (pub.PY != null) writeTag(bw, "PY", pub.PY.toString());
			writeTag(bw, "SO", pub.SO);
			writeTag(bw, "VL", pub.VL);
			writeTag(bw, "IS", pub.IS);
			writeTag(bw, "AR", pub.AR);
			if (pub.BP != null) writeTag(bw, "BP", pub.BP.toString());
			if (pub.EP != null) writeTag(bw, "EP", pub.EP.toString());
			if (pub.PG != null) writeTag(bw, "PG", pub.PG.toString());
			if (pub.TC != null) writeTag(bw, "TC", pub.TC.toString());
			
			writeTag(bw, "CR", pub.crList.stream().map(it -> {

				if (it.type == CRType.TYPE_WOS) return it.getCR();
				
				/* Generate CR-String in WoS format */
				String res = (it.getAU_L() != null) ? it.getAU_L() + " " : "";
				if (it.getAU_F() != null) res += it.getAU_F();
				if (it.getRPY() != null) res += ", " + it.getRPY();
				if ((it.getVOL()!=null) || (it.getPAG()!=null)) {
					if (it.getJ_N()!=null) res += ", " + it.getJ_N(); 
					if (it.getVOL()!=null) res += ", V" + it.getVOL();
					if (it.getPAG()!=null) res += ", P" + it.getPAG();
				} else {
					res += ", " + it.getJ();
				}
				if (it.getDOI()!=null) res += ", DOI " + it.getDOI();
				
				return res;
				
			}).collect(Collectors.toList()));
			
			
			writeTag(bw, "NR", String.valueOf(pub.crList.size()));
			writeTag(bw, "DI", pub.DI);
			writeTag(bw, "AB", pub.AB);
			writeTag(bw, "DE", pub.DE);
			writeTag(bw, "DT", pub.DT);
			writeTag(bw, "UT", pub.UT);
			
			bw.write("ER");
			bw.newLine();
			bw.newLine();
		
			StatusBar.get().incProgressbar();
		
		}
		bw.write("EF"); 
		bw.newLine();
		bw.close();
		StatusBar.get().setValue("Saving WoS file done", crTab.getInfoString());

			
	}
	
	
	private static void writeTag (BufferedWriter bw, String tag, String v) throws IOException {
		if (v == null) return;
		bw.write(tag+" ");
		bw.write(v);
		bw.newLine();
	}
	
	private static void writeTag (BufferedWriter bw, String tag, List<String> values) throws IOException {
		if (values == null) return;
		if (values.size()==0) return;
		int pos = 0;
		for (String v: values) {
			bw.write((pos==0) ? tag+" " : "   ");
			bw.write(v);
			bw.newLine();
			pos++;
		}
	}
	
	private static CRType parseCR (String line, int[] yearRange) {

		CRType cr = new CRType();
		cr.setCR(line); // [3..-1] // .toUpperCase()
		cr.type = CRType.TYPE_WOS;
		cr.setRPY(null);
		String[] crsplit = cr.getCR().split (",", 3);
		
		
		int yearPos = 1;
		while ((cr.getRPY() == null) && (yearPos >= 0)) {
			String yearS = crsplit.length > 1 ? crsplit[yearPos].trim() : "";
			if (yearS.length() <= 4) {
				try {
					int year = Integer.parseInt(yearS);
					if (((year < yearRange[0]) && (yearRange[0]!=0)) || ((year > yearRange[1]) && (yearRange[1]!=0))) return null;
					cr.setRPY(year);
				} catch (NumberFormatException e) { }
			}
			yearPos--;
		}

		if ((cr.getRPY() == null) && ((yearRange[0]!=0) || (yearRange[1]!=0))) return null;
		
		cr.setAU(crsplit[0].trim());
		
		// process "difficult" last names starting with "von" etc.
		if ((cr.getAU().length()>0) && (cr.getAU().charAt(0)=='v')) {
			
			for (Pattern p: sWoS_matchAuthorVon) {
				Matcher matchVon = p.matcher(cr.getAU());
				if (matchVon.matches()) {
					cr.setAU_L((matchVon.group(1) + (matchVon.group(2)==null?"":matchVon.group(2)) + matchVon.group( ((matchVon.group(3).equals("")) ? 5 : 3) )).replaceAll(" ","").replaceAll("\\-",""));
					String tmp = ((((matchVon.group(3).equals("")) ? "" : matchVon.group(5)) + matchVon.group(6)).trim());
					cr.setAU_F(tmp.equals("") ? "" : tmp.substring(0,1));	// cast as List to avoid Index out of Bounds exception
					break;
				}
			}
		}
		
		// process all other authors
		if (cr.getAU_L() == null) {
			Matcher WoS_matchAuthor = sWoS_matchAuthor.matcher(cr.getAU());
			if (WoS_matchAuthor.matches()) {
				cr.setAU_L(WoS_matchAuthor.group(1).replaceAll("\\-",""));
				cr.setAU_F((WoS_matchAuthor.group(3) == null ? " " : WoS_matchAuthor.group(3) + " ").substring(0, 1));
			}
		}
			
		// process all journals
		cr.setJ(crsplit.length > 2 ? crsplit[2].trim() : "");
		cr.setJ_N(cr.getJ().equals(",") ? "" : cr.getJ().split(",")[0]);	// 1994er problem (Mail Robin) --> if (CR.J == ",") -> split.size()==0
		String[] split = cr.getJ_N().split(" ");
		if (split.length==1) {
			cr.setJ_S(split[0]); 
		} else {
			cr.setJ_S("");
			for (String s: split) {
				if (s.length()>0) cr.setJ_S(cr.getJ_S() + s.charAt(0));
			}
		}
		
		
		// find Volume, Pages and DOI
		for (String it: cr.getJ().split(",")) {
			Matcher WoS_matchPageVolumes = sWoS_matchPageVolumes.matcher(it.trim());
			if (WoS_matchPageVolumes.matches()) {
				if (WoS_matchPageVolumes.group(1).equals("P")) cr.setPAG(WoS_matchPageVolumes.group(2));
				if (WoS_matchPageVolumes.group(1).equals("V")) cr.setVOL(WoS_matchPageVolumes.group(2));
			}
			
			Matcher WoS_matchDOI = sWoS_matchDOI.matcher(it.trim());
			if (WoS_matchDOI.matches()) {
				cr.setDOI(WoS_matchDOI.group(1).replaceAll("  ","").toUpperCase());
			}
		}
		
		return cr;
				
		
	}
	
}

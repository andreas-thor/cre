package cre.test.data.source;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRTable;
import cre.test.data.CRType;
import cre.test.data.PubType;
import cre.test.ui.StatusBar;

public class Scopus_csv  {

	static Pattern sScopus_matchEMail = Pattern.compile ("\\s(\\S+@\\w+(\\.\\w+)+)\\W*");
	
	static Pattern sScopus_matchBlock = Pattern.compile ("^([A-Y ]+)(\\: )(.*)");
	
	static List<Pattern> sScopus_matchPAG  = new ArrayList<Pattern>() {
		private static final long serialVersionUID = 1L;
	{
		add(Pattern.compile ("p\\. ([0-9]+)$"));
		add(Pattern.compile ("p\\. ([0-9]+)[\\.;,]"));
		add(Pattern.compile ("pp\\. ([0-9]+)\\-[0-9]+[\\.;,]"));
		add(Pattern.compile ("pp\\. ([0-9]+)\\-[0-9]+$"));
	}};
	
	static List<Pattern> sScopus_matchVOL = new ArrayList<Pattern>() {
		private static final long serialVersionUID = 1L;
	{
		add(Pattern.compile ("([0-9]+)"));
		add(Pattern.compile ("([0-9]+) \\([0-9]+\\)"));
		add(Pattern.compile ("([0-9]+) \\([0-9]+\\-[0-9]+\\)"));
	}};

	static Pattern sScopus_matchAuthor = Pattern.compile ("^([^,]+),([ A-Z\\-\\.]+\\.),(.*)");
	static Pattern sScopus_matchYearTitle = Pattern.compile ("(.*?)\\((\\d{4})\\)(.*)");
	static Pattern sScopus_matchDOI = Pattern.compile (".*((DOI)|(doi))\\:?=?(10\\.[^/]+/ *[^;,]+).*");

	
	
	public static void load (File[] files, CRTable crTab, StatusBar stat, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {
		
		long ts1 = System.currentTimeMillis();
		
		crTab.abort = false;	// can be changed by "wait dialog"
		
		Date startDate = new Date();
		stat.setValue(String.format("%1$s: Loading Scopus files ...", startDate), "");
		
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
			

			HashMap<String, Integer> attribute2Index = new HashMap<String, Integer>();
			for (int i=0; i<attributes.length; i++) {
				attribute2Index.put(attributes[i], i);
			}

			
			int stepSize = 5;
			int modulo = content.size()*stepSize/100;
			AtomicLong countPub = new AtomicLong(0);

			int fileNr = idx+1;
			crTab.pubData.addAll(content.parallelStream().map ( (String[] it) -> {
			
				/* if user abort or maximum number of CRs reached --> do no process anymore */
				if (crTab.abort) return null;
				if ((maxCR>0) && (countCR.get()>=maxCR)) return null;
				
				PubType pub = new PubType(); // .parseScopus(it, attributes, yearRange);
				
				pub.PT = "J"; // TODO: what is the default Publication Type? (No value in scopus!)
						
				// Scopus Authors: Lastname1 I1., Lastname2 I2.I2. ...
				pub.AU = new ArrayList<String>();
				if (it[attribute2Index.get("Authors")]!=null) {
					for (String name: it[attribute2Index.get("Authors")].split("\\., ")) {
						name = name.replaceAll("\\.", ""); 
						int pos = name.lastIndexOf(" ");
						pub.AU.add ((pos>0) ? name.substring(0, pos) + "," + name.substring (pos) : name);
					}
				}
				
				pub.AF = new ArrayList<String>(pub.AU);		// there are no full names in Scopus 

				// Authors with affiliations: "<lastname>, <initials with dots>, affiliation"
				pub.C1 = new ArrayList<String[]>();
				pub.EM = new ArrayList<String>();
				if (it[attribute2Index.get("Authors with affiliations")] != null) {
				
					for (String author: it[attribute2Index.get("Authors with affiliations")].split("; ")) {
						String[] split = author.split(", ", 3);
						if (split.length == 3) {
							pub.C1.add (new String[] { (split[0]+", "+split[1].replaceAll("\\.", "")), split[2] });
						}
						
						if (author.contains("@")) System.out.println ("@@@");
					
						Matcher Scopus_matchEMail = sScopus_matchEMail.matcher(author);
						if (Scopus_matchEMail.find()) {
							System.out.println (Scopus_matchEMail.group(1));
							pub.EM.add (Scopus_matchEMail.group(1));
						}
					}
				}
					
				pub.AA = new ArrayList<String>();
				for (String aff: it[attribute2Index.get("Affiliations")].split("; ")) pub.AA.add(aff); 
						
				pub.TI = it[attribute2Index.get("Title")];
				try { pub.PY = Integer.valueOf(it[attribute2Index.get("Year")]); } catch (NumberFormatException e) { }

				pub.SO = it[attribute2Index.get("Source title")];
				pub.VL = it[attribute2Index.get("Volume")];
				pub.IS = it[attribute2Index.get("Issue")];
				pub.AR = it[attribute2Index.get("Art. No.")];
				
				try { pub.BP = Integer.valueOf(it[attribute2Index.get("Page start")]); } catch (NumberFormatException e) { }
				try { pub.EP = Integer.valueOf(it[attribute2Index.get("Page end")]); } catch (NumberFormatException e) { }
				try { pub.PG = Integer.valueOf(it[attribute2Index.get("Page count")]); } catch (NumberFormatException e) { }
				try { pub.TC = Integer.valueOf(it[attribute2Index.get("Cited by")]); } catch (NumberFormatException e) { }
				
				/* parse list of CRs */
				pub.crList = new ArrayList<CRType>();
				if (it[attribute2Index.get("References")] != null) {
					for (String crString: it[attribute2Index.get("References")].split(";")) {
						CRType cr = parseCR (crString, yearRange);
						if (cr != null) pub.crList.add (cr); 
					}
				}
				
				pub.DI = it[attribute2Index.get("DOI")];
				pub.LI = it[attribute2Index.get("Link")];
				pub.AB = it[attribute2Index.get("Abstract")];
				pub.DE = it[attribute2Index.get("Author Keywords")];
				pub.DT = it[attribute2Index.get("Document Type")];
				pub.FS = it[attribute2Index.get("Source")];
				pub.UT = it[attribute2Index.get("EID")];
				
				
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
		stat.setValue(String.format("%1$s: Loading Scopus files done", startDate), crTab.getInfoString());
	}
	
	
	
	private static CRType parseCR (String line, int[] yearRange) {
		
		line = line.trim();
		if (line.length() == 0) return null;
		
		CRType res = new CRType();
		res.type = CRType.TYPE_SCOPUS;
		res.setCR(line);
		
		// parse all authors (but save first author only in AU_L, AU_F, AU; all authors go to AU_A)
		boolean firstAuthor = true;
		Matcher Scopus_matchAuthor = sScopus_matchAuthor.matcher(line);
		while (Scopus_matchAuthor.matches()) {
			if (firstAuthor) {
				res.setAU_L(Scopus_matchAuthor.group(1));
				res.setAU_F((Scopus_matchAuthor.group(2).trim()).substring(0,  1));
				res.setAU(Scopus_matchAuthor.group(1) + "," + Scopus_matchAuthor.group(2));
				res.setAU_A(Scopus_matchAuthor.group(1) + "," + Scopus_matchAuthor.group(2));
			} else {
				res.setAU_A(res.getAU_A() + "; " + Scopus_matchAuthor.group(1) + "," + Scopus_matchAuthor.group(2));
			}
			firstAuthor = false;
			line = Scopus_matchAuthor.group(3).trim();
			Scopus_matchAuthor.reset(line);
		}
		
		// find publication year and title
		res.setJ_N("");
		res.setJ("");
		res.setTI("");
		Matcher Scopus_matchYearTitle = sScopus_matchYearTitle.matcher(line);
		if (Scopus_matchYearTitle.matches()) {
			if (Scopus_matchYearTitle.group(1).length() == 0) {
				try { res.setRPY(Integer.valueOf (Scopus_matchYearTitle.group(2))); } catch (NumberFormatException e) {}
				int pos = Scopus_matchYearTitle.group(3).indexOf(", ,");
				if (pos>=0) {
					res.setTI(Scopus_matchYearTitle.group(3).substring(0, pos));
					res.setJ_N("");
					res.setJ(Scopus_matchYearTitle.group(3).substring (pos+3).trim());
				} else {
					String[] crsplit = Scopus_matchYearTitle.group(3).split (",", 2);
					res.setJ_N(crsplit[0].trim());
					res.setJ(Scopus_matchYearTitle.group(3).trim());
				}
				
			} else {
				res.setTI(Scopus_matchYearTitle.group(1));
				try { res.setRPY(Integer.valueOf (Scopus_matchYearTitle.group(2))); } catch (NumberFormatException e) {}
				String[] crsplit = Scopus_matchYearTitle.group(3).split (",", 2);
				res.setJ_N(crsplit[0].trim());
				res.setJ(Scopus_matchYearTitle.group(3).trim());
			}
		}


		// check if year available and in the given year range
//		if (res.RPY == null) return null;
		if (res.getRPY() != null) {
			if (((res.getRPY() < yearRange[0]) && (yearRange[0]!=0)) || ((res.getRPY() > yearRange[1]) && (yearRange[1]!=0))) return null;
		} else {
			if ((yearRange[0]!=0) || (yearRange[1]!=0)) return null;
		}

		
		// process Journal names
		String[] split = res.getJ_N().split(" ");
		res.setJ_S((split.length==1) ? split[0] : Arrays.stream (split).reduce("",  (x,y) -> x + ((y.length()>0) ? y.substring(0,1) : "") ));

		
		Matcher Scopus_matchDOI = sScopus_matchDOI.matcher (res.getJ().replaceAll(" ",""));
		if (Scopus_matchDOI.matches()) {
			res.setDOI(Scopus_matchDOI.group(4));
		}
		
		if ((res.getJ().toLowerCase().indexOf("doi")>=0) && (res.getDOI() == null)) {
			// TODO: J8 improve DOI identification
			System.out.println ("DOI could not been identified in: " + res.getJ());
		}
		
		
		for (String it: res.getJ().split (",")) {
			
			String s = it.trim();
			for (Pattern p: sScopus_matchPAG) {
				Matcher matchP = p.matcher(s.trim());
				if (matchP.matches()) res.setPAG(matchP.group(1));
			}
			
			for (Pattern p: sScopus_matchVOL) {
				Matcher matchV = p.matcher(s.trim());
				if (matchV.matches()) res.setVOL(matchV.group(1));
			}
		}
		
		return res;
	}




	public static void save (File file, CRTable crTab, StatusBar stat) throws IOException {
		
		stat.initProgressbar(crTab.pubData.size(), "Saving Scopus file ...");
		int count = 0;

		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
				
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		
		csv.writeNext(new String[] {"Authors","Title","Year","Source title","Volume","Issue","Art. No.","Page start","Page end","Page count","Cited by","DOI","Link","Affiliations","Authors with affiliations","Abstract","Author Keywords","References","Document Type","Source","EID"});
		
		for (PubType pub: crTab.pubData) {
			ArrayList<String> row = new ArrayList<String>();
			
			row.add ((pub.AU == null) ? "" :
				pub.AU.stream().map ( a -> {
					String[] split = a.split(", ", 2);
					String res = (split.length==2) ? split[0] + ", " + split[1].replaceAll("([A-Z])", "$1.") : a; 
					return  res;
				}).collect (Collectors.joining(", "))
			);

			row.add (pub.TI == null ? "" : pub.TI);
			row.add (pub.PY == null ? "" : pub.PY.toString());
			row.add (pub.SO == null ? "" : pub.SO);
			row.add (pub.VL == null ? "" : pub.VL);
			row.add (pub.IS == null ? "" : pub.IS);
			row.add (pub.AR == null ? "" : pub.AR);
			row.add (pub.BP == null ? "" : pub.BP.toString());
			row.add (pub.EP == null ? "" : pub.EP.toString());
			row.add (pub.PG == null ? "" : pub.PG.toString());
			row.add (pub.TC == null ? "" : pub.TC.toString());
			row.add (pub.DI == null ? "" : pub.DI);
			row.add (pub.LI == null ? "" : pub.LI);

			row.add (pub.AA == null ? "" : pub.AA.stream().collect(Collectors.joining("; ")));
			
			row.add ((pub.C1 == null) ? "" :
				pub.C1.stream().map(it -> {
					String[] split = it[0].split(", ", 2);
					String res = (split.length==2) ? (split[0] + ", " + split[1].replaceAll("([A-Z])", "$1.") + ", " + it[1]) : (it[0] + ", " + it[1]);
					return res;
				}).collect(Collectors.joining("; "))
			);
				
			row.add (pub.AB == null ? "" : pub.AB);
			row.add (pub.DE == null ? "" : pub.DE);

			row.add (pub.crList.stream().map ( cr -> { 
				
				if (cr.type == CRType.TYPE_SCOPUS) return cr.getCR();
				
				/* generate CR string in Scopus format */
				String res = "";
				if (cr.getAU_A() == null) {
					if (cr.getAU_L() != null) res += cr.getAU_L() + ", " + cr.getAU_F().replaceAll("([A-Z])", "$1."); 
				} else {
					res += cr.getAU_A().replaceAll(";", ",");
				}
				res += ",";
				if (cr.getTI() != null)	res += cr.getTI();
				if (cr.getRPY() != null) res += " (" + cr.getRPY() + ") ";
				if (cr.getJ_N() != null) res += cr.getJ_N();
				if (cr.getVOL() != null) res += ", " + cr.getVOL();
				if (cr.getPAG() != null) res += ", pp." + cr.getPAG();
				if (cr.getDOI() != null) res += ", DOI " + cr.getDOI();

				return res;
			} ).collect (Collectors.joining ("; ")));
			
			row.add (pub.DT == null ? "" : pub.DT);
			row.add (pub.FS == null ? "" : pub.FS);
			row.add (pub.UT == null ? "" : pub.UT);
					
			csv.writeNext ((String[]) row.toArray(new String[row.size()]));
		
			stat.updateProgressbar(++count);
		}
					
			
		csv.close();

		stat.setValue(String.format("%1$s: Saving Scopus file done", new Date()), crTab.getInfoString());
	}
	
	
	
	
} 


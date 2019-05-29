package main.cre.data.source;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import main.cre.data.Statistics;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.PubType_MM;
import main.cre.ui.statusbar.StatusBar;

public class Scopus_csv extends ImportReader  {

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

	
	private Map<String, Integer> attribute2Index = null;
	private CSVReader csv = null;
	
	
	@Override
	public void init(File file) throws IOException {
		attribute2Index = null;
		csv = null;
		super.init(file);
	}
		
	@Override
	protected void computeNextEntry () throws IOException {
		
		entry = null;
		
		if (csv==null) {
			csv = new CSVReader(br);
			String[] line = csv.readNext();
			if (line == null) return;
			String[] attributes = Arrays.stream(line).map(it ->  it.trim()).toArray(size -> new String[size]);
			if (attributes[0].startsWith("\uFEFF")) attributes[0] = attributes[0].substring(1);
			attribute2Index = IntStream.range(0, attributes.length).mapToObj (i -> Integer.valueOf(i)).collect(Collectors.toMap(i -> attributes[i].toUpperCase(), i -> i));
		}
		
		
		String[] line = csv.readNext();
		if (line == null) return;
		entry = parsePub(line);
	}
	
	
	@Override
	public void close() throws IOException {
		csv.close();
		super.close();
	}
	
	
	private PubType_MM parsePub (String[] line) {
		
		PubType_MM pub = new PubType_MM(); // .parseScopus(it, attributes, yearRange);
		
		pub.setPT("J"); // TODO: what is the default Publication Type? (No value in scopus!)
				
		// Scopus Authors: Lastname1 I1., Lastname2 I2.I2. ...
		if ((attribute2Index.get("AUTHORS")!=null) && (line[attribute2Index.get("AUTHORS")]!=null)) {
			for (String name: line[attribute2Index.get("AUTHORS")].split("\\., ")) {
				name = name.replaceAll("\\.", ""); 
				int pos = name.lastIndexOf(" ");
				pub.addAU((pos>0) ? name.substring(0, pos) + "," + name.substring (pos) : name);
			}
		}
		
		pub.getAU().forEach (e -> pub.addAF(e));		// there are no full names in Scopus 
		
		// Authors with affiliations: "<lastname>, <initials with dots>, affiliation"
		if ((attribute2Index.get("AUTHORS WITH AFFILIATIONS") != null) && (line[attribute2Index.get("AUTHORS WITH AFFILIATIONS")] != null)) {
		
			for (String author: line[attribute2Index.get("AUTHORS WITH AFFILIATIONS")].split("; ")) {
				String[] split = author.split(", ", 3);
				if (split.length == 3) {
					pub.addC1(new String[] { (split[0]+", "+split[1].replaceAll("\\.", "")), split[2] });
				}
				
				if (author.contains("@")) System.out.println ("@@@");
			
				Matcher Scopus_matchEMail = sScopus_matchEMail.matcher(author);
				if (Scopus_matchEMail.find()) {
					System.out.println (Scopus_matchEMail.group(1));
					pub.addEM(Scopus_matchEMail.group(1));
				}
			}
		}
			
		if ((attribute2Index.get("AFFILIATIONS") != null) && (line[attribute2Index.get("AFFILIATIONS")] != null)) {
			for (String aff: line[attribute2Index.get("AFFILIATIONS")].split("; ")) pub.addAA(aff);
		}
				
		pub.setTI(attribute2Index.get("TITLE") != null ? line[attribute2Index.get("TITLE")] : null);
		try { pub.setPY(Integer.valueOf(line[attribute2Index.get("YEAR")])); } catch (Exception e) { }

		pub.setSO(attribute2Index.get("SOURCE TITLE") != null ? line[attribute2Index.get("SOURCE TITLE")] : null);
		pub.setVL(attribute2Index.get("VOLUME") != null ? line[attribute2Index.get("VOLUME")] : null);
		pub.setIS(attribute2Index.get("ISSUE") != null ? line[attribute2Index.get("ISSUE")] : null);
		pub.setAR(attribute2Index.get("ART. NO.") != null ? line[attribute2Index.get("ART. NO.")] : null);
		
		try { pub.setBP(Integer.valueOf(line[attribute2Index.get("PAGE START")])); } catch (Exception e) { }
		try { pub.setEP(Integer.valueOf(line[attribute2Index.get("PAGE END")])); } catch (Exception e) { }
		try { pub.setPG(Integer.valueOf(line[attribute2Index.get("PAGE COUNT")])); } catch (Exception e) { }
		try { pub.setTC(Integer.valueOf(line[attribute2Index.get("CITED BY")])); } catch (Exception e) { }
		
		/* parse list of CRs */
		if ((attribute2Index.get("REFERENCES") != null) && (line[attribute2Index.get("REFERENCES")] != null)) {
			for (String crString: line[attribute2Index.get("REFERENCES")].split(";")) {
//				pub.addCR (parseCR (crString), true);
				
				CRType_MM cr = parseCR(crString);
				if (cr != null) {
					pub.addCR(cr, true);  
				}				
			}
		}
		
		pub.setDI(attribute2Index.get("DOI") != null ? line[attribute2Index.get("DOI")] : null);
		pub.setLI(attribute2Index.get("LINK") != null ? line[attribute2Index.get("LINK")] : null);
		pub.setAB(attribute2Index.get("ABSTRACT") != null ? line[attribute2Index.get("ABSTRACT")] : null);
		pub.setDE(attribute2Index.get("AUTHOR KEYWORDS") != null ? line[attribute2Index.get("AUTHOR KEYWORDS")] : null);
		pub.setDT(attribute2Index.get("DOCUMENT TYPE") != null ? line[attribute2Index.get("DOCUMENT TYPE")] : null);
		pub.setFS(attribute2Index.get("SOURCE") != null ? line[attribute2Index.get("SOURCE")] : null);
		
		System.out.println(pub.getTI());
		pub.setUT(attribute2Index.get("EID") != null ? line[attribute2Index.get("EID")] : null);
		
		return pub;
	}
	
	protected static CRType_MM parseCR (String line) {
		
		line = line.trim();
		if (line.length() == 0) return null;
		
		CRType_MM res = new CRType_MM();
		res.setType (CRType.FORMATTYPE.SCOPUS);
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
		
//		if (res.getRPY() != null) {
//			if (((res.getRPY() < yearRange[0]) && (yearRange[0]>0)) || ((res.getRPY() > yearRange[1]) && (yearRange[1]>0))) return null;
//		} else {
//			if ((yearRange[0]>0) || (yearRange[1]>0)) return null;
//		}

		
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



	/**
	 * 
	 * @param file_name
	 * @param includePubsWithoutCRs
	 * @param filter
	 * @param comp IS IGNORED
	 * @throws IOException
	 */
	public static void save (String file_name, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException {
		
		/* TODO: Filter not supported yet ... nun drin? */
		
		StatusBar.get().initProgressbar(Statistics.getNumberOfPubs());
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		
		csv.writeNext(new String[] {"Authors","Title","Year","Source title","Volume","Issue","Art. No.","Page start","Page end","Page count","Cited by","DOI","Link","Affiliations","Authors with affiliations","Abstract","Author Keywords","References","Document Type","Source","EID"});
		
		CRTable.get().getPub(includePubsWithoutCRs).forEach(pub -> {
			ArrayList<String> row = new ArrayList<String>();
			
			row.add ((pub.getAU().count() == 0) ? "" :
				pub.getAU().map ( a -> {
					String[] split = a.split(", ", 2);
					String res = (split.length==2) ? split[0] + ", " + split[1].replaceAll("([A-Z])", "$1.") : a; 
					return res;
				}).collect (Collectors.joining(", "))
			);

			row.add (pub.getTI() == null ? "" : pub.getTI());
			row.add (pub.getPY() == null ? "" : pub.getPY().toString());
			row.add (pub.getSO() == null ? "" : pub.getSO());
			row.add (pub.getVL() == null ? "" : pub.getVL());
			row.add (pub.getIS() == null ? "" : pub.getIS());
			row.add (pub.getAR() == null ? "" : pub.getAR());
			row.add (pub.getBP() == null ? "" : pub.getBP().toString());
			row.add (pub.getEP() == null ? "" : pub.getEP().toString());
			row.add (pub.getPG() == null ? "" : pub.getPG().toString());
			row.add (pub.getTC() == null ? "" : pub.getTC().toString());
			row.add (pub.getDI() == null ? "" : pub.getDI());
			row.add (pub.getLI() == null ? "" : pub.getLI());

			row.add (pub.getAA().count() == 0 ? "" : pub.getAA().collect(Collectors.joining("; ")));
			
			row.add ((pub.getC1().count() == 0) ? "" :
				pub.getC1().map(it -> {
					String[] split = it[0].split(", ", 2);
					String res = (split.length==2) ? (split[0] + ", " + split[1].replaceAll("([A-Z])", "$1.") + ", " + it[1]) : (it[0] + ", " + it[1]);
					return res;
				}).collect(Collectors.joining("; "))
			);
				
			row.add (pub.getAB() == null ? "" : pub.getAB());
			row.add (pub.getDE() == null ? "" : pub.getDE());

			row.add (pub.getCR()
				.filter(cr -> filter.test(cr))	
				.map ( cr -> (cr.getType()==CRType.FORMATTYPE.SCOPUS) ? cr.getCR() : generateCRString (cr))
				.collect (Collectors.joining ("; ")));
			row.add (pub.getDT() == null ? "" : pub.getDT());
			row.add (pub.getFS() == null ? "" : pub.getFS());
			row.add (pub.getUT() == null ? "" : pub.getUT());
					
			csv.writeNext ((String[]) row.toArray(new String[row.size()]));
		
			StatusBar.get().incProgressbar();
		});
					
			
		csv.close();

		StatusBar.get().setValue ("Saving Scopus file done");
	}
	
	
	public static String generateCRString (CRType<?> cr) {
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
	}
	
} 


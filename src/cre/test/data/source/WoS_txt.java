package cre.test.data.source;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import cre.test.ui.StatusBar;

/** 
 * Provides iterator over all PubType elements in a list of Scopus files
 */
public class WoS_txt extends ImportReader {

	static Pattern sWoS_matchAuthor = Pattern.compile("([^ ]*)( )?(.*)?");
	
	static Pattern[] sWoS_matchAuthorVon = new Pattern[] { 
			Pattern.compile("(von )()([^ ]*)( )([^ ]*)(.*)"), 
			Pattern.compile("(van )(der )?([^ ]*)( )([^ ]*)(.*)"), 
			Pattern.compile("(van't )()([^ ]*)( )([^ ]*)(.*)") 
		};
	
	static Pattern sWoS_matchPageVolumes = Pattern.compile ("([PV])([0-9]+)");
	static Pattern sWoS_matchDOI = Pattern.compile(".*DOI (10\\.[^/]+/ *[^ ,]+).*");
	
	

	@Override
	protected void computeNextEntry () throws IOException {
		
		entry = null;
			
		// read until next ER
		ArrayList<String> block = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("ER")) {
				entry = this.parsePub(block);
				if (entry != null) {	// found next entry
					return;			
				} else {	// go to next block
					block = new ArrayList<String>();
					continue;
				}
			}
			block.add(line); 
		}
	}
	



	
	
	
	private PubType parsePub (List<String> it) {

		String currentTag = "";
		String tagBlock = "";
		String value = "";
			
		PubType pub = new PubType();
		pub.setFS("WoS");
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
			
			case "PT": pub.setPT(value); break;
			
			/* Concatenated Strings */
			case "TI": pub.setTI((pub.getTI()==null) ? value : pub.getTI()+" "+value); break;
			case "SO": pub.setSO((pub.getSO()==null) ? value : pub.getSO()+" "+value); break;
			case "VL": pub.setVL((pub.getVL()==null) ? value : pub.getVL()+" "+value); break;
			case "IS": pub.setIS((pub.getIS()==null) ? value : pub.getIS()+" "+value); break;
			case "AR": pub.setAR((pub.getAR()==null) ? value : pub.getAR()+" "+value); break;
			case "DI": pub.setDI((pub.getDI()==null) ? value : pub.getDI()+" "+value); break;
			case "LI": pub.setLI((pub.getLI()==null) ? value : pub.getLI()+" "+value); break;
			case "AB": pub.setAB((pub.getAB()==null) ? value : pub.getAB()+" "+value); break;
			case "DE": pub.setDE((pub.getDE()==null) ? value : pub.getDE()+" "+value); break;
			case "DT": pub.setDT((pub.getDT()==null) ? value : pub.getDT()+" "+value); break;
			case "UT": pub.setUT((pub.getUT()==null) ? value : pub.getUT()+" "+value); break;
			
			/* Integer values */
			case "PY": try { pub.setPY(Integer.valueOf(value)); } catch (NumberFormatException e) { }; break;
			case "BP": try { pub.setBP(Integer.valueOf(value)); } catch (NumberFormatException e) { }; break;
			case "EP": try { pub.setEP(Integer.valueOf(value)); } catch (NumberFormatException e) { }; break;
			case "PG": try { pub.setPG(Integer.valueOf(value)); } catch (NumberFormatException e) { }; break;
			case "TC": try { pub.setTC(Integer.valueOf(value)); } catch (NumberFormatException e) { }; break;
			
			/* Parse Cited References */
			case "CR":
				CRType cr = parseCR(value);
				if (cr != null) {
					pub.addCR(cr, true);  
				}
				break;
			
			/* Authors */
			case "AU": pub.addAU(value); break;
			case "AF": pub.addAF(value); break;
			case "EM": Arrays.stream(value.split("; ")).forEach(e -> pub.addEM(e)); break;
			/* store C1 values in a separate list for further processing */
			case "C1": C1.add(value); break;
			}
		}
		
		it = null;
		if (pub.getPT()==null) return null;
		
		for (String corr: C1) {
			int pos = corr.indexOf(']');
			if (pos>0) {
				String names = corr.substring(1, pos);
				String affiliation = corr.substring (pos+2);
				for (String name: names.split("; ")) {
					pub.addC1(new String[] { name, affiliation });
					pub.addAA(affiliation);
				}
			} else {
				pub.addC1(new String[] { "", corr });
				pub.addAA(corr);
			}
		}	
		
		return pub;
	}
	
	
	
	
	private CRType parseCR (String line) {

		CRType cr = new CRType();
		cr.setCR(line); // [3..-1] // .toUpperCase()
		cr.setType (CRType.TYPE_WOS);
		cr.setRPY(null);
		
	
		String[] crsplit = cr.getCR().split (",", 3);
		
		
		int yearPos = 1;
		while ((cr.getRPY() == null) && (yearPos >= 0)) {
			String yearS = crsplit.length > 1 ? crsplit[yearPos].trim() : "";
			if (yearS.length() <= 4) {
				try {
					int year = Integer.parseInt(yearS);
//					if (((year < rpyRange[0]) && (rpyRange[0]>0)) || ((year > rpyRange[1]) && (rpyRange[1]>0))) return null;
					cr.setRPY(year);
				} catch (NumberFormatException e) { }
			}
			yearPos--;
		}

//		if ((cr.getRPY() == null) && ((rpyRange[0]>0) || (rpyRange[1]>0))) return null;
		
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
	

	
	public static void save (String file_name) throws IOException, RuntimeException {
		
		StatusBar.get().initProgressbar(CRStats.getSizePub());
						
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		bw.write("FN Thomson Reuters Web of Science\u0153 modified by CRExplorer");
		bw.newLine();
		bw.write("VR 1.0");
		bw.newLine();
		
		CRTable.get().getPub(UserSettings.get().getIncludePubsWithoutCRs()).forEach (pub -> {
			try {
				writeTag(bw, "PT", pub.getPT() == null ? "J" : pub.getPT());	// TODO: Is "J" the correct default for publication type?
				writeTag(bw, "AU", pub.getAU());
				writeTag(bw, "AF", pub.getAF());
				if (pub.getC1() != null) {
					writeTag(bw, "C1", pub.getC1().map(it -> { return "[" + it[0] + "] " + it[1]; }));
				}
				
				if (pub.getEM() != null) {
					writeTag (bw, "EM", pub.getEM().distinct().collect(Collectors.joining("; ")));
				}
				
				// make sure TI value is split into lines up to 70 characters (=maxLength)
				ArrayList<String> linesTI = new ArrayList<String>();
				String title = new String(pub.getTI() == null ? "" : pub.getTI());
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
				writeTag(bw, "TI", linesTI.stream());
				
				if (pub.getPY() != null) writeTag(bw, "PY", pub.getPY().toString());
				writeTag(bw, "SO", pub.getSO());
				writeTag(bw, "VL", pub.getVL());
				writeTag(bw, "IS", pub.getIS());
				writeTag(bw, "AR", pub.getAR());
				if (pub.getBP() != null) writeTag(bw, "BP", pub.getBP().toString());
				if (pub.getEP() != null) writeTag(bw, "EP", pub.getEP().toString());
				if (pub.getPG() != null) writeTag(bw, "PG", pub.getPG().toString());
				if (pub.getTC() != null) writeTag(bw, "TC", pub.getTC().toString());
				
				writeTag(bw, "CR", pub.getCR().map(it -> {
	
					if (it.getType() == CRType.TYPE_WOS) return it.getCR();
					
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
					
				}));
				
				
				writeTag(bw, "NR", String.valueOf(pub.getSizeCR()));
				writeTag(bw, "DI", pub.getDI());
				writeTag(bw, "AB", pub.getAB());
				writeTag(bw, "DE", pub.getDE());
				writeTag(bw, "DT", pub.getDT());
				writeTag(bw, "UT", pub.getUT());
				
				bw.write("ER");
				bw.newLine();
				bw.newLine();
			
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		
			StatusBar.get().incProgressbar();
		
		});
		bw.write("EF"); 
		bw.newLine();
		bw.close();
		StatusBar.get().setValue("Saving WoS file done");

			
	}
	
	private static void writeTag (BufferedWriter bw, String tag, String v) throws IOException {
		if (v == null) return;
		bw.write(tag+" ");
		bw.write(v);
		bw.newLine();
	}
	
	
	private static void writeTag (BufferedWriter bw, String tag, Stream<String> values) throws IOException  {
		if (values == null) return;
		boolean first = true;
		for (String v: values.collect(Collectors.toList())) {
			writeTag (bw, first ? tag : "  ", v);
			first = false;
		}
	}
	
}
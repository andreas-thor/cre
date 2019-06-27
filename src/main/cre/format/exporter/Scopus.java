package main.cre.format.exporter;
 
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.ui.statusbar.StatusBar;

public class Scopus   {


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
		final Comparator<CRType<?>> compCR = (comp == null) ? CRType<?>::compareTo : comp; // default: sort by ID

		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfPubs());
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		
		csv.writeNext(new String[] {"Authors","Title","Year","Source title","Volume","Issue","Art. No.","Page start","Page end","Page count","Cited by","DOI","Link","Affiliations","Authors with affiliations","Abstract","Author Keywords","References","Document Type","Source","EID"});
		
		CRTable.get().getPub(includePubsWithoutCRs).sorted().forEach(pub -> {
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

			row.add (pub.getCR().sorted(compCR)
				.filter(cr -> filter.test(cr))	
				.map ( cr -> (cr.getFormatType()==CRType.FORMATTYPE.SCOPUS) ? cr.getCR() : generateCRString (cr))
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
	
	
	private static String generateCRString (CRType<?> cr) {
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


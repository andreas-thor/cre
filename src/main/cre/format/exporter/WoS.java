package main.cre.format.exporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.ui.statusbar.StatusBar;

/** 
 * Provides iterator over all PubType elements in a list of Scopus files
 */
public class WoS {


	
	/**
	 * 
	 * @param file_name
	 * @param includePubsWithoutCRs
	 * @param filter
	 * @param comp IS IGNORED
	 * @throws IOException
	 * @throws RuntimeException
	 */
	
	public static void save (OutputStream out, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException, RuntimeException {
		
		/* TODO: Filter is not supported yet */
		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfPubs());
						
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(out, "UTF-8"));
		bw.write("FN Thomson Reuters Web of Science\u0153 modified by CRExplorer");
		bw.newLine();
		bw.write("VR 1.0");
		bw.newLine();
		
		CRTable.get().getPub(includePubsWithoutCRs, true).forEach (pub -> {
			try {
				writeTag(bw, "PT", pub.getPT() == null ? "J" : pub.getPT());	// TODO: Is "J" the correct default for publication type?
				writeTag(bw, "AU", pub.getAU());
				writeTag(bw, "AF", pub.getAF());
				if (pub.getC1() != null) {
					System.out.println(pub.getID());
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
				
				writeTag(bw, "CR", pub.getCR().sorted(comp).map(cr -> (cr.getFormatType()==CRType.FORMATTYPE.WOS) ? cr.getCR() : generateCRString(cr) ));
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
	
	
	private static String generateCRString (CRType<?> cr) {
		/* Generate CR-String in WoS format */
		String res = (cr.getAU_L() != null) ? cr.getAU_L() + " " : "";
		if (cr.getAU_F() != null) res += cr.getAU_F();
		if (cr.getRPY() != null) res += ", " + cr.getRPY();
		if ((cr.getVOL()!=null) || (cr.getPAG()!=null)) {
			if (cr.getJ_N()!=null) res += ", " + cr.getJ_N(); 
			if (cr.getVOL()!=null) res += ", V" + cr.getVOL();
			if (cr.getPAG()!=null) res += ", P" + cr.getPAG();
		} else {
			res += ", " + cr.getJ();
		}
		if (cr.getDOI()!=null) res += ", DOI " + cr.getDOI();
		
		return res;

		
	}
	
}
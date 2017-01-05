package cre.data.source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.com.bytecode.opencsv.CSVWriter;
import cre.data.CRTable;
import cre.data.CRType;
import cre.data.PubType;
import cre.ui.StatusBar;

public class CRE_csv {

	private static String[] csvColumnsCR = new String[] {"CRID", "CR", "AU", "AU_F", "AU_L", "AU_A", "TI", "J", "J_N", "J_S", "N_CR", "RPY", "PAG", "VOL", "DOI", "PERC_YR", "PERC_ALL"};
	private static String[] csvColumnsPub = new String[] {"PUBID", "PT", "AU", "AF", "C1", "EM", "AA", "TI", "PY", "SO", "VL", "IS", "AR", "BP", "EP", "PG", "TC", "DI", "LI", "AB", "DE", "DT", "FS", "UT"}; 
	
	private static HashMap<String, String> getCRExport (CRType cr) {
		
		HashMap<String, String> result = new HashMap<String, String>();
		result.put ("CRID", 							 String.valueOf(cr.ID));
		result.put ("CR", 		(cr.CR==null)?""		:cr.CR);
		result.put ("AU", 		(cr.AU==null)?""		:cr.AU);
		result.put ("AU_F", 	(cr.AU_F==null)?""		:cr.AU_F);
		result.put ("AU_L", 	(cr.AU_L==null)?""		:cr.AU_L);
		result.put ("AU_A", 	(cr.AU_A==null)?""		:cr.AU_A);
		result.put ("TI", 		(cr.TI==null)?""		:cr.TI);
		result.put ("J", 		(cr.J==null)?""			:cr.J);
		result.put ("J_N", 		(cr.J_N==null)?""		:cr.J_N);
		result.put ("J_S", 		(cr.J_S==null)?""		:cr.J_S);
		result.put ("N_CR", 							 String.valueOf(cr.N_CR));
		result.put ("RPY", 		(cr.RPY==null)?""		:cr.RPY.toString());
		result.put ("PAG", 		(cr.PAG==null)?""		:cr.PAG);
		result.put ("VOL", 		(cr.VOL==null)?""		:cr.VOL);
		result.put ("DOI", 		(cr.DOI==null)?""		:cr.DOI);
		result.put ("PERC_YR", 	(cr.PERC_YR==null)?""	:cr.PERC_YR.toString());
		result.put ("PERC_ALL",	(cr.PERC_ALL==null)?""	:cr.PERC_ALL.toString());
		return result;
	}
	
	
	
	private static HashMap<String, String> getPubExport (PubType pub, int id) {
		
		HashMap<String, String> result = new HashMap<String, String>();
		result.put ("PUBID",							 String.valueOf(id));
		result.put ("PT", 		(pub.PT==null)?""		:pub.PT);
		result.put ("AU", 		(pub.AU==null)?""		:String.join("; ", pub.AU));
		result.put ("AF", 		(pub.AF==null)?""		:String.join("; ", pub.AF));
		result.put ("C1", 		(pub.C1==null)?""		:String.join("; ", pub.C1.stream().map(it -> "["+String.join("; ", it)+"]").collect(Collectors.toList())));
		result.put ("EM", 		(pub.EM==null)?""		:String.join("; ", pub.EM));
		result.put ("AA", 		(pub.AA==null)?""		:String.join("; ", pub.AA));
		result.put ("TI", 		(pub.TI==null)?""		:pub.TI);
		result.put ("PY", 		(pub.PY==null)?""		:pub.PY.toString());
		result.put ("SO", 		(pub.SO==null)?""		:pub.SO);
		result.put ("VL", 		(pub.VL==null)?""		:pub.VL);
		result.put ("IS", 		(pub.IS==null)?""		:pub.IS);
		result.put ("AR", 		(pub.AR==null)?""		:pub.AR);
		result.put ("BP", 		(pub.BP==null)?""		:pub.BP.toString());
		result.put ("EP", 		(pub.EP==null)?""		:pub.EP.toString());
		result.put ("PG", 		(pub.PG==null)?""	 	:pub.PG.toString());
		result.put ("TC", 		(pub.TC==null)?"0"		:pub.TC.toString());
		result.put ("DI", 		(pub.DI==null)?""		:pub.DI);
		result.put ("LI", 		(pub.LI==null)?""		:pub.LI);
		result.put ("AB", 		(pub.AB==null)?""		:pub.AB);
		result.put ("DE", 		(pub.DE==null)?""		:pub.DE);
		result.put ("DT", 		(pub.DT==null)?""		:pub.DT);
		result.put ("FS", 		(pub.FS==null)?""		:pub.FS);
		result.put ("UT", 		(pub.UT==null)?""		:pub.UT);
		return result;
	}
	
	
	
	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	public static void saveCR (File file, CRTable crTab, StatusBar stat) throws IOException {

		stat.initProgressbar(crTab.crData.size(), "Saving CSV file (Cited References) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(csvColumnsCR); 
		
		int count = 0;
		for (CRType cr : crTab.crData) {
			stat.updateProgressbar(++count);
			HashMap<String, String> exportCR = getCRExport(cr);
			csv.writeNext(Arrays.stream(csvColumnsCR).map (attr -> exportCR.get(attr)).toArray(String[]::new)); 
		}
		
		csv.close();
		stat.setValue("Saving CSV file (Cited References) done", crTab.getInfoString());
	}

	
	public static void savePub (File file, CRTable crTab, StatusBar stat) throws IOException {

		stat.initProgressbar(crTab.pubData.size(), "Saving CSV file (Citing Publications) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(csvColumnsPub); 
		
		int count = 0;
		for (PubType pub: crTab.pubData) {
			stat.updateProgressbar(++count);
			HashMap<String, String> exportPub = getPubExport(pub, count);
			csv.writeNext(Arrays.stream(csvColumnsPub).map (attr -> exportPub.get(attr)).toArray(String[]::new)); 
		}
		
		csv.close();
		stat.setValue("Saving CSV file (Citing Publications) done", crTab.getInfoString());
	}
	
	
	
	public static void saveCRPub (File file, CRTable crTab, StatusBar stat) throws IOException {

		stat.initProgressbar(crTab.pubData.size(), "Saving CSV file (Cited References + Citing Publications) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext (Stream.concat (Arrays.stream(csvColumnsPub),	Arrays.stream(csvColumnsCR)).toArray(String[]::new)); 
		
		int count = 0;
		for (PubType pub: crTab.pubData) {
			stat.updateProgressbar(++count);
			HashMap<String, String> exportPub = getPubExport(pub, count);
			
			for (CRType cr: pub.crList) {
				HashMap<String, String> exportCR = getCRExport(cr);
			
				csv.writeNext (Stream.concat (
						Arrays.stream(csvColumnsPub).map (attr -> exportPub.get(attr)),
						Arrays.stream(csvColumnsCR).map (attr -> exportCR.get(attr))
				).toArray(String[]::new)); 
			}
		}
		
		csv.close();
		stat.setValue("Saving CSV file (Cited References + Citing Publications) done", crTab.getInfoString());
	}

	
	
	/**
	 * Save Graph data to CSV file
	 * @param file
	 * @throws IOException 
	 */
	
	public static void saveGraph (File file, CRTable crTab, StatusBar stat) throws IOException {

		stat.setValue("Saving Graph as CSV file ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(new String[] {"Year", "NCR", String.format("Median-%d", 2*crTab.getMedianRange()+1)});
		
		crTab.getChartData().forEach ( (Integer year, int[] values) -> {
			csv.writeNext (new String[] {year.toString(), String.valueOf(values[0]), String.valueOf(values[1])});
		});
				
		csv.close();
		stat.setValue("Saving Graph as CSV file done", crTab.getInfoString());
	}
	
}


	
/*	
	// DEPRECATED
	public static void saveRuediger (File file, CRTable crTab, StatusBar stat) {
		
		stat.initProgressbar(crTab.pubData.size(), "Saving CSV file ...");
		
		// add csv extension if necessary
		String file_name = file.toString()
		if (!file_name.endsWith(".csv")) file_name += ".csv"
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		
		List<String> csvColumns = CRType.attr.collect{ it.key } - ["N_CR", "CID2", "CID_S"]	// ignore N_CR and cluster information
		csv.writeNext(csvColumns + ["PY"] as String[]) 
		
		
		crTab.pubData.eachWithIndex { PubType pub, int pubIdx ->
			stat.updateProgressbar(pubIdx+1);
			
			pub.crList.eachWithIndex  { CRType it, int idx ->
			
				List<String> csvValues = csvColumns.collect { name -> it[name] as String}
				csvValues << (pub.PY as String)
				csv.writeNext (csvValues as String[])
			}
		}
		csv.close()
		
		stat.setValue("Saving CSV file done", crTab.getInfoString())
	}
		
		
*/	
	

		
	/**
	 * DEPRECATED
	 * Load CR table from CSV file
	 * @param file
	 */
	
	/*
	public static void load (File file, CRTable crTab, StatusBar stat) throws AbortedException {
		
		crTab.abort = false	// can be changed by "wait dialog"
		
		String d = "${new Date()}: "
		stat.setValue(d + "Loading CSV file ...", "")

		crTab.init()
		
		Map<String, Integer> attrPos = [:]

		long fileSize = file.length()
		long fileSizeRead = 0
		
		CSVReader csv = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))
		String[] line = csv.readNext()
		
		if (line != null) {

			line.each { String it -> fileSizeRead += it.length()+3 }
			line.eachWithIndex { String a, int i -> attrPos[a.toUpperCase()] = i }
//			println attrPos
			while ((line = csv.readNext()) != null) {

				if (crTab.abort) {
					crTab.init()
					crTab.updateData(true)
					stat.setValue("${new Date()}: Loading CSV file aborted", crTab.getInfoString())
					crTab.abort = false
					throw new AbortedException()
				}
				
				
				line.each { String it -> fileSizeRead += it.length()+3 }
				stat.setValue ("Loading CSV file ...", (long) (fileSizeRead*100.0/fileSize).intValue())
				
									
				CRType cr = new CRType()
				CRType.attr.each {
					if (attrPos[it.key] != null) {
						String v = line[attrPos[it.key]]
						cr[it.key] = ["ID", "N_CR", "RPY"].contains(it.key) ? v.toInteger() : ( ["PERC_YR", "PERC_ALL"].contains(it.key) ? v.toDouble() : v)
					}
				}
				cr.VI = 1	// visible
				cr.CO = 0	// default color
				cr.CID2 = new cre.data.CRCluster(cr.ID, new Integer(1))
				cr.CID_S = 1

				["_SAMEAS":2, "_DIFFERENTTO":-2].each { String k, int v ->
					if (line[attrPos[k]] != "") {
						line[attrPos[k]].split(",").each { String it ->
							crTab.setMapping(line[attrPos["ID"]].toInteger(), it.toInteger(), v as Double, true, false)
						}
					}
				}

				crTab.crData << cr
			}
		}
		
		crTab.updateData(true)
		stat.setValue("Loading CSV file done", crTab.getInfoString())
		
	}
	*/
	


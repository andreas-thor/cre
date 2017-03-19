package cre.test.data.source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVWriter;

import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import cre.test.ui.CRTableView.CRColumn;
import cre.test.ui.StatusBar;

public class CRE_csv {

	private static String[] csvColumnsCR = new String[] {"CRID", "CR", "AU", "AU_F", "AU_L", "AU_A", "TI", "J", "J_N", "J_S", "N_CR", "RPY", "PAG", "VOL", "DOI", "PERC_YR", "PERC_ALL"};
	private static String[] csvColumnsPub = new String[] {"PUBID", "PT", "AU", "AF", "C1", "EM", "AA", "TI", "PY", "SO", "VL", "IS", "AR", "BP", "EP", "PG", "TC", "DI", "LI", "AB", "DE", "DT", "FS", "UT"}; 
	
	
	
	private static int pubId;
	
	private static HashMap<String, String> getCRExport (CRType cr) {
		
		HashMap<String, String> result = new HashMap<String, String>();
		result.put ("CRID", 							 String.valueOf(cr.getID()));
		result.put ("CR", 		(cr.getCR()==null)?""		:cr.getCR());
		result.put ("AU", 		(cr.getAU()==null)?""		:cr.getAU());
		result.put ("AU_F", 	(cr.getAU_F()==null)?""		:cr.getAU_F());
		result.put ("AU_L", 	(cr.getAU_L()==null)?""		:cr.getAU_L());
		result.put ("AU_A", 	(cr.getAU_A()==null)?""		:cr.getAU_A());
		result.put ("TI", 		(cr.getTI()==null)?""		:cr.getTI());
		result.put ("J", 		(cr.getJ()==null)?""			:cr.getJ());
		result.put ("J_N", 		(cr.getJ_N()==null)?""		:cr.getJ_N());
		result.put ("J_S", 		(cr.getJ_S()==null)?""		:cr.getJ_S());
		result.put ("N_CR", 							 String.valueOf(cr.getN_CR()));
		result.put ("RPY", 		(cr.getRPY()==null)?""		:cr.getRPY().toString());
		result.put ("PAG", 		(cr.getPAG()==null)?""		:cr.getPAG());
		result.put ("VOL", 		(cr.getVOL()==null)?""		:cr.getVOL());
		result.put ("DOI", 		(cr.getDOI()==null)?""		:cr.getDOI());
		result.put ("PERC_YR", 	(cr.getPERC_YR()==null)?""	:cr.getPERC_YR().toString());
		result.put ("PERC_ALL",	(cr.getPERC_ALL()==null)?""	:cr.getPERC_ALL().toString());
		return result;
	}
	
	
	
	private static HashMap<String, String> getPubExport (PubType pub) {
		
		HashMap<String, String> result = new HashMap<String, String>();
		result.put ("PUBID",							 String.valueOf(++pubId));
		result.put ("PT", 		(pub.getPT()==null)?""		:pub.getPT());
		result.put ("AU", 		(pub.getAU()==null)?""		:String.join("; ", pub.getAU()));
		result.put ("AF", 		(pub.getAF()==null)?""		:String.join("; ", pub.getAF()));
		result.put ("C1", 		(pub.getC1()==null)?""		:String.join("; ", pub.getC1().stream().map(it -> "["+String.join("; ", it)+"]").collect(Collectors.toList())));
		result.put ("EM", 		(pub.getEM()==null)?""		:String.join("; ", pub.getEM()));
		result.put ("AA", 		(pub.getAA()==null)?""		:String.join("; ", pub.getAA()));
		result.put ("TI", 		(pub.getTI()==null)?""		:pub.getTI());
		result.put ("PY", 		(pub.getPY()==null)?""		:pub.getPY().toString());
		result.put ("SO", 		(pub.getSO()==null)?""		:pub.getSO());
		result.put ("VL", 		(pub.getVL()==null)?""		:pub.getVL());
		result.put ("IS", 		(pub.getIS()==null)?""		:pub.getIS());
		result.put ("AR", 		(pub.getAR()==null)?""		:pub.getAR());
		result.put ("BP", 		(pub.getBP()==null)?""		:pub.getBP().toString());
		result.put ("EP", 		(pub.getEP()==null)?""		:pub.getEP().toString());
		result.put ("PG", 		(pub.getPG()==null)?""	 	:pub.getPG().toString());
		result.put ("TC", 		(pub.getTC()==null)?"0"		:pub.getTC().toString());
		result.put ("DI", 		(pub.getDI()==null)?""		:pub.getDI());
		result.put ("LI", 		(pub.getLI()==null)?""		:pub.getLI());
		result.put ("AB", 		(pub.getAB()==null)?""		:pub.getAB());
		result.put ("DE", 		(pub.getDE()==null)?""		:pub.getDE());
		result.put ("DT", 		(pub.getDT()==null)?""		:pub.getDT());
		result.put ("FS", 		(pub.getFS()==null)?""		:pub.getFS());
		result.put ("UT", 		(pub.getUT()==null)?""		:pub.getUT());
		return result;
	}
	
	
	
	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	public static void saveCR (File file, CRTable crTab) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSize(), "Saving CSV file (Cited References) ...");
		
		
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));

		csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.id).toArray(String[]::new)); 
//		csv.writeNext(csvColumnsCR); 
		
		crTab.getCR().forEach(cr -> {
			StatusBar.get().incProgressbar();
			HashMap<String, String> exportCR = getCRExport(cr);
			
			csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr).getValue()).map(val -> val==null ? "" : String.valueOf(val)).toArray(String[]::new)); 

			
//			csv.writeNext(Arrays.stream(csvColumnsCR).map (attr -> exportCR.get(attr)).toArray(String[]::new)); 
		});
		
		csv.close();
		StatusBar.get().setValue("Saving CSV file (Cited References) done");
	}

	
	public static void savePub (File file, CRTable crTab) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSizePub(), "Saving CSV file (Citing Publications) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(csvColumnsPub); 
		
		pubId = 0;
		crTab.getPub().forEach(pub -> {
			StatusBar.get().incProgressbar();
			HashMap<String, String> exportPub = getPubExport(pub);
			csv.writeNext(Arrays.stream(csvColumnsPub).map (attr -> exportPub.get(attr)).toArray(String[]::new)); 
		});
		
		csv.close();
		StatusBar.get().setValue("Saving CSV file (Citing Publications) done");
	}
	
	
	
	public static void saveCRPub (File file, CRTable crTab) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSizePub(), "Saving CSV file (Cited References + Citing Publications) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext (Stream.concat (Arrays.stream(csvColumnsPub),	Arrays.stream(csvColumnsCR)).toArray(String[]::new)); 
		
		pubId = 0;
		crTab.getPub().forEach(pub -> {
			StatusBar.get().incProgressbar();
			HashMap<String, String> exportPub = getPubExport(pub);
			
			pub.getCR().forEach(cr -> {
				HashMap<String, String> exportCR = getCRExport(cr);
			
				csv.writeNext (Stream.concat (
						Arrays.stream(csvColumnsPub).map (attr -> exportPub.get(attr)),
						Arrays.stream(csvColumnsCR).map (attr -> exportCR.get(attr))
				).toArray(String[]::new)); 
			});
		});
		
		csv.close();
		StatusBar.get().setValue("Saving CSV file (Cited References + Citing Publications) done");
	}

	
	
	/**
	 * Save Graph data to CSV file
	 * @param file
	 * @throws IOException 
	 */
	
	public static void saveGraph (File file, CRTable crTab) throws IOException {

		StatusBar.get().setValue("Saving Graph as CSV file ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(new String[] {"Year", "NCR", String.format("Median-%d", 2*UserSettings.get().getMedianRange()+1)});
		
		int[][] data = crTab.getChartData();
		for (int i=0; i<data[0].length; i++) {
			csv.writeNext (new String[] {String.valueOf(data[0][i]), String.valueOf(data[1][i]), String.valueOf(data[2][i])});
		}
				
		csv.close();
		StatusBar.get().setValue("Saving Graph as CSV file done");
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
	


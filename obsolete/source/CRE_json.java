package main.cre.data.source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import main.cre.Exceptions.AbortedException;
import main.cre.Exceptions.FileTooLargeException;
import main.cre.Exceptions.UnsupportedFileFormatException;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.CRType.FORMATTYPE;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.Clustering_MM;
import main.cre.data.type.mm.PubType_MM;
import main.cre.ui.statusbar.StatusBar;


public class CRE_json {

	/**
	 * if loadMutlipleFiles == TRUE: 
	 * 	load is executed multiple times (i.e., multiple CRE files are loaded and unified)
	 *  Ids (both, for CRs and Pubs) are re-adjusted
	 * @param file
	 * @param loadMutlipleFiles	true, if  -->
	 * @throws OutOfMemoryError
	 * @throws Exception 
	 */
	public static void load (File file, boolean loadMutlipleFiles) throws OutOfMemoryError, Exception {
		
		ZipEntry entry = null;
		ZipFile zipFile = new ZipFile(file);
		
		Map<Integer, CRType_MM> mapId2CR;
	
		entry = zipFile.getEntry("crdata.json");
		if (entry != null) {
			StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file crdata ...");
			mapId2CR = loadCRData(zipFile.getInputStream(entry), loadMutlipleFiles);
			StatusBar.get().setValue("Loading CRE file crdata done");
		} else {
			zipFile.close();
			throw new Exception ("Could not find crdata.");
		}
		
		
		entry = zipFile.getEntry("pubdata.json");
		if (entry != null) {
			StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file pubdata ...");
			loadPubData(zipFile.getInputStream(entry), loadMutlipleFiles, mapId2CR);
			StatusBar.get().setValue("Loading CRE file pubdata done");
		} else {
			zipFile.close();
			throw new Exception ("Could not find pubdata.");
		}			
		
		
		entry = zipFile.getEntry("crmatch.json");
		if (entry != null) {
			StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file crmatch ...");
			loadCRMatchData(zipFile.getInputStream(entry), mapId2CR);
			StatusBar.get().setValue("Loading CRE file crmatch done");
		}
			
		zipFile.close();
	}


	/**
	 * 
	 * @param in
	 * @param checkForDuplicates
	 * @return Map: internal CRID => CR
	 * @throws UnsupportedFileFormatException
	 * @throws FileTooLargeException
	 * @throws AbortedException
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	private static Map<Integer, CRType_MM> loadCRData (InputStream in, boolean checkForDuplicates) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {
		
		Map<Integer, CRType_MM> result = new HashMap<Integer, CRType_MM>();
		
		CRTable crTab = CRTable.get(); 
		JsonParser parser = Json.createParser(in);
		CRType_MM cr = null;
		String key = "";
		while (!crTab.isAborted() && parser.hasNext()) {
			
			
			switch (parser.next()) {
			case START_OBJECT: 	cr = new CRType_MM(); break; 
			case END_OBJECT: 	
				int internalId = cr.getID();
				cr = (CRType_MM) crTab.addCR(cr, checkForDuplicates);
				cr.setCID2(cr);
				result.put(internalId, cr);  
				break;
			case KEY_NAME:		key = parser.getString(); break;
			case VALUE_STRING: 
				switch (key) {
				case "CR": 		cr.setCR(parser.getString()); break;
				case "AU": 		cr.setAU(parser.getString()); break;
				case "AU_F": 	cr.setAU_F(parser.getString()); break;
				case "AU_L": 	cr.setAU_L(parser.getString()); break;
				case "AU_A": 	cr.setAU_A(parser.getString()); break;
				case "TI": 		cr.setTI(parser.getString()); break;
				case "J": 		cr.setJ(parser.getString()); break;
				case "J_N": 	cr.setJ_N(parser.getString()); break;
				case "J_S": 	cr.setJ_S(parser.getString()); break;
				case "PAG": 	cr.setPAG(parser.getString()); break;
				case "VOL": 	cr.setVOL(parser.getString()); break;
				case "DOI": 	cr.setDOI(parser.getString()); break;
				case "CID2": 	cr.setCID2(parser.getString()); break;
				default: System.out.println("CRDATA.json >> Unknow Key with String Value: " + key); 
				}
				break;
			case VALUE_NUMBER:
				switch (key) {
				case "ID": 		cr.setID(parser.getInt()); break;
				case "N_CR": 	/*cr.setN_CR(parser.getInt());*/ break;
				case "RPY": 	cr.setRPY(parser.getInt()); break;
				case "CID_S": 	/*cr.setCID_S(parser.getInt()); */ break;
				case "VI": 		cr.setVI(parser.getInt()==1); break;
				case "CO": 		cr.setCO(parser.getInt()); break;
				case "type": 	cr.setFormatType (FORMATTYPE.values()[parser.getInt()-1]); break;	// LEGACY: Type were enumerated 1,2,...
				default: System.out.println("CRDATA.json >> Unknow Key with Number Value: " + key); 
				}
				break;
			default:break;  
			}
		
			StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
		}		
		
		return result;
	}

	
	private static void loadPubData (InputStream in, boolean checkForDuplicates, Map<Integer, CRType_MM> mapId2CR) {
		
		CRTable crTab = CRTable.get(); 

		JsonParser parser = Json.createParser(in);
		PubType_MM pub = null;
		List<String> C1List = null;
		List<CRType_MM> CRList = null; 
		int arrayLevel = 0;
		String key = "";
		while (!crTab.isAborted() && parser.hasNext()) {
			
			switch (parser.next()) {
			case START_OBJECT:
				pub = new PubType_MM();
				CRList = new ArrayList<CRType_MM>();
				break; 
			case END_OBJECT: 	
				pub = crTab.addPub(pub, false, checkForDuplicates);
				for (CRType cr: CRList) {
					pub.addCR(cr, true);
				}
				break;
			case KEY_NAME:		
				key = parser.getString(); 
				break;
			case START_ARRAY:	
				arrayLevel++;
				switch (arrayLevel) {
				case 2:
					switch (key) {
					case "AU":	break;
					case "AF":	break;
					case "C1":	break;	
					case "EM":	break;
					case "AA":	break;
//						case "CRLISTID":	pub.crList = new ArrayList<CRType>(); break;
					}
					break;
				case 3:			C1List = new ArrayList<String>(); break;
				}
				break;
			case END_ARRAY: 
				if ((arrayLevel == 3) && (C1List != null)) pub.addC1((String[]) C1List.toArray(new String[C1List.size()])); 
				arrayLevel--;
				break;
			case VALUE_STRING: 
				switch (arrayLevel) {
				case 1:
					switch (key) {
					case "PT": 	pub.setPT(parser.getString()); break;
					case "TI": 	pub.setTI(parser.getString()); break;
					case "SO": 	pub.setSO(parser.getString()); break;
					case "VL": 	pub.setVL(parser.getString()); break;
					case "IS": 	pub.setIS(parser.getString()); break;
					case "AR": 	pub.setAR(parser.getString()); break;
					case "DI": 	pub.setDI(parser.getString()); break;
					case "LI": 	pub.setLI(parser.getString()); break;
					case "AB": 	pub.setAB(parser.getString()); break;
					case "DE": 	pub.setDE(parser.getString()); break;
					case "DT": 	pub.setDT(parser.getString()); break;
					case "FS": 	pub.setFS(parser.getString()); break;
					case "UT": 	pub.setUT(parser.getString()); break;
					default: System.out.println("PUBDATA.json >> Unknow Key with String Value: " + key); 
					}
					break;
					
				case 2:
					switch (key) {
					case "AU":	pub.addAU(parser.getString()); break;
					case "AF":	pub.addAF(parser.getString()); break;
					case "EM":	pub.addEM(parser.getString()); break;
					case "AA":	pub.addAA(parser.getString()); break;
					default: System.out.println("PUBDATA.json >> Unknow Key with String Value: " + key); 
					}
					break;
				case 3: 
					C1List.add(parser.getString()); break;
				}
				break;
			case VALUE_NUMBER:
				switch (key) {
				case "PY": 	pub.setPY(parser.getInt()); break;
				case "BP": 	pub.setBP(parser.getInt()); break;
				case "EP": 	pub.setEP(parser.getInt()); break;
				case "PG": 	pub.setPG(parser.getInt()); break;
				case "TC": 	pub.setTC(parser.getInt()); break;
				case "CRLISTID":	
					CRList.add(mapId2CR.get(parser.getInt()));	//  --> das wird erst nach AddPub gemacht: pub.addCR(mapId2CR.get(parser.getInt()), true); 
					break;
				// local mapping: case "CRLISTID":	crTab.crData.get(crTab.crMatch.crId2Index.get(parser.getInt()))); break;
				default: System.out.println("PUBDATA.json >> Unknow Key with Number Value: " + key); 
				}
				break;
			default:
				System.out.println("DEFAULT");

				break;
			}
			StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
		}		
	}
	
	
	
	private static void loadCRMatchData (InputStream in, Map<Integer, CRType_MM> mapId2CR) {

		CRTable crTab = CRTable.get(); 
		JsonParser parser = Json.createParser(in);
		
		boolean isManual = false;
		int id1 = 0, id2 = 0;
		int level = 0;
		
		while (!crTab.isAborted() && parser.hasNext()) {
			switch (parser.next()) {
			case START_OBJECT: 	level++; break; 
			case END_OBJECT: 	level--; break;
			case KEY_NAME:		
				String key = parser.getString();
				switch (level) {
				case 1: 
					if (key.equalsIgnoreCase("MATCH_AUTO")) isManual = false; 
					if (key.equalsIgnoreCase("MATCH_MANU")) isManual = true;
					break;
				case 2: id1 = Integer.valueOf(key); break;
				case 3: id2 = Integer.valueOf(key); break;
				}
				break;
			case VALUE_NUMBER:
				CRType cr1 = mapId2CR.get(id1);
				CRType cr2 = mapId2CR.get(id2);
				if (!(cr1==null) && !(cr2==null)) {
					Clustering_MM.get().addPair(cr1, cr2, parser.getBigDecimal().doubleValue(), isManual, false, null);
				}
				break;
			default:break;  
			}
			StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
		}
		
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
		 
		/* TODO: filter is not supported yet */
		
		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfCRs() + CRTable.get().getStatistics().getNumberOfPubs() + CRTable.get().getClustering().getNumberOfMatches(true) + CRTable.get().getClustering().getNumberOfMatches(false));
		
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file_name), Charset.forName("UTF-8"));
		
		zip.putNextEntry(new ZipEntry("crdata.json"));
		saveCRData(Json.createGenerator(zip));
		zip.closeEntry();
	
		zip.putNextEntry(new ZipEntry("pubdata.json"));
		savePubData(Json.createGenerator(zip), includePubsWithoutCRs);
		zip.closeEntry();
		 
		zip.putNextEntry(new ZipEntry("crmatch.json"));
		saveCRMatchData(Json.createGenerator(zip));
		zip.closeEntry();
		
		zip.flush();
		zip.close();
	}


	private static void saveCRData (JsonGenerator jgenCR) throws IOException {
		
		jgenCR.writeStartArray();

		CRTable.get().getCR().forEach(cr -> {	
			jgenCR.writeStartObject();
									jgenCR.write("ID", cr.getID());
			if (cr.getCR()!=null) 	jgenCR.write("CR", cr.getCR());
			if (cr.getAU()!=null) 	jgenCR.write("AU", cr.getAU());
			if (cr.getAU_F()!=null) jgenCR.write("AU_F", cr.getAU_F());
			if (cr.getAU_L()!=null) jgenCR.write("AU_L", cr.getAU_L());
			if (cr.getAU_A()!=null) jgenCR.write("AU_A", cr.getAU_A());
			if (cr.getTI()!=null) 	jgenCR.write("TI", cr.getTI());
			if (cr.getJ()!=null) 	jgenCR.write("J", cr.getJ());
			if (cr.getJ_N()!=null) 	jgenCR.write("J_N", cr.getJ_N());
			if (cr.getJ_S()!=null) 	jgenCR.write("J_S", cr.getJ_S());
									jgenCR.write("N_CR", cr.getN_CR());
			if (cr.getRPY()!=null)	jgenCR.write("RPY", cr.getRPY());
			if (cr.getPAG()!=null) 	jgenCR.write("PAG", cr.getPAG());
			if (cr.getVOL()!=null) 	jgenCR.write("VOL", cr.getVOL());
			if (cr.getDOI()!=null) 	jgenCR.write("DOI", cr.getDOI());
			if (cr.getClusterId()!=null) jgenCR.write("CID2", cr.getClusterId());
			 						jgenCR.write("CID_S", cr.getClusterSize());
									jgenCR.write("VI", cr.getVI()?1:0);
									jgenCR.write("CO", cr.getCO());
									jgenCR.write("type", cr.getFormatType().ordinal()+1);	// LEGACY: Types were encoded 1,2, ...			
			jgenCR.writeEnd();
			
			StatusBar.get().incProgressbar();
		});
		jgenCR.writeEnd();
		jgenCR.flush();
	}
	
	private static void savePubData (JsonGenerator jgenPub, boolean includePubsWithoutCRs) throws IOException {

		jgenPub.writeStartArray();
		CRTable.get().getPub(includePubsWithoutCRs).forEach(pub -> {
			jgenPub.writeStartObject();
			
			if (pub.getPT()!=null) 	jgenPub.write("PT", pub.getPT());
			if (pub.getAU().count()>0) {	jgenPub.writeStartArray("AU"); pub.getAU().forEach(x -> jgenPub.write(x)); jgenPub.writeEnd(); }
			if (pub.getAF().count()>0) {	jgenPub.writeStartArray("AF"); pub.getAF().forEach(x -> jgenPub.write(x)); jgenPub.writeEnd(); }
			if (pub.getC1().count()>0) {
				jgenPub.writeStartArray("C1");
				pub.getC1().forEach(y -> { 
					jgenPub.writeStartArray();
					for (String x:y) jgenPub.write(x); 
					jgenPub.writeEnd();
				});
				jgenPub.writeEnd();
			}
			if (pub.getEM().count()>0) {	jgenPub.writeStartArray("EM"); pub.getEM().forEach(x -> jgenPub.write(x)); jgenPub.writeEnd(); }
			if (pub.getAA().count()>0) {	jgenPub.writeStartArray("AA"); pub.getAA().forEach(x -> jgenPub.write(x)); jgenPub.writeEnd(); }
			if (pub.getTI()!=null) 	jgenPub.write("TI", pub.getTI());
			if (pub.getPY()!=null) 	jgenPub.write("PY", pub.getPY());
			if (pub.getSO()!=null) 	jgenPub.write("SO", pub.getSO());
			if (pub.getVL()!=null) 	jgenPub.write("VL", pub.getVL());
			if (pub.getIS()!=null) 	jgenPub.write("IS", pub.getIS());
			if (pub.getAR()!=null) 	jgenPub.write("AR", pub.getAR());
			if (pub.getBP()!=null) 	jgenPub.write("BP", pub.getBP());
			if (pub.getEP()!=null) 	jgenPub.write("EP", pub.getEP());
			if (pub.getPG()!=null) 	jgenPub.write("PG", pub.getPG());
			if (pub.getTC()!=null) 	jgenPub.write("TC", pub.getTC());
			
			jgenPub.writeStartArray("CRLISTID"); 
			pub.getCR().forEach(cr -> { jgenPub.write(cr.getID()); }); 
			jgenPub.writeEnd(); 
			
			if (pub.getDI()!=null) 	jgenPub.write("DI", pub.getDI());
			if (pub.getLI()!=null) 	jgenPub.write("LI", pub.getLI());
			if (pub.getAB()!=null) 	jgenPub.write("AB", pub.getAB());
			if (pub.getDE()!=null) 	jgenPub.write("DE", pub.getDE());
			if (pub.getDT()!=null) 	jgenPub.write("DT", pub.getDT());
			if (pub.getFS()!=null) 	jgenPub.write("FS", pub.getFS());
			if (pub.getUT()!=null) 	jgenPub.write("UT", pub.getUT());
			
			jgenPub.writeEnd();
			
			StatusBar.get().incProgressbar();
		});
		jgenPub.writeEnd();
		jgenPub.flush();		
	}
	

	private static void saveCRMatchData (JsonGenerator jgenMatch) throws IOException {

		/**
		 * {"MATCH_MANU":{"A1":{"B1":sim1,"C1":sim2,...},"A2":{"B2":sim3,...}, "MATCH_AUTO":{"A3":{...
		 */
		
		jgenMatch.writeStartObject();
		for (boolean loop: new boolean[] { false, true }) {
			jgenMatch.writeStartObject(loop?"MATCH_MANU":"MATCH_AUTO");
//			for (Entry<Integer, Map<Integer, Double>> it: crTab.crMatch.match.get(loop).entrySet()) {
			Clustering_MM.get().matchResult.get(loop).forEach((cr1, pairs) -> {
				if (pairs.size()>0) {
					jgenMatch.writeStartObject(String.valueOf(cr1.getID()));
					pairs.forEach((cr2, sim) -> {
						jgenMatch.write(String.valueOf(cr2.getID()), sim);
					});
					jgenMatch.writeEnd();
				}
				
				StatusBar.get().incProgressbar();
			});
			jgenMatch.writeEnd();
		}
		jgenMatch.writeEnd();
		jgenMatch.flush();		
	}
	
		
	
}

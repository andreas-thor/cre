package cre.test.data.source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import cre.test.Exceptions.AbortedException;
import cre.test.data.CRCluster;
import cre.test.data.CRTable;
import cre.test.data.CRType;
import cre.test.data.PubType;
import cre.test.ui.StatusBar;

public class CRE_json {

	
	public static void load (File file, CRTable crTab) throws IOException, AbortedException {
		
		
		StatusBar.get().setValue("Loading CRE file ...");
		
		
		crTab.init();
		
//		ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
		ZipEntry entry = null;

		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		
		Map<Integer, CRType> mapId2CR = new HashMap<Integer, CRType>();
		
		
		JsonParser parser;
//		while ( (entry = zip.getNextEntry()) != null ) {
		while ( zipEntries.hasMoreElements()) {
 
			entry = zipEntries.nextElement();
			if (entry.getName().equals("crdata.json")) {

				System.out.println("entry size is " + entry.getSize());
				StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file crdata ...");
				parser = Json.createParser(zipFile.getInputStream(entry) /*zip*/);
				CRType cr = null;
				String key = "";
				while (!crTab.isAborted() && parser.hasNext()) {
					
					
					switch (parser.next()) {
					case START_OBJECT: 	cr = new CRType(); break; 
					case END_OBJECT: 	crTab.add(cr); break;
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
						case "CID2": 	cr.setCID2(new CRCluster(parser.getString())); break;
						default: System.out.println("CRDATA.json >> Unknow Key with String Value: " + key); 
						}
						break;
					case VALUE_NUMBER:
						switch (key) {
						case "ID": 		cr.setID(parser.getInt()); mapId2CR.put(cr.getID(), cr); break;
						case "N_CR": 	cr.setN_CR(parser.getInt()); break;
						case "RPY": 	cr.setRPY(parser.getInt()); break;
						case "CID_S": 	cr.setCID_S(parser.getInt()); break;
						case "VI": 		cr.setVI(parser.getInt()==1); break;
						case "CO": 		cr.setCO(parser.getInt()); break;
						case "type": 	cr.type = (byte) parser.getInt(); break;
						default: System.out.println("CRDATA.json >> Unknow Key with Number Value: " + key); 
						}
						break;
					default:break;  
					}
				
					StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
				}
//				crTab.updateData(true);
				StatusBar.get().setValue("Loading CRE file crdata done");
			}
			
			if (entry.getName().equals("pubdata.json")) {

				StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file pubdata ...");
				parser = Json.createParser(zipFile.getInputStream(entry) /*zip*/);
				PubType pub = null;
				List<String> C1List = null;
				int arrayLevel = 0;
				String key = "";
				while (!crTab.isAborted() && parser.hasNext()) {
					
					switch (parser.next()) {
					case START_OBJECT: 	pub = new PubType(); break; 
					case END_OBJECT: 	crTab.addPub(pub); break;
					case KEY_NAME:		key = parser.getString(); break;
					case START_ARRAY:	
						arrayLevel++;
						switch (arrayLevel) {
						case 2:
							switch (key) {
							case "AU":	pub.AU = new ArrayList<String>(); break;
							case "AF":	pub.AF = new ArrayList<String>(); break;
							case "C1":	pub.C1 = new ArrayList<String[]>(); break;	
							case "EM":	pub.EM = new ArrayList<String>(); break;
							case "AA":	pub.AA = new ArrayList<String>(); break;
//							case "CRLISTID":	pub.crList = new ArrayList<CRType>(); break;
							}
							break;
						case 3:			C1List = new ArrayList<String>(); break;
						}
						break;
					case END_ARRAY: 	
						if ((arrayLevel == 3) && (C1List != null)) pub.C1.add((String[]) C1List.toArray(new String[C1List.size()])); 
						arrayLevel--;
						break;
					case VALUE_STRING: 
						switch (arrayLevel) {
						case 2:
							switch (key) {
							case "PT": 	pub.PT = parser.getString(); break;
							case "AU":	pub.AU.add(parser.getString()); break;
							case "AF":	pub.AF.add(parser.getString()); break;
							case "EM":	pub.EM.add(parser.getString()); break;
							case "AA":	pub.AA.add(parser.getString()); break;
							case "TI": 	pub.TI = parser.getString(); break;
							case "SO": 	pub.SO = parser.getString(); break;
							case "VL": 	pub.VL = parser.getString(); break;
							case "IS": 	pub.IS = parser.getString(); break;
							case "AR": 	pub.AR = parser.getString(); break;
							case "DI": 	pub.DI = parser.getString(); break;
							case "LI": 	pub.LI = parser.getString(); break;
							case "AB": 	pub.AB = parser.getString(); break;
							case "DE": 	pub.DE = parser.getString(); break;
							case "DT": 	pub.DT = parser.getString(); break;
							case "FS": 	pub.FS = parser.getString(); break;
							case "UT": 	pub.UT = parser.getString(); break;
							default: System.out.println("PUBDATA.json >> Unknow Key with String Value: " + key); 
							}
							break;
						case 3: 
							C1List.add(parser.getString()); break;
						}
						break;
					case VALUE_NUMBER:
						switch (key) {
						case "PY": 	pub.PY = parser.getInt(); break;
						case "BP": 	pub.BP = parser.getInt(); break;
						case "EP": 	pub.EP = parser.getInt(); break;
						case "PG": 	pub.PG = parser.getInt(); break;
						case "TC": 	pub.TC = parser.getInt(); break;
						case "CRLISTID":	pub.addCR(mapId2CR.get(parser.getInt())); break;
						// local mapping: case "CRLISTID":	crTab.crData.get(crTab.crMatch.crId2Index.get(parser.getInt()))); break;
						default: System.out.println("PUBDATA.json >> Unknow Key with Number Value: " + key); 
						}
						break;
					default:
						break;
					}
					StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
				}
				StatusBar.get().setValue("Loading CRE file pubdata done");
			}			
			
			
			
			if (entry.getName().equals("crmatch.json")) {
				int level = 0;
				StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file crmatch ...");
				parser = Json.createParser(zipFile.getInputStream(entry) /*zip*/);
				
				boolean isManual = false;
				int id1 = 0, id2 = 0;
				
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
						crTab.setMapping(id1, id2, parser.getBigDecimal().doubleValue(), isManual, false);
						break;
					default:break;  
					}
					StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
				}
				StatusBar.get().setValue("Loading CRE file crmatch done");
			}
			
		}
		zipFile.close();

		
				
//		JsonSlurper slurper = new JsonSlurper();
//		
//		crTab.crData.addAll (slurper.parseText(data.get("crdata.json").toString()).collect { new CRType().parseJSON ((JSONObject) it) })
//		stat.setValue(d + "Loading CRE file ...", 50, "")
//		
//		HashMap<Integer, Integer> crId2Index = [:]
//		crTab.crData.eachWithIndex { CRType cr, int idx -> crId2Index[cr.ID] = idx }
//		crTab.pubData.addAll (slurper.parseText(data.get("pubdata.json").toString()).collect { new PubType().parseJSON ((JSONObject) it, crTab.crData, crId2Index) })
//		
//		crTab.crMatch.parseJSON(slurper.parseText(data.get("crmatch.json").toString()) as JSONObject)
				
		
		// Check for abort by user
		if (crTab.isAborted()) {
			crTab.init();
			crTab.updateData(false);
			StatusBar.get().setValue("Loading CRE file aborted (due to user request)");
			throw new AbortedException();
		}
		
		crTab.updateData(true);
		StatusBar.get().setValue("Loading CRE file done");
	}



	public static void save (File file, CRTable crTab) throws IOException {
		 
		StatusBar.get().initProgressbar(crTab.getStats().getSize() + crTab.getStats().getSizePub() + crTab.getStats().getSizeMatch(true) + crTab.getStats().getSizeMatch(false), "Saving CRE file ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".cre")) file_name += ".cre";
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file_name), Charset.forName("UTF-8"));
				
		
		zip.putNextEntry(new ZipEntry("crdata.json"));
		JsonGenerator jgenCR = Json.createGenerator(zip);
		jgenCR.writeStartArray();

		crTab.getCR().forEach(cr -> {	
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
			if (cr.getCID2()!=null) jgenCR.write("CID2", cr.getCID2().toString());
									jgenCR.write("CID_S", cr.getCID_S());
									jgenCR.write("VI", cr.getVI()?1:0);
									jgenCR.write("CO", cr.getCO());
									jgenCR.write("type", cr.type);			
			jgenCR.writeEnd();
			
			StatusBar.get().incProgressbar();
		});
		jgenCR.writeEnd();
		jgenCR.flush();
		zip.closeEntry();

		
		zip.putNextEntry(new ZipEntry("pubdata.json"));
		JsonGenerator jgenPub = Json.createGenerator(zip);
		jgenPub.writeStartArray();
		crTab.getPub().forEach(pub -> {
			jgenPub.writeStartObject();
			
			if (pub.PT!=null) 	jgenPub.write("PT", pub.PT);
			if (pub.AU!=null) {	jgenPub.writeStartArray("AU"); for (String x:pub.AU) jgenPub.write(x); jgenPub.writeEnd(); }
			if (pub.AF!=null) {	jgenPub.writeStartArray("AF"); for (String x:pub.AF) jgenPub.write(x); jgenPub.writeEnd(); }
			if (pub.C1!=null) {
				jgenPub.writeStartArray("C1");
				for (String[] y:pub.C1) {
					jgenPub.writeStartArray();
					for (String x:y) jgenPub.write(x); 
					jgenPub.writeEnd();
				}
				jgenPub.writeEnd();
			}
			if (pub.EM!=null) {	jgenPub.writeStartArray("EM"); for (String x:pub.EM) jgenPub.write(x); jgenPub.writeEnd(); }
			if (pub.AA!=null) {	jgenPub.writeStartArray("AA"); for (String x:pub.AA) jgenPub.write(x); jgenPub.writeEnd(); }
			if (pub.TI!=null) 	jgenPub.write("TI", pub.TI);
			if (pub.PY!=null) 	jgenPub.write("PY", pub.PY);
			if (pub.SO!=null) 	jgenPub.write("SO", pub.SO);
			if (pub.VL!=null) 	jgenPub.write("VL", pub.VL);
			if (pub.IS!=null) 	jgenPub.write("IS", pub.IS);
			if (pub.AR!=null) 	jgenPub.write("AR", pub.AR);
			if (pub.BP!=null) 	jgenPub.write("BP", pub.BP);
			if (pub.EP!=null) 	jgenPub.write("EP", pub.EP);
			if (pub.PG!=null) 	jgenPub.write("PG", pub.PG);
			if (pub.TC!=null) 	jgenPub.write("TC", pub.TC);
			
			jgenPub.writeStartArray("CRLISTID"); 
			pub.getCR().forEach(cr -> { jgenPub.write(cr.getID()); }); 
			jgenPub.writeEnd(); 
			
			if (pub.DI!=null) 	jgenPub.write("DI", pub.DI);
			if (pub.LI!=null) 	jgenPub.write("LI", pub.LI);
			if (pub.AB!=null) 	jgenPub.write("AB", pub.AB);
			if (pub.DE!=null) 	jgenPub.write("DE", pub.DE);
			if (pub.DT!=null) 	jgenPub.write("DT", pub.DT);
			if (pub.FS!=null) 	jgenPub.write("FS", pub.FS);
			if (pub.UT!=null) 	jgenPub.write("UT", pub.UT);
			
			jgenPub.writeEnd();
			
			StatusBar.get().incProgressbar();
		});
		jgenPub.writeEnd();
		jgenPub.flush();
		zip.closeEntry();

		/**
		 * {"MATCH_MANU":{"A1":{"B1":sim1,"C1":sim2,...},"A2":{"B2":sim3,...}, "MATCH_AUTO":{"A3":{...
		 */
		 
		zip.putNextEntry(new ZipEntry("crmatch.json"));
		JsonGenerator jgenMatch = Json.createGenerator(zip);
		jgenMatch.writeStartObject();
		for (boolean loop: new boolean[] { false, true }) {
			jgenMatch.writeStartObject(loop?"MATCH_MANU":"MATCH_AUTO");
//			for (Entry<Integer, Map<Integer, Double>> it: crTab.crMatch.match.get(loop).entrySet()) {
			crTab.getMatch(loop).forEach(it -> {
				if (it.getValue().size()>0) {
					jgenMatch.writeStartObject(String.valueOf(it.getKey()));
					for (Entry<Integer, Double> pair: it.getValue().entrySet()) {
						jgenMatch.write(String.valueOf(pair.getKey()), pair.getValue());
					}
					jgenMatch.writeEnd();
				}
				
				StatusBar.get().incProgressbar();
			});
			jgenMatch.writeEnd();
		}
		jgenMatch.writeEnd();
		jgenMatch.flush();
		zip.closeEntry();

		
		zip.flush();
		zip.close();
		
		StatusBar.get().setValue("Saving CRE file done");

		
	}
	
		
	
}

package cre.data.source;

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

import cre.data.CRCluster;
import cre.data.CRTable;
import cre.data.CRType;
import cre.data.PubType;
import cre.ui.StatusBar;

public class CRE_json {

	
	public static void load (File file, CRTable crTab, StatusBar stat) throws IOException {
		
		
		crTab.abort = false;	// can be changed by "wait dialog"
		stat.setValue("Loading CRE file ...", "");
		
		
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
				stat.initProgressbar(entry.getSize(), "Loading CRE file crdata ...");
				parser = Json.createParser(zipFile.getInputStream(entry) /*zip*/);
				CRType cr = null;
				String key = "";
				while (parser.hasNext()) {
					switch (parser.next()) {
					case START_OBJECT: 	cr = new CRType(); break; 
					case END_OBJECT: 	crTab.crData.add(cr); break;
					case KEY_NAME:		key = parser.getString(); break;
					case VALUE_STRING: 
						switch (key) {
						case "CR": 		cr.CR = parser.getString(); break;
						case "AU": 		cr.AU = parser.getString(); break;
						case "AU_F": 	cr.AU_F = parser.getString(); break;
						case "AU_L": 	cr.AU_L = parser.getString(); break;
						case "AU_A": 	cr.AU_A = parser.getString(); break;
						case "TI": 		cr.TI = parser.getString(); break;
						case "J": 		cr.J = parser.getString(); break;
						case "J_N": 	cr.J_N = parser.getString(); break;
						case "J_S": 	cr.J_S = parser.getString(); break;
						case "PAG": 	cr.PAG = parser.getString(); break;
						case "VOL": 	cr.VOL = parser.getString(); break;
						case "DOI": 	cr.DOI = parser.getString(); break;
						case "CID2": 	cr.CID2 = new CRCluster(parser.getString()); break;
						default: System.out.println("CRDATA.json >> Unknow Key with String Value: " + key); 
						}
						break;
					case VALUE_NUMBER:
						switch (key) {
						case "ID": 		cr.ID = parser.getInt(); mapId2CR.put(cr.ID, cr); break;
						case "N_CR": 	cr.N_CR = parser.getInt(); break;
						case "RPY": 	cr.RPY = parser.getInt(); break;
						case "CID_S": 	cr.CID_S = parser.getInt(); break;
						case "VI": 		cr.VI = parser.getInt(); break;
						case "CO": 		cr.CO = parser.getInt(); break;
						case "type": 	cr.type = (byte) parser.getInt(); break;
						default: System.out.println("CRDATA.json >> Unknow Key with Number Value: " + key); 
						}
						break;
					default:break;  
					}
				
					stat.updateProgressbar(parser.getLocation().getStreamOffset());
				}
//				crTab.updateData(true);
				stat.setValue("Loading CRE file crdata done");
			}
			
			if (entry.getName().equals("pubdata.json")) {

				stat.initProgressbar(entry.getSize(), "Loading CRE file pubdata ...");
				parser = Json.createParser(zipFile.getInputStream(entry) /*zip*/);
				PubType pub = null;
				List<String> C1List = null;
				int arrayLevel = 0;
				String key = "";
				while (parser.hasNext()) {
					switch (parser.next()) {
					case START_OBJECT: 	pub = new PubType(); break; 
					case END_OBJECT: 	crTab.pubData.add(pub); break;
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
							case "CRLISTID":	pub.crList = new ArrayList<CRType>(); break;
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
						case "CRLISTID":	pub.crList.add(mapId2CR.get(parser.getInt())); break;
						// local mapping: case "CRLISTID":	crTab.crData.get(crTab.crMatch.crId2Index.get(parser.getInt()))); break;
						default: System.out.println("PUBDATA.json >> Unknow Key with Number Value: " + key); 
						}
						break;
					default:
						break;
					}
					stat.updateProgressbar(parser.getLocation().getStreamOffset());
				}
				stat.setValue("Loading CRE file pubdata done");
			}			
			
			
			
			if (entry.getName().equals("crmatch.json")) {
				int level = 0;
				stat.initProgressbar(entry.getSize(), "Loading CRE file crmatch ...");
				parser = Json.createParser(zipFile.getInputStream(entry) /*zip*/);
				
				boolean isManual = false;
				int id1 = 0, id2 = 0;
				
				while (parser.hasNext()) {
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
						crTab.crMatch.setMapping(id1, id2, parser.getBigDecimal().doubleValue(), isManual, false);
						break;
					default:break;  
					}
					stat.updateProgressbar(parser.getLocation().getStreamOffset());
				}
				stat.setValue("Loading CRE file crmatch done");
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
				
		
		crTab.updateData(true);

		crTab.creFile = file;
		stat.setValue("Loading CRE file done", crTab.getInfoString());
	}



	public static void save (File file, CRTable crTab, StatusBar stat) throws IOException {
		 
		int count = 0;
		stat.initProgressbar(crTab.getSize() + crTab.pubData.size() + crTab.crMatch.match.get(true).size() + crTab.crMatch.match.get(false).size(), "Saving CRE file ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".cre")) file_name += ".cre";
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file_name), Charset.forName("UTF-8"));
				
		
		zip.putNextEntry(new ZipEntry("crdata.json"));
		JsonGenerator jgen = Json.createGenerator(zip);
		jgen.writeStartArray();
		for (CRType it: crTab.crData) {
			jgen.writeStartObject();
								jgen.write("ID", it.ID);
			if (it.CR!=null) 	jgen.write("CR", it.CR);
			if (it.AU!=null) 	jgen.write("AU", it.AU);
			if (it.AU_F!=null) 	jgen.write("AU_F", it.AU_F);
			if (it.AU_L!=null) 	jgen.write("AU_L", it.AU_L);
			if (it.AU_A!=null) 	jgen.write("AU_A", it.AU_A);
			if (it.TI!=null) 	jgen.write("TI", it.TI);
			if (it.J!=null) 	jgen.write("J", it.J);
			if (it.J_N!=null) 	jgen.write("J_N", it.J_N);
			if (it.J_S!=null) 	jgen.write("J_S", it.J_S);
								jgen.write("N_CR", it.N_CR);
			if (it.RPY!=null)	jgen.write("RPY", it.RPY);
			if (it.PAG!=null) 	jgen.write("PAG", it.PAG);
			if (it.VOL!=null) 	jgen.write("VOL", it.VOL);
			if (it.DOI!=null) 	jgen.write("DOI", it.DOI);
			if (it.CID2!=null) 	jgen.write("CID2", it.CID2.toString());
								jgen.write("CID_S", it.CID_S);
								jgen.write("VI", it.VI);
								jgen.write("CO", it.CO);
								jgen.write("type", it.type);			
			jgen.writeEnd();
			
			stat.updateProgressbar(++count);
		};
		jgen.writeEnd();
		jgen.flush();
		zip.closeEntry();

		
		zip.putNextEntry(new ZipEntry("pubdata.json"));
		jgen = Json.createGenerator(zip);
		jgen.writeStartArray();
		for (PubType it: crTab.pubData) {
			jgen.writeStartObject();
			
			if (it.PT!=null) 	jgen.write("PT", it.PT);
			if (it.AU!=null) {	jgen.writeStartArray("AU"); for (String x:it.AU) jgen.write(x); jgen.writeEnd(); }
			if (it.AF!=null) {	jgen.writeStartArray("AF"); for (String x:it.AF) jgen.write(x); jgen.writeEnd(); }
			if (it.C1!=null) {
				jgen.writeStartArray("C1");
				for (String[] y:it.C1) {
					jgen.writeStartArray();
					for (String x:y) jgen.write(x); 
					jgen.writeEnd();
				}
				jgen.writeEnd();
			}
			if (it.EM!=null) {	jgen.writeStartArray("EM"); for (String x:it.EM) jgen.write(x); jgen.writeEnd(); }
			if (it.AA!=null) {	jgen.writeStartArray("AA"); for (String x:it.AA) jgen.write(x); jgen.writeEnd(); }
			if (it.TI!=null) 	jgen.write("TI", it.TI);
			if (it.PY!=null) 	jgen.write("PY", it.PY);
			if (it.SO!=null) 	jgen.write("SO", it.SO);
			if (it.VL!=null) 	jgen.write("VL", it.VL);
			if (it.IS!=null) 	jgen.write("IS", it.IS);
			if (it.AR!=null) 	jgen.write("AR", it.AR);
			if (it.BP!=null) 	jgen.write("BP", it.BP);
			if (it.EP!=null) 	jgen.write("EP", it.EP);
			if (it.PG!=null) 	jgen.write("PG", it.PG);
			if (it.TC!=null) 	jgen.write("TC", it.TC);
			if (it.crList!=null) {	jgen.writeStartArray("CRLISTID"); for (CRType x:it.crList) jgen.write(x.ID); jgen.writeEnd(); }
			if (it.DI!=null) 	jgen.write("DI", it.DI);
			if (it.LI!=null) 	jgen.write("LI", it.LI);
			if (it.AB!=null) 	jgen.write("AB", it.AB);
			if (it.DE!=null) 	jgen.write("DE", it.DE);
			if (it.DT!=null) 	jgen.write("DT", it.DT);
			if (it.FS!=null) 	jgen.write("FS", it.FS);
			if (it.UT!=null) 	jgen.write("UT", it.UT);
			
			jgen.writeEnd();
			
			stat.updateProgressbar(++count);
		};
		jgen.writeEnd();
		jgen.flush();
		zip.closeEntry();

		/**
		 * {"MATCH_MANU":{"A1":{"B1":sim1,"C1":sim2,...},"A2":{"B2":sim3,...}, "MATCH_AUTO":{"A3":{...
		 */
		 
		zip.putNextEntry(new ZipEntry("crmatch.json"));
		jgen = Json.createGenerator(zip);
		jgen.writeStartObject();
		for (boolean loop: new boolean[] { false, true }) {
			jgen.writeStartObject(loop?"MATCH_MANU":"MATCH_AUTO");
			for (Entry<Integer, Map<Integer, Double>> it: crTab.crMatch.match.get(loop).entrySet()) {
				if (it.getValue().size()>0) {
					jgen.writeStartObject(String.valueOf(it.getKey()));
					for (Entry<Integer, Double> pair: it.getValue().entrySet()) {
						jgen.write(String.valueOf(pair.getKey()), pair.getValue());
					}
					jgen.writeEnd();
				}
				
				stat.updateProgressbar(++count);
			}
			jgen.writeEnd();
		}
		jgen.writeEnd();
		jgen.flush();
		zip.closeEntry();

		
		zip.flush();
		zip.close();
		
		crTab.creFile = file;
		stat.setValue("Saving CRE file done", crTab.getInfoString());

		
	}
	
		
	
}

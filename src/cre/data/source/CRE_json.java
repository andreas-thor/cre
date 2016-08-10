package cre.data.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import cre.data.CRTable;
import cre.data.CRType;
import cre.data.PubType;
import cre.ui.StatusBar;

public class CRE_json {

	
	public static void load (File file, CRTable crTab, StatusBar stat) throws IOException {
		
		
		crTab.abort = false;	// can be changed by "wait dialog"
		Date startDate = new Date();
		stat.setValue(String.format("%1$s: Loading CRE file ...", startDate), 0, "");
		
		
		crTab.init();
		
		ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
		ZipEntry entry = null;
		HashMap<String, StringBuffer> data = new HashMap<String, StringBuffer>();

		JsonParser parser;
		
		while ( (entry = zip.getNextEntry()) != null ) {

			System.out.println("start " + entry.getName());
			parser = Json.createParser(zip);
			int count=0;
			while (parser.hasNext()) {
				count++;
				parser.next();
			}

			System.out.println("end  " + count);
			
		}
		zip.close();

		
				
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
				
		
		
		stat.setValue(String.format("%1$s: Loading CRE file ...", startDate), 90, "");
		crTab.updateData(false);
		stat.setValue(String.format("%1$s: Loading CRE file done", new Date()), 0, crTab.getInfoString());
	}



	public static void save (File file, CRTable crTab, StatusBar stat) throws IOException {
		 
		int blockSize = (crTab.crData.size() + crTab.pubData.size() + crTab.crMatch.match.get(true).size() + crTab.crMatch.match.get(false).size()) / 20;
		int blockCount = 0;
		int count = 0;
		
		Date startDate = new Date();
		stat.setValue(String.format("%1$s: Saving CRE file ...", startDate), 0, "");
		
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
			
			count++;
			if (blockCount*blockSize<count) {
				stat.setValue(String.format("%1$s: Saving CRE file ...", startDate), 5*blockCount, "");
				blockCount++;
			}
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
				System.out.println(it.C1);
				jgen.writeStartArray("C1");
				for (String[] y:it.C1) {
					System.out.println(y);
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
			
			count++;
			if (blockCount*blockSize<count) {
				stat.setValue(String.format("%1$s: Saving CRE file ...", startDate), 5*blockCount, "");
				blockCount++;
			}

		};
		jgen.writeEnd();
		jgen.flush();
		zip.closeEntry();

		 
		zip.putNextEntry(new ZipEntry("crmatch.json"));
		jgen = Json.createGenerator(zip);
		jgen.writeStartObject();
		for (boolean loop: new boolean[] { false, true }) {
			jgen.writeStartObject(loop?"MATCH_MANU":"MATCH_AUTO");
			for (Entry<Integer, Map<Integer, Double>> it: crTab.crMatch.match.get(loop).entrySet()) {
				jgen.writeStartObject(String.valueOf(it.getKey()));
				for (Entry<Integer, Double> pair: it.getValue().entrySet()) {
					jgen.write(String.valueOf(pair.getKey()), pair.getValue());
				}
				jgen.writeEnd();
				
				count++;
				if (blockCount*blockSize<count) {
					stat.setValue(String.format("%1$s: Saving CRE file ...", startDate), 5*blockCount, "");
					blockCount++;
				}

			}
			jgen.writeEnd();
		}
		jgen.writeEnd();
		jgen.flush();
		zip.closeEntry();

		
		zip.flush();
		zip.close();
		stat.setValue(String.format("%1$s: Saving CRE file done", new Date()), 0, crTab.getInfoString());

		
	}
	
		
	
}

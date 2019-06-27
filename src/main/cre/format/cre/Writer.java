package main.cre.format.cre;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import main.cre.data.type.abs.CRTable;
import main.cre.ui.statusbar.StatusBar;


public class Writer {

	/**
	 * 
	 * @param file
	 * @param includePubsWithoutCRs
	 * @param filter
	 * @param comp IS IGNORED
	 * @throws IOException
	 */
	public static void save (File file, boolean includePubsWithoutCRs) throws IOException {
		 
		/* TODO: filter is not supported yet */
		
		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfCRs() + CRTable.get().getStatistics().getNumberOfPubs() + CRTable.get().getClustering().getNumberOfMatches(true) + CRTable.get().getClustering().getNumberOfMatches(false));
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file), Charset.forName("UTF-8"));
		
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
			CRTable.get().getClustering().getMatchPairGroups(loop).forEach(group -> {
				if (group.getMatches().size()>0) {
					jgenMatch.writeStartObject(String.valueOf(group.getCrId1()));
					for (Entry<Integer, Double> p: group.getMatches().entrySet()) {
						jgenMatch.write(String.valueOf(p.getKey()), p.getValue());
					}
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

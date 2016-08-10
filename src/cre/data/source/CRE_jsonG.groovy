package cre.data.source
 
import java.io.BufferedReader
import java.io.File
import java.nio.charset.Charset
import java.util.HashMap
import java.util.List
import java.util.regex.Matcher
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

import com.orsoncharts.util.json.JSONObject;

import cre.data.*
import cre.ui.StatusBar
import groovy.json.JsonBuilder;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
public class CRE_jsonG  {

	
	public static void load (File file, CRTable crTab, StatusBar stat) {
		
		
		crTab.abort = false	// can be changed by "wait dialog"
		String d = "${new Date()}: "
		stat.setValue(d + "Loading CRE file ...", 0, "")
		
		
		crTab.init()
		
		ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
		ZipEntry entry = null;
		HashMap<String, StringBuffer> data = [:]
		while ( (entry = zip.getNextEntry()) != null ) {
		
			StringBuffer sb = new StringBuffer()
			byte[] buf = new byte[1024];
			int n
			while ((n = zip.read(buf, 0, 1024)) > -1) {
				sb.append (new String (buf, 0, n))
			}

			data.put(entry.getName(), sb)
		}
		zip.close()
		
				
		JsonSlurper slurper = new JsonSlurper()
		
		crTab.crData.addAll (slurper.parseText(data.get("crdata.json").toString()).collect { new CRType().parseJSON ((JSONObject) it) })
		stat.setValue(d + "Loading CRE file ...", 50, "")
		
		HashMap<Integer, Integer> crId2Index = [:]
		crTab.crData.eachWithIndex { CRType cr, int idx -> crId2Index[cr.ID] = idx }
		crTab.pubData.addAll (slurper.parseText(data.get("pubdata.json").toString()).collect { new PubType().parseJSON ((JSONObject) it, crTab.crData, crId2Index) })
		
		crTab.crMatch.parseJSON(slurper.parseText(data.get("crmatch.json").toString()) as JSONObject)
				
// crTab.crMatch.clusterId2Objects[cr.CID2] = [indexCount+1]
		
		
		
		stat.setValue(d + "Loading CRE file ...", 90, "")
		crTab.updateData(false)
		stat.setValue("${d}: Loading CRE file done", 0, crTab.getInfoString())
	}



	public static void save (File file, CRTable crTab, StatusBar stat) {
		
		
		String d = "${new Date()}: "
		stat.setValue(d + "Saving CRE file ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString()
		if (!file_name.endsWith(".cre")) file_name += ".cre"
		
		JsonBuilder jb = new JsonBuilder()
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file_name))

		zip.putNextEntry(new ZipEntry("crdata.json"))
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(zip, Charset.forName("UTF-8")))
		
		println crTab.crData.get(0).getJSON().toString()
		println crTab.crData.get(0).getJSON().toJSONString()
		
		bw.write(jb ( crTab.crData.collect { it.getJSON().toString() } ).toString())
		bw.flush()
		zip.closeEntry();

		zip.putNextEntry(new ZipEntry("pubdata.json"))
		bw.write(jb ( crTab.pubData.collect { it.getJSON().toString() } ).toString())
		bw.flush()
		zip.closeEntry();

		zip.putNextEntry(new ZipEntry("crmatch.json"))
		bw.write(crTab.crMatch.getJSON().toString())
		bw.flush()
		zip.closeEntry();

		
		zip.close();		
		bw.close()
		stat.setValue("${new Date()}: Saving CRE file done", 0, crTab.getInfoString())
		
	}
	
	
	
	
} 
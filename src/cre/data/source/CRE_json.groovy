package cre.data.source
 
import java.io.BufferedReader
import java.io.File
import java.util.HashMap
import java.util.List
import java.util.regex.Matcher

import cre.data.*
import cre.ui.StatusBar
import groovy.json.JsonBuilder;
import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
public class CRE_json extends FileImportExport {

	
	public CRE_json(int[] yearRange, BufferedReader br) {
		super (yearRange, br)

	}
	
	
	@Override
	public PubType getNextPub() {
		

		
	}



	public static void save (File file, CRTable crTab, StatusBar stat) {
		
		String d = "${new Date()}: "
		stat.setValue(d + "Saving CRE file ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString()
		if (!file_name.endsWith(".cre")) file_name += ".cre"
		
		
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))

//		bw.writeLine(
//		JsonOutput.toJson (
//			[crData : crTab.crData.collect {
//				it.getJSON()
//			}]
//			
//		)
//		)
		
		JsonBuilder jb = new JsonBuilder()
		jb (
			crData: crTab.crData.collect { it.getJSON() },
			pubData: crTab.pubData.collect { it.getJSON() }
		)
		
		bw.writeLine (jb.toString())
//		crTab.crData.each { bw.writeLine(it.getJSON().toString()) }
		
		
				
		
		bw.close()
		stat.setValue("${new Date()}: Saving CRE file done", 0, crTab.getInfoString())
		
	}
	
	
	
	
} 
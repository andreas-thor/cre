package cre.data.source
 
import java.io.BufferedReader
import java.io.File
import java.util.HashMap
import java.util.List
import java.util.regex.Matcher
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
		
		JsonBuilder jb = new JsonBuilder()
		
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file_name))
		zip.putNextEntry(new ZipEntry("crData.json"))
		
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(zip))
		bw.write(jb ( crData: crTab.crData.collect { it.getJSON() } ).toString())
		bw.flush()
		zip.closeEntry();
//		bw.close();
		zip.close();		
		
//            int len;
//            byte[] buffer = new byte[2048];
//            while ((len = fis.read(buffer, 0, buffer.length)) > 0) {
//                zos.write(buffer, 0, len);
//            } )
		
		
		

//		bw.writeLine(
//		JsonOutput.toJson (
//			[crData : crTab.crData.collect {
//				it.getJSON()
//			}]
//			
//		)
//		)
		
		
//		jb (
//			crData: crTab.crData.collect { it.getJSON() },
//			pubData: crTab.pubData.collect { it.getJSON() }
//		)
//		
//		bw.writeLine (jb.toString())
		
		
//		crTab.crData.each { bw.writeLine(it.getJSON().toString()) }
		
		
				
		
		bw.close()
		stat.setValue("${new Date()}: Saving CRE file done", 0, crTab.getInfoString())
		
	}
	
	
	
	
} 
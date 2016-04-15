package cre.data.source

import groovy.transform.CompileStatic
import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import cre.Exceptions.AbortedException
import cre.data.CRCluster
import cre.data.CRTable
import cre.data.CRType
import cre.data.PubType
import cre.ui.StatusBar

@CompileStatic
class CRE_csv {

	
	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	public static void save (File file, CRTable crTab, StatusBar stat) {

		String d = "${new Date()}: "
		stat.setValue(d + "Saving CSV file ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		
		List<String> csvColumns = CRType.attr.collect{ it.key } - ["CID2", "CID_S"]	// ignore cluster information
		csv.writeNext(csvColumns + ["_SAMEAS", "_DIFFERENTTO"] as String[]) // add columns for manual match result
		
		crTab.crData.eachWithIndex  { CRType it, int idx ->
			stat.setValue (d + "Save CSV file ...", ((idx+1)*100.0/crTab.crData.size()).intValue())
		
			List<String> csvValues = csvColumns.collect { name -> it[name] as String}
			
			// add manual match result (2 columns: _SAMEAS, _DIFFERENTTO)
			Map mM = ["SAMEAS":[], "DIFFERENTTO":[]]
			crTab.getMapping(it.ID, true).each { k, v ->
				if (v!=null) {
					mM[((v as Double) ==2d)?"SAMEAS":"DIFFERENTTO"] << (Integer)k
				}
			}
			csvValues << mM["SAMEAS"].join(",") << mM["DIFFERENTTO"].join(",")

			csv.writeNext (csvValues as String[])
		}
		csv.close()
		
		stat.setValue("${new Date()}: Saving CSV file done", 0, crTab.getInfoString())
	}

	
	public static void saveRuediger (File file, CRTable crTab, StatusBar stat) {
		
		String d = "${new Date()}: "
		stat.setValue(d + "Saving CSV file ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		
		List<String> csvColumns = CRType.attr.collect{ it.key } - ["N_CR", "CID2", "CID_S"]	// ignore N_CR and cluster information
		csv.writeNext(csvColumns + ["PY"] as String[]) 
		
		
		crTab.pubData.eachWithIndex { PubType pub, int pubIdx ->
			
			stat.setValue (d + "Save CSV file ...", ((pubIdx+1)*100.0/crTab.pubData.size()).intValue())
			
			pub.crList.eachWithIndex  { CRType it, int idx ->
			
				List<String> csvValues = csvColumns.collect { name -> it[name] as String}
				csvValues << (pub.PY as String)
				csv.writeNext (csvValues as String[])
			}
		}
		csv.close()
		
		stat.setValue("${new Date()}: Saving CSV file done", 0, crTab.getInfoString())
	}
		
		
	
	
	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	public static void saveGraph (File file, CRTable crTab, StatusBar stat) {

		String d = "${new Date()}: "
		stat.setValue(d + "Saving Graph as CSV file ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		csv.writeNext(["Year", "NCR", "Median-${2*crTab.getMedianRange()+1}"] as String[])
		crTab.getChartData().each { int year, int[] values ->
			csv.writeNext ([year, values[0], values[1]] as String[])
		}
				
		csv.close()
		
		stat.setValue("${new Date()}: Saving Graph as CSV file done", 0, crTab.getInfoString())
	}
	
	
		
	/**
	 * Load CR table from CSV file
	 * @param file
	 */
	public static void load (File file, CRTable crTab, StatusBar stat) throws AbortedException {
		
		crTab.abort = false	// can be changed by "wait dialog"
		
		String d = "${new Date()}: "
		stat.setValue(d + "Loading CSV file ...", 0, "")

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
					crTab.updateData(true);
					stat.setValue("${new Date()}: Loading CSV file aborted", 0, crTab.getInfoString())
					crTab.abort = false
					throw new AbortedException()
				}
				
				
				line.each { String it -> fileSizeRead += it.length()+3 }
				stat.setValue ("Loading CSV file ...", (fileSizeRead*100.0/fileSize).intValue())
				
									
				CRType cr = new CRType()
				CRType.attr.each {
					if (attrPos[it.key] != null) {
						String v = line[attrPos[it.key]]
						cr[it.key] = ["ID", "N_CR", "RPY"].contains(it.key) ? v.toInteger() : ( ["PERC_YR", "PERC_ALL"].contains(it.key) ? v.toDouble() : v)
					}
				}
				cr.VI = 1	// visible
				cr.CO = 0	// default color
				cr.CID2 = new CRCluster(cr.ID, new Integer(1))
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
		
		crTab.updateData(true);
		stat.setValue("${new Date()}: Loading CSV file done", 0, crTab.getInfoString())
		
	}
	
	
}

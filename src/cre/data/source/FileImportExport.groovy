package cre.data.source
 
import java.io.BufferedReader
import java.io.File

import cre.Exceptions.AbortedException
import cre.Exceptions.FileTooLargeException
import cre.Exceptions.UnsupportedFileFormatException
import cre.data.*
import cre.ui.StatusBar
import groovy.transform.CompileStatic

@CompileStatic
public abstract class FileImportExport {

	
	protected int[] yearRange
	protected BufferedReader br
	
	
	public FileImportExport(int[] yearRange, BufferedReader br) {
		this.yearRange = yearRange
		this.br = br
	}
	
	public abstract PubType getNextPub() 
	
	
	/**
	 * Load data files from Web Of Science (WOS)
	 * @param files array of files
	 */
	public static  void load (CRTable crTab, StatusBar stat, String source, File[] files, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError {
		
		crTab.abort = false	// can be changed by "wait dialog"
		
		String d = "${new Date()}: "
		stat.setValue(d + "Loading files ...", 0, "")
		
		// TODO: initialize crDup  when "no init" mode  
		HashMap<Character,  HashMap<String,Integer>> crDup = [:]	// first character -> (crString -> id )
		int indexCount = 0
		crTab.init()
		
		int stepCount = 0
		int stepSize = 5
		
		
		files.eachWithIndex { File f, int idx ->

			int fileSizeStep = (int) (f.length()*stepSize/100)
			long fileSizeRead = 0

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))
			FileImportExport parser = null
			switch (source) {
				case "WoS_txt": parser = new WoS_txt(yearRange, br); break
				case "Scopus_csv": parser = new Scopus_csv(yearRange, br); break
				default: throw new UnsupportedFileFormatException()
			}
			
			
			PubType pub
			while ((pub = parser.getNextPub()) != null) {
			
				// Check for abort by user
				if (crTab.abort) {
					crTab.init()
					crTab.updateData(false)
					stat.setValue("${new Date()}: Loading files aborted", 0)
					crTab.abort = false
					throw new AbortedException()
				}
				
				// update status bar
				fileSizeRead += pub.length
				if (stepCount*fileSizeStep < fileSizeRead) {
					stat.setValue (d + "Loading WOS file ${idx+1} of ${files.length}", stepCount*stepSize)
					stepCount++
				}
				
								
				pub.crList.eachWithIndex { CRType cr, int crPos ->
					
					// if CR already in database: increase N_CR by 1; else add new record
					if (crDup[cr.CR.charAt(0)] == null) crDup[cr.CR.charAt(0)] = [:]
					Integer id = crDup[cr.CR.charAt(0)][cr.CR]
					if (id != null) {
						crTab.crData[id].N_CR++
						pub.crList[crPos] = crTab.crData[id]
						 
//						println cr.CR
					} else {
						crDup[cr.CR.charAt(0)][cr.CR] = indexCount
						
						if ((maxCR>0) && (indexCount==maxCR)) {
							crTab.updateData(false)
							stat.setValue("${new Date()}: Loading WOS files aborted", 0)
							throw new FileTooLargeException (indexCount)
						}
						
						// todo: add new CR as separate function (make clusterId2Objects private again)
						
						cr.ID = indexCount+1
						cr.CID2 = new CRCluster (indexCount+1, 1)
						cr.CID_S = 1
						crTab.crData << cr
						
						HashSet<Integer> tmp = new HashSet<Integer>();
						tmp.add(indexCount+1);
						crTab.crMatch.clusterId2Objects[cr.CID2] = tmp;
//						crTab.crMatch.clusterId2Objects[cr.CID2] = [indexCount+1];
						indexCount++
					}
					
				} 
				
				crTab.pubData << pub
//				this.noOfPubs++
			}
			
//			this.noOfPubs += parser.noOfPubs
		}

		
//		long timeEnd = System.currentTimeMillis()
//		long overall = timeEnd-timeStart
//		println "Overall: ${overall}"
//		for (int i=0; i<timeDiffs.length; i++) {
//			long percent = (long) (timeDiffs[i]*100 / overall)
//			println "${i} : ${percent}%"
//		}
//
//		println indexCount
		
		
		crTab.updateData(false)
		stat.setValue("${new Date()}: Loading files done", 0, crTab.getInfoString())
	}
	
}

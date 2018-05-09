package cre.test.script;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.CRStatsInfo
import cre.test.data.CRTable
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.data.UserSettings.Sampling
import cre.test.data.match.CRMatch2
import cre.test.data.source.ImportExportFormat;
import cre.test.data.type.CRType
import cre.test.ui.StatusBar;
import cre.test.ui.StatusBarText
import groovy.io.FileType
import groovy.lang.Script;
import groovy.swing.factory.ImageIconFactory

abstract class CREDSL extends Script {


	public static StatusBarText status
	
	

	private static List<File> getFiles (String operator, Map<String, Object> param) {
		
		// set list of import files
		List<File> files = new ArrayList<File>();
		if (param.get("FILE") != null) files.add(new File (param.get("FILE")));
		if (param["FILES"] != null)	files.addAll(param["FILES"].collect { new File(it) });
		if (param["DIR"] != null) new File (param["DIR"]).eachFile(FileType.FILES) { files.add(it) };
		if (files.size()==0) {
			throw new Exception (String.format("%s: no files specified (using file, files, or dir)", operator));
		}
		
		return files;
	}
	
	private static ImportExportFormat getFileFormat  (String operator, Map<String, Object> param) {
	
		switch (param.getOrDefault("TYPE", "").toUpperCase()) {
			case "WOS": return ImportExportFormat.WOS; 
			case "SCOPUS": return ImportExportFormat.SCOPUS; 
		}
		throw new Exception (String.format("%s: missing or unknown file format (must be WOS or SCOPUS)", operator));
		
	}
	
	
	/**
	 * File > Open
	 * ===========
	 * FILE = string
	 */
	public static void openFile (Map<String, Object> map) throws Exception {
		Map<String, Object> param = makeParamsCaseInsensitive(map);
		List<File> files = getFiles ("openFile", param); 
		ImportExportFormat.CRE.load(files);
	}

	

	
	public static void analyzeFile (Map<String, Object> map)  {
		Map<String, Object> param = makeParamsCaseInsensitive(map);
		List<File> files = getFiles ("analyzeFile", param);
		ImportExportFormat fileFormat = getFileFormat("analyzeFile", param);
		fileFormat.analyze(files);
		println CRStatsInfo.get().toString()
	}
	
	/**
	 * File > Import
	 * =============
	 * FILE = string | FILE = [string] | DIR = string
	 * TYPE = "WOS" OR "SCOPUS"
	 * SAMPLING = "NONE" (default), "RANDOM", "SYSTEMATIC", or "CLUSTER"
	 * RPY = [min, max, importWithoutRPY]; default = [0, 0, true]
	 * PY = [min, max, importWithoutPY]; default = [0, 0, true]
	 * MAXCR = int; default = 0
	 */
	public static void importFile (Map<String, Object> map)  {

		Map<String, Object> param = makeParamsCaseInsensitive(map);

		List<File> files = getFiles ("importFile", param);
		ImportExportFormat fileFormat = getFileFormat("importFile", param);

		fileFormat.analyze(files);
		
		// set RPY import range: [min, max, without=true]
		int[] rangeRPY = CRStatsInfo.get().getRangeRPY()	// default: max range
		boolean withoutRPY = true	// default: include CRs without RPY
		if (param["RPY"] != null) {
			rangeRPY[0] = param["RPY"][0]
			rangeRPY[1] = param["RPY"][1]
			if (param["RPY"].size() > 2) {
				withoutRPY = param["RPY"][2]
			}
		}
		UserSettings.get().setRange(RangeType.ImportRPYRange, rangeRPY);
		UserSettings.get().setImportCRsWithoutYear(withoutRPY);

		
		// set PY import range: [min, max, without=true]
		int[] rangePY = CRStatsInfo.get().getRangePY();		// default: max range
		boolean withoutPY = true	// default: include Pubs without PY
		if (param["PY"] != null) {
			rangePY[0] = param["PY"][0]
			rangePY[1] = param["PY"][1]
			if (param["PY"].size() > 2) {
				withoutPY = param["PY"][2]
			}
		}
		UserSettings.get().setRange(RangeType.ImportPYRange, rangePY);
		UserSettings.get().setImportPubsWithoutYear(withoutPY);

		
		// set maximum number of CRs
		int maxCR = 0
		if (param["MAXCR"] != null) {
			maxCR = param["MAXCR"]
		}
		UserSettings.get().setMaxCR(maxCR);


		// sampling
		Sampling sampling = null;
		switch (param.getOrDefault("SAMPLING", "NONE").toUpperCase()) {
			case "NONE": sampling = Sampling.NONE; break;
			case "RANDOM": sampling = Sampling.RANDOM; break;
			case "SYSTEMATIC": sampling = Sampling.SYSTEMATIC; break;
			case "CLUSTER": sampling = Sampling.CLUSTER; break;
			default: throw new Exception ("importFile: unknown sampling (must be NONE, RANDOM, SYSTEMATIC, or CLUSTER)");
		}
		
		sampling.offset = param.getOrDefault("OFFSET", 0);
		UserSettings.get().setSampling(sampling);

		fileFormat.load(files);

	}

	/**
	 * File > Save
	 * ===========
	 * FILE (mandatory)
	 * RPY (optional filter RPY range)
	 */
	public static void saveFile (Map<String, String> map) throws Exception  {

		Map<String, Object> param = makeParamsCaseInsensitive(map)

		// set file
		if (param.get("FILE")==null) {
			throw new Exception ("saveFile: missing parameter file");
		}
		File file = new File (param.get("FILE"));
		
		// set filter
		Predicate<CRType> filter = { cr -> true };
		if (param.keySet().contains("RPY")) {
			int[] range = param["RPY"];
			filter = { cr -> (cr.getRPY() != null) && (cr.getRPY()>=range[0]) && (cr.getRPY()<=range[1]) };
		}

		ImportExportFormat.CRE.save(file, filter);
	}
	
	
	/**
	 * File > Export
	 * FILE (mandatory)
	 * TYPE = "WOS", "SCOPUS", "CSV_CR", "CSV_PUB", "CSV_CR_PUB", or "CSV_GRAPH"
	 * RPY (optional filter RPY range)
	 */

	public static void exportFile (Map<String, String> map) throws Exception {

		Map<String, Object> param = makeParamsCaseInsensitive(map)

		// set file
		if (param.get("FILE")==null) {
			throw new Exception ("saveFile: missing parameter file");
			
		}
		File file = new File (param.get("FILE"));
		
		// set filter
		Predicate<CRType> filter = { cr -> true };
		if (param.keySet().contains("RPY")) {
			int[] range = param["RPY"];
			filter = { cr -> (cr.getRPY() != null) && (cr.getRPY()>=range[0]) && (cr.getRPY()<=range[1]) };
		}
		
		
		// set file format
		ImportExportFormat fileFormat = null;
		switch (param.getOrDefault("TYPE", "").toUpperCase()) {
			case "WOS": fileFormat = ImportExportFormat.WOS; break;
			case "SCOPUS": fileFormat = ImportExportFormat.SCOPUS; break;
			case "CSV_CR": fileFormat = ImportExportFormat.CRE_CR; break;
			case "CSV_PUB": fileFormat = ImportExportFormat.CRE_PUB; break;
			case "CSV_CR_PUB": fileFormat = ImportExportFormat.CRE_CR_PUB; break;
			case "CSV_GRAPH": fileFormat = ImportExportFormat.GRAPH; break;
			default: throw new Exception ("importFile: missing or unknown file format (must be WOS, SCOPUS, CSV_CR, CSV_PUB, CSV_CR_PUB, or CSV_GRAPH)");
		}
		

		fileFormat.save(file, filter);
	}





	/**
	 * Edit > Remove Cited References [by N_CR, by RPY, w/o RPY]
	 * N_CR 
	 * RPY = [min, max] or NULL
	 */
	public static void removeCR (Map<String, Object> map) {

		Map<String, Object> param = makeParamsCaseInsensitive(map)

		if (param["N_CR"] != null) {
			int[] range = param["N_CR"];
			CRTable.get().removeCRByN_CR(range);
			return;
		}

		if (param.keySet().contains("RPY")) {
			if (param["RPY"] != null) {
				int[] range = param["RPY"]
				CRTable.get().removeCRByYear(range)
			} else {
				CRTable.get().removeCRWithoutYear()
			}
			return;
		}

		throw new Exception ("removeCR: Missing parameter (must have N_CR or RPY)")
	}

	
	
	/**
	 * Edit > Retain Publications within Publication Year
	 * PY = [min, max] 
	 */
	public static void retainPub (map) throws Exception {

		Map<String, Object> param = makeParamsCaseInsensitive(map)

		if (param["PY"] != null) {
			int[] range = param["PY"]
			CRTable.get().removePubByCitingYear(range)
		} else {
			throw new Exception ("retainPub: Missing parameter PY")
		}
	}


	/**
	 * Disambiguation > Cluster equivalent Cited References
	 * THRESHOLD (float)
	 * VOLUME (boolean)
	 * PAGE (boolean)
	 * DOI (boolean)
	 */

	public static void cluster (map) {

		Map<String, Object> param = makeParamsCaseInsensitive(map)

		CRMatch2.get().generateAutoMatching();

		double threshold = param.getOrDefault ("THRESHOLD", 0.8)
		boolean useVol = param.getOrDefault ("VOLUME", false)
		boolean usePag = param.getOrDefault ("PAGE", false)
		boolean useDOI = param.getOrDefault ("DOI", false)

		CRMatch2.get().updateClustering(CRMatch2.ClusteringType2.REFRESH, null, threshold, useVol, usePag, useDOI);
	}

	
	/**
	 * Disambiguation > Merge clustered References
	 */
	public static void merge () {
		CRTable.get().merge();
	}

	
	
	public static void set (Map<String, Object> map) throws Exception { 
	
		Map<String, Object> param = makeParamsCaseInsensitive(map)
	
		if (param.get("N_PCT_RANGE") != null) {
			if (UserSettings.get().setNPCTRange(param.get("N_PCT_RANGE").toString()) != 0) {
				throw new Exception("Wrong value for set parameter N_PCT_RANGE: " + param.get("N_PCT_RANGE"));
			}
			param.remove("N_PCT_RANGE");
			CRTable.get().updateData();
		}
		
		if (param.get("MEDIAN_RANGE") != null) {
			if (UserSettings.get().setMedianRange(param.get("MEDIAN_RANGE").toString()) != 0) {
				throw new Exception("Wrong value for set parameter MEDIAN_RANGE: " + param.get("MEDIAN_RANGE"));
			}
			param.remove("MEDIAN_RANGE");
			CRTable.get().updateChartData();
		}
		
		
		/* there should be no remaining parameter */
		for (String unknownParam: param.keySet()) {
			throw new Exception("Unknown set parameter: " + unknownParam);
		}
		
			
	}
	
	
	
	public static void progress (boolean b) {
//		status.setShowProgress (b)
		StatusBar.get().setShowProgress (b)
	}

	public static void info() {
		StatusBar.get().updateInfo();
	}

	
	
	public Class use (String filename) {
		return  this.class.classLoader.parseClass(new File (new File(getClass().protectionDomain.codeSource.location.path).parent, filename))
	}

	private static Map<String, Object> makeParamsCaseInsensitive  (Map<String, Object> map) {
		return map.collectEntries { key, value ->
			return [(key.toUpperCase()): value]
		}
	}
	
}

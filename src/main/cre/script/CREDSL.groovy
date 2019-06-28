package main.cre.script;

import java.util.function.Function
import java.util.function.Predicate

import groovy.io.FileType
import main.cre.data.CRStatsInfo
import main.cre.data.Sampling
import main.cre.data.source.Crossref
import main.cre.data.source.ImportExportFormat;
import main.cre.data.type.abs.CRTable
import main.cre.data.type.abs.Clustering.ClusteringType
import main.cre.data.type.extern.CitedReference
import main.cre.data.type.mm.CRType_MM
import main.cre.format.cre.Writer
import main.cre.format.exporter.ExportFormat
import main.cre.format.importer.ImportFormat
import main.cre.ui.statusbar.StatusBar;
import main.cre.ui.statusbar.StatusBarText

abstract class CREDSL extends Script {


	public static StatusBarText status

	public CREDSL () {
		status = new StatusBarText()
		StatusBar.get().setUI(status);
	}

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


	private static int[] getRangeRPY (Map<String, Object> param, int[] defaultRangeRPY = null) {

		// default: max range
		int[] rangeRPY = defaultRangeRPY ?: CRStatsInfo.get().getRangeRPY();  	
		if (param["RPY"] != null) {
			rangeRPY[0] = param["RPY"][0]
			rangeRPY[1] = param["RPY"][1]
		}
		return rangeRPY;
	}


	private static boolean getWithoutRPY (Map<String, Object> param) {

		boolean withoutRPY = true;	// default: include CRs without RPY
		if (param["RPY"] != null) {
			if (param["RPY"].size() > 2) {
				withoutRPY = param["RPY"][2];
			}
		}
		return withoutRPY;
	}

	private static int[] getRangePY (Map<String, Object> param, int[] defaultRangePY = null) {

		int[] rangePY = defaultRangePY ?: CRStatsInfo.get().getRangePY();	// default: max range
		if (param["PY"] != null) {
			rangePY[0] = param["PY"][0];
			rangePY[1] = param["PY"][1];
		}
		return rangePY;
	}


	private static boolean getWithoutPY (Map<String, Object> param) {

		boolean withoutPY = true	// default: include Pubs without PY
		if (param["PY"] != null) {
			if (param["PY"].size() > 2) {
				withoutPY = param["PY"][2];
			}
		}
		return withoutPY;
	}
	
	private static Sampling getSampling (Map<String, Object> param) {
		
		Sampling sampling = null;
		switch (param.getOrDefault("SAMPLING", "NONE").toUpperCase()) {
			case "NONE": sampling = Sampling.NONE; break;
			case "RANDOM": sampling = Sampling.RANDOM; break;
			case "SYSTEMATIC": sampling = Sampling.SYSTEMATIC; break;
			case "CLUSTER": sampling = Sampling.CLUSTER; break;
			default: throw new Exception ("importFile: unknown sampling (must be NONE, RANDOM, SYSTEMATIC, or CLUSTER)");
		}
		sampling.offset = param.getOrDefault("OFFSET", 0);
		return sampling;
	}
	
	
	private static String[] getDOI (Map<String, Object> param) { 
		
		List<String> dois = new ArrayList<String>();
		if (param.get("DOI") != null) {
			if (param.get("DOI") instanceof String) {
				dois.add(param.get("DOI"));
			} else {
				dois.addAll(param.get("DOI"));
			}
		}
		return dois as String[];
	}

	/**
	 * File > Open
	 * ===========
	 * FILE = string
	 */
	public static void openFile (Map<String, Object> map) throws Exception {
		Map<String, Object> param = makeParamsCaseInsensitive(map);
		List<File> files = getFiles ("openFile", param);
		if (files.size()>1) {
			throw new Exception (String.format("openFile: too many files specified (%d; only one file allowed)", files.size()));
		}
		CRTable.get().createReader().load(files[0]);
	}




	public static void analyzeFile (Map<String, Object> map)  {
		Map<String, Object> param = makeParamsCaseInsensitive(map);
		List<File> files = getFiles ("analyzeFile", param);
		try {
			ImportFormat fileFormat = ImportFormat.valueOf (param.getOrDefault("TYPE", "").toUpperCase()).
			fileFormat.analyze(files);
		} catch (IllegalArgumentException e) {
			throw new Exception ("analyzeFile: missing or unknown file format (must be WOS, SCOPUS, or CROSSREF)");
		}
		println CRStatsInfo.get().toString()
		
	}

	/**
	 * File > Import
	 * =============
	 * FILE = string | FILE = [string] | DIR = string
	 * TYPE = "WOS" OR "SCOPUS" OR "CROSSREF"
	 * SAMPLING = "NONE" (default), "RANDOM", "SYSTEMATIC", or "CLUSTER"
	 * RPY = [min, max, importWithoutRPY]; default = [0, 0, true]
	 * PY = [min, max, importWithoutPY]; default = [0, 0, true]
	 * MAXCR = int; default = 0
	 */
	public static void importFile (Map<String, Object> map)  {

		Map<String, Object> param = makeParamsCaseInsensitive(map);

		List<File> files = getFiles ("importFile", param);
		
		try {
			ImportFormat fileFormat = ImportFormat.valueOf (param.getOrDefault("TYPE", "").toUpperCase()).
			fileFormat.analyze(files);
			fileFormat.load(files, getRangeRPY(param), getWithoutRPY(param), getRangePY(param), getWithoutPY(param), param.getOrDefault("MAXCR", 0), getSampling(param));
		} catch (IllegalArgumentException e) {
			throw new Exception ("importFile: missing or unknown file format (must be WOS, SCOPUS, or CROSSREF)");
		}
			
	}

	
	public static void importSearch (Map<String, Object> map)  {
		
		Map<String, Object> param = makeParamsCaseInsensitive(map);
		List<File> files = Crossref.download(getDOI (param), param.getOrDefault("ISSN", ""), getRangePY(param, [-1,-1] as int[]));
		ImportFormat.CROSSREF.analyze(files);
		ImportFormat.CROSSREF.load(files, getRangeRPY(param), getWithoutRPY(param), getRangePY(param), getWithoutPY(param), param.getOrDefault("MAXCR", 0), getSampling(param));
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
		Predicate<CRType_MM> filter = { cr -> true };
		if (param.keySet().contains("RPY")) {
			int[] range = param["RPY"];
			filter = { CRType_MM cr -> (cr.getRPY() != null) && (cr.getRPY()>=range[0]) && (cr.getRPY()<=range[1]) };
		}

		Writer.save(file, true);
	}


	/**
	 * File > Export
	 * FILE (mandatory)
	 * TYPE = "WOS", "SCOPUS", "CSV_CR", "CSV_PUB", "CSV_CR_PUB", or "CSV_GRAPH"
	 * RPY (optional filter RPY range)
	 */

	public static void exportFile (Map<String, String> map) throws Exception {

		Map<String, Object> param = makeParamsCaseInsensitive(map)
		Comparator<CRType_MM> compCRType = null;
		
		// set file
		if (param.get("FILE")==null) {
			throw new Exception ("saveFile: missing parameter file");

		}
		File file = new File (param.get("FILE"));


		// includePubsWithoutCRs
		boolean includePubsWithoutCRs = true;
		if (param.keySet().contains("W/O_CR")) {
			includePubsWithoutCRs = param.get("W/O_CR");
		}

		// set filter 
		Predicate<CRType_MM> filter = { cr -> true };
		if (param.keySet().contains("RPY")) {
			int[] range = param["RPY"];
			filter = { CRType_MM cr -> (cr.getRPY() != null) && (cr.getRPY()>=range[0]) && (cr.getRPY()<=range[1]) };
		}
		if (param["FILTER"] != null) {
			filter = { ((Predicate<CitedReference>) param["FILTER"]).test(CitedReference.createFromCRType(it)) };
		}

		// set sort order
		if (param.keySet().contains("SORT")) {

			// building a CitedReferences comparator
			Comparator<CitedReference> compCitedReference = null; 
			String[] values = (param["SORT"] instanceof String) ? [param["SORT"]] : param["SORT"];
			
			
			for (String s: values) {		// list of order by properties
				String[] split = s.split(" ");
				String prop = split[0];
				boolean reversed = (split.length>1) && (split[1].toUpperCase().equals("DESC"));		
				
				Comparator<CitedReference> compProp = Comparator.comparing({ it."${prop}" }  as Function);
			
				
				
				compProp = reversed ? compProp.reversed() : compProp;
				compCitedReference = (compCitedReference == null) ? compProp : compCitedReference.thenComparing (compProp);
			}
			
			// build the "real" comparator
			compCRType = new Comparator<CRType_MM>() {
				@Override
				public int compare(CRType_MM o1, CRType_MM o2) {
					return compCitedReference.compare(CitedReference.createFromCRType(o1), CitedReference.createFromCRType(o2));
				}
			};
		}
		
		
		
		
		
		

//		// set file format
//		ExportFormat fileFormat = null;
//		switch (param.getOrDefault("TYPE", "").toUpperCase()) {
//			case "WOS": fileFormat = ExportFormat.WOS; break;
//			case "SCOPUS": fileFormat = ExportFormat.SCOPUS; break;
//			case "CSV_CR": fileFormat = ExportFormat.CSV_CR; break;
//			case "CSV_PUB": fileFormat = ExportFormat.CSV_PUB; break;
//			case "CSV_CR_PUB": fileFormat = ExportFormat.CSV_CR_PUB; break;
//			case "CSV_GRAPH": fileFormat = ExportFormat.CSV_GRAPH; break;
//			default: throw new Exception ("importFile: missing or unknown file format (must be WOS, SCOPUS, CSV_CR, CSV_PUB, CSV_CR_PUB, or CSV_GRAPH)");
//		}


		try {
			ExportFormat.valueOf(param.getOrDefault("TYPE", "").toUpperCase()).save(file, includePubsWithoutCRs, filter, compCRType);
		} catch (IllegalArgumentException e) {
			throw new Exception ("importFile: missing or unknown file format (must be WOS, SCOPUS, CSV_CR, CSV_PUB, CSV_CR_PUB, or CSV_GRAPH)");
		}
		
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

		if (param["FILTER"] != null) {
			CRTable.get().removeCR({ ((Predicate<CitedReference>) param["FILTER"]).test(CitedReference.createFromCRType(it)) });
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
			CRTable.get().retainPubByCitingYear(range)
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

		CRTable.get().getClustering().generateInitialClustering();

		double threshold = param.getOrDefault ("THRESHOLD", 0.8)
		boolean useVol = param.getOrDefault ("VOLUME", false)
		boolean usePag = param.getOrDefault ("PAGE", false)
		boolean useDOI = param.getOrDefault ("DOI", false)

		CRTable.get().getClustering().updateClustering(ClusteringType.REFRESH, null, threshold, useVol, usePag, useDOI);
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

			try {
				CRTable.get().setNpctRange(Integer.valueOf(param.get("N_PCT_RANGE").toString()).intValue())
				CRTable.get().updateData();
			} catch (Exception e) {
				throw new Exception("Wrong value for set parameter N_PCT_RANGE: " + param.get("N_PCT_RANGE"));
			}
			param.remove("N_PCT_RANGE");

		}

		if (param.get("MEDIAN_RANGE") != null) {

			try {
				CRTable.get().getChartData().setMedianRange(Integer.valueOf(param.get("MEDIAN_RANGE").toString()).intValue());
			} catch (Exception e) {
				throw new Exception("Wrong value for set parameter MEDIAN_RANGE: " + param.get("MEDIAN_RANGE"));
			}
			param.remove("MEDIAN_RANGE");
		}


		/* there should be no remaining parameter */
		for (String unknownParam: param.keySet()) {
			throw new Exception("Unknown set parameter: " + unknownParam);
		}


	}



	public static void progress (boolean b) {
		status.setShowProgress (b)
	}

	public static void info() {
		StatusBar.get().updateInfo();
	}

	
	public static void input() {
		println "input..."
		new Scanner(System.in).nextLine();
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

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
import cre.test.data.CRTable
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.data.match.CRMatch2
import cre.test.data.source.ImportExportFormat;
import cre.test.data.type.CRType
import cre.test.ui.StatusBar;
import groovy.lang.Script;

abstract class CREDSL extends Script {


	private Map<String, Object> makeParamsCaseInsensitive  (Map<String, Object> map) {
		return map.collectEntries { key, value ->
			return [(key.toUpperCase()): value]
		  }
	}

	
	public void progress (boolean b) {
		status.setShowProgress (b)
	}
	
	public void load (Map<String, Object> map)  {
	
		def param = makeParamsCaseInsensitive(map)
	
		// set RPY import range: [min, max, without=true] 
		int[] rangeRPY = [0, 0]		// default: all RPYs
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
		int[] rangePY = [0, 0]		// default: all PYs
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
		
		// set list of import files 
		List<File> files = new ArrayList<File>();
		if (param.get("FILE") != null) files.add(new File (param.get("FILE")));
		if (param["FILES"] != null)	files.addAll(param["FILES"].collect { new File(it) });	
		if (param["DIR"] != null) new File (param["DIR"]).eachFile() { files.add(it) };
			
		
		
		
		ImportExportFormat.valueOf(param.getOrDefault("TYPE", "CRE").toUpperCase()).load(files);
		
	}
	
	
	public void save (Map<String, String> map)  {
		
		def param = makeParamsCaseInsensitive(map)
		
		String type = param.getOrDefault("TYPE", "CRE").toUpperCase();
		File file = new File (param.get("FILE"));
		
		Predicate<CRType> filter = { cr -> true };
		if (param.keySet().contains("RPY")) {
			int[] range = param["RPY"];
			filter = { cr -> (cr.getRPY() != null) && (cr.getRPY()>=range[0]) && (cr.getRPY()<=range[1]) };
		}
		
		ImportExportFormat.valueOf(type).save(file, filter);
	}
	
	public void removeCR (Map<String, Object> map) {
		
		def param = makeParamsCaseInsensitive(map)
		
		if (param["N_CR"] != null) {
			int[] range = param["N_CR"] 
			CRTable.get().removeCRByN_CR(range)
		}
		
		if (param.keySet().contains("RPY")) {
			if (param["RPY"] != null) {
				int[] range = param["RPY"]
				CRTable.get().removeCRByYear(range)
			} else {
				CRTable.get().removeCRWithoutYear()
			}
		}

	}
	
	
	public void retainPub (map) {
		
		def param = makeParamsCaseInsensitive(map)
		
		if (param["PY"] != null) {
			int[] range = param["PY"]
			CRTable.get().removePubByCitingYear(range)
		}
	}
	
	
	public void info() {
		StatusBar.get().updateInfo();
	}
	
	
	public void cluster (map) {
		
		def param = makeParamsCaseInsensitive(map)
		
		CRMatch2.get().generateAutoMatching();
		
		double threshold = param.getOrDefault ("THRESHOLD", 0.8)
		boolean useVol = param.getOrDefault ("VOLUME", false)
		boolean usePag = param.getOrDefault ("PAGE", false)
		boolean useDOI = param.getOrDefault ("DOI", false)
		
		CRMatch2.get().updateClustering(CRMatch2.ClusteringType2.REFRESH, null, threshold, useVol, usePag, useDOI);
	}
	
	public void merge () {
		CRTable.get().merge();
	}
	
}

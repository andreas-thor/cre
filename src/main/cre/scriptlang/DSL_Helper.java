package main.cre.scriptlang;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.format.exporter.ExportFormat;
import main.cre.format.importer.ImportFormat;
import main.cre.ui.dialog.Sampling;

public class DSL_Helper {

	
	public static Map<String, Object> makeParamsUpperCase  (Map<String, Object> map) {
		return map.entrySet().stream().collect(Collectors.toMap(p -> p.getKey().toUpperCase(), p -> p.getValue()));
	}
	
	
	public static List<File> getFiles (Map<String, Object> params) throws Exception {

		// set list of import files
		List<File> files = new ArrayList<File>();
		
		if (params.get("FILE") != null) {
			files.add(new File ((String) params.get("FILE")));
		}
		
		if (params.get("FILES") != null) {
			files.addAll (((List<?>)params.get("FILES")).stream().map(f -> new File((String)f)).collect(Collectors.toList())); 
		}
		
		if (params.get("DIR") != null) {
			files.addAll (Files.walk(Paths.get((String)params.get("DIR"))).filter(Files::isRegularFile).map(f -> f.toFile()).collect(Collectors.toList())); 
		}
		
		return files;
	}
	
	
	public static ImportFormat getImportFormat (Map<String, Object> params) throws Exception {
		try {
			return ImportFormat.valueOf (((String) params.getOrDefault("TYPE", "")).toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new Exception ("Missing or unknown import file format (must be WOS, SCOPUS, or CROSSREF)");
		}
	}
	
	public static ExportFormat getExportFormat (Map<String, Object> params) throws Exception {
		try {
			return ExportFormat.valueOf (((String) params.getOrDefault("TYPE", "")).toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new Exception ("Missing or unknown export file format (must be WOS, SCOPUS, CSV_CR, CSV_PUB, CSV_CR_PUB, or CSV_GRAPH)");
		}
	}
	
	
	
	public static IntRange getRange (Object range, IntRange defaultRange) {
		
		if (range == null) return defaultRange;

		return new IntRange(
			Integer.valueOf((String) ((List<?>)range).get(0)).intValue(),
			Integer.valueOf((String) ((List<?>)range).get(1)).intValue());
	}
	
	public static boolean getWithoutYear (Object range) {
		
		if ((range == null) || (((List<?>)range).size()<3)) return true;

		return Boolean.valueOf((String) ((List<?>)range).get(2)).booleanValue();
	}
	
	
	public static Sampling getSampling (Map<String, Object> params) throws Exception {
		
		try {
			Sampling sampling = Sampling.valueOf(((String)params.getOrDefault("SAMPLING", "NONE")).toUpperCase());
			sampling.offset = Integer.valueOf((String)params.getOrDefault("OFFSET", "0")).intValue();
			return sampling;
		} catch (IllegalArgumentException e) {
			throw new Exception ("Unknown sampling (must be NONE, RANDOM, SYSTEMATIC, or CLUSTER)");
		}
	}
	
	
	public static String[] getDOI (Map<String, Object> params) { 
		
		if (params.get("DOI") == null) return new String[] {};
		
		if (params.get("DOI") instanceof String) return new String[] { (String) params.get("DOI") };

		return ((List<?>) params.get("DOI")).stream().map(d -> (String) d).toArray(String[]::new);
	}
	

}

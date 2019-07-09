package main.cre.scriptlang;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.naming.OperationNotSupportedException;

import groovy.lang.Script;
import main.cre.data.CRStatsInfo;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.format.importer.Crossref;
import main.cre.format.importer.ImportFormat;
import main.cre.ui.statusbar.StatusBar;
import main.cre.ui.statusbar.StatusBarText;

public class DSL extends Script {

	public static StatusBarText status;

	public DSL() {
		status = new StatusBarText();
		StatusBar.get().setUI(status);
	}

	public static void progress(boolean b) {
		status.setShowProgress(b);
	}

	public static void info() {
		StatusBar.get().updateInfo();
	}

	public static void input() {
		input(null);
	}

	public static void input(String msg) {
		System.out.print(msg == null ? "" : msg);
		Scanner s = new Scanner(System.in);
		s.nextLine();
		s.close();
	}

	public static void openFile(Map<String, Object> map) throws Exception {
		
		List<File> files = DSL_Helper.getFiles (DSL_Helper.makeParamsUpperCase(map));
		if (files.size() != 1) {
			throw new Exception (String.format("openFile: requires one file (%d specified)", files.size()));
		}
		CRTable.get().getReader().load(files.get(0));
	}

	
	public static void analyzeFile(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		DSL_Helper.getImportFormat(params).analyze(DSL_Helper.getFiles (params));
		System.out.println(CRStatsInfo.get().toString());
	}

	
	public static void importFile(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		analyzeAndLoadFile(DSL_Helper.getImportFormat(params), DSL_Helper.getFiles (params), params);
	}

	
	
	public static void importSearch(Map<String, Object> map) throws Exception {
		
		throw new OperationNotSupportedException();
		/*
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		List<File> files = Crossref.download(
			"", 
			DSL_Helper.getDOI (params), 
			(String) params.getOrDefault("ISSN", ""), 
			DSL_Helper.getYearRange(params.get("PY"), new IntRange(-1,  -1)));

		analyzeAndLoadFile(ImportFormat.CROSSREF, DSL_Helper.getFiles (params), params);
		*/
	}

	
	private static void analyzeAndLoadFile (ImportFormat fileFormat, List<File> files, Map<String, Object> params) throws Exception {
		fileFormat.analyze(files);
		fileFormat.load(
				files, 
				DSL_Helper.getYearRange(params.get("RPY"), CRStatsInfo.get().getRangeRPY()), 
				DSL_Helper.getWithoutYear(params.get("RPY")), 
				DSL_Helper.getYearRange(params.get("PY"), CRStatsInfo.get().getRangePY()),
				DSL_Helper.getWithoutYear(params.get("PY")), 
				((Integer) params.getOrDefault("MAXCR", 0)).longValue(),  
				DSL_Helper.getSampling(params));
	}
	
	
	public static void saveFile(Map<String, String> map) {
	}

	public static void exportFile(Map<String, String> map) throws Exception {
	}

	public static void removeCR(Map<String, Object> map) {
	}

	public static void retainPub(Map<String, Object> map) throws Exception {
	}

	public static void cluster(Map<String, Object> map) {
	}

	public static void merge() {
	}

	public static void set(Map<String, Object> map) throws Exception {
	}

	public Class use(String filename) {
		return DSL_UseClass.use(filename);
	}

	@Override
	public Object run() {
		// TODO Auto-generated method stub
		return null;
	}

}

package cre.test.script;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cre.test.Exceptions.AbortedException;
import cre.test.Exceptions.FileTooLargeException;
import cre.test.Exceptions.UnsupportedFileFormatException;
import cre.test.data.UserSettings;
import cre.test.data.UserSettings.RangeType;
import cre.test.data.source.ImportExportFormat;
import cre.test.ui.StatusBar;
import groovy.lang.Script;

abstract class CREDSL extends Script {

	
	public void RPY (int from, int to, boolean without=true) {
		int[] r = new int[2]; 
		r[0]=from; 
		r[1]=to;
		UserSettings.get().setRange(RangeType.ImportRPYRange, r);
		UserSettings.get().setImportCRsWithoutYear(without);
	}
	
	public void PY (int from, int to, boolean without=true) {
		int[] r = new int[2];
		r[0]=from;
		r[1]=to;
		UserSettings.get().setRange(RangeType.ImportPYRange, r);
		UserSettings.get().setImportPubsWithoutYear(without);
	}

	public void MaxCR (int m) {
		UserSettings.get().setMaxCR(m);
	}
	
	
	public void load (Map<String, String> param)  {
		
		String type = param.getOrDefault("type", "CRE_JSON");
		
		List<File> files = new ArrayList<File>();
		if (param.get("file") != null) files.add(new File (param.get("file")));
		if (param["files"] != null)	files.addAll(param["files"].collect { new File(it) });	
		if (param["dir"] != null) new File (param["dir"]).eachFile() { files.add(it) };
			
		
		ImportExportFormat.valueOf(type).load(files);
		StatusBar.get().updateInfo();
		
	}
	
	public void save (Map<String, String> param)  {
		
		String type = param.getOrDefault("type", "CRE_JSON");
		File file = new File (param.get("file"));
		ImportExportFormat.valueOf(type).save(file);
	}
}

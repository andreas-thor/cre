package main.cre.format.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import main.cre.data.type.abs.CRType;
import main.cre.ui.statusbar.StatusBar;

public enum ExportFormat {

	
	WOS("Web of Science", "txt", WoS::save),

	SCOPUS("Scopus", "csv", Scopus::save),

	CSV_CR("Cited References", "csv", CSV::saveCR), 
	
	CSV_PUB("Citing Publications", "csv", CSV::savePub), 

	CSV_CR_PUB("Cited References + Citing Publications", "csv", CSV::saveCRPub), 

	CSV_GRAPH("CRE Graph", "csv", CSV::saveGraph);  
	
	
	

	public interface Export {
	   void save(String file_name, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException, RuntimeException;
	}
	
	private final String label;
	private final String fileExtension;
	private final Export exportSave;

	
	ExportFormat(String label, String fileExtension, Export exportSave) {
		this.label = label;
		this.fileExtension = fileExtension;
		this.exportSave = exportSave;
	}

	public void save (File file, boolean includePubsWithoutCRs) throws IOException {
		this.save (file, includePubsWithoutCRs, null);
	}
	
	public void save (File file, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter) throws IOException {
		this.save (file, includePubsWithoutCRs, filter, null);
	}
	
	public void save (File file, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException {
		
		ExportFormat.valueOf("s");
		
		// add extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith("." + this.getFileExtension())) file_name += "." + this.getFileExtension();
		
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s ...", file.getName(), this.getLabel()));
		this.exportSave.save(file_name, includePubsWithoutCRs, Optional.ofNullable(filter).orElse(it -> true), Optional.ofNullable(comp).orElse(CRType<?>::compareTo));
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s done", file.getName(), this.getLabel()));

	}
	
		


	public String getLabel() {
		return label;
	}

	public String getFileExtension() {
		return fileExtension;
	}


};
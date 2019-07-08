package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import main.cre.data.type.abs.Clustering;
import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.format.cre.Writer;
import main.cre.format.exporter.ExportFormat;
import main.cre.format.importer.ImportFormat;
import main.cre.ui.UISettings;
import main.cre.ui.dialog.Sampling;

public class StorageEngine {

	/**
	 * This test checks if the storage engines "MM" (main memory) and "DB" (database) are equivalent.
	 * To this end, several files are imported, some data manipulation is done (remove CRs, clustering, merging)
	 * and the resulting export files are checked if they are byte-wise equivalent.
	 */
	
	private final static String DATAFOLDER = "data/";

	
	@Test
	public void test_DB_vs_MM () throws OutOfMemoryError, Exception {

		
		for (IntRange removeCRByYear: new IntRange[] { null, new IntRange (10, 2013) }) {
			for (IntRange removeCRByN_CR: new IntRange[] { null, new IntRange(0, 10) }) {
				for (Double threshold: new Double[] { null, 0.5, 0.75, 0.9 }) {
					for (boolean merge: new boolean[] { false, true } ) {
						
						if ((threshold==null) && merge) continue;	// merge is only possible after clustering
						
						checkForEqualOutputFiles_DB_vs_MM(
							getImportGenerator(ImportFormat.WOS, new String[] { "savedrecs_JOI1.txt", "savedrecs_JOI2.txt"} , removeCRByYear, removeCRByN_CR, threshold, merge)
						);
						
						checkForEqualOutputFiles_DB_vs_MM(
							getImportGenerator(ImportFormat.SCOPUS, new String[] { "scopus/scopus_export_csv_incl_citations_abstract_references.csv"} , removeCRByYear, removeCRByN_CR, threshold, merge)
						);					
											
						checkForEqualOutputFiles_DB_vs_MM(
							getImportGenerator(ImportFormat.WOS, new String[] { "climate gross/data_climate_100t.txt"} , removeCRByYear, removeCRByN_CR, threshold, merge)
						);
					}
					
				}
			}
		}
	}
	
	
	private Consumer<Void> getImportGenerator(ImportFormat format, String[] files, IntRange removeCRByYear, IntRange removeCRByN_CR, Double threshold, boolean merge) {
		
		return (x) -> {
			try {
				format.load(
						Arrays.stream(files).map(s -> new File(DATAFOLDER + s)).collect(Collectors.toList()), 
						new IntRange(0, 0), 
						true,
						new IntRange(0, 0),
						true, 
						0, 
						Sampling.NONE
					);
				
				if (removeCRByYear != null) {
					CRTable.get().removeCRByYear(removeCRByYear);
				}
				
				if (removeCRByN_CR != null) {
					CRTable.get().removeCRByN_CR(removeCRByN_CR);
				}
				
				if (threshold != null) {
					CRTable.get().getClustering().generateInitialClustering();
					CRTable.get().getClustering().updateClustering(Clustering.ClusteringType.REFRESH, null, threshold, false, false, false);
				}
				
				if (merge) {
					CRTable.get().merge();
				}
				
				
			} catch (OutOfMemoryError | Exception e) {
				 throw new RuntimeException(e);
			}
		};
	}
	

	
	
	private void checkForEqualOutputFiles_DB_vs_MM(Consumer<Void> generateTable) throws OutOfMemoryError, Exception {


		final String TESTFOLDER = DATAFOLDER + "test/";
		final Function<TABLE_IMPL_TYPES, File> creFile = (type) -> new File (String.format("%sout_%s.cre", TESTFOLDER, type.toString())); 
		final BiFunction<TABLE_IMPL_TYPES, ExportFormat, File> exportFile = (type, outFormat) -> new File (String.format("%sout_%s_%s.%s", TESTFOLDER, type.toString(), outFormat.toString(), outFormat.getFileExtension())); 

		
		for (TABLE_IMPL_TYPES type: CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			generateTable.accept(null);
			
			for (ExportFormat outFormat: ExportFormat.values()) {
				outFormat.save(exportFile.apply(type, outFormat), UISettings.get().getIncludePubsWithoutCRs());
			}
			
			Writer.save(creFile.apply(type), UISettings.get().getIncludePubsWithoutCRs());
		}
		
		
		// we are checking if the output files are byte-wise equivalent
		for (ExportFormat outFormat: ExportFormat.values()) {
			System.out.println(outFormat.toString());
			assertTrue(FileUtils.contentEquals(exportFile.apply(TABLE_IMPL_TYPES.MM, outFormat), exportFile.apply(TABLE_IMPL_TYPES.DB, outFormat)));
		}

		// a cre file is a zip file --> we are pairwise checking the zipped json files
		for (String name: new String[] { "crdata", "pubdata", "crmatch" }) {
			InputStream[] toCompare = new InputStream[2];
			for (TABLE_IMPL_TYPES type: CRTable.TABLE_IMPL_TYPES.values()) {
				ZipFile zip = new ZipFile(creFile.apply(type));
				toCompare[type.ordinal()] = zip.getInputStream(zip.getEntry(name + ".json"));
			}
			
			System.out.println("CRE/" + name);
			assertTrue (IOUtils.contentEquals(toCompare[0], toCompare[1]));
		}

	}
	
	

}

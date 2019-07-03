package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.format.cre.Writer;
import main.cre.format.exporter.ExportFormat;
import main.cre.format.importer.ImportFormat;
import main.cre.ui.UISettings;
import main.cre.ui.dialog.Sampling;

public class Im {

	private final static String TESTFOLDER = "data/test/";
	private final Function<TABLE_IMPL_TYPES, File> creFile = (type) -> new File (String.format("%sout_%s.cre", TESTFOLDER, type.toString())); 
	private final BiFunction<TABLE_IMPL_TYPES, ExportFormat, File> exportFile = (type, outFormat) -> new File (String.format("%sout_%s_%s.%s", TESTFOLDER, type.toString(), outFormat.toString(), outFormat.getFileExtension())); 

	@Test
	public void test() throws OutOfMemoryError, Exception {

		
		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File("data/savedrecs_JOI1.txt"));
		inputFiles.add(new File("data/savedrecs_JOI2.txt"));
		
		for (TABLE_IMPL_TYPES type: CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			
			ImportFormat.WOS.load(
					inputFiles, 
					new IntRange(0, 0), 
					true,
					new IntRange(0, 0),
					true, 
					0, 
					Sampling.NONE
				);
			
//			CRTable.get().removeCRByYear(new IntRange (10, 2013));
//			CRTable.get().removeCRByN_CR(new IntRange(0, 10));
			
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

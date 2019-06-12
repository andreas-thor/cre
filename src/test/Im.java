package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import main.cre.data.Sampling;
import main.cre.data.source.ImportExportFormat;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import main.cre.ui.UISettings;

public class Im {

	@Test
	public void test() throws OutOfMemoryError, Exception {
		
		ImportExportFormat[] outFormats = { ImportExportFormat.WOS, ImportExportFormat.SCOPUS, ImportExportFormat.CRE_CR, ImportExportFormat.CRE_PUB, ImportExportFormat.CRE_CR_PUB, ImportExportFormat.GRAPH };
		
		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File("data/savedrecs_JOI1.txt"));
		inputFiles.add(new File("data/savedrecs_JOI2.txt"));
		
		for (TABLE_IMPL_TYPES type: CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			
			ImportExportFormat.WOS.load(
					inputFiles, 
					new int[] {0, 0},
					true,
					new int[] {0, 0}, 
					true, 
					0, 
					Sampling.NONE
				);
			
			for (ImportExportFormat outFormat: outFormats) {
				File outputFile = new File(String.format("data/test/out_%s_%s.%s", type.toString(), outFormat.toString(), outFormat.getFileExtension()));
				outFormat.save(outputFile, UISettings.get().getIncludePubsWithoutCRs());
			}
			
		}
		
		
		
		
		

	}
	
	

}

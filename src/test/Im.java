package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import main.cre.data.Sampling;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import main.cre.format.cre.Writer;
import main.cre.format.exporter.ExportFormat;
import main.cre.format.importer.ImportFormat;
import main.cre.ui.UISettings;

public class Im {

	@Test
	public void test() throws OutOfMemoryError, Exception {
		
		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File("data/savedrecs_JOI1.txt"));
		inputFiles.add(new File("data/savedrecs_JOI2.txt"));
		
		for (TABLE_IMPL_TYPES type: CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			
			ImportFormat.WOS.load(
					inputFiles, 
					new int[] {0, 0},
					true,
					new int[] {0, 0}, 
					true, 
					0, 
					Sampling.NONE
				);
			
			for (ExportFormat outFormat: ExportFormat.values()) {
				File outputFile = new File(String.format("data/test/out_%s_%s.%s", type.toString(), outFormat.toString(), outFormat.getFileExtension()));
				outFormat.save(outputFile, UISettings.get().getIncludePubsWithoutCRs());
			}
			
			File creFile = new File(String.format("data/test/out_%s.cre", type.toString()));
			Writer.save(creFile, UISettings.get().getIncludePubsWithoutCRs());
		}

	}
	
	

}

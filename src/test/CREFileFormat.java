package test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import main.cre.format.cre.Writer;
import main.cre.ui.UISettings;

public class CREFileFormat {

	
	
	private final static String DATAFOLDER = "testdata/";

	@Test
	public void testCRE_Reader_and_Writer () throws OutOfMemoryError, Exception {
		
		final String TESTFOLDER = DATAFOLDER + "tmp/";
		new File(TESTFOLDER).mkdirs();
		
		for (TABLE_IMPL_TYPES type: CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
		
			CRTable.get().setNpctRange(0);
			CRTable.get().getReader().load(new File (DATAFOLDER + "sciento_bearb.cre"));
			
			Writer.save(new File (TESTFOLDER + type.toString() + ".cre"), UISettings.get().getIncludePubsWithoutCRs());

			ZipFile zip_orig = new ZipFile (new File (DATAFOLDER + "sciento_bearb.cre"));
			ZipFile zip_dupl = new ZipFile (new File (TESTFOLDER + type.toString() + ".cre"));
			
			// a cre file is a zip file --> we are pairwise checking the zipped json files
			for (String name: new String[] { "crdata", "pubdata", "crmatch" }) {
				InputStream[] toCompare = new InputStream[] {
						zip_orig.getInputStream(zip_orig.getEntry(name + ".json")),
						zip_dupl.getInputStream(zip_dupl.getEntry(name + ".json")),
				};
				
				System.out.println("CRE/" + name);
				assertTrue (IOUtils.contentEquals(toCompare[0], toCompare[1]));
			}
			
		}
		
	}
	
}

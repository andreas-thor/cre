package cre.test.data.source;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.stream.Stream;

import com.opencsv.CSVWriter;

import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.type.PubType.PubColumn;
import cre.test.ui.CRTableView.CRColumn;
import cre.test.ui.StatusBar;

public class CRE_csv {

	
	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	public static void saveCR (String file_name) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSize());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		CRTable.get().getCR().sorted().forEach(cr -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr).getValue()).map(val -> val==null ? "" : String.valueOf(val)).toArray(String[]::new)); 
		});
		
		csv.close();
	}


	
	public static void savePub (String file_name) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSizePub());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(PubColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		CRTable.get().getPub(UserSettings.get().getIncludePubsWithoutCRs()).sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(PubColumn.values()).map(col -> col.prop.apply(pub).getValue()).map(val -> val==null ? "" : String.valueOf(val)).toArray(String[]::new)); 
		});
		
		csv.close();
	}
	
	
	
	public static void saveCRPub (String file_name) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSizePub());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext (Stream.concat (
				Arrays.stream(PubColumn.values()).map(col -> col.id),
				Arrays.stream(CRColumn.values()).map(col -> col.id))
		.toArray(String[]::new)); 
		
		CRTable.get().getPub().sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			
			pub.getCR().sorted().forEach(cr -> {
				csv.writeNext (Stream.concat (
					Arrays.stream(PubColumn.values()).map(col -> col.prop.apply(pub).getValue()).map(val -> val==null ? "" : String.valueOf(val)), 
					Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr).getValue()).map(val -> val==null ? "" : String.valueOf(val)) 
				).toArray(String[]::new)); 
			});
		});
		
		csv.close();
	}

	
	
	public static void saveGraph (String file_name) throws IOException {

		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(new String[] {"Year", "NCR", String.format("Median-%d", 2*UserSettings.get().getMedianRange()+1)});
		
		int[][] data = CRTable.get().getChartData();
		for (int i=0; i<data[0].length; i++) {
			csv.writeNext (new String[] {String.valueOf(data[0][i]), String.valueOf(data[1][i]), String.valueOf(data[2][i])});
		}
				
		csv.close();
	}
	
}

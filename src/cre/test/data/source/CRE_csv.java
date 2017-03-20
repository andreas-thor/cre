package cre.test.data.source;

import java.io.File;
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
	public static void saveCR (File file, CRTable crTab) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSize(), "Saving CSV file (Cited References) ...");
		
		
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		crTab.getCR().sorted().forEach(cr -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr).getValue()).map(val -> val==null ? "" : String.valueOf(val)).toArray(String[]::new)); 
		});
		
		csv.close();
		StatusBar.get().setValue("Saving CSV file (Cited References) done");
	}


	
	public static void savePub (File file, CRTable crTab) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSizePub(), "Saving CSV file (Citing Publications) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(PubColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		crTab.getPub().sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(PubColumn.values()).map(col -> col.prop.apply(pub).getValue()).map(val -> val==null ? "" : String.valueOf(val)).toArray(String[]::new)); 
		});
		
		csv.close();
		StatusBar.get().setValue("Saving CSV file (Citing Publications) done");
	}
	
	
	
	public static void saveCRPub (File file, CRTable crTab) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getSizePub(), "Saving CSV file (Cited References + Citing Publications) ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext (Stream.concat (
				Arrays.stream(PubColumn.values()).map(col -> col.id),
				Arrays.stream(CRColumn.values()).map(col -> col.id))
		.toArray(String[]::new)); 
		
		crTab.getPub().sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			
			pub.getCR().sorted().forEach(cr -> {
				csv.writeNext (Stream.concat (
					Arrays.stream(PubColumn.values()).map(col -> col.prop.apply(pub).getValue()).map(val -> val==null ? "" : String.valueOf(val)), 
					Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr).getValue()).map(val -> val==null ? "" : String.valueOf(val)) 
				).toArray(String[]::new)); 
			});
		});
		
		csv.close();
		StatusBar.get().setValue("Saving CSV file (Cited References + Citing Publications) done");
	}

	
	
	/**
	 * Save Graph data to CSV file
	 * @param file
	 * @throws IOException 
	 */
	
	public static void saveGraph (File file, CRTable crTab) throws IOException {

		StatusBar.get().setValue("Saving Graph as CSV file ...");
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(new String[] {"Year", "NCR", String.format("Median-%d", 2*UserSettings.get().getMedianRange()+1)});
		
		int[][] data = crTab.getChartData();
		for (int i=0; i<data[0].length; i++) {
			csv.writeNext (new String[] {String.valueOf(data[0][i]), String.valueOf(data[1][i]), String.valueOf(data[2][i])});
		}
				
		csv.close();
		StatusBar.get().setValue("Saving Graph as CSV file done");
	}
	
}

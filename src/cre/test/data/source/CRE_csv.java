package cre.test.data.source;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.opencsv.CSVWriter;

import cre.test.data.CRChartData;
import cre.test.data.CRChartData.SERIESTYPE;
import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.UserSettings;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType.PubColumn;
import cre.test.ui.CRTableView.CRColumn;
import cre.test.ui.StatusBar;

public class CRE_csv {

	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	
	
	public static void saveCR (String file_name, boolean includePubsWithoutCRs, Predicate<CRType> filter) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getNumberOfCRs());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		CRTable.get().getCR().filter(filter).sorted().forEach(cr -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr)).map(val -> val == null ? "" : String.valueOf(val.getValue())).toArray(String[]::new)); 
		});
		
		csv.close();
	}


	
	public static void savePub (String file_name, boolean includePubsWithoutCRs, Predicate<CRType> filter) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getNumberOfPubs());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(PubColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		CRTable.get().getPub(includePubsWithoutCRs).sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(PubColumn.values()).map(col -> col.prop.apply(pub)).map(val -> val==null ? "" : String.valueOf(val.getValue())).toArray(String[]::new)); 
		});
		
		csv.close();
	}
	
	
	
	public static void saveCRPub (String file_name, boolean includePubsWithoutCRs, Predicate<CRType> filter) throws IOException {

		StatusBar.get().initProgressbar(CRStats.getNumberOfPubs());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext (Stream.concat (
				Arrays.stream(PubColumn.values()).map(col -> col.id),
				Arrays.stream(CRColumn.values()).map(col -> col.id))
		.toArray(String[]::new)); 
		
		CRTable.get().getPub().sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			
			pub.getCR().filter(filter).sorted().forEach(cr -> {
				csv.writeNext (Stream.concat (
					Arrays.stream(PubColumn.values()).map(col -> col.prop.apply(pub)).map(val -> val==null ? "" : String.valueOf(val.getValue())), 
					Arrays.stream(CRColumn.values()).map(col -> col.prop.apply(cr)).map(val -> val==null ? "" : String.valueOf(val.getValue())) 
				).toArray(String[]::new)); 
			});
		});
		
		csv.close();
	}

	
	
	public static void saveGraph (String file_name, boolean includePubsWithoutCRs, Predicate<CRType> filter) throws IOException {

		/* TODO: Filter not supported yet */
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(new String[] {"Year", "NCR", String.format("Median-%d", 2*UserSettings.get().getMedianRange()+1), "AVG"});
		
		DecimalFormat avgFormat = new DecimalFormat("#.###");
		
		CRChartData data = null; // FIXME = CRTable.get().getChartData().
		for (int index=0; index<data.getRPY().length; index++) {
			String avg = data.getSeriesValue(SERIESTYPE.CNT, index) > 0 ? avgFormat.format((1.0d*data.getSeriesValue(SERIESTYPE.NCR, index))/data.getSeriesValue(SERIESTYPE.CNT, index)) : "";
			csv.writeNext (new String[] {
				String.valueOf(data.getRPYValue(index)), 
				String.valueOf(data.getSeriesValue(SERIESTYPE.NCR, index)), 
				String.valueOf(data.getSeriesValue(SERIESTYPE.MEDIANDIFF, index)), 
				avg});
		}
				
		csv.close();
	}

}

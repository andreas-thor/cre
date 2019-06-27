package main.cre.format.exporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.opencsv.CSVWriter;

import main.cre.data.CRChartData;
import main.cre.data.CRChartData.SERIESTYPE;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.extern.CRType_ColumnView;
import main.cre.data.type.extern.PubType_ColumnView;
import main.cre.ui.statusbar.StatusBar;

public class CSV {

	
	/**
	 * Save CR table to CSV file
	 * @param file
	 */
	
	
	public static void saveCR (String file_name, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException {

		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfCRs());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(CRType_ColumnView.CRColumn.values()).map(col -> col.id).toArray(String[]::new)); 

		CRTable.get().getCR().filter(filter).sorted(comp).forEach(cr -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(CRType_ColumnView.CRColumn.values()).map(col -> col.prop.apply(cr)).map(val -> val == null ? "" : String.valueOf(val.getValue())).toArray(String[]::new)); 
		});
		
		csv.close();
	}


	/**
	 * 
	 * @param file_name
	 * @param includePubsWithoutCRs
	 * @param filter IS IGNORED
	 * @param comp IS IGNORED
	 * @throws IOException
	 */
	public static void savePub (String file_name, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException {

		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfPubs());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> col.id).toArray(String[]::new)); 
		
		CRTable.get().getPub(includePubsWithoutCRs).sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			csv.writeNext(Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> col.prop.apply(pub)).map(val -> val==null ? "" : String.valueOf(val.getValue())).toArray(String[]::new)); 
		});
		
		csv.close();
	}
	
	
	
	public static void saveCRPub (String file_name, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException {

		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfPubs());
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext (Stream.concat (
				Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> col.id),
				Arrays.stream(CRType_ColumnView.CRColumn.values()).map(col -> col.id))
		.toArray(String[]::new)); 
		
		CRTable.get().getPub().sorted().forEach(pub -> {
			StatusBar.get().incProgressbar();
			
			pub.getCR().filter(filter).sorted(comp).forEach(cr -> {
				csv.writeNext (Stream.concat (
					Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> col.prop.apply(pub)).map(val -> val==null ? "" : String.valueOf(val.getValue())), 
					Arrays.stream(CRType_ColumnView.CRColumn.values()).map(col -> col.prop.apply(cr)).map(val -> val==null ? "" : String.valueOf(val.getValue())) 
				).toArray(String[]::new)); 
			});
		});
		
		csv.close();
	}

	
	/**
	 * 
	 * @param file_name
	 * @param includePubsWithoutCRs
	 * @param filter
	 * @param comp IS IGNORED
	 * @throws IOException
	 */
	public static void saveGraph (String file_name, boolean includePubsWithoutCRs, Predicate<CRType<?>> filter, Comparator<CRType<?>> comp) throws IOException {

		/* TODO: Filter not supported yet */
		
		CRChartData data = CRTable.get().getChartData();
		
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"));
		csv.writeNext(new String[] {"Year", "NCR", String.format("Median-%d", 2*data.getMedianRange()+1), "AVG"});
		
		DecimalFormat avgFormat = new DecimalFormat("#.###");
		
		
		for (int index=0; index<data.getRPYLength(); index++) {
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

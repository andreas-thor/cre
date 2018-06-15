package main.cre.data.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import main.cre.Exceptions.BadResponseCodeException;
import main.cre.data.CRTable;
import main.cre.data.type.CRType;
import main.cre.data.type.CRType_Member;
import main.cre.data.type.PubType;
import main.cre.ui.statusbar.StatusBar;

/**
 * Extra Methode, die alle Properties einer CR ausliest
 * Dann zuerst parse("unstructured")
 * Dann �berschreiben der CR-Properties mit Werten von JSON
 * @author Andreas
 *
 */


/**
 * Documentation: https://github.com/CrossRef/rest-api-doc/blob/master/api_format.md 
 */


public class Crossref extends ImportReader {

	private JsonArray items;
	private int currentItemIndex;
	
	@Override
	public void init(File file) throws IOException {
		
		JsonReader reader = Json.createReader(new FileInputStream(file));
		JsonObject msg = reader.readObject();

		// check if msg is ok
		
		
		// store all items (=publications) as Json-Array; will be step-wise processed by computeNextEntry
		this.items = msg.getJsonObject("message").getJsonArray("items");
		this.currentItemIndex = 0;
		
		reader.close();
		super.init(file);
	}
	
	
	/**
	 * We assume that the parser is at the beginning of the next item / publication, 
	 * i.e., the first event should be START_OBJECT
	 */
	
	@Override
	protected void computeNextEntry() throws IOException {
		
		this.entry = null;

		// all items processed
		if (this.currentItemIndex >= this.items.size()) {
			return;
		}
		
		// item must be a JSON object
		JsonValue jsonObject = this.items.get(this.currentItemIndex);
		if (jsonObject.getValueType() != ValueType.OBJECT) {
			return;
		}

		JsonObject item = (JsonObject) jsonObject;
		this.entry = new PubType();
		this.entry.length = item.toString().length();
		
		if (item.getJsonArray("title") != null) {
			if (item.getJsonArray("title").size()>0) {
				this.entry.setTI(item.getJsonArray("title").getString(0));
			}
		}
		
		
		// "published-print" : [ [ year, month, day ] ]
		if (item.get("published-print") != null) {
			this.entry.setPY(item.getJsonObject("published-print").getJsonArray("date-parts").getJsonArray(0).getInt(0));
		} else {
			if (item.get("published-online") != null) {
				this.entry.setPY(item.getJsonObject("published-online").getJsonArray("date-parts").getJsonArray(0).getInt(0));
			}			
		}
		
		
		if (item.get("reference") != null) {
			item.getJsonArray("reference").stream().map(jsonCR -> (JsonObject)jsonCR).forEach(jsonCR -> { 

				CRType cr = null;
				
				// default values from parsing unstructured string
				if (jsonCR.get("unstructured") != null) {
					cr = Scopus_csv.parseCR (jsonCR.getString("unstructured").replace("\n", "").replace("\r", ""));
				}
				
				if (cr == null) {
					cr = new CRType_Member();
				}
				
				// overwrite with JSON data
				if (jsonCR.get("year") != null) {
					try {
						cr.setRPY(Integer.parseInt (jsonCR.getString("year")));
					} catch (NumberFormatException e) { }
				}
				if (jsonCR.get("DOI") != null) {
					cr.setDOI(jsonCR.getString("DOI"));
				}
				if (jsonCR.get("volume") != null) {
					cr.setVOL(jsonCR.getString("volume"));
				}
				if (jsonCR.get("first-page") != null) {
					cr.setPAG(jsonCR.getString("first-page"));
				}
				if (jsonCR.get("author") != null) {
					cr.setAU_A(jsonCR.getString("author"));
				}
				
				if (jsonCR.get("journal-title") != null) {
					cr.setJ(jsonCR.getString("journal-title"));
				}
				if (jsonCR.get("article-title") != null) {
					cr.setTI(jsonCR.getString("article-title"));
				}
				
				if (cr.getCR()==null) {
					cr.setCR(jsonCR.toString());
				}
				this.entry.addCR (cr, true);
				
			});	
		}
		
		this.currentItemIndex++;
	}
	
	
	private static List<File> downloadByFilter (List<String> filter) throws IOException, BadResponseCodeException  {
		
		List<File> result = new ArrayList<File>();
		
		// set cache path 
		Path cachePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("CRExplorerDownload");
		try {
			Files.createDirectory(cachePath);
		} catch (java.nio.file.FileAlreadyExistsException e) {
			// not a problem
		} catch (IOException e2) {
			e2.printStackTrace();
		}		
		System.out.println("Cache path is " + cachePath.toString());

		StatusBar.get().setValue(String.format ("Downloading CrossRef data to %s ...", cachePath.toString()));
		
		int total_results = 0;
		int offset = 0;
		int rows_per_Request = 100;		// 1.000 is maximum per page
		
		while (offset<10000) {	// 10.000 is the maximal offset according to API
		
			if (CRTable.get().isAborted()) {
				StatusBar.get().setValue("Download aborted by user");
				return new ArrayList<File>();		// return empty result
			}
			
			if (offset>0) {
				StatusBar.get().initProgressbar(total_results);
				StatusBar.get().incProgressbar(offset);
			}
			
			String url = "https://api.crossref.org/works";
			url += "?filter=" + filter.stream().collect(Collectors.joining( "," ));
			url += "&rows=" + rows_per_Request;
			url += "&offset=" + offset;
			url += "&mailto=thor@hft-leipzig.de";
			System.out.println(url);
			
			String filename = String.format("%s.crossref", DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS").format(LocalDateTime.now()));  
			File file = cachePath.resolve(filename).toFile();
			org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), file);
			result.add(file);
			
			
			// get the number of total_results from the first downloaded file
			if (total_results == 0) {
				try {
					JsonReader reader = Json.createReader(new FileInputStream(file));
					JsonObject msg = reader.readObject();
					total_results = msg.getJsonObject("message").getInt("total-results");
					reader.close();
				} catch (Exception e) {
					// if we do not get the total-results --> do nothing --> we will not download any more files
				}
			}
			
			if ((offset + rows_per_Request)<total_results) {
				offset += rows_per_Request;		// adjust offset to get next chunk of data
			} else {
				break; 		// do not download any more files
			}
		}
		
		return result;
		
	}
	
	



	public static List<File> download(String[] DOI, String issn, int[] range) throws IOException, BadResponseCodeException {
		
		List<String> filter = new ArrayList<String>();
		
		Arrays.stream(DOI).forEach(s -> filter.add("doi:" + s));
		if (issn.length()>0) 	filter.add("issn:" + issn);
		if (range[0]!=-1) 		filter.add("from-print-pub-date:" + range[0]);
		if (range[1]!=-1) 		filter.add("until-print-pub-date:" + range[1]);
		
		return downloadByFilter(filter);

	}
	

}

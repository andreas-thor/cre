package main.cre.data.source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParser;

import groovy.json.internal.Exceptions;
import main.cre.Exceptions.BadResponseCodeException;
import main.cre.data.type.CRType;
import main.cre.data.type.CRType_Member;
import main.cre.data.type.PubType;

/**
 * Extra Methode, die alle Properties einer CR ausliest
 * Dann zuerst parse("unstructured")
 * Dann Überschreiben der CR-Properties mit Werten von JSON
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
		this.entry.setTI(item.getJsonArray("title").getString(0));
		
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

				CRType cr = new CRType_Member();
				
				// default values from parsing unstructured string
				if (jsonCR.get("unstructured") != null) {
					cr = Scopus_csv.parseCR (jsonCR.getString("unstructured").replace("\n", "").replace("\r", ""));
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
				
				this.entry.addCR (cr, true);
				
			});	
		}
		
		this.currentItemIndex++;
	}
	
	
	private static List<File> downloadByFilter (String filter) throws IOException, BadResponseCodeException  {
		
		List<File> result = new ArrayList<File>();
		
		String url = "https://api.crossref.org/works";
		url += "?filter=" + filter;
		url += "&rows=1000";
		url += "&mailto=thor@hft-leipzig.de";
		System.out.println(url);
		
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "CRExplorer (http://www.crexplorer.net; mailto:thor@hft-leipzig.de)");
		
		Path cachePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("CRExplorerCrossrefDownload");
		try {
			Files.createDirectory(cachePath);
		} catch (java.nio.file.FileAlreadyExistsException e) {
			// not a problem
		} catch (IOException e2) {
			e2.printStackTrace();
		}
			
		
		if (con.getResponseCode()!=200) {
			throw new BadResponseCodeException(con.getResponseCode());
		}
		
		System.out.println("Cache path is " + cachePath.toString());

		String timestamp = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS").format(LocalDateTime.now());  
		
		File f = cachePath.resolve(timestamp + ".crossref").toFile();
		BufferedWriter outFile = Files.newBufferedWriter(f.toPath());
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			outFile.write(inputLine);
		}
		in.close();
		outFile.close();
		
		
		con.disconnect();
		result.add(f);
			
		return result;
		
	}
	
	
	public static List<File> downloadByDOI (String[] DOI) throws IOException, BadResponseCodeException  {
		
		List<File> result = new ArrayList<File>();
		
		if (DOI.length==0) {
			return result;
		}
		String doiList = Arrays.stream(DOI).map(s -> "doi:" + s).collect(Collectors.joining( "," ));

		return downloadByFilter(doiList);
	}


	public static List<File> downloadByISSNAndRange(String issn, int[] range) throws IOException, BadResponseCodeException {
		
		String filter = "issn:" + issn;
		
		if (range[0]!=-1) {
			filter += ",from-print-pub-date:" + range[0];
		}
		if (range[1]!=-1) {
			filter += ",until-print-pub-date:" + range[1];
		}
		
		return downloadByFilter(filter);

	}
	

}

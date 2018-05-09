package cre.test.data.source;

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

import cre.test.data.type.CRType;
import cre.test.data.type.CRType_Member;
import cre.test.data.type.PubType;

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
					cr.setRPY(Integer.parseInt (jsonCR.getString("year")));
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
	
	

	public static List<File> downloadByDOI (String[] DOI, boolean useCache) throws IOException  {
		
		/*
		 * ow does it work? Simple. You can do one of two things to get directed to the "polite pool":

    Include a "mailto" parameter in your query. For example:

https://api.crossref.org/works?filter=has-full-text:true&mailto=GroovyBib@example.org

    Include a "mailto:" in your User-Agent header. For example:

GroovyBib/1.1 (https://example.org/GroovyBib/; mailto:GroovyBib@example.org) BasedOnFunkyLib/1.4.

Note that this only works if you query the API using HTTPS. You really should be doing that anyway (wags finger).
		 */
		
		List<File> result = new ArrayList<File>();
		
		if (DOI.length==0) {
			return result;
		}
		
		String query = Arrays.stream(DOI).map(s -> "doi:" + s).collect(Collectors.joining( "," ));
		
		System.out.println("http://api.crossref.org/works?filter=" + query);
		URL url = new URL("http://api.crossref.org/works?filter=" + query);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
//		int status = con.getResponseCode();
//		System.out.println("Status Code is " + status);

		
		Path cachePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("CRExplorerCrossrefDownload");
		try {
			Files.createDirectory(cachePath);
		} catch (java.nio.file.FileAlreadyExistsException e) {
			// not a problem
		} catch (IOException e2) {
			e2.printStackTrace();
		}
			
		
		
		System.out.println("Cache path is " + cachePath.toString());

		File f = cachePath.resolve("1").toFile();
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
	

}

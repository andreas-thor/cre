package cre.test.data.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.json.Json;
import javax.json.stream.JsonParser;

import cre.test.data.type.PubType;

public class Crossref extends ImportReader {

	private JsonParser parser;
	
	@Override
	public void init(File file) throws IOException {
		
		parser = Json.createParser(new FileInputStream(file));
		
		String key = "";
		boolean firstItemFound = false;
		
		// move parser to first item (=first publication)
		while (parser.hasNext() && !firstItemFound) {

			switch (parser.next()) {

			case KEY_NAME: 
				key = parser.getString();
				System.out.println("key=" + key);
				break;
				
			case START_ARRAY:
				if (key.equals("items")) {
					firstItemFound = true;
				}
				break;
			default:
				break;
			}
		}
		
		
		super.init(file);
	}
	
	
	
	@Override
	protected void computeNextEntry() throws IOException {
		this.entry = null;
		
		int level = 0;
		String[] keyStack = new String[] { "", "", "", "", "", "", "", "", "", ""};
		
		while (parser.hasNext()) {

			switch (parser.next()) {
			
			case KEY_NAME:		
				keyStack[level] = parser.getString(); 
				break;
				
			case START_OBJECT: 	
				
				if (level == 0) {
					this.entry = new PubType();
				}
				
				level++;
				break;
				
			case END_OBJECT:
				level--;
				if (level == 0) {
					return;
				}
				
			case VALUE_STRING: 
				
				switch (level) {
				case 1:
					switch (keyStack[level]) {
					case "title": this.entry.setTI(parser.getString()); break;
					
					}
					break;
				case 2:
					if ((keyStack[1].equals("reference")) && (keyStack[2].equals("unstructured"))) {
						this.entry.addCR(Scopus_csv.parseCR(parser.getString()), true);
						System.out.println("added CR");
					}
					break;
				}
				break;
				
			default:
				break;

			}
			
		}
		
		
	}
	
	@Override
	public void close() throws IOException {
		parser.close();
		super.close();
	}

}

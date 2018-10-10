package main.cre.data.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import main.cre.data.type.PubType;

public abstract class ImportReader implements Iterator<PubType> {

	protected PubType entry = null;
	protected BufferedReader br = null;
	protected boolean stop = false;
	
	protected abstract void computeNextEntry() throws IOException;
	
	public void init(File file) throws IOException {
		this.init (new FileInputStream(file));
	}
	

	public void init(InputStream is) throws IOException {
		this.entry = null;
		this.br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		this.stop = false;
		computeNextEntry();
	}
	
	
	
	public void stop () {
		this.stop = true;
	}
	
	public boolean hasNext() {
		if (stop) return false;
		return entry != null;
	}
	
	
	public PubType next() {
		PubType result = entry;
		
		try {
			computeNextEntry();
		} catch (IOException e) {
			entry = null;
		}
		
		return result;
	}
	
	
	
	public void close() throws IOException {
		br.close();
	}
	
	public Iterable<PubType> getIterable () { 
		return () -> this;
	}
	
}

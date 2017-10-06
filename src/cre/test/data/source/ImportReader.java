package cre.test.data.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import cre.test.data.CRTable;
import cre.test.data.type.PubType;

public abstract class ImportReader implements Iterator<PubType> {

	protected PubType entry = null;
	
	protected BufferedReader br = null;
	protected int maxCR = 0;
	protected int countCR = 0;
	protected int maxPub = 0;
	protected int countPub = 0;
	protected int[] yearRange = new int[] {0,0};
	protected double ratioCR = 1d;
	
	protected abstract void computeNextEntry() throws IOException;
	
	public void init(File file, int maxCR, int maxPub, int[] yearRange, double ratioCR) throws IOException {
		this.br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		this.maxCR = maxCR;
		this.countCR = 0;
		this.maxPub = maxPub;
		this.countPub = 0;
		this.yearRange = yearRange;
		this.ratioCR = ratioCR;
		computeNextEntry();
	}
	
	public boolean hasNext() {
		
		// check for max number of pubs and CRs
		if (CRTable.get().isAborted()) return false;
		if ((this.maxPub>0) && (this.countPub >= this.maxPub)) return false;
		if ((this.maxCR>0) && (this.countCR >= this.maxCR)) return false;
		
		return entry != null;
	}
	
	
	public PubType next() {
		PubType result = entry;
		
		try {
			computeNextEntry();
		} catch (IOException e) {
			entry = null;
		}
		
		this.countPub++;
		this.countCR += result.getSizeCR();
		return result;
	}
	
	
	
	public void close() throws IOException {
		br.close();
	}
	
	public Iterable<PubType> getIterable () { 
		return () -> this;
	}
	
}

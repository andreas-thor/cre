package cre.data.source

import cre.data.PubType;
import groovy.transform.CompileStatic

@CompileStatic
public abstract class FileImport {

	
	protected int[] yearRange
	protected BufferedReader br
	protected PubType fe
	
	
	public FileImport (int[] yearRange, BufferedReader br) {
		this.yearRange = yearRange
		this.br = br
		this.fe = null
	}
	
//	public abstract CRType parseLine(String line)
	
	public abstract boolean hasNextPub()
	
	public PubType getNextPub() {
		return this.fe
	} 
	

}

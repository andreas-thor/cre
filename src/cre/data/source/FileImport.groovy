package cre.data.source

import cre.data.PubType;
import groovy.transform.CompileStatic

@CompileStatic
public abstract class FileImport {

	
	protected int[] yearRange
	protected BufferedReader br
	
	
	public FileImport (int[] yearRange, BufferedReader br) {
		this.yearRange = yearRange
		this.br = br
	}
	
	public abstract PubType getNextPub() 
	

	
	
}

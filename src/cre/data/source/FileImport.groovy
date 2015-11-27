package cre.data.source

import groovy.transform.CompileStatic;

import java.util.regex.Matcher

import cre.data.CRType;

@CompileStatic
public abstract class FileImport {

	int noOfPubs
	int[] yearRange
	
	public FileImport (int[] yearRange) {
		noOfPubs = 0
		this.yearRange = yearRange
	}
	
	public abstract CRType parseLine(String line)
	

}

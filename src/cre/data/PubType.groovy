package cre.data


import groovy.transform.CompileStatic

@CompileStatic
public class PubType {

	public List<CRType> crList
	public StringBuffer export
	public int year
	public int length
	public HashMap<String, String> entries
	
	public PubType() {
		super()
		this.crList = new ArrayList<CRType>()
		this.year = 0
		this.length = 0
		this.export = new StringBuffer()
		this.entries = [:]

	}
}
package cre.data


import groovy.transform.CompileStatic

@CompileStatic
public class PubType {

	public List<CRType> crList
	public int year
	public int length
	
	public PubType() {
		super()
		this.crList = new ArrayList<CRType>()
		this.year = 0
		this.length = 0
		

	}
}
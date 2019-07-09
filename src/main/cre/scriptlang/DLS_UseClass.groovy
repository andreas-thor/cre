package main.cre.scriptlang;

class DSL_UseClass {

	public static Class use (String filename) {
		return  this.class.classLoader.parseClass(new File (new File(getClass().protectionDomain.codeSource.location.path).parent, filename))
	}
}

package test;

import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Test;

import main.cre.scriptlang.ScriptExecutionEngine;

public class Script {

	
	
	@Test
	public void checkScript () throws CompilationFailedException, IOException {
		
		
		
		ScriptExecutionEngine.main(new String[] {"testdata/script/import_export.crs", "-db=scriptdb"});

//		ScriptExecutionEngine.main(new String[] {"testdata/script/first.crs", "-db=scriptdb"});
		
	}
}

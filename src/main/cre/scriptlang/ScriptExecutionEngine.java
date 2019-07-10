package main.cre.scriptlang;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import main.cre.CitedReferencesExplorer;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;

public class ScriptExecutionEngine {

	public static void main(String[] args) throws CompilationFailedException, IOException {

		Binding bind = new Binding();
		CompilerConfiguration config = new CompilerConfiguration();  
		config.setScriptBaseClass("main.cre.scriptlang.DSL");                                  
		GroovyShell shell = new GroovyShell(ScriptExecutionEngine.class.getClassLoader(), bind, config);             

		System.out.println(CitedReferencesExplorer.title);

		for (String arg: args) {
			if (arg.toLowerCase().startsWith("-db")) {
				CRTable.type = TABLE_IMPL_TYPES.DB;
				String[] split = arg.split("=");
				CRTable.name = (split.length==2) ? split[1] : "test";
				System.out.println(String.format("***DB MODE*** (%s)", CRTable.name)); 
			}
		}
		
		//shell.run (new File (this.args[0]));
		shell.evaluate(new File (args[0]));

	}

}

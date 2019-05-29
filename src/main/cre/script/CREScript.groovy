package main.cre.script

import org.codehaus.groovy.control.CompilerConfiguration

import main.cre.CitedReferencesExplorer
import main.cre.data.type.abs.CRTable
import main.cre.data.type.abs.CRTable.TABLE_IMPL_TYPES
import main.cre.ui.statusbar.StatusBar
import main.cre.ui.statusbar.StatusBarText

//StatusBarText status = new StatusBarText()
//StatusBar.get().setUI(status);

def bind = new Binding()
//bind.setVariable("status", status)
def config = new CompilerConfiguration()  
config.scriptBaseClass = 'main.cre.script.CREDSL'                                  
def shell = new GroovyShell(this.class.classLoader, bind, config)             

println CitedReferencesExplorer.title

if (this.args.contains("-db")) { 
	CRTable.type = TABLE_IMPL_TYPES.DB;
	println "***DB MODE***"
}

shell.evaluate(new File (this.args[0]));


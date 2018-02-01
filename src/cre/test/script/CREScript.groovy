package cre.test.script

import org.codehaus.groovy.control.CompilerConfiguration

import cre.test.CitedReferencesExplorer
import cre.test.data.type.CRType
import cre.test.ui.StatusBar
import cre.test.ui.StatusBarText

StatusBarText status = new StatusBarText()
StatusBar.get().setUI(status);

def bind = new Binding()
bind.setVariable("status", status)
def config = new CompilerConfiguration()  
config.scriptBaseClass = 'cre.test.script.CREDSL'                                  
def shell = new GroovyShell(this.class.classLoader, bind, config)             

println CitedReferencesExplorer.title
shell.evaluate(new File (this.args[0]));


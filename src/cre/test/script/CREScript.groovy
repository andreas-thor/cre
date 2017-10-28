package cre.test.script

import org.codehaus.groovy.control.CompilerConfiguration
import cre.test.ui.StatusBar
import cre.test.ui.StatusBarText


StatusBar.get().setUI(new StatusBarText());

def config = new CompilerConfiguration()  
config.scriptBaseClass = 'cre.test.script.CREDSL'                                  
def shell = new GroovyShell(this.class.classLoader, config)             

shell.evaluate(new File (this.args[0]));


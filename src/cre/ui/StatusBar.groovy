package cre.ui 

import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic

import javax.swing.*
import javax.swing.border.BevelBorder


/**
 * Factory class for status panel
 * @author thor
 *
 */

class StatusBar {

	
		
	public JPanel pan
	private JLabel sblabel
	private JProgressBar sbpb
	private JLabel sbinfo
	

	public StatusBar() {
		
		SwingBuilder sb = new SwingBuilder()
		
		pan = sb.panel (preferredSize:[300, 28], border: BorderFactory.createBevelBorder(BevelBorder.LOWERED)) {
			gridBagLayout()
			sblabel = label (text:"   ",
				constraints: gbc(gridx:0,gridy:0,fill:java.awt.GridBagConstraints.BOTH,anchor:java.awt.GridBagConstraints.WEST,weightx:0, weighty:0))
			sbpb = progressBar(preferredSize:[300, 16], minimum: 0, maximum: 100,value:0,
				constraints: gbc(gridx:1,gridy:0,fill:java.awt.GridBagConstraints.BOTH,anchor:java.awt.GridBagConstraints.EAST,weightx:1, weighty:0))
			sbinfo = label (text:"   ",
				constraints: gbc(gridx:2,gridy:0,fill:java.awt.GridBagConstraints.BOTH,anchor:java.awt.GridBagConstraints.WEST,weightx:0, weighty:0))
		}
	}

		
	@CompileStatic
	public void setValue (String label, int value, String info=null) {
		sblabel.text = "   ${label}       "
		sbpb.setValue(value)
		if (info != null) sbinfo.text = "       ${info}   "
	}
	
	
}

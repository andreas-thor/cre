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
	
	private long blockSize;
	private long blockCount;
	private Date date;
	private String label;
	
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
	public void setValue (String label, long value, String info=null) {
		setValue(label, value, info, new Date());
	}

	public void setValue (String label, String info=null) {
		setValue(label, 0L, info, new Date());
	}

		
	public void setValue (String label, long value, String info, Date d) {
		sblabel.text = String.format("   %1\$s: %2\$s       ", d, label);
		sbpb.setValue((int)value)
		if (info != null) sbinfo.text = "       ${info}   "
	}
	
	
	public void initProgressbar (long maxSize, String label) {
		
		this.blockSize = maxSize/20;
		this.blockCount = 0;
		this.date = new Date();
		this.label = label;
		this.setValue(label, 0, "", this.date);
		
		
	}
	
	public void updateProgressbar (long count) {
		if (blockCount*blockSize<count) {
			this.setValue(label, 5*blockCount, "", date);
			blockCount++;
		}
	}
	
	
}

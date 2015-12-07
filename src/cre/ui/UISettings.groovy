package cre.ui 

import groovy.lang.Closure;
import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic

import java.awt.BasicStroke
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.util.Map.Entry;
import java.util.prefs.Preferences
import java.text.DecimalFormat
import java.text.NumberFormat

import javax.swing.*

import org.codehaus.groovy.util.StringUtil;
import org.jfree.chart.ChartPanel
import org.jfree.util.StringUtils;

import cre.CitedReferencesExplorer
import cre.data.CRTable
import cre.data.CRType

/**
 * Helper class that holds all properties that are bound to a UI element
 * @author thor
 *
 */

class UISettings {
	
	private Preferences userPrefs
	private JTable tab
	private JFrame mainFrame
	
	private ChartPanel chpan
	private byte[] attributes
	private byte[] lines
	private byte[] seriesSizes
	private File lastFileDir
	private int maxCR
	private int[] yearRange
	private int digits;
	
	
	public UISettings(JTable tab, ChartPanel chpan, JFrame mainFrame) {
		this.tab = tab
		this.chpan = chpan
		this.mainFrame = mainFrame
		
		userPrefs = Preferences.userNodeForPackage( CitedReferencesExplorer.getClass() )
		setAttributes(userPrefs.getByteArray("attributes", CRType.attr.collect {k, v -> 1} as byte[]))
		setLines(userPrefs.getByteArray("lines", CRTable.line.collect {k, v -> 1} as byte[]))
		setSeriesSizes(userPrefs.getByteArray("seriesSizes", ([6,3] as byte[])))
		setLastDirectory(new File (userPrefs.get("lastFileDir", "")))
		setMaxCR(userPrefs.getInt("maxCR", 100000))
		setYearRange([userPrefs.getInt("minYear", 0), userPrefs.getInt("maxYear", 0)] as int[])
		setDigits (userPrefs.getInt("digits", 3));
		
		mainFrame.setSize(userPrefs.getInt("WindowWidth", 800), userPrefs.getInt("WindowHeight", 600))
		mainFrame.setLocation(userPrefs.getInt("WindowX", 0), userPrefs.getInt("WindowY", 0))
	}
	
	
	public void setWindowPos () {
		userPrefs.putInt("WindowX", mainFrame.getX());
		userPrefs.putInt("WindowY", mainFrame.getY());
		userPrefs.putInt("WindowWidth", mainFrame.getWidth());
		userPrefs.putInt("WindowHeight", mainFrame.getHeight());
	}

	
	public byte[] getAttributes () {
		return attributes
	}
	
	
	public void setAttributes (byte[] attributes) {
		this.attributes = attributes
		userPrefs.putByteArray ("attributes", attributes)
		
		//  adjust visibile attribute columns
		attributes.eachWithIndex { byte v, int idx ->
			tab.getTableHeader().getColumnModel().getColumn(idx+2).with { // 2 = offset to ignore first two columns (VI, CO)
				setMaxWidth((v==1) ? 800 : 0)
				setMinWidth((v==1) ?  30 : 0)
				setWidth((v==1) ? tab.getWidth() : 0)
				setPreferredWidth((v==1) ? tab.getWidth() : 0)
				setResizable(v==1)
			}
		}
	}
	
	public byte[] getLines () {
		return lines
	}
	
	public void setLines (byte[] lines) {
		if ((lines[0]==0) && (lines[1]==0)) lines[0] = 1 // at least one line must be visible 
		this.lines = lines
		userPrefs.putByteArray ("lines", lines)
		
		// store and adjust visible graph lines
		chpan.getChart().getXYPlot().getRenderer().with {
			lines.eachWithIndex { byte v, int idx -> setSeriesVisible (idx, new Boolean (v==1)) }
		}
	}
	
	
	public byte[] getSeriesSizes () {
		return seriesSizes
	}
	
	public void setSeriesSizes (byte[] seriesSizes) {
		this.seriesSizes = seriesSizes.collect { byte it -> Math.max(it, 1) }
		userPrefs.putByteArray ("seriesSizes", this.seriesSizes)
		
		// adjust line sizes in the chart
		chpan.getChart().getXYPlot().getRenderer().with {
				setSeriesShape(0, new Rectangle2D.Double(-this.seriesSizes[1]/2,-this.seriesSizes[1]/2,this.seriesSizes[1],this.seriesSizes[1]))
				setSeriesStroke(0, new BasicStroke(this.seriesSizes[0]))
				setSeriesShape(1, new Ellipse2D.Double(-this.seriesSizes[1]/2,-this.seriesSizes[1]/2,this.seriesSizes[1],this.seriesSizes[1]))
				setSeriesStroke(1, new BasicStroke(this.seriesSizes[0]))
		}
		
	}
	
	
	public File getLastDirectory () {
		return lastFileDir
	}
	
	public void setLastDirectory (File f) {
		lastFileDir = f
		userPrefs.put("lastFileDir", lastFileDir.getAbsolutePath())
		
		// set the default directory for "Save as png/jpg"
		try { chpan.setDefaultDirectoryForSaveAs(lastFileDir) } catch (Exception e) { } 
	}
	
	public int getMaxCR () {
		return maxCR
	}
	
	public void setMaxCR (int maxCR) {
		this.maxCR = maxCR
		userPrefs.putInt("maxCR", maxCR)
	}
	
	
	public int[] getYearRange () {
		return yearRange
	}
	
	public void setYearRange (int[] yearRange) {
		this.yearRange = yearRange
		userPrefs.putInt("minYear", yearRange[0])
		userPrefs.putInt("maxYear", yearRange[1])
	}
	
	
	public int getDigits () {
		return this.digits
	}

	public void setDigits (int digits) {
		this.digits = Math.min (Math.max(digits, 1), 10)	// must be in [1,10]
		userPrefs.putInt ("digits", this.digits)
		TableFactory.formatter = new DecimalFormat( "##0." + ((1..this.digits).collect { "0" }.join()) + "%" )
	}
	
	
	
	private static JFormattedTextField createTF (int value, boolean grouping=true) {
		NumberFormat f = NumberFormat.getNumberInstance(Locale.ENGLISH)
		f.setGroupingUsed(grouping)
		JFormattedTextField result = new JFormattedTextField(f)
		result.setColumns(10)
		result.setValue(value)
		result
	}
	

	
	
	public JDialog createSettingsDlg (JFrame f /*, byte[] colVal, byte[] lineVal, byte[] seriesSize, int maxCR, int[] yearRange, Closure action*/) {

		SwingBuilder sb = new SwingBuilder()
		JButton defBtn
		JDialog setDlg = sb.dialog(modal:true, title: "Settings")
		setDlg.getContentPane().add (
			sb.panel(border: BorderFactory.createEmptyBorder(3, 3, 3, 3)) {
			
				tableLayout(id:'m', cellpadding:10) {
					tr { td (align:'center', colspan:2) {

						tabbedPane(id: 'tabs', tabLayoutPolicy:JTabbedPane.SCROLL_TAB_LAYOUT) {
							
							sb.panel(name: 'Table', border: BorderFactory.createEmptyBorder(3, 3, 3, 3)) {
								tableLayout(cellpadding:0) {
									tr { td (align:'left') { sb.panel (border: BorderFactory.createTitledBorder("Show Columns in Table")) {
										tableLayout (id: 'columns', cellpadding: 0 ) {
											Iterator attIt = CRType.attr.iterator()
											Map.Entry<String,String> entry
											int idx = 0
											while (attIt.hasNext()) {
												tr {
													(0..2).each { 
														if (attIt.hasNext()) {											
															entry = (Map.Entry) attIt.next()
															td { checkBox(text: entry.getValue(), selected: attributes[idx]==1) }
															idx++
														}
													}
												}
											} 
										}
									}}}
									tr { td (align:'left') { sb.panel (border: BorderFactory.createTitledBorder("Show Values")) {
										tableLayout (id: 'val', cellpadding: 0 ) {
											tr {
												td (align:'right') { label(text:"Number of Digits:  ") }
												td (align:'left') {  widget (createTF(digits, false))  }
											}
										}
									}}}
									
								}	
							}
						
							sb.panel(name: 'Chart', border: BorderFactory.createEmptyBorder(3, 3, 3, 3)) {
								tableLayout(cellpadding:0) {
									tr { td (align:'left') { sb.panel (border: BorderFactory.createTitledBorder("Show Lines in Chart")) {
										tableLayout (id: 'lines', cellpadding: 0 ) {
											CRTable.line.eachWithIndex { name, label, idx -> tr { td { checkBox(id: name, text: label, selected: lines[idx]==1) } } }
										}
									} } }
									tr { td (align:'left') { sb.panel (border: BorderFactory.createTitledBorder("Size of Chart Lines")) {
										tableLayout (id: 'seriesSizes', cellpadding: 0 ) {
											tr {
												td (align:'right') { label(text:"Stroke Size:  ") }
												td (align:'left') {  widget (createTF(seriesSizes[0]))  }
											}
											tr {
												td (align:'right') { label(text:"Shape Size:  ") }
												td (align:'left') {  widget (createTF(seriesSizes[1]))  }
											}
											
										}
									}}}
								}
							}
							
							sb.panel(name: 'Import', border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
								
								tableLayout(cellpadding:0) {
									
									tr { td (align:'left') { sb.panel (border: BorderFactory.createTitledBorder("Restrict WoS Import of Cited References")) {
										tableLayout (id: 'imp', cellpadding: 0 ) {
											tr {
												td (align:'right') { label(text:"Maximum Number of Cited References:  ") }
												td (align:'left') {  widget (createTF(maxCR))  }
											}
											tr {
												td (align:'right') { label(text:"Minimum Publication Year of Cited References:  ") }
												td (align:'left') {  widget (createTF(yearRange[0], false))  }
											}
											tr {
												td (align:'right') { label(text:"Maximum Publication Year of Cited References:  ") }
												td (align:'left') {  widget (createTF(yearRange[1], false))  }
											}
										}
									}}}
								}
							}
	
						}
					} }
					
					tr {
						td (align:'right') {
							defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: {
								setAttributes (((JPanel) sb.columns).getComponents().collect { JCheckBox cb ->  cb.isSelected()?1:0} as byte[])
								setDigits ((Integer) (((JPanel) sb.val).getComponents())[1].getValue())
								setLines (((JPanel) sb.lines).getComponents().collect { JCheckBox cb ->  cb.isSelected()?1:0} as byte[])
								setSeriesSizes ((0..1).collect { (Integer) ((JPanel) sb.seriesSizes).getComponents()[2*it+1].getValue() } as byte[])
								setMaxCR ((Integer) (((JPanel) sb.imp).getComponents())[1].getValue())
								setYearRange ((1..2).collect { (Integer) ((JPanel) sb.imp).getComponents()[2*it+1].getValue() } as int[])
								setDlg.dispose()
							})
						}
						td (align:'left') {
							button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { setDlg.dispose() })
						}
					}
				
				}
			}
		)
		setDlg.getRootPane().setDefaultButton(defBtn)
		setDlg.pack()
		setDlg.setLocationRelativeTo(f)
		return setDlg

	}

	
}


//	class UICol {
//
//		// table columns
//		boolean ID = true
//		boolean CR = true
//		boolean RPY = true
//		boolean N_CR = true
//		boolean PERC_YR = true
//		boolean PERC_ALL = true
//		boolean AU = true
//		boolean AU_L = true
//		boolean AU_F = true
//		boolean J = true
//		boolean J_N = true
//		boolean J_S = true
//		boolean VOL = true
//		boolean PAG = true
//		boolean DOI = true
//		boolean CID2 = true
//		boolean CID_S = true
//
//		// chart lines
//		boolean NCR_PER_YEAR = true
//		boolean DEV_FROM_MED = true
//	}
//	@Bindable UICol showCol = new UICol()
//
//
//
//
//
//
//	// Matcher configuration
//	class UIMatcher {
//		String attribute
//		String similarity
//		String threshold
//	}
//	@Bindable UIMatcher[] uiMatchers = [new UIMatcher(attribute:CRType.attr['AU_L']+"+"+CRType.attr['J_N'], similarity:'Levenshtein', threshold:0.5), new UIMatcher(attribute:'None', similarity:'Levenshtein', threshold:0.0)]
//
//
//	class UIMatcherOverall{
//		String globalThreshold
//		boolean useClustering
//	}
//	@Bindable UIMatcherOverall uiMatcherOverall = new UIMatcherOverall(globalThreshold:"0.5", useClustering:false)
//	class UIClustering {
//		double progress
//	}
//	@Bindable UIClustering uiClustering = new UIClustering(progress:70.7)
	

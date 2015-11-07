package cre 

import groovy.transform.CompileStatic

import java.awt.BasicStroke
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.util.prefs.Preferences

import javax.swing.JTable

import org.jfree.chart.ChartPanel

/**
 * Helper class that holds all properties that are bound to a UI element
 * @author thor
 *
 */

@CompileStatic
class UISettings {
	
	private Preferences userPrefs
	private JTable tab
	private ChartPanel chpan
	private byte[] attributes
	private byte[] lines
	private byte[] seriesSizes
	private File lastFileDir
	private int maxCR
	
	public UISettings(JTable tab, ChartPanel chpan) {
		this.tab = tab
		this.chpan = chpan
		
		userPrefs = Preferences.userNodeForPackage( CitedReferencesExplorer.getClass() )
		setAttributes(userPrefs.getByteArray("attributes", CRType.attr.collect {k, v -> 1} as byte[]))
		setLines(userPrefs.getByteArray("lines", CRTable.line.collect {k, v -> 1} as byte[]))
		setSeriesSizes(userPrefs.getByteArray("seriesSizes", ([6,3] as byte[])))
		setLastDirectory(new File (userPrefs.get("lastFileDir", "")))
		setMaxCR(userPrefs.getInt("maxCR", 100000))
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
				setPreferredWidth((v==1) ? tab.getWidth() : 0)
				setResizable(v==1)
			}
		}
	}
	
	public byte[] getLines () {
		return lines
	}
	
	public void setLines (byte[] lines) {
		this.lines = lines
		userPrefs.putByteArray ("lines", lines)
		
		// store and adjust visibile graph lines
		chpan.getChart().getXYPlot().getRenderer().with {
			lines.eachWithIndex { byte v, int idx -> setSeriesVisible (idx, new Boolean (v==1)) }
		}
	}
	
	
	public byte[] getSeriesSizes () {
		return seriesSizes
	}
	
	public void setSeriesSizes (byte[] seriesSizes) {
		this.seriesSizes = seriesSizes
		userPrefs.putByteArray ("seriesSizes", seriesSizes)
		
		// adjust line sizes in the chart
		chpan.getChart().getXYPlot().getRenderer().with {
				setSeriesShape(0, new Rectangle2D.Double(-seriesSizes[1]/2,-seriesSizes[1]/2,seriesSizes[1],seriesSizes[1]))
				setSeriesStroke(0, new BasicStroke(seriesSizes[0]))
				setSeriesShape(1, new Ellipse2D.Double(-seriesSizes[1]/2,-seriesSizes[1]/2,seriesSizes[1],seriesSizes[1]))
				setSeriesStroke(1, new BasicStroke(seriesSizes[0]))
		}
		
	}
	
	
	public File getLastDirectory () {
		return lastFileDir
	}
	
	public void setLastDirectory (File f) {
		lastFileDir = f
		userPrefs.put("lastFileDir", lastFileDir.getAbsolutePath())
		
		// set the default directorz for "Save as png/jpg"
		chpan.setDefaultDirectoryForSaveAs(f)
	}
	
	public int getMaxCR () {
		return maxCR
	}
	
	public void setMaxCR (int maxCR) {
		this.maxCR = maxCR
		userPrefs.putInt("maxCR", maxCR)
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
	

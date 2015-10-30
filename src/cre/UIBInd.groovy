package cre 

import groovy.beans.Bindable
import groovy.transform.CompileStatic;

/**
 * Helper class that holds all properties that are bound to a UI element
 * @author thor
 *
 */

class UIBind {
	class UICol {
		
		// table columns
		boolean ID = true
		boolean CR = true
		boolean RPY = true
		boolean N_CR = true
		boolean PERC_YR = true
		boolean PERC_ALL = true
		boolean AU = true
		boolean AU_L = true
		boolean AU_F = true
		boolean J = true
		boolean J_N = true
		boolean J_S = true
		boolean VOL = true
		boolean PAG = true
		boolean DOI = true
		boolean CID2 = true
		boolean CID_S = true
		
		// chart lines
		boolean NCR_PER_YEAR = true
		boolean DEV_FROM_MED = true
	}
	@Bindable UICol showCol = new UICol()
	

	
	// Matcher configuration
	class UIMatcher {
		String attribute
		String similarity
		String threshold
	}
	@Bindable UIMatcher[] uiMatchers = [new UIMatcher(attribute:CRType.attr['AU_L']+"+"+CRType.attr['J_N'], similarity:'Levenshtein', threshold:0.5), new UIMatcher(attribute:'None', similarity:'Levenshtein', threshold:0.0)]
	
	
	class UIMatcherOverall{
		String globalThreshold
		boolean useClustering
	}
	@Bindable UIMatcherOverall uiMatcherOverall = new UIMatcherOverall(globalThreshold:"0.5", useClustering:false)

	
	// Configuration of interactive matching
	class UIMatchConfig {
		double threshold = 0.75
		boolean useVol = false
		boolean usePag = false
		boolean useDOI = false
	}
	@Bindable UIMatchConfig uiMatchConfig = new UIMatchConfig()
	
		 
//	class UIClustering {
//		double progress
//	}
//	@Bindable UIClustering uiClustering = new UIClustering(progress:70.7)
	
	
	// Range configuration: [Remove Year, Remove NCR, Filter Year]
	class UIRange {
		String from
		String to
	}
	@Bindable UIRange[] uiRanges = [new UIRange(from:'', to:''), new UIRange(from:'', to:''), new UIRange(from:'', to:'')]
	
	
	File lastFileDir = new File("D:/Dev/CitationTools/lutz/")
}

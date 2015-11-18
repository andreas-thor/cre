package cre.ui

import groovy.beans.Bindable
import groovy.transform.CompileStatic

/**
 * Helper class that holds all properties that are bound to a UI element
 * @author thor
 *
 */

@CompileStatic
class UIBind {
	
	// Configuration of interactive matching
	class UIMatchConfig {
		double threshold = 0.75
		boolean useVol = false
		boolean usePag = false
		boolean useDOI = false
	}
	@Bindable UIMatchConfig uiMatchConfig = new UIMatchConfig()
	
	// Range configuration: [Remove Year, Remove NCR, Filter Year]
	class UIRange {
		String from = ''
		String to = ''
	}
	@Bindable UIRange[] uiRanges = [new UIRange(), new UIRange(), new UIRange()]
	
	
	
}

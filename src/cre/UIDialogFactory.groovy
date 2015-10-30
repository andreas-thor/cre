package cre 

import groovy.swing.SwingBuilder
import cre.UIBind.UICol;
import cre.UIBind.UIRange;

import javax.swing.*

class UIDialogFactory {

	
	
	
	static JDialog createWaitDlg (JFrame f) {
	
		SwingBuilder sb = new SwingBuilder()
		JDialog waitDlg = sb.dialog(modal:true, title: "Waiting", defaultCloseOperation:JFrame.DO_NOTHING_ON_CLOSE )
		waitDlg.getContentPane().add (
			sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
				label(text:"Please Wait")
			}
		)
		waitDlg.pack()
		waitDlg.setLocationRelativeTo(f)
		return waitDlg
	}
	
	

	static JDialog createInfoDlg (JFrame f, CRTable crTable) {
		
			SwingBuilder sb = new SwingBuilder()
			JButton defBtn
			JDialog infoDlg = sb.dialog(modal:true, title: "Info")
			infoDlg.getContentPane().add (
				sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
					
					tableLayout(cellpadding:10) {
						
						tr {
							td (align:'center') {
								panel(border:BorderFactory.createTitledBorder("Cited References Dataset")) {
									tableLayout (cellpadding: 3 ){
										crTable.getInfo().each { l, value ->
											tr {
												td (align:'right') { label(text:"${l}:  ") }
												td (colfill:true) { textField(columns: 10, text: value, editable: false ) }
											}
										}
									}
								}
							 }
						}
						tr {
							td (align:'center') {
								defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: { infoDlg.dispose() })
							}
						}
					}
				}
			)
			infoDlg.getRootPane().setDefaultButton(defBtn)
			infoDlg.pack()
			infoDlg.setLocationRelativeTo(f)
			return infoDlg
		}
	
	
	
	static JDialog createShowColDlg (JFrame f, UICol showCol, String title, Map items, Closure action) {
		
		SwingBuilder sb = new SwingBuilder()
		JButton defBtn
		JDialog colDialog = sb.dialog(modal:true, title: title)
		colDialog.getContentPane().add (
			sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
				
				tableLayout(id:'m', cellpadding:10) {
					
					tr {
						td (align:'center', colspan:2) {
							panel(border:BorderFactory.createTitledBorder("Visibility")) {
								tableLayout (cellpadding: 3 ){
									items.eachWithIndex { name, label, idx ->
										tr {
											td { checkBox(id: name, text: label, selected: bind (name, source:showCol, mutual:true)) }
										}
									}
								}
							}
						 }
					}
					tr {
						td (align:'right') {
							defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: {
								
	//							JTable tab = (JTable)sb.tab
								action()
								colDialog.dispose()
							})
						}
						td (align:'left') {
							button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { colDialog.dispose() })
						}
					}
				}
	
			}
		)
			
		colDialog.getRootPane().setDefaultButton(defBtn)
		colDialog.pack()
		colDialog.setLocationRelativeTo(f)
		return colDialog
			
	}
	
	
	
	static JDialog createIntRangeDlg (JFrame f, UIRange range, String title, String value, ArrayList maxRange, Closure dlgAction) {
		
		SwingBuilder sb = new SwingBuilder()
		JButton defBtn
		JDialog rangeDialog = sb.dialog(modal:true, title: title)
		rangeDialog.getContentPane().add (
			sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
				
				tableLayout(id:'m', cellpadding:10) {
					
					tr {
						td (align:'center', colspan:2) {
							panel(border:BorderFactory.createTitledBorder(value), preferredSize:[225, 95]) {
								tableLayout (cellpadding: 3 ){
									tr {
										td (align:'right') { label(text:"From:") }
										td (align:'left') { textField(id:'tfFrom', columns: 10, text: bind('from', source: range, mutual:true) ) }
										td (colfill:true) { checkBox(text:'Minimum', actionPerformed: { e -> sb.tfFrom.enabled = !e.source.selected; if (e.source.selected) sb.tfFrom.text = maxRange[0] } ) }
									}
									tr {
										td (align:'right') { label(text:"To:") }
										td (align:'left') { textField(id:'tfTo', columns: 10, text: bind('to', source: range, mutual:true) ) }
										td (colfill:true) { checkBox(text:'Maximum', actionPerformed: { e -> sb.tfTo.enabled = !e.source.selected; if (e.source.selected) sb.tfTo.text = maxRange[1] } ) }
									}
								}
							}
						 }
					}
					tr {
						td (align:'right') {
							defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: {
								range.with {
									if (from.isInteger() && to.isInteger()) {
										int min = from.toInteger()
										int max = to.toInteger()
										if (min<=max) {
											rangeDialog.dispose()
											dlgAction (min, max)
										} else {
											optionPane().showMessageDialog(null, "No valid range (${max} is smaller than ${min})!")
										}
									} else {
										optionPane().showMessageDialog(null, "No valid values (${from},${to})")
									}
									
								}
							})
						}
						td (align:'left') {
							button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { rangeDialog.dispose() })
						}
					}
				}
			}
		)
		
		rangeDialog.getRootPane().setDefaultButton(defBtn)
		rangeDialog.pack()
		rangeDialog.setLocationRelativeTo(f)
		return rangeDialog
	}
	
}



//
//	static JDialog createMatcherDlg (UIBind uibind, Closure dlgAction) {
//		
//		/* Matcher configuration is currently NOT USED */
//		
//		SwingBuilder sb = new SwingBuilder()
//		JButton defBtn
//		
//		def matcherDlg =  { n ->
//			sb.panel(border:BorderFactory.createTitledBorder("Matcher #${n+1}")) {
//				tableLayout (cellpadding: 3 ){
//					tr {
//						td (align:'right') { label(text:"Attribute:") }
//						td (colfill:true) { comboBox (enabled: false, items:['None', CRType.attr['AU_L']+"+"+CRType.attr['J_N'], CRType.attr['J_N']], selectedItem: bind('attribute', source: uibind.uiMatchers[n], mutual:true)) }
//					}
//					tr {
//						td (align:'right') { label(text:"Similarity:") }
//						td (colfill:true) { comboBox (enabled: false, items:['Levenshtein','Trigram .. not yet'], selectedItem: bind('similarity', source: uibind.uiMatchers[n], mutual:true)) }
//					}
//					tr {
//						td (align:'right') { label(text:"Threshold:") }
//						td (colfill:true) { textField(enabled: false, columns: 10, text: bind('threshold', source: uibind.uiMatchers[n], mutual:true) ) }
//					}
//				}
//			}
//		}
//		
//		def getMatcher = { n -> // matcherDlg panel
//	
//			uibind.uiMatchers[n].with {
//				if (attribute == 'None') return []
//				if ((threshold.isDouble()) && (threshold.toDouble()>=0) && (threshold.toDouble()<=1)) {
//					return [1] // [CRTable.attr.find { it.value == attribute}.key, similarity, threshold.toDouble()]
//				} else {
//					sb.optionPane().showMessageDialog(null, "No valid threshold ${threshold} (must be a number between 0 and 1)!")
//					return null
//				}
//			}
//			
//		}
//	
//
//		
//		JDialog matchDialog = sb.dialog(modal:true, title: "Choose matcher configuration")
//		matchDialog.metaClass.clickOK = { defBtn.doClick() }
//
//		matchDialog.getContentPane().add (
//			sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
//				
//				tableLayout(id:'m', cellpadding:10) {
//					
//					tr {
//						td (colspan:2) { matcherDlg(0) }
//						td { /* matcherDlg(1)*/  }
//					}
//					tr {
//						td (colspan:2) {
//							/*
//							panel(border:BorderFactory.createTitledBorder("Overall")) {
//								tableLayout (cellpadding: 3 ){
//									tr {
//										td (align:'right') { label(text:"Threshold:") }
//										td (colfill:true) { textField(columns: 10, text: bind('globalThreshold', source: uibind.uiMatcherOverall, mutual:true) ) }
//									}
//									tr {
//										td (align:'right') { label(text:"Cluster Method:") }
//										td (colfill:true) { checkBox(text: "Sub-clustering only", selected: bind ('useClustering', source:uibind.uiMatcherOverall, mutual:true)) }
//									}
//								}
//							}
//							*/
//						}
//					}
//					tr {
//						td (align:'right') {
//							defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: {
//								
//								uibind.uiMatcherOverall.with {
//									if (!((globalThreshold.isDouble()) && (globalThreshold.toDouble()>=0) && (globalThreshold.toDouble()<=1))) {
//										sb.optionPane().showMessageDialog(null, "No valid overall threshold ${globalThreshold} (must be a number between 0 and 1)!")
//									}
//								}
//								
//								def matchers = (0..1).collect { getMatcher(it) }.findAll { it != [] }
//								if (matchers.size()==0) {
//									sb.optionPane().showMessageDialog(null, "No matcher specified!")
//								} else {
//									if (!matchers.contains(null)) {
//										if ((uibind.uiMatcherOverall.globalThreshold.isDouble()) && (uibind.uiMatcherOverall.globalThreshold.toDouble()>=0) && (uibind.uiMatcherOverall.globalThreshold.toDouble()<=1)) {
//											matchDialog.dispose()
//											dlgAction (matchers, uibind.uiMatcherOverall.globalThreshold.toDouble(), uibind.uiMatcherOverall.useClustering)
//										} else {
//											sb.optionPane().showMessageDialog(null, "No valid overall threshold ${uibind.uiMatcherOverall.globalThreshold} (must be a number between 0 and 1)!")
//										}
//									}
//								}
//							})
//						}
//						td (align:'left') {
//							button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { matchDialog.dispose() })
//						}
//					}
//				}
//			})
//			
//			matchDialog.getRootPane().setDefaultButton(defBtn)
//			matchDialog.pack()
//			return matchDialog
//		
//	}
//	

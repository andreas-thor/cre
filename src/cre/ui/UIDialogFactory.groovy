package cre.ui

import groovy.swing.SwingBuilder

import java.awt.event.ActionEvent
import java.text.NumberFormat

import javax.swing.*

import cre.data.*
import cre.ui.UIBind.UIRange

class UIDialogFactory {



	/**
	 * Wait dialog
	 * @param f parent main frame
	 * @param action Triggered action (defined as closure) when cancel button is clicked
	 * @return
	 */
	static JDialog createWaitDlg (JFrame f, Closure action) {

		SwingBuilder sb = new SwingBuilder()
		JDialog waitDlg = sb.dialog(modal:true, title: "Waiting", defaultCloseOperation:JFrame.DO_NOTHING_ON_CLOSE )
		waitDlg.getContentPane().add (
				sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
					tableLayout(cellpadding:10) {
						tr { td (align:'center') { label(text:"Please Wait") } }
						tr { td (align:'center') { button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { action() }) } }
					}
				}
				)
		waitDlg.pack()
		waitDlg.setLocationRelativeTo(f)
		return waitDlg
	}



	/**
	 * @param f parant main frame
	 * @param info Information to show (list of [info label, info value] pairs) 
	 * @return
	 */
	static JDialog createInfoDlg (JFrame f, Map<String, Integer> info) {

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
										info.each { l, value ->
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

	

	
	
	static JDialog createThresholdDlg (JFrame f, String title, String value, Closure dlgAction) {
		
		NumberFormat percentFormat = NumberFormat.getPercentInstance()
		percentFormat.setMinimumFractionDigits(1)
		JFormattedTextField tval = new JFormattedTextField(percentFormat)
		tval.setColumns(10)
		tval.setValue(0)
		
		
				SwingBuilder sb = new SwingBuilder()
				JButton defBtn
				JDialog threshDialog = sb.dialog(modal:true, title: title)
				threshDialog.getContentPane().add (
						sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
		
							tableLayout(cellpadding:10) {
		
								tr {
									td (align:'center', colspan:2) {
										panel(border:BorderFactory.createTitledBorder(value), preferredSize:[225, 95]) {
											tableLayout (id: 'thresh', cellpadding: 3 ){
												tr {
													td (align:'right') { comboBox(items:['<', '<=', '=', '>=', '>']) }
													td (align:'left') { widget (tval) }
												}
											}
										}
									}
								}
								tr {
									td (align:'right') {
										defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: {
											JComboBox b = (JComboBox) ((JPanel) sb.thresh).getComponent(0) 
											JFormattedTextField c = (JFormattedTextField) ((JPanel) sb.thresh).getComponent(1) 
											dlgAction ((b.getSelectedItem()) as String, c.getValue() as double)
											threshDialog.dispose()
										})
									}
									td (align:'left') {
										button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { threshDialog.dispose() })
									}
								}
							}
						}
						)
		
				threshDialog.getRootPane().setDefaultButton(defBtn)
				threshDialog.pack()
				threshDialog.setLocationRelativeTo(f)
				return threshDialog
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

	 
	
	public static AbstractAction showCRDetails (JFrame f, CRTable crTab) {
		AbstractAction save = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				JTable tab = (JTable)e.getSource()
				if (tab.getSelectedRow()!=-1) {
					CRType cr = crTab.crData[tab.convertRowIndexToModel(tab.getSelectedRow())]
//					JOptionPane.showConfirmDialog (null, crTab.crData[tab.convertRowIndexToModel(tab.getSelectedRow())].toString(), "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
					
					 
					SwingBuilder sb = new SwingBuilder()
					JButton defBtn
					JDialog CRDlg = sb.dialog(modal:true, title: "CR Info")
					CRDlg.getContentPane().add (
						sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
							tableLayout(cellpadding:3) {
								tr {
									td (align:'center') {
										panel(border:BorderFactory.createTitledBorder("Cited Reference")) {
											tableLayout (cellpadding: 3 ) {
												tr {
													td (align:'right') { label(text:"ID:  ") }
													td (colfill:true) { label(text:cr.ID) }
												}
												tr { 
													td (align:'right') { label(text:"CR:  ") } 
													td (colfill:true) { label(text:cr.CR) }	
												}
												tr { 
													td (align:'right') { label(text:"AU:  ") } 
													td (colfill:true) { label(text:cr.AU) }	
												}
												tr { 
													td (align:'right') { label(text:"AU_F:  ") } 
													td (colfill:true) { label(text:cr.AU_F) }	
												}
												tr { 
													td (align:'right') { label(text:"AU_L:  ") } 
													td (colfill:true) { label(text:cr.AU_L) }	
												}
												tr { 
													td (align:'right') { label(text:"AU_A:  ") } 
													td (colfill:true) { label(text:cr.AU_A) }	
												}
												tr { 
													td (align:'right') { label(text:"TI:  ") } 
													td (colfill:true) { label(text:cr.TI) }	
												}
												tr { 
													td (align:'right') { label(text:"J:  ") } 
													td (colfill:true) { label(text:cr.J) }	
												}
												tr { 
													td (align:'right') { label(text:"J_N:  ") } 
													td (colfill:true) { label(text:cr.J_N) }	
												}
												tr { 
													td (align:'right') { label(text:"J_S:  ") } 
													td (colfill:true) { label(text:cr.J_S) }	
												}
												tr { 
													td (align:'right') { label(text:"N_CR:  ") } 
													td (colfill:true) { label(text:cr.N_CR) }	
												}
												tr { 
													td (align:'right') { label(text:"RPY:  ") } 
													td (colfill:true) { label(text:cr.RPY) }	
												}
												tr { 
													td (align:'right') { label(text:"PAG:  ") } 
													td (colfill:true) { label(text:cr.PAG) }	
												}
												tr { 
													td (align:'right') { label(text:"VOL:  ") } 
													td (colfill:true) { label(text:cr.VOL) }	
												}
												tr { 
													td (align:'right') { label(text:"DOI:  ") } 
													td (colfill:true) { label(text:cr.DOI) }	
												}
												tr { 
													td (align:'right') { label(text:"CID2:  ") } 
													td (colfill:true) { label(text:cr.CID2) }	
												}
												tr { 
													td (align:'right') { label(text:"CID_S:  ") } 
													td (colfill:true) { label(text:cr.CID_S) }	
												}
											}
										}
									}
								}
								tr {
									td (align:'center') {
										defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: { CRDlg.dispose() })
									}
								}
							}
						}
					)
					CRDlg.getRootPane().setDefaultButton(defBtn)
					CRDlg.pack()
					CRDlg.setLocationRelativeTo(f)
					CRDlg.visible = true
			
					
					
					
				}
				
//				JOptionPane.showConfirmDialog (null, "File exists! " + crTab.getInfoString() + "Overwrite?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION)
//					TestTableKeyBinding.this.table, "Action Triggered.");
//				table.editingCanceled(null);
			}
		}
		return save
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

//	static JDialog createShowColDlg (JFrame f, UICol showCol, String title, Map items, Closure action) {
//
//		SwingBuilder sb = new SwingBuilder()
//		JButton defBtn
//		JDialog colDialog = sb.dialog(modal:true, title: title)
//		colDialog.getContentPane().add (
//				sb.panel(border: BorderFactory.createEmptyBorder(10, 10, 10, 10)) {
//
//					tableLayout(id:'m', cellpadding:10) {
//
//						tr {
//							td (align:'center', colspan:2) {
//								panel(border:BorderFactory.createTitledBorder("Visibility")) {
//									tableLayout (cellpadding: 0 ){
//										items.eachWithIndex { name, label, idx ->
//											tr {
//												td { checkBox(id: name, text: label, selected: bind (name, source:showCol, mutual:true)) }
//											}
//										}
//									}
//								}
//							}
//						}
//						tr {
//							td (align:'right') {
//								defBtn = button(preferredSize:[100, 25], text:'Ok', actionPerformed: {
//
//									//							JTable tab = (JTable)sb.tab
//									action()
//									colDialog.dispose()
//								})
//							}
//							td (align:'left') {
//								button(preferredSize:[100, 25], text:'Cancel', actionPerformed: { colDialog.dispose() })
//							}
//						}
//					}
//				}
//				)
//
//		colDialog.getRootPane().setDefaultButton(defBtn)
//		colDialog.pack()
//		colDialog.setLocationRelativeTo(f)
//		return colDialog
//	}


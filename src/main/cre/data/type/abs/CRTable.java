package main.cre.data.type.abs;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import main.cre.data.type.db.CRTable_DB;
import main.cre.data.type.mm.CRTable_MM;

public abstract class CRTable {
 
	public static enum COMPARATOR { LT, LTE, EQ, GTE, GT };
	
	public static enum TABLE_IMPL_TYPES { MM, DB }
	
	private boolean aborted;
	private int npctRange;
	
	public static TABLE_IMPL_TYPES type = TABLE_IMPL_TYPES.MM;
	
	public static CRTable get() {
		
		switch (type) {
		case MM: return CRTable_MM.get();
		case DB: return CRTable_DB.get();
		default: return null;
		}
	}
	
	/**
	 * Initialize empty CRTable
	 */
	
	public abstract void init();
	
	public abstract Stream<? extends CRType> getCR();

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public abstract Stream<? extends PubType> getPub (boolean includePubsWithoutCRs);
	
	public Stream<? extends PubType> getPub() {
		return this.getPub(false);
	}	

	public CRType addCR(CRType cr) {
		return this.addCR(cr, false);
	}
	
	public abstract CRType addCR(CRType cr, boolean checkForDuplicatesAndSetId);
	
	
	
	
	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
//	public abstract PubType addPub (PubType pub, boolean addCRs);
	
	public abstract PubType addPub (PubType pub, boolean addCRs, boolean checkForDuplicates);
	

	
	/**
	 * Merge CRs based on clustering
	 */

	public abstract void merge ();
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	
	public abstract void updateData () throws OutOfMemoryError;

	
	
	

	
	
	
	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	public abstract void removeCR (List<CRType> toDelete);
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public abstract void retainCR (List<CRType> toRetain);
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	public abstract void removeCRWithoutYear ();

	
	/**
	 * Remove all CRS within a given RPY range
	 * @param range
	 */
	public abstract void removeCRByYear (int[] range);

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	public abstract void removeCRByN_CR(int[] range);
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold
	 */
	
	public abstract void removeCRByPERC_YR (String comp, double threshold);
	
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	public abstract void removePubByCR (List<CRType> selCR);
	
	
	
	
	/**
	 * Remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	public abstract void removePubByCitingYear (int[] range);
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public abstract void filterByYear (int[] range);
	

	
	public abstract void filterByCluster (List<CRType> sel);
	
	

	
	
	public abstract void setShowNull (boolean showNull);
	
	public abstract void showAll();
	
	

	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}


	public int getNpctRange() {
		return npctRange;
	}


	public void setNpctRange(int npctRange) {
		if (npctRange>=0) {
			this.npctRange = npctRange;
		}
	}
	
	

}


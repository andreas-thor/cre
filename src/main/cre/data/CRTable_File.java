package main.cre.data;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import main.cre.data.type.CRType;
import main.cre.data.type.PubType;

public class CRTable_File extends CRTable {

	private static CRTable_File crTab = null;

	
	public static CRTable_File get() {
		if (crTab == null) {
			crTab = new CRTable_File();
		}
		return crTab;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<CRType> getCR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<PubType> getPub(boolean includePubsWithoutCRs) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public CRType addCR(CRType cr, boolean checkForDuplicatesAndSetId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PubType addPub(PubType pub, boolean addCRs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PubType addPub(PubType pub, boolean addCRs, boolean checkForDuplicates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void merge() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateData() throws OutOfMemoryError {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCR(Predicate<CRType> cond) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCR(List<CRType> toDelete) {
		// TODO Auto-generated method stub

	}

	@Override
	public void retainCR(List<CRType> toRetain) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRWithoutYear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRByYear(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRByN_CR(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRByPERC_YR(String comp, double threshold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePubByCR(List<CRType> selCR) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePubByCitingYear(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void filterByYear(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void filterByCluster(List<CRType> sel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShowNull(boolean showNull) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAll() {
		// TODO Auto-generated method stub

	}

}

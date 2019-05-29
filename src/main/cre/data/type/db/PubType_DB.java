package main.cre.data.type.db;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import main.cre.data.type.abs.PubType;

public class PubType_DB extends PubType<CRType_DB> {

	
	@Override
	public Stream<CRType_DB> getCR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSizeCR() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addCR(CRType_DB cr, boolean inverse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removeCR(CRType_DB cr, boolean inverse) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeCRByYear(int[] range, boolean keepCRsWithoutYear, boolean inverse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCRByProbability(float probability, int offset, AtomicLong noToImportCRs, AtomicLong noAvailableCRs, AtomicInteger currentOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllCRs(boolean inverse) {
		// TODO Auto-generated method stub
		
	}

}

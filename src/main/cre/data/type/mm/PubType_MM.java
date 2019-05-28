package main.cre.data.type.mm;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import javafx.beans.value.ObservableValue;
import main.cre.data.CRStatsInfo;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.PubType;
import main.cre.ui.CRTableView.ColDataType;

public abstract class PubType_MM extends PubType {

	public static enum PubColumn {
		
		ID 	("ID", "ID", ColDataType.INT, PubType::getIDProp),
		PT 	("PT", "PT", ColDataType.STRING, PubType::getPTProp),
		
		AU 	("AU", "AU", ColDataType.STRING, PubType::getAUProp),
		AF 	("AF", "AF", ColDataType.STRING, PubType::getAFProp),
		C1 	("C1", "C1", ColDataType.STRING, PubType::getC1Prop),
		EM 	("EM", "EM", ColDataType.STRING, PubType::getEMProp),
		AA 	("AA", "AA", ColDataType.STRING, PubType::getAAProp),
		
		TI 	("TI", "TI", ColDataType.STRING, PubType::getTIProp),
		PY 	("PY", "PY", ColDataType.INT, PubType::getPYProp),
		SO 	("SO", "SO", ColDataType.STRING, PubType::getSOProp),
		VL 	("VL", "VL", ColDataType.STRING, PubType::getVLProp),
		IS 	("IS", "IS", ColDataType.STRING, PubType::getISProp),
		AR 	("AR", "AR", ColDataType.STRING, PubType::getARProp),
		BP 	("BP", "BP", ColDataType.INT, PubType::getBPProp),
		EP 	("EP", "EP", ColDataType.INT, PubType::getEPProp),
		PG 	("PG", "PG", ColDataType.INT, PubType::getPGProp),
		TC 	("TC", "TC", ColDataType.INT, PubType::getTCProp),
		
		
		DI 	("DI", "DI", ColDataType.STRING, PubType::getDIProp),
		LI 	("LI", "LI", ColDataType.STRING, PubType::getLIProp),
		AB 	("AB", "AB", ColDataType.STRING, PubType::getABProp),
		DE 	("DE", "DE", ColDataType.STRING, PubType::getDEProp),
		DT 	("DT", "DT", ColDataType.STRING, PubType::getDTProp),
		FS 	("FS", "FS", ColDataType.STRING, PubType::getFSProp),
		UT 	("UT", "UT", ColDataType.STRING, PubType::getUTProp);

		
		public String id;
		public String title;
		public ColDataType type;
		public Function<PubType, ObservableValue<?>> prop;
		
		PubColumn(String id, String title, ColDataType type, Function<PubType, ObservableValue<?>> prop) {
			this.id = id;
			this.title = title;
			this.type = type;
			this.prop = prop;
		}
		
		public String getSQLCreateTable() {
			StringBuffer sb = new StringBuffer();
			sb.append("pub_");
			sb.append (this.id);
			sb.append(" ");
			switch (this.type) {
				case INT: sb.append ("int"); break;
				case STRING: sb.append ("varchar"); break;
				default:
			}
			return sb.toString();
		}
		
	}
	


	public int length; // approx. size for import status bar

	
	private Set<CRType_MM> crList;

	private boolean flag;
	
	
	public static PubType_MM create() {
		return new PubType_Prop();
//		return new PubType_Tiny();
	}
	
	public PubType_MM() {
		super();
		
		crList = new LinkedHashSet<CRType_MM>();
		setFlag(false);
	}
	
	
	@Override
	public String toLineString() {
		return String.format("[%d] %s: %s (%d)", getID(), getAUProp().getValue(), getTI(), getPY());
	}

	@Override
	public Stream<CRType_MM> getCR() {
		return crList.stream();
	}

	@Override
	public int getSizeCR() {
		return crList.size();
	}

	/**
	 * Adds a CR to a PUB
	 * 
	 * @param cr
	 *            to be added
	 * @param inverse
	 *            true, if this PUB should also be added to the publist of the
	 *            CR
	 */

	@Override
	public void addCR(CRType cr, boolean inverse) {
		if (cr == null)
			return;
		if (inverse) {
			cr.addPub(this, false);
		}
		this.crList.add((CRType_MM) cr);
	}

	/**
	 * Removes a CR from a PUB
	 * 
	 * @param cr
	 *            to be removed
	 * @param inverse
	 *            true, if this PUB should also be removed from the publist of
	 *            the CR
	 */
	
	@Override
	public boolean removeCR(CRType cr, boolean inverse) {
		if (cr == null)
			return false;
		if (inverse) {
			cr.removePub(this, false);
		}
		return this.crList.remove(cr);
	}

	@Override
	public void removeCRByYear (int[] range, boolean keepCRsWithoutYear, boolean inverse) {
		
		
		this.crList.removeIf(cr -> {
			
			boolean toBeRemoved = false;
			if (cr.getRPY()==null) {
				toBeRemoved = !keepCRsWithoutYear;
			} else {
				int rpy = cr.getRPY().intValue();
				if ((range[0]!=CRStatsInfo.NONE) && (range[0]>rpy)) toBeRemoved = true;
				if ((range[1]!=CRStatsInfo.NONE) && (range[1]<rpy)) toBeRemoved = true;
			}
			
			if (toBeRemoved && inverse) {
				cr.removePub(this, false);
			}
			return toBeRemoved;	
		});
		
	}
	
	@Override
	public void removeCRByProbability (float probability, int offset, AtomicLong noToImportCRs, AtomicLong noAvailableCRs, AtomicInteger currentOffset) {
		
		this.crList.removeIf(cr -> {
			
			boolean remove = true;
			
			if ((noToImportCRs.get()>0) && (probability*noAvailableCRs.get() <= 1.0f*noToImportCRs.get())) {
			
				if (currentOffset.get()==offset) {
					noToImportCRs.decrementAndGet();
					currentOffset.set(0);
					remove = false;
				} else {
					currentOffset.incrementAndGet();
				}
			}
			
			noAvailableCRs.decrementAndGet();
			return remove;
			
		});
	}

	@Override
	public void removeAllCRs(boolean inverse) {
		if (inverse) {
			crList.forEach(cr -> cr.removePub(this, false));
		}
		this.crList.clear();
	}

	
	

//	public int getLength() {
//		return length;
//	}

//	public Set<CRType> getCrList() {
//		return crList;
//	}





	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}

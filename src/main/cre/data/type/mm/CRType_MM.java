package main.cre.data.type.mm;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.PubType;

public abstract class CRType_MM extends CRType  {

	
	
	
	private FORMATTYPE type = null;	
	private boolean flag;
	
	private Set<PubType_MM> pubList;
	
	public CRType_MM() {
		pubList = new HashSet<PubType_MM>();
	}
	
	
	public static int mein() {
		return 3;
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public FORMATTYPE getType() {
		return type;
	}

	public void setType(FORMATTYPE type) {
		this.type = type;
	}

	public Stream<PubType_MM> getPub() {
		return pubList.stream();
	}

	@Override
	public int getNumberOfPubs() {
		return pubList.size();
	}

	
	public Stream<PubType_MM> getPub(int py) {
		return pubList.stream().filter(pub -> (pub.getPY()!=null) && (pub.getPY().equals(py)));
	}
	
	
	/**
	 * Adds a PUB to the CR
	 * @param pub to be added
	 * @param inverse true, if this CR should also be added to the PUB
	 */
	@Override
	public void addPub(PubType pub, boolean inverse) {
		if (inverse) {
			pub.addCR(this, false);
		}
		this.resetN_CR();	// invalidate N_CR --> updated on next get access
		this.pubList.add((PubType_MM) pub);
	}


	/**
	 * Removes a PUB from the CR
	 * @param pub to be removed
	 * @param inverse true, if this CR should also be remove from the PUB
	 * @return if the PUB was in the publist
	 */
	@Override
	public boolean removePub(PubType pub, boolean inverse) {
		if (inverse) {
			pub.removeCR(this, false);
		}
		this.resetN_CR();	// invalidate N_CR --> updated on next get access
		return this.pubList.remove(pub);
	}
	
	@Override
	public void removeAllPubs(boolean inverse) {
		if (inverse) {
			pubList.forEach(pub -> pub.removeCR(this, false));
		}
		this.resetN_CR();
		pubList.clear();
		
	}
	
	

	
	

	








	
}




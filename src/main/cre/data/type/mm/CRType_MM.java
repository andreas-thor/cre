package main.cre.data.type.mm;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRType;
import main.cre.data.type.mm.clustering.CRCluster;

public class CRType_MM extends CRType<PubType_MM>  {

	
	private Set<PubType_MM> pubList;
	
	private CRCluster cluster;
	
	
	public CRType_MM() {
		super();
		pubList = new HashSet<PubType_MM>();
	}
	
	
	@Override
	public int getN_CR() {
		return this.pubList.size();
	}
	
	@Override
	public Stream<PubType_MM> getPub() {
		return pubList.stream();
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
	public void addPub(PubType_MM pub, boolean inverse) {
		if (inverse) {
			pub.addCR(this, false);
		}
		this.pubList.add(pub);
	}


	/**
	 * Removes a PUB from the CR
	 * @param pub to be removed
	 * @param inverse true, if this CR should also be remove from the PUB
	 * @return if the PUB was in the publist
	 */
	@Override
	public boolean removePub(PubType_MM pub, boolean inverse) {
		if (inverse) {
			pub.removeCR(this, false);
		}
		return this.pubList.remove(pub);
	}
	
	@Override
	public void removeAllPubs(boolean inverse) {
		if (inverse) {
			pubList.forEach(pub -> pub.removeCR(this, false));
		}
		pubList.clear();
		
	}
	
/*	
	public CRCluster getCID2() {
		return cluster;
	}
	public void setCID2(CRCluster cID2) {
		cluster = (CRCluster_MM) cID2;
	}
	
	
	@Override
	public void setCID2(String s) {
		cluster = new CRCluster_MM (s);
	}
	
	@Override
	public  void setCID2(CRType cr) {
		cluster = new CRCluster_MM (cr);
	}
	
	public void setCID2(CRType cr, int c1) {
		cluster = new CRCluster_MM (cr, c1);
	}
*/	
	
	
	@Override
	public int getClusterC1() {
		return cluster.getC1();
	}

	@Override
	public int getClusterC2() {
		return cluster.getC2();
	}
	
	@Override
	public int getClusterSize() {
		return cluster.getSize();
	}
	

	public CRCluster getCluster() {
		return this.cluster;
	}
	
	public void setCluster (CRCluster cluster) {
		this.cluster = cluster;
	}



	

	
}




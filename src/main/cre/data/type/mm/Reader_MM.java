package main.cre.data.type.mm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.cre.format.cre.Reader;

public class Reader_MM extends Reader {

	private Map<Integer, CRType_MM> mapId2CR;
	
	public Reader_MM() {
		
		this.mapId2CR = new HashMap<Integer, CRType_MM>();
	}
	
	@Override
	public void onNewCR(CRType_MM cr) {
		this.mapId2CR.put(cr.getID(), cr);
	}

	@Override
	public void onNewPub(PubType_MM pub, List<Integer> crIds) {
		for (int crId: crIds) {
			pub.addCR(mapId2CR.get(crId), true);
		}
		CRTable_MM.get().addPub(pub, false);
	}

	@Override
	public void onNewMatchPair(int crId1, int crId2, double sim, boolean isManual) {
	
		CRType_MM cr1 = mapId2CR.get(crId1);
		CRType_MM cr2 = mapId2CR.get(crId2);
		
		if ((cr1==null) || (cr2==null)) return;
		
		CRTable_MM.get().getClustering().addPair(cr1, cr2, sim, isManual);
	}

	
}

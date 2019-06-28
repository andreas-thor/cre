package main.cre.data.type.db;

import java.util.List;

import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.PubType_MM;
import main.cre.format.cre.Reader;

public class Reader_DB extends Reader {

	@Override
	public void onNewCR(CRType_MM cr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewPub(PubType_MM pub, List<Integer> crIds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewMatchPair(int crId1, int crId2, double sim, boolean isManual) {
		// TODO Auto-generated method stub

	}

}

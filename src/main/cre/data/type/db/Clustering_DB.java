package main.cre.data.type.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import main.cre.data.type.abs.Clustering;

public class Clustering_DB extends Clustering<CRType_DB, PubType_DB> {

	private Connection dbCon;
	
	public Clustering_DB(Connection dbCon) {
		this.dbCon = dbCon;
	}
	
	
	@Override
	public void addManuMatching(List<CRType_DB> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol,
			boolean usePag, boolean useDOI) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateAutoMatching() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoManuMatching(double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateClustering(ClusteringType type, Set<CRType_DB> changeCR, double threshold, boolean useVol,
			boolean usePag, boolean useDOI) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNumberOfMatches(boolean manual) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNumberOfClusters() {
		try {
			dbCon.setAutoCommit(true);
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ( SELECT  DISTINCT CR_ClusterId1, CR_ClusterId2  FROM CR ) AS T");
			rs.next();
			long res = rs.getLong(1);
			stmt.close();
			return res;
		} catch (Exception e) {
			return -1l;
		}
	}

}

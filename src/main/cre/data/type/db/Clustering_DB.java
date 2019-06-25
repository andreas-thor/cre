package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.Clustering;
import main.cre.ui.statusbar.StatusBar;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class Clustering_DB extends Clustering<CRType_DB, PubType_DB> {

	private Connection dbCon;
	
	public Clustering_DB(Connection dbCon) {
		this.dbCon = dbCon;
		

	}
	
	
	@Override
	public void addManuMatching(List<CRType_DB> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {

		assert selCR != null;
		assert selCR.stream().filter(cr -> cr==null).count() == 0;
		
		Long timestamp = System.currentTimeMillis();		// used to group together all individual mapping pairs of match operation
		String crIds = selCR.stream().map(cr -> String.valueOf(cr.getID())).collect(Collectors.joining(","));

		try {
			// manual-same is indicated by similarity = 2; different = -2
			if ((matchType==Clustering.ManualMatchType.SAME) || (matchType==Clustering.ManualMatchType.DIFFERENT)) {
				double sim = (matchType==Clustering.ManualMatchType.SAME) ? 2d : -2d;
			
			
			
				PreparedStatement insertMatchManu_PrepStmt = dbCon.prepareStatement(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(DB_Store.SQL_FILE_PREFIX + "pst_insert_match_manu.sql").toURI())), StandardCharsets.UTF_8));

				for (CRType_DB cr1: selCR) {
					for (CRType_DB cr2: selCR) {
						if (cr1.getID()<cr2.getID()) {
							insertMatchManu_PrepStmt.setInt(1,  cr1.getID());
							insertMatchManu_PrepStmt.setInt(2,  cr2.getID());
							insertMatchManu_PrepStmt.setDouble(3,  sim);
							insertMatchManu_PrepStmt.setLong(4,  timestamp);
							insertMatchManu_PrepStmt.addBatch();
						}
					}
				}
				insertMatchManu_PrepStmt.executeBatch();
			}
	
	
			
			if (matchType==Clustering.ManualMatchType.EXTRACT) {
				dbCon.createStatement().execute(
					String.format(
						"MERGE INTO CR_MATCH_MANU  (CR_ID1, CR_ID2, sim , tstamp) " + 
						"SELECT (CASE WHEN CR1.CR_ID < CR2.CR_ID THEN CR1.CR_ID ELSE CR2.CR_ID END), " + 
						"       (CASE WHEN CR1.CR_ID > CR2.CR_ID THEN CR1.CR_ID ELSE CR2.CR_ID END), " + 
						"       -2, %d " +
						"FROM  CR  AS CR1 JOIN CR AS CR2 " + 
						"ON (CR1.CR_ID != CR2.CR_ID AND CR1.CR_ClusterId1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterId2 = CR2.CR_ClusterId2) " +
						"WHERE CR1.CR_ID IN (%s)" ,
						timestamp, crIds)
				);
			}
		} catch (SQLException | IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		
		// changeCR = all CRs that are in the same cluster as selCR
		Set<CRType_DB> changeCR = CRTable_DB.get().getDBStore().selectCR(String.format("WHERE (CR_ClusterId1, CR_ClusterId2) IN (SELECT CR_ClusterId1, CR_ClusterId2 FROM CR WHERE CR.CR_ID IN (%s))", crIds)).collect(Collectors.toSet());
		updateClustering(Clustering.ClusteringType.REFRESH, changeCR, matchThreshold, useVol, usePag, useDOI);
		
		
	}

	@Override
	public void generateAutoMatching() {

		// parameters
		final double threshold = 0.5;
		
		// standard blocking: year + first letter of last name
		StatusBar.get().setValue(String.format("Blocking of %d objects...", CRTable.get().getStatistics().getNumberOfCRs()));
		
		
		Map<String, Long> blocks = null;
		
		try {

			dbCon.createStatement().execute(
				"UPDATE CR SET CR_BLOCKINGKEY = " + 
				"CASE WHEN (cr_RPY is not null) AND  (cr_AU_L is not null) AND (length(cr_AU_L)>0) " + 
				"THEN concat (cr_rpy, lower (substring(cr_AU_L,  1, 1))) ELSE '' END ");

			ResultSet rs = dbCon.createStatement().executeQuery ("SELECT CR_BLOCKINGKEY, COUNT(*) FROM CR WHERE CR_BLOCKINGKEY != '' GROUP BY CR_BLOCKINGKEY");
			blocks = new HashMap<String, Long>();
			while (rs.next()) {
				blocks.put(rs.getString(1), rs.getLong(2));
			}
			rs.close();
			
			StatusBar.get().initProgressbar(blocks.entrySet().stream().mapToInt(entry -> (int) (entry.getValue()*(entry.getValue()-1))/2).sum(), String.format("Matching %d objects in %d blocks", CRTable.get().getStatistics().getNumberOfCRs(), blocks.size()));

			this.dbCon.createStatement().execute("TRUNCATE TABLE CR_MATCH_AUTO");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Levenshtein l = new Levenshtein();
		
		Long stop1 = System.currentTimeMillis(); 
		
		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		
		// Matching: author lastname & journal name
		blocks.entrySet().parallelStream().forEach ( entry -> {

			StatusBar.get().incProgressbar(entry.getValue()*(entry.getValue()-1)/2);
			
			try {
				PreparedStatement insertMatchAuto_PrepStmt = dbCon.prepareStatement(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(DB_Store.SQL_FILE_PREFIX + "pst_insert_match_auto.sql").toURI())), StandardCharsets.UTF_8));
				AtomicInteger insertMatchAuto_Counter = new AtomicInteger(0);
				
				if (entry.getKey().equals("")) return;	// non-matchable block 
	
				List<CRType<?>> crlist = CRTable_DB.get().getDBStore().selectCR(String.format("WHERE CR_BLOCKINGKEY = '%s'", entry.getKey())).collect(Collectors.toList());
				crossCompareCR(crlist, l, (CRType<?>[] pair, Double sim) -> {
					
					try {
						insertMatchAuto_PrepStmt.setInt(1, Math.min(pair[0].getID(), pair[1].getID()));
						insertMatchAuto_PrepStmt.setInt(2, Math.max(pair[0].getID(), pair[1].getID()));
						insertMatchAuto_PrepStmt.setDouble(3, sim);
						insertMatchAuto_PrepStmt.addBatch();
						
						if (insertMatchAuto_Counter.incrementAndGet()>=1000) {
							insertMatchAuto_PrepStmt.executeBatch();
							insertMatchAuto_Counter.set(0);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return;
				});
				
				if (insertMatchAuto_Counter.get()>0) {
					insertMatchAuto_PrepStmt.executeBatch();
				}
			} catch (URISyntaxException | IOException | SQLException e) {
				e.printStackTrace();
			}

		});

		
		
		Long stop2 = System.currentTimeMillis();
		System.out.println("Match time is " + ((stop2-stop1)/100) + " deci-seconds");
		
		StatusBar.get().setValue("Matching done");
		updateClustering(Clustering.ClusteringType.INIT, null, threshold, false, false, false);
		
		
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

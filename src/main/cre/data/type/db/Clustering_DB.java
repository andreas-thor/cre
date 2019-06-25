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
import main.cre.data.type.abs.Clustering;
import main.cre.data.type.mm.CRCluster;
import main.cre.ui.statusbar.StatusBar;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class Clustering_DB extends Clustering<CRType_DB, PubType_DB> {

	private Connection dbCon;
	
	public Clustering_DB(Connection dbCon) {
		this.dbCon = dbCon;
		

	}
	
	
	@Override
	public Set<CRType_DB> addManuMatching(List<CRType_DB> selCR, ManualMatchType matchType) {

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
		return CRTable_DB.get().getDBStore().selectCR(String.format("WHERE (CR_ClusterId1, CR_ClusterId2) IN (SELECT CR_ClusterId1, CR_ClusterId2 FROM CR WHERE CR.CR_ID IN (%s))", crIds)).collect(Collectors.toSet());
	}
	

	@Override
	public void generateAutoMatching() {

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
	
				List<CRType_DB> crlist = CRTable_DB.get().getDBStore().selectCR(String.format("WHERE CR_BLOCKINGKEY = '%s'", entry.getKey())).collect(Collectors.toList());
				crossCompareCR(crlist, l, (CRType_DB cr1, CRType_DB cr2, double sim) -> {
					
					try {
						insertMatchAuto_PrepStmt.setInt(1, Math.min(cr1.getID(), cr2.getID()));
						insertMatchAuto_PrepStmt.setInt(2, Math.max(cr1.getID(), cr2.getID()));
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
	}




	@Override
	public Set<CRType_DB> undoManuMatching() {
		// TODO Auto-generated method stub
		return null;
		
	}

	@Override
	public void updateClustering(ClusteringType type, Set<CRType_DB> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		try {

			String changeCRIds = (changeCR != null) ? changeCR.stream().map(cr -> String.valueOf(cr.getID())).collect(Collectors.joining(",")) : null; 

			if (type == Clustering.ClusteringType.INIT) {	// consider manual (automatic?) matches only
				// reset all clusters (each CR forms an individual clustering)
				dbCon.createStatement().execute("UPDATE CR SET CR_ClusterId1 = CR_ID, CR_ClusterId2 = CR_ID");
			}
			
			if (type == Clustering.ClusteringType.REFRESH) {
				// reset clusterId2 only 
				dbCon.createStatement().execute(String.format("UPDATE CR SET CR_ClusterId2 = CR_ID %s", changeCRIds==null ? "" : String.format("WHERE CR_ID IN (%s)", changeCRIds)));
			}
		
			StatusBar.get().initProgressbar(1, String.format("Clustering %d objects (%s) with threshold %.2f", CRTable.get().getStatistics().getNumberOfCRs(), type.toString(), threshold));

			
			String and = "";
			and += useVol ? "AND COALESCE(CR1.CR_VOL, 'A') = COALESCE(CR2.CR_VOL, 'B') " : "";
			and += usePag ? "AND COALESCE(CR1.CR_PAG, 'A') = COALESCE(CR2.CR_PAG, 'B') " : "";
			and += useDOI ? "AND COALESCE(CR1.CR_DOI, 'A') = COALESCE(CR2.CR_DOI, 'B') " : "";
			and += (changeCRIds != null) ? String.format ("AND CR1.CR_ID IN (%s) ", changeCRIds) : "";
			and += (changeCRIds != null) ? String.format ("AND CR2.CR_ID IN (%s) ", changeCRIds) : "";
			
			String sql = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(DB_Store.SQL_FILE_PREFIX + "updateclustering.sql").toURI())), StandardCharsets.UTF_8);
			PreparedStatement updateclustering_PrepStmt = dbCon.prepareStatement(String.format(sql, threshold, and));
			
			int noOfUpdates = -1;
			while ((noOfUpdates = updateclustering_PrepStmt.executeUpdate()) > 0) { 
				System.out.println("NoOfUpdates = " + noOfUpdates);
				
			}
			updateclustering_PrepStmt.close();
			
			dbCon.createStatement().execute("update cr set cr_clustersize = (select count(*) from cr as cr2 where cr.cr_clusterid1=cr2.cr_clusterid1 AND cr.cr_clusterid2 = cr2.cr_clusterid2)");

			

			
			
			
		} catch (SQLException | IOException | URISyntaxException e) {
			e.printStackTrace(); 	// TODO Auto-generated catch block
		}

		
		
		
	}

	@Override
	public long getNumberOfMatches(boolean manual) {
		try {
			dbCon.setAutoCommit(true);
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM CR_MATCH_%s", manual ? "MANU" : "AUTO"));
			rs.next();
			long res = rs.getLong(1);
			stmt.close();
			return res;
		} catch (Exception e) {
			return -1l;
		}
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

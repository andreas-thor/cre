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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.Clustering;
import main.cre.data.type.mm.CRType_MM;
import main.cre.ui.statusbar.StatusBar;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

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
				PreparedStatement insertMatch_PrepStmt = dbCon.prepareStatement(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(DB_Store.SQL_FILE_PREFIX + "pst_insert_automatch.sql").toURI())), StandardCharsets.UTF_8));
				AtomicInteger insertMatch_Counter = new AtomicInteger(0);
				
				if (entry.getKey().equals("")) return;	// non-matchable block 
	
				List<CRType<?>> crlist = CRTable_DB.get().getDBStore().selectCR(String.format("WHERE CR_BLOCKINGKEY = '%s'", entry.getKey())).collect(Collectors.toList());
				crossCompareCR(crlist, l, (CRType<?>[] pair, Double sim) -> {
					
					try {
						insertMatch_PrepStmt.setInt(1, Math.min(pair[0].getID(), pair[1].getID()));
						insertMatch_PrepStmt.setInt(2, Math.max(pair[0].getID(), pair[1].getID()));
						insertMatch_PrepStmt.setDouble(3, sim);
						insertMatch_PrepStmt.addBatch();
						
						if (insertMatch_Counter.incrementAndGet()>=1000) {
							insertMatch_PrepStmt.executeBatch();
							insertMatch_Counter.set(0);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return;
				});
				
				if (insertMatch_Counter.get()>0) {
					insertMatch_PrepStmt.executeBatch();
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

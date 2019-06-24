package main.cre.data.type.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		Map<String, Long> blocks = CRTable.get().getCR().collect(Collectors.groupingBy(
			BLOCKING_FUNCTION, 
			Collectors.counting()
		));

		StatusBar.get().initProgressbar(blocks.entrySet().stream().mapToInt(entry -> (int) (entry.getValue()*(entry.getValue()-1))/2).sum(), String.format("Matching %d objects in %d blocks", CRTable.get().getStatistics().getNumberOfCRs(), blocks.size()));
		
		this.dbCon.createStatement().execute("TRUNCATE TABLE CR_MATCH_AUTO");
		Levenshtein l = new Levenshtein();
		
		AtomicLong testCount = new AtomicLong(0);
		Long stop1 = System.currentTimeMillis(); 
		
		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		
		// Matching: author lastname & journal name
		blocks.entrySet().stream().forEach ( entry -> {

			StatusBar.get().incProgressbar(entry.getValue()*(entry.getValue()-1)/2);
			
			if (entry.getKey().equals("")) return;	// non-matchable block 

			List<CRType<PubType_DB>> crlist = CRTable_DB.get().getCR().filter(cr -> BLOCKING_FUNCTION.apply(cr).equals(entry.getKey())).collect(Collectors.toList());

			
			crossCompareCR(crlist, l, (CRType<PubType_DB>[] pair, Double sim) -> {
				
				return;
			});
			

		// ... and invoke sequentially
		matchResult.forEach(it -> { addPair(it, false, true, null); });
		
		
		Long stop2 = System.currentTimeMillis();
		System.out.println("Match time is " + ((stop2-stop1)/100) + " deci-seconds");
		
		assert testCount.get() == getNumberOfMatches(false);
		
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

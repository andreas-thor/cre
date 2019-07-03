package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.Statistics;
import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.PubType_MM;
import main.cre.format.cre.Reader;

public class CRTable_DB extends CRTable<CRType_DB, PubType_DB> {


	
	private static CRTable_DB crTab = null;

	private Connection dbCon;
	private DB_Store dbStore;
	
	
	private boolean showNull;

	private int numberOfPubs; 
	
	private Statistics_DB statistics;
	private Clustering_DB clustering;
	private Reader_DB reader;
	

	public static CRTable_DB get() {
		if (crTab == null) {
			crTab = new CRTable_DB();
		}
		return crTab;
	}
	
	
	@Override
	public Reader getReader() {
		return this.reader;
	}
	
	@Override
	public Statistics getStatistics() {
		return this.statistics;
	}
	
	@Override
	public Clustering_DB getClustering() {
		return this.clustering;
	}

	
	DB_Store getDBStore() {
		return this.dbStore;
	}
	
	private CRTable_DB () { 
		
		try {
			Class.forName("org.h2.Driver" );
			
			dbCon = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");	// embedded (file)
//			dbCon = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");	// in-memory

			dbStore = new DB_Store(dbCon);
			statistics = new Statistics_DB(dbCon);
			clustering = new Clustering_DB(dbCon);
			reader = new Reader_DB(dbCon);
			
		} catch (ClassNotFoundException | SQLException | IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		init();
	}
	
	
	@Override
	public void init() {
		// TODO Auto-generated method stub

		this.setNpctRange(1);
		this.setAborted(false);
		this.numberOfPubs = 0;
		this.showNull = true;

		try {
			this.dbStore.init();
		} catch (SQLException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	
	@Override
	public Stream<CRType_DB> getCR() {
		return this.dbStore.selectCR("");
	}

	
	@Override
	public Stream<PubType_DB> getPub(boolean includePubsWithoutCRs) {
		return this.dbStore.selectPub(includePubsWithoutCRs ? "" : "WHERE PUB_ID IN (SELECT PUB_ID FROM PUB_CR)");
	}




	@Override
	public PubType_DB addPub(PubType_MM pub) {

		this.numberOfPubs++;
		pub.setID(this.numberOfPubs);
		
		try {

			dbStore.insertPub(pub);
			for(CRType_MM cr: pub.getCR().collect(Collectors.toSet())) {
				
				try {
					dbStore.insertCR(cr, pub.getID());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return null;		
		
	}

	@Override
	public void merge() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateData() throws OutOfMemoryError {
		
		
		try {
			// we may have some insert statements in the batch to be executed after loading
			dbStore.finishInsert();

			Statement stmt = dbCon.createStatement();

			IntRange range_RPY  = statistics.getMaxRangeRPY();
			IntRange range_PY  = statistics.getMaxRangePY();
			int NCR_ALL = statistics.getSumNCR()[0];
			
			int[] NCR_RPY = new int[range_RPY.getSize()];
			int[] CNT_RPY = new int[range_RPY.getSize()];
			
			ResultSet rs = stmt.executeQuery("SELECT CR_RPY, SUM(CR_N_CR), COUNT(CR_ID) FROM CR WHERE NOT (CR_RPY IS NULL) GROUP BY CR_RPY ORDER BY CR_RPY");
			while (rs.next()) {
				int rpyIdx = rs.getInt(1)-range_RPY.getMin();
				NCR_RPY[rpyIdx] = rs.getInt(2);
				CNT_RPY[rpyIdx] = rs.getInt(3);
			}
			rs.close();

			computeForAllCRs (range_RPY, range_PY, NCR_ALL, NCR_RPY, CNT_RPY);
			
			getChartData().updateChartData(range_RPY.getMin(), range_RPY.getMax(), NCR_RPY, CNT_RPY);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void computeForAllCRs (IntRange range_RPY, IntRange range_PY, int NCR_ALL, int[] NCR_RPY, int[] CNT_RPY) {
		
	
		int crSize = -1;
		int firstPY = -1; 
		int lastPY = -1;
		int pySize = -1;
		
		int lastCrId = -1;
		int idx = -1;
		
		int[][] NCR_CR_PY = null;	
		int[] NCR_CR = null;	
		int[] NCR_CR_all = null;	
		int[] NPYEARS_CR = null;
		int[] NCR_PY = null; 	
		int[] NCR = null; 
		int[] mapCrIdxToCrId = null;	
		
		try {
			// get all CRs ordered by RPY ...
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery(
				" SELECT CR.CR_RPY, CR.CR_ID, CR.CR_N_CR, PUB.PUB_PY, COUNT(*) " + 
				" FROM CR " + 
				" JOIN PUB_CR ON (CR.CR_ID = PUB_CR.CR_ID) " + 
				" JOIN PUB ON (PUB_CR.PUB_ID = PUB.PUB_ID) " + 
				" WHERE NOT (CR.CR_RPY IS NULL) " + 
				" GROUP BY CR.CR_RPY,  CR.CR_ID, CR.CR_N_CR, PUB.PUB_PY " + 
				" ORDER BY CR.CR_RPY, CR.CR_ID  ");
		
			
			int lastRPY = -1;
			int rpyIdx = -1;
			boolean invalidRPY_PY_Range = false; 

			
		
			while (rs.next()) {
				
				int rpy = rs.getInt(1);
				
				if ((rpy == lastRPY) && (invalidRPY_PY_Range)) continue;
				
				// ... we determine the blocks of CRs sharing the same RPY and compute indicators per block (rpy)
				if (rpy != lastRPY) {
					
					if ((lastRPY != -1) && (!invalidRPY_PY_Range)) {
						int[] mapCrIdxToCrId_final = mapCrIdxToCrId;
						computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
								(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
									dbStore.updateCRIndicators(mapCrIdxToCrId_final[crIdx], N_PYEARS, PYEAR_PERC, PERC_YR, PERC_ALL, N_PCT, N_PCT_AboveAverage, SEQUENCE, TYPE);
								}
						);						
					}
					
					// new RPY
					lastRPY = rpy;
					rpyIdx = rpy-range_RPY.getMin();

					// check if meaningful range of PYs
					invalidRPY_PY_Range = false; 
					firstPY = (rpy<=range_PY.getMin()) ? range_PY.getMin() : rpy;	// usually: rpy<=range_PY[0] 
					lastPY = range_PY.getMax();
					if (lastPY < firstPY) {
						invalidRPY_PY_Range = true;
						continue;
					};
					
					// init all data structures
					crSize = CNT_RPY[rpyIdx];
					pySize = lastPY-firstPY+1;
					NCR_CR_PY = new int[crSize][pySize];	
					NCR_CR = new int[crSize];	
					NCR_CR_all = new int[crSize];	
					NPYEARS_CR = new int[crSize];
					NCR_PY = new int[pySize];	
					NCR = new int[1];
					mapCrIdxToCrId = new int[crSize];
					
					lastCrId = -1;
					idx = -1;
				}
				
				int crId = rs.getInt(2);
				if (crId != lastCrId) {
					lastCrId = crId;
					idx++;
					NCR_CR_all[idx] = rs.getInt(3);
					mapCrIdxToCrId[idx] = crId; 
				}
				
				int py = rs.getInt(4);
				int count = rs.getInt(5);
				if ((py>=firstPY) && (py<=lastPY)) {	// PY is out of range
					int pyIdx = py-firstPY;
					NPYEARS_CR[idx]++;
					NCR_CR_PY[idx][pyIdx] = count;
					NCR_CR[idx] += count;
					NCR_PY[pyIdx] += count;
					NCR[0] += count;
				}
			}
			rs.close();
			stmt.close();
			
			
			// wrap up: process last block ...
			if ((lastRPY != -1) && (!invalidRPY_PY_Range)) {
				int[] mapCrIdxToCrId_final = mapCrIdxToCrId;
				computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
						(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
							dbStore.updateCRIndicators(mapCrIdxToCrId_final[crIdx], N_PYEARS, PYEAR_PERC, PERC_YR, PERC_ALL, N_PCT, N_PCT_AboveAverage, SEQUENCE, TYPE);
						}
				);						
			}
			
			// ... and finish
			dbStore.finishUpdateCRIndicators();

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


	
	
	@Override
	public void removeCR(List<CRType_DB> toDelete) {
		String crList = toDelete.stream().map(cr -> String.valueOf(cr.getID())).collect(Collectors.joining(","));
		dbStore.removeCR(String.format("CR_ID IN (%s)", crList));
	}

	@Override
	public void retainCR(List<CRType_DB> toRetain) {
		String crList = toRetain.stream().map(cr -> String.valueOf(cr.getID())).collect(Collectors.joining(","));
		dbStore.removeCR(String.format("CR_ID NOT IN (%s)", crList));
	}

	@Override
	public void removeCRWithoutYear() {
		dbStore.removeCR("CR_RPY IS NULL");
	}

	@Override
	public void removeCRByYear(IntRange range) {
		dbStore.removeCR(String.format("NOT(CR_RPY IS NULL) AND (%d<=CR_RPY) AND (%d>=CR_RPY)", range.getMin(), range.getMax()));  
	}

	@Override
	public void removeCRByN_CR(IntRange range) {
		dbStore.removeCR(String.format("(%d<=CR_N_CR) AND (%d>=CR_N_CR)", range.getMin(), range.getMax()));
	}

	@Override
	public void removeCRByPERC_YR(String comp, double threshold) {
		dbStore.removeCR(String.format("CR_PERC_YR %s %f", comp, threshold));
	}

	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	@Override
	public void removePubByCR(List<CRType_DB> selCR) {
		String crIds = selCR.stream().map(it -> String.valueOf(it.getID())).collect(Collectors.joining(","));
		dbStore.removePub(String.format("PUB_ID NOT IN (SELECT PUB_ID FROM PUB_CR WHERE CR_ID IN (%s))", crIds));
	}
	

	@Override
	public void retainPubByCitingYear(IntRange range) {
		dbStore.removePub(String.format("(PUB_PY IS NULL) OR (%d > PUB_PY) OR (PUB_PY > %d)", range.getMin(), range.getMax()));
	}

	@Override
	public void filterByYear(IntRange range) {
		String newValue = String.format("(NOT(CR_RPY IS NULL) AND (%d<=CR_RPY) AND (%d>=CR_RPY)) %s", range.getMin(), range.getMax(), this.showNull?" OR (CR_RPY IS NULL)":"");  
		dbStore.updateCR_VI(newValue, null);
	}

	@Override
	public void filterByCluster(List<CRType_DB> sel) {
		String clList = sel.stream().map(cr -> "(" + cr.getClusterC1() + "," + cr.getClusterC2() + ")").collect (Collectors.joining( "," ));
		dbStore.updateCR_VI(String.format("(CR_Clusterid1, CR_Clusterid2) in (%s)", clList), null);
	}

	@Override
	public void setShowNull(boolean showNull) {
		dbStore.updateCR_VI(showNull?"1":"0", "CR_RPY IS NULL");
		this.showNull = showNull;
	}

	@Override
	public void showAll() {
		dbStore.updateCR_VI("1", null);
		this.showNull = true;		
	}


}

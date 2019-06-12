package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRIndicatorsUpdate;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.Statistics;
import main.cre.data.type.abs.CRType.PERCENTAGE;
import main.cre.data.type.mm.CRCluster;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.PubType_MM;

public class CRTable_DB extends CRTable<CRType_DB, PubType_DB> {


	
	private static CRTable_DB crTab = null;

	private Connection dbCon;
	private DB_Store dbStore;
	
	
	private boolean showNull;

	
	private int numberOfPubs; 
	private int numberOfCRs;
	
	
	
	private Statistics_DB statistics;
	private Clustering_DB clustering;
	

	public static CRTable_DB get() {
		if (crTab == null) {
			crTab = new CRTable_DB();
		}
		return crTab;
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
		this.numberOfCRs = 0;
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
	public PubType_DB addPub(PubType_MM pub, boolean addCRs, boolean checkForDuplicates) {

		/* TODO: Check for Duplicates */
		this.numberOfPubs++;
		pub.setID(this.numberOfPubs);
		
		try {

			dbStore.insertPub(pub);
			for(CRType_MM cr: pub.getCR().collect(Collectors.toSet())) {
				
				try {
					cr.setID(++this.numberOfCRs);
					cr.setCluster(new CRCluster(cr));
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
		
		// we may have some insert statements in the batch to be executed
		try {
			dbStore.finishInsert();

			Statement stmt = dbCon.createStatement();

			int[] range_RPY  = statistics.getMaxRangeRPY();
			int[] range_PY  = statistics.getMaxRangePY();
			int NCR_ALL = statistics.getSumNCR()[0];

			
			int[] NCR_RPY = new int[range_RPY[1]-range_RPY[0]+1];
			int[] CNT_RPY = new int[range_RPY[1]-range_RPY[0]+1];
			ResultSet rs = stmt.executeQuery("SELECT CR_RPY, SUM(CR_N_CR), COUNT(CR_ID) FROM CR WHERE NOT (CR_RPY IS NULL) GROUP BY CR_RPY ORDER BY CR_RPY");
			while (rs.next()) {
				int rpyIdx = rs.getInt(1)-range_RPY[0];
				NCR_RPY[rpyIdx] = rs.getInt(2);
				CNT_RPY[rpyIdx] = rs.getInt(3);
			}
			rs.close();

			
			List<Integer> rpyList = new ArrayList<Integer>();
			rs = stmt.executeQuery("SELECT CR_RPY, COUNT(*) FROM CR WHERE NOT (CR_RPY IS NULL)  GROUP BY CR_RPY ORDER BY CR_RPY");
			while (rs.next()) {
				int rpy = rs.getInt(1);
				int crSize = rs.getInt(2);
				computeForAllCRsOfTheSameRPY(rpy, rpy-range_RPY[0], crSize, range_PY, NCR_ALL, NCR_RPY);
			}
			rs.close();
			stmt.close();
			
			rpyList.forEach(rpy -> {
				
			});
			
			
			
			
			getChartData().updateChartData(range_RPY[0], range_RPY[range_RPY.length-1], NCR_RPY, CNT_RPY);


			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Indicators.get().update();
//		Indicators.get().updateChartData();

		
		
	}
	
	
	private void computeForAllCRsOfTheSameRPY (int rpy, int rpyIdx, int crSize, int[] range_PY, int NCR_ALL, int[] NCR_RPY) {
		
		int firstPY = (rpy<=range_PY[0]) ? range_PY[0] : rpy;	// usually: rpy<=range_PY[0] 
		int lastPY = range_PY[1];
		if (lastPY < firstPY) return;
		int pySize = lastPY-firstPY+1;
		
		int[][] NCR_CR_PY = new int[crSize][pySize];	
		int[] NCR_CR = new int[crSize];	
		int[] NCR_CR_all = new int[crSize];	
		int[] NPYEARS_CR = new int[crSize];
		int[] NCR_PY = new int[pySize];	
		int[] NCR = new int[1];
		int[] mapCrIdxToCrId = new int[crSize];
		
		
		try {
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(
					"SELECT CR.CR_ID, CR.CR_N_CR, PUB.PUB_PY, COUNT(*) \r\n" + 
					"FROM CR \r\n" + 
					"JOIN PUB_CR ON (CR.CR_ID = PUB_CR.CR_ID)\r\n" + 
					"JOIN PUB ON (PUB_CR.PUB_ID = PUB.PUB_ID) \r\n" + 
					"WHERE CR_RPY = %d \r\n" + 
					"GROUP BY CR.CR_ID, CR.CR_N_CR, PUB.PUB_PY", rpy));
			
			
			int lastCrId = -1;
			int idx = -1;
			while (rs.next()) {
				
				int crId = rs.getInt(1);
				if (crId != lastCrId) {
					lastCrId = crId;
					idx++;
					NCR_CR_all[idx] = rs.getInt(2);
					mapCrIdxToCrId[idx] = crId; 
				}
				
				int py = rs.getInt(3);
				int count = rs.getInt(4);
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
			
			computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
					(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
						dbStore.updateCRIndicators(mapCrIdxToCrId[crIdx], N_PYEARS, PYEAR_PERC, PERC_YR, PERC_ALL, N_PCT, N_PCT_AboveAverage, SEQUENCE, TYPE);
					}
			);
					
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
	public void removeCRByYear(int[] range) {
		dbStore.removeCR(String.format("NOT(CR_RPY IS NULL) AND (%d<=CR_RPY) AND (%d>=CR_RPY)", range[0], range[1]));  
	}

	@Override
	public void removeCRByN_CR(int[] range) {
		dbStore.removeCR(String.format("(%d<=CR_N_CR) AND (%d>=CR_N_CR)", range[0], range[1]));
	}

	@Override
	public void removeCRByPERC_YR(String comp, double threshold) {
		// TODO NOT YET SUPPORTED
	}

	@Override
	public void removePubByCR(List<CRType_DB> selCR) {
		// TODO Auto-generated method stub
	}

	@Override
	public void retainPubByCitingYear(int[] range) {
		dbStore.removePub(String.format("(PUB_PY IS NULL) OR (%d > PUB_PY) OR (PUB_PY > %d)", range[0], range[1]));
	}

	@Override
	public void filterByYear(int[] range) {
		String newValue = String.format("(NOT(CR_RPY IS NULL) AND (%d<=CR_RPY) AND (%d>=CR_RPY)) %s", range[0], range[1], this.showNull?" OR (CR_RPY IS NULL)":"");  
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

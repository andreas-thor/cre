package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.Statistics;
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
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Indicators.get().update();
//		Indicators.get().updateChartData();

		
		
		super.updateData();
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

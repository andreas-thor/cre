package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;

public class CRTable_DB extends CRTable<CRType_DB, PubType_DB> {


	
	private static CRTable_DB crTab = null;

	private Connection dbCon;


	
	private PreparedStatement insertCR_PrepStmt;

	private int numberOfPubs; 
	private int numberOfCRs;
	
	private CRType_DB_Storage crTypeDB;
	private PubType_DB_Storage pubTypeDB;
	
	
	public static CRTable_DB get() {
		if (crTab == null) {
			crTab = new CRTable_DB();
		}
		return crTab;
	}
	
	

	
	private CRTable_DB () { 
		
		
		try {
			Class.forName("org.h2.Driver" );
			
			dbCon = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");	// embedded (file)
//			dbCon = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");	// in-memory

			pubTypeDB = new PubType_DB_Storage(dbCon);
			crTypeDB = new CRType_DB_Storage(dbCon);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
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
		try {
			this.crTypeDB.init();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	
	@Override
	public Stream<CRType_DB> getCR() {
		
		return this.crTypeDB.selectCR();
		

	}

	@Override
	public Stream<PubType_DB> getPub(boolean includePubsWithoutCRs) {
		// TODO Auto-generated method stub
		Stream<PubType_DB> emptyStr = Stream.of();
		return emptyStr;
	}





	@Override
	public CRType_DB addCR(CRType_DB cr, boolean checkForDuplicatesAndSetId) {
		

		
		
		return null;
	}

//	@Override
//	public PubType addPub(PubType pub, boolean addCRs) {
//		return addPub (pub, addCRs, false);
//	}

	@Override
	public PubType_DB addPub(PubType_DB pub, boolean addCRs, boolean checkForDuplicates) {

		/* TODO: Check for Duplicates */
		this.numberOfPubs++;
		pub.setID(this.numberOfPubs);
		

		try {

			pubTypeDB.insertPub(pub);
			
//			insertPub_PrepStmt.addBatch();
//			insertPub_BatchCount++;
			
			for(CRType cr: pub.getCR().collect(Collectors.toSet())) {
				
//				CRType crMain = this.crDataMap.get(cr);
//				if (crMain == null) {
//					this.crDataMap.put(cr, cr);
//					cr.setID(this.crDataMap.size());
//					cr.setCID2(new CRCluster (cr));
//				} else {
//					pub.removeCR(cr, false);	
//					pub.addCR(crMain, false);
//					crMain.addPub(pub, false);	
//				}
			
				
				try {
					cr.setID(this.numberOfCRs+1);
					cr.setCID2(new CRCluster_DB(cr.getID()));
					int id = crTypeDB.insertCR(cr, this.numberOfCRs+1, pub.getID());
					if (id > this.numberOfCRs) {
						this.numberOfCRs++;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
//			if (insertPub_BatchCount >= insertPub_BatchSize) {
//				insertPub_PrepStmt.executeBatch();
//				crTypeDB.executeBatch();
//				insertPub_BatchCount = 0;
//			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		sql.append (Arrays.stream(PubColumn.values()).map(col -> { return "?"; } ).collect(Collectors.joining(", ")));

		
		
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
			crTypeDB.finishInsertCR();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}


	@Override
	public void removeCR(List<CRType_DB> toDelete) {
		// TODO Auto-generated method stub

	}

	@Override
	public void retainCR(List<CRType_DB> toRetain) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRWithoutYear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRByYear(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRByN_CR(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRByPERC_YR(String comp, double threshold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePubByCR(List<CRType_DB> selCR) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePubByCitingYear(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void filterByYear(int[] range) {
		// TODO Auto-generated method stub

	}

	@Override
	public void filterByCluster(List<CRType_DB> sel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShowNull(boolean showNull) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAll() {
		// TODO Auto-generated method stub

	}

}

package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import main.cre.data.type.abs.CRType;

class CRType_DB_Storage { 

	
	private static class CRType_ResultSet implements Iterator<CRType_DB> {

		private ResultSet rs; 
		
		private CRType_ResultSet(Connection dbCon) throws IOException, SQLException {
			String sql = "select * from CR";
			PreparedStatement pSt = dbCon.prepareStatement(sql);
			rs = pSt.executeQuery();
		}
		
		private void close() throws IOException, SQLException {
			rs.close();
		}
		
		@Override
		public boolean hasNext() {
			try {
				return rs.next();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public CRType_DB next() {
			try {
				CRType_DB cr = new CRType_DB();
				cr.setID(rs.getInt("CR_ID"));
				cr.setCR(rs.getString("CR_CR"));
				cr.setRPY(rs.getInt("CR_RPY"));
				cr.setN_CR(rs.getInt("CR_N_CR"));
				cr.setAU(rs.getString("CR_AU"));
				cr.setAU_L(rs.getString("CR_AU_L"));
				cr.setAU_F(rs.getString("CR_AU_F"));
				cr.setAU_A(rs.getString("CR_AU_A"));
				cr.setTI(rs.getString("CR_TI"));
				cr.setJ(rs.getString("CR_J"));
				cr.setJ_N(rs.getString("CR_J_N"));
				cr.setJ_S(rs.getString("CR_J_S"));
				cr.setVOL(rs.getString("CR_VOL"));
				cr.setPAG(rs.getString("CR_PAG"));
				cr.setDOI(rs.getString("CR_DOI"));
				cr.setCluster(rs.getInt("CR_ClusterId1"), rs.getInt("CR_ClusterId2"), rs.getInt("CR_ClusterSize"));
				return cr;
			} catch (Exception e) {
				return null;
			}
		}
		
		public Iterable<CRType_DB> getIterable () { 
			return () -> this;
		}
		
	}	
	
	private PreparedStatement insertCR_PrepStmt;
	private int batchSizeCounter = 0;
	private final int BATCH_SIZE_MAX = 100;
	
	
	private PreparedStatement selectCR_PrepStmt;
	private PreparedStatement updateCR_PrepStmt;
	
	private Connection dbCon;

	private String finishSQL;
	private String startSQL;
	
	
	CRType_DB_Storage(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		this.dbCon = dbCon;
		
		Path path = Paths.get(getClass().getClassLoader().getResource("main/cre/data/type/db/CRType_DB.sql").toURI());
		Stream<String> lines = Files.lines(path);
		String[] sql = lines.collect(Collectors.joining("\n")).split("###");
		lines.close();
		

		startSQL = sql[0];
		finishSQL = sql[2];
		
		/* create prepared statements */
		insertCR_PrepStmt = dbCon.prepareStatement(sql[1]);
		selectCR_PrepStmt = dbCon.prepareStatement("SELECT CR_ID FROM CR WHERE CR_CR = ?");
		updateCR_PrepStmt = dbCon.prepareStatement("UPDATE CR SET CR_N_CR = CR_N_CR+1 WHERE CR_ID = ?");
	}
	
	
	void init () throws SQLException {
		/* create tables */
		Statement stmt = dbCon.createStatement();
		stmt.execute(startSQL);
		stmt.close();
		
		dbCon.commit();
		dbCon.setAutoCommit(false);
		
	}
	
	int insertCR (CRType<?> cr, int defaultId, int pubId) throws SQLException {
		
		int crId;
		
//		selectCR_PrepStmt.setString(1, cr.getCR());
//		ResultSet rs = selectCR_PrepStmt.executeQuery();
//		
//		if (rs.next()) {	// we do have an id
//			int id = rs.getInt(1);
//			updateCR_PrepStmt.setInt(1, id);
//			updateCR_PrepStmt.execute();
//			return id;
//		} else {
			crId = defaultId;
		
			
			
//			insertCR_PrepStmt.setString	( 1, cr.getCR());
			
			
			int start = 0;
			insertCR_PrepStmt.setInt	(start+ 1, crId);
			insertCR_PrepStmt.setString	(start+ 2, cr.getCR());
			
			if (cr.getRPY() == null) {
				insertCR_PrepStmt.setNull(start+ 3, java.sql.Types.INTEGER);
			} else {
				insertCR_PrepStmt.setInt (start+ 3, cr.getRPY());
			}
			
			insertCR_PrepStmt.setString	(start+ 4, cr.getAU());
			insertCR_PrepStmt.setString	(start+ 5, cr.getAU_L());
			insertCR_PrepStmt.setString	(start+ 6, cr.getAU_F());
			insertCR_PrepStmt.setString	(start+ 7, cr.getAU_A());
			insertCR_PrepStmt.setString	(start+ 8, cr.getTI());
			insertCR_PrepStmt.setString	(start+ 9, cr.getJ());
			insertCR_PrepStmt.setString	(start+10, cr.getJ_N());
			insertCR_PrepStmt.setString	(start+11, cr.getJ_S());
			insertCR_PrepStmt.setString	(start+12, cr.getVOL());
			insertCR_PrepStmt.setString	(start+13, cr.getPAG());
			insertCR_PrepStmt.setString	(start+14, cr.getDOI());
			insertCR_PrepStmt.setInt	(start+15,  cr.getClusterC1());
			insertCR_PrepStmt.setInt	(start+16,  cr.getClusterC2());
			insertCR_PrepStmt.setInt	(start+17,  cr.getClusterSize());
			insertCR_PrepStmt.setInt	(start+18,  pubId);
			insertCR_PrepStmt.addBatch();
			
			batchSizeCounter++;
			
			
			
			if (batchSizeCounter>=BATCH_SIZE_MAX) {
//				insertPubCR_PrepStmt.executeBatch();	// PUBCR must be executed before CR, since CR might update PUBCR
				insertCR_PrepStmt.executeBatch();
				batchSizeCounter = 0;
			}
			
			
			return crId;
//		}
	}

	

	Stream<CRType_DB> selectCR() {
		
		System.out.println("selectCR");
		try {
			return StreamSupport.stream(new CRType_ResultSet(dbCon).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<CRType_DB> emptyStr = Stream.of();
			return emptyStr;
		}

	}


	void finishInsertCR() throws SQLException {
		if (batchSizeCounter>0) {
			insertCR_PrepStmt.executeBatch();
			batchSizeCounter = 0;
		}
		

		
		System.out.println("Executing " + finishSQL);
		Statement stmt = dbCon.createStatement();
		stmt.execute(finishSQL);
		stmt.close();
		dbCon.commit();
				
	}

//	public void executeBatch() throws SQLException {
//		insertCR_PrepStmt.executeBatch();
//		insertPubCR_PrepStmt.executeBatch();
//	}
//	
	
	
}

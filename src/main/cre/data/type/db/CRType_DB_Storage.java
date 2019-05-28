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

import main.cre.data.type.abs.CRCluster;
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
				cr.setCID2(rs.getInt("CR_ClusterId1"), rs.getInt("CR_ClusterId2"), rs.getInt("CR_ClusterSize"));
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
	private PreparedStatement insertPubCR_PrepStmt;
	
	private PreparedStatement selectCR_PrepStmt;
	private PreparedStatement updateCR_PrepStmt;
	
	private Connection dbCon;
	
	private int insertCR_batchSize = 0;
	private int insertPubCR_batchSize = 0;
	
	
	CRType_DB_Storage(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		this.dbCon = dbCon;
		
		Path path = Paths.get(getClass().getClassLoader().getResource("main/cre/data/type/db/CRType_DB.sql").toURI());
		Stream<String> lines = Files.lines(path);
		String[] sql = lines.collect(Collectors.joining("\n")).split("###");
		lines.close();
		
		/* create tables */
		Statement stmt = dbCon.createStatement();
		stmt.execute(sql[0]);
		stmt.close();
	
		/* create prepared statements */
		insertCR_PrepStmt = dbCon.prepareStatement(sql[1]);
		insertPubCR_PrepStmt = dbCon.prepareStatement(sql[2]);
		selectCR_PrepStmt = dbCon.prepareStatement("SELECT CR_ID FROM CR WHERE CR_CR = ?");
		updateCR_PrepStmt = dbCon.prepareStatement("UPDATE CR SET CR_N_CR = CR_N_CR+1 WHERE CR_ID = ?");
	}
	
	
	void init () throws SQLException {
		Statement stmt = dbCon.createStatement();
		stmt.execute("TRUNCATE TABLE CR; TRUNCATE TABLE PUB_CR;");
		stmt.close();
	}
	
	int insertCR (CRType cr, int defaultId, int pubId) throws SQLException {
		
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
		
			insertCR_PrepStmt.setString	( 1, cr.getCR());
			insertCR_PrepStmt.setInt	( 2, crId);
			insertCR_PrepStmt.setString	( 3, cr.getCR());
			
			if (cr.getRPY() == null) {
				insertCR_PrepStmt.setNull(4, java.sql.Types.INTEGER);
			} else {
				insertCR_PrepStmt.setInt	( 4, cr.getRPY());
			}
			
			insertCR_PrepStmt.setInt	( 5, 1);

			if (cr.getAU() == null) {
				insertCR_PrepStmt.setNull	( 6, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 6, cr.getAU());
			}
			
			if (cr.getAU_L() == null) {
				insertCR_PrepStmt.setNull	( 7, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 7, cr.getAU_L());
			}
			
			if (cr.getAU_F() == null) {
				insertCR_PrepStmt.setNull	( 8, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 8, cr.getAU_F());
			}

			if (cr.getAU_A() == null) {
				insertCR_PrepStmt.setNull	( 9, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 9, cr.getAU_A());
			}

			if (cr.getTI() == null) {
				insertCR_PrepStmt.setNull	(10, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(10, cr.getTI());
			}
			
			if (cr.getJ() == null) {
				insertCR_PrepStmt.setNull	(11, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(11, cr.getJ());
			}
			
			if (cr.getJ_N() == null) {
				insertCR_PrepStmt.setNull	(12, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(12, cr.getJ_N());
			}
			
			if (cr.getJ_S() == null) {
				insertCR_PrepStmt.setNull	(13, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(13, cr.getJ_S());
			}
			
			if (cr.getVOL() == null) {
				insertCR_PrepStmt.setNull	(14, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(14, cr.getVOL());
			}
			
			if (cr.getPAG() == null) {
				insertCR_PrepStmt.setNull	(15, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(15, cr.getPAG());
			}
			
			if (cr.getDOI() == null) {
				insertCR_PrepStmt.setNull	(16, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(16, cr.getDOI());
			}
			
			insertCR_PrepStmt.setInt(17,  cr.getCID2().getC1());
			insertCR_PrepStmt.setInt(18,  cr.getCID2().getC2());
			insertCR_PrepStmt.setInt(19,  cr.getCID2().getSize());
			
			insertCR_PrepStmt.addBatch();
			
			
			insertCR_batchSize++;
			
			if (insertCR_batchSize==10000) {
				insertCR_PrepStmt.executeBatch();
				insertCR_batchSize = 0;
			}
			
			
			
			insertPubCR_PrepStmt.setInt(1, pubId);
			insertPubCR_PrepStmt.setInt(2, crId);
			insertPubCR_PrepStmt.addBatch();
			insertPubCR_batchSize++;
			
			if (insertPubCR_batchSize==10000) {
				insertPubCR_PrepStmt.executeBatch();
				insertPubCR_batchSize=0;
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
		if (insertCR_batchSize>1000) {
			insertCR_PrepStmt.executeBatch();
			insertCR_batchSize = 0;
		}
		if (insertPubCR_batchSize>1000) {
			insertPubCR_PrepStmt.executeBatch();
			insertPubCR_batchSize=0;
		}
				
	}

//	public void executeBatch() throws SQLException {
//		insertCR_PrepStmt.executeBatch();
//		insertPubCR_PrepStmt.executeBatch();
//	}
//	
	
	
}

package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.CRType.PERCENTAGE;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.extern.CRType_ColumnView;
import main.cre.data.type.extern.PubType_ColumnView;

class DB_Store { 

	public final static String SQL_FILE_PREFIX = "main/cre/data/type/db/sql/"; 
	
	private PreparedStatement insertCR_PrepStmt;
	private int insertCR_Counter;
	
	private PreparedStatement insertPub_PrepStmt;
	private int insertPub_Counter;

	private PreparedStatement updateCRIndicators_PrepStmt;
	private int updateCRIndicators_Counter;

	
	private final int BATCH_SIZE_MAX = 100;
	
	

	
	private Connection dbCon;

	private String wrapup_insert_SQL;
	
	
	DB_Store(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		this.dbCon = dbCon;
		

	}
	
	
	void init () throws SQLException, URISyntaxException, IOException {


		dbCon.setAutoCommit(false);
		
		/* create tables */
		Statement stmt = dbCon.createStatement();
		stmt.execute(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(SQL_FILE_PREFIX + "create_schema.sql").toURI())), StandardCharsets.UTF_8));
		stmt.close();
		
		/* create prepared statements & sql scripts */
		insertCR_PrepStmt = dbCon.prepareStatement(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(SQL_FILE_PREFIX + "pst_insert_cr.sql").toURI())), StandardCharsets.UTF_8));
		insertCR_Counter = 0;
		insertPub_PrepStmt = dbCon.prepareStatement(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(SQL_FILE_PREFIX + "pst_insert_pub.sql").toURI())), StandardCharsets.UTF_8));
		insertPub_Counter = 0;
		updateCRIndicators_PrepStmt = dbCon.prepareStatement(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(SQL_FILE_PREFIX + "pst_update_cr_indicators.sql").toURI())), StandardCharsets.UTF_8));
		updateCRIndicators_Counter = 0;		
		wrapup_insert_SQL = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(SQL_FILE_PREFIX + "wrapup_insert.sql").toURI())), StandardCharsets.UTF_8);
		
		dbCon.commit();
	}
	
	void insertCR (CRType<?> cr, int pubId) throws SQLException {
		
		CRType_DB.addToBatch(insertCR_PrepStmt, cr, pubId);
		
		if (++insertCR_Counter>=BATCH_SIZE_MAX) {
			insertCR_PrepStmt.executeBatch();
			insertCR_Counter = 0;
			dbCon.commit();
		}		
	}
	
	void insertPub (PubType<?> pub) throws SQLException {
		
		PubType_DB.addToBatch(insertPub_PrepStmt, pub);
		
		if (++insertPub_Counter>=BATCH_SIZE_MAX) {
			insertPub_PrepStmt.executeBatch();
			insertPub_Counter = 0;
			dbCon.commit();
		}	
	}
	

	void finishInsert() throws SQLException {
		
		if (insertCR_Counter>0) {
			insertCR_PrepStmt.executeBatch();
			insertCR_Counter = 0;
		}
		
		if (insertPub_Counter>0) {
			insertPub_PrepStmt.executeBatch();
			insertPub_Counter = 0;
		}	
		
		System.out.println("Executing " + wrapup_insert_SQL);
		Statement stmt = dbCon.createStatement();
		stmt.execute(wrapup_insert_SQL);
		stmt.close();
		dbCon.commit();
		
		System.out.println("...done!");

				
	}	
	
	
	
	void updateCRIndicators (int crId, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE)  {
		
		try {
			updateCRIndicators_PrepStmt.clearParameters();
			updateCRIndicators_PrepStmt.setInt		( 1, N_PYEARS); 
			updateCRIndicators_PrepStmt.setDouble 	( 2, PYEAR_PERC); 
			updateCRIndicators_PrepStmt.setDouble 	( 3, PERC_YR); 
			updateCRIndicators_PrepStmt.setDouble 	( 4, PERC_ALL); 
			updateCRIndicators_PrepStmt.setInt		( 5, N_PCT[PERCENTAGE.P50.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 6, N_PCT[PERCENTAGE.P75.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 7, N_PCT[PERCENTAGE.P90.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 8, N_PCT[PERCENTAGE.P99.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 9, N_PCT[PERCENTAGE.P999.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(10, N_PCT_AboveAverage[PERCENTAGE.P50.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(11, N_PCT_AboveAverage[PERCENTAGE.P75.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(12, N_PCT_AboveAverage[PERCENTAGE.P90.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(13, N_PCT_AboveAverage[PERCENTAGE.P99.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(14, N_PCT_AboveAverage[PERCENTAGE.P999.ordinal()]); 
			updateCRIndicators_PrepStmt.setString	(15, SEQUENCE); 
			updateCRIndicators_PrepStmt.setString	(16, TYPE); 
			updateCRIndicators_PrepStmt.setInt		(17, crId); 
			updateCRIndicators_PrepStmt.addBatch();
			
			if (++updateCRIndicators_Counter>=BATCH_SIZE_MAX) {
				updateCRIndicators_PrepStmt.executeBatch();
				updateCRIndicators_Counter = 0;
				dbCon.commit();
			}	
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	void finishUpdateCRIndicators() {
		try {
			if (updateCRIndicators_Counter>0) {
				updateCRIndicators_PrepStmt.executeBatch();
				updateCRIndicators_Counter = 0;
				dbCon.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	Stream<CRType_DB> selectCR(String where ) {
		
		System.out.println("selectCR");
		try {
			return StreamSupport.stream(new CRType_DB.CRType_ResultSet(dbCon.prepareStatement("SELECT * FROM CR " + where).executeQuery()).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<CRType_DB> emptyStr = Stream.of();
			return emptyStr;
		}

	}
	
	Stream<PubType_DB> selectPub(String where) {
		
		System.out.println("selectPub");
		try {
			return StreamSupport.stream(new PubType_DB.PubType_ResultSet(dbCon.prepareStatement("SELECT * FROM Pub " + where).executeQuery()).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<PubType_DB> emptyStr = Stream.of();
			return emptyStr;
		}

	}




	
	void updateCR_VI (String newValue, String predicate) {
		try {
			Statement stmt = dbCon.createStatement();
			stmt.executeUpdate(String.format ("UPDATE CR SET CR_VI = %s %s", newValue, (predicate==null)?"":"WHERE " + predicate)); 
			dbCon.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void removeCR (String predicate) {
		try {
			Statement stmt = dbCon.createStatement();
			stmt.executeUpdate(String.format ("DELETE PUB_CR WHERE CR_ID IN (SELECT CR_ID FROM CR WHERE %s)",  predicate)); 
			stmt.executeUpdate(String.format ("DELETE CR_MATCH_AUTO WHERE CR_ID1 IN (SELECT CR_ID FROM CR WHERE %1$s) OR CR_ID2 IN (SELECT CR_ID FROM CR WHERE %1$s)",  predicate)); 
			stmt.executeUpdate(String.format ("DELETE CR_MATCH_MANU WHERE CR_ID1 IN (SELECT CR_ID FROM CR WHERE %1$s) OR CR_ID2 IN (SELECT CR_ID FROM CR WHERE %1$s)",  predicate)); 
			stmt.executeUpdate(String.format ("DELETE CR WHERE %s",  predicate)); 
			dbCon.commit();
			CRTable_DB.get().updateData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	void removePub (String predicate) {
		
		try {
			Statement stmt = dbCon.createStatement();

			// mark CR that reference at least one publication (--> N_CR=NULL)
			stmt.executeUpdate(String.format ("UPDATE CR SET CR_N_CR = NULL WHERE CR_ID IN (SELECT CR_ID FROM PUB_CR WHERE PUB_ID IN (SELECT PUB_ID FROM PUB WHERE %s))",  predicate));
			
			// delete pub-cr-relationship and pubs
			stmt.executeUpdate(String.format ("DELETE PUB_CR WHERE PUB_ID IN (SELECT PUB_ID FROM PUB WHERE %s)",  predicate));
			stmt.executeUpdate(String.format ("DELETE PUB WHERE %s",  predicate));
			
			// update N_CR 
			stmt.executeUpdate("UPDATE CR SET CR_N_CR = (SELECT COUNT(*) FROM PUB_CR WHERE PUB_CR.CR_ID = CR.CR_ID) WHERE CR_N_CR IS NULL");
			
			// remove CRs with N_CR=0
			removeCR("CR_N_CR = 0"); 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	

	int getNumber (String sql) {
		
		try {
			ResultSet rs = dbCon.createStatement().executeQuery(sql);
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	
}

package main.cre.data.db;

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
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.CRType;

public class CRType_DB {

	private PreparedStatement insertCR_PrepStmt;
	private PreparedStatement selectCR_PrepStmt;
	private PreparedStatement updateCR_PrepStmt;
	
	private Connection dbCon;
	
	public CRType_DB(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		this.dbCon = dbCon;
		
		Path path = Paths.get(getClass().getClassLoader().getResource("main/cre/data/db/CRType_DB.sql").toURI());
		Stream<String> lines = Files.lines(path);
		String[] sql = lines.collect(Collectors.joining("\n")).split("###");
		lines.close();
		
		/* create tables */
		Statement stmt = dbCon.createStatement();
		stmt.execute(sql[0]);
		stmt.close();
	
		/* create prepared statements */
		insertCR_PrepStmt = dbCon.prepareStatement(sql[1]);
		selectCR_PrepStmt = dbCon.prepareStatement("SELECT CR_ID FROM CR WHERE CR_CR = ?");
		updateCR_PrepStmt = dbCon.prepareStatement("UPDATE CR SET CR_N_CR = CR_N_CR+1 WHERE CR_ID = ?");
	}
	
	
	public void init () throws SQLException {
		Statement stmt = dbCon.createStatement();
		stmt.execute("TRUNCATE TABLE CR; TRUNCATE TABLE PUB_CR;");
		stmt.close();
	}
	
	public int insertCR (CRType cr, int defaultId) throws SQLException {
		
//		selectCR_PrepStmt.setString(1, cr.getCR());
//		ResultSet rs = selectCR_PrepStmt.executeQuery();
//		
//		if (rs.next()) {	// we do have an id
//			int id = rs.getInt(1);
//			updateCR_PrepStmt.setInt(1, id);
//			updateCR_PrepStmt.execute();
//			return id;
//		} else {
			insertCR_PrepStmt.setInt	( 1, defaultId);
			insertCR_PrepStmt.setString	( 2, cr.getCR());
			
			if (cr.getRPY() == null) {
				insertCR_PrepStmt.setNull(3, java.sql.Types.INTEGER);
			} else {
				insertCR_PrepStmt.setInt	( 3, cr.getRPY());
			}
			
			insertCR_PrepStmt.setInt	( 4, 1);

			if (cr.getAU() == null) {
				insertCR_PrepStmt.setNull	( 5, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 5, cr.getAU());
			}
			
			if (cr.getAU_L() == null) {
				insertCR_PrepStmt.setNull	( 6, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 6, cr.getAU_L());
			}
			
			if (cr.getAU_F() == null) {
				insertCR_PrepStmt.setNull	( 7, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 7, cr.getAU_F());
			}

			if (cr.getAU_A() == null) {
				insertCR_PrepStmt.setNull	( 8, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 8, cr.getAU_A());
			}

			if (cr.getTI() == null) {
				insertCR_PrepStmt.setNull	( 9, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	( 9, cr.getTI());
			}
			
			if (cr.getJ() == null) {
				insertCR_PrepStmt.setNull	(10, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(10, cr.getJ());
			}
			
			if (cr.getJ_N() == null) {
				insertCR_PrepStmt.setNull	(11, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(11, cr.getJ_N());
			}
			
			if (cr.getJ_S() == null) {
				insertCR_PrepStmt.setNull	(12, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(12, cr.getJ_S());
			}
			
			if (cr.getVOL() == null) {
				insertCR_PrepStmt.setNull	(13, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(13, cr.getVOL());
			}
			
			if (cr.getPAG() == null) {
				insertCR_PrepStmt.setNull	(14, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(14, cr.getPAG());
			}
			
			if (cr.getDOI() == null) {
				insertCR_PrepStmt.setNull	(15, java.sql.Types.VARCHAR);
			} else {
				insertCR_PrepStmt.setString	(15, cr.getDOI());
			}
			
			insertCR_PrepStmt.execute();
			return defaultId;
//		}
	}
	
	
	
}

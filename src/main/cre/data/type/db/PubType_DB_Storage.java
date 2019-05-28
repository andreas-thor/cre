package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import main.cre.data.type.abs.PubType;
import main.cre.data.type.mm.PubType_MM;
import main.cre.data.type.mm.PubType_MM.PubColumn;
import main.cre.ui.CRTableView.ColDataType;

public class PubType_DB_Storage {

	
	private Connection dbCon;
	private final int insertPub_BatchSize = 100;
	private int insertPub_BatchCount;
	private PreparedStatement insertPub_PrepStmt;

	
	public PubType_DB_Storage(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		this.dbCon = dbCon;

		Statement stmt = dbCon.createStatement();
		
		StringBuffer sql = new StringBuffer();
		sql.append ("DROP TABLE IF EXISTS PUB;");
		sql.append ("CREATE TABLE PUB ( ");
		sql.append (Arrays.stream(PubColumn.values()).map(col -> { return col.getSQLCreateTable(); } ).collect(Collectors.joining(", ")));
		sql.append (", PRIMARY KEY (pub_" + PubColumn.ID.id + "));");
		stmt.execute(sql.toString());
		
		sql = new StringBuffer();
		sql.append ("INSERT INTO PUB (");
		sql.append (Arrays.stream(PubColumn.values()).map(col -> { return "pub_" + col.id; } ).collect(Collectors.joining(", ")));
		sql.append (") VALUES (");
		sql.append (Arrays.stream(PubColumn.values()).map(col -> { return "?"; } ).collect(Collectors.joining(", ")));
		sql.append (");");
		insertPub_PrepStmt = dbCon.prepareStatement(sql.toString());
		
		insertPub_BatchCount = 0;
		
		stmt.close();
		
	}
	
	public void insertPub (PubType pub) throws SQLException {
		
		insertPub_PrepStmt.clearParameters();
		int parameterIndex = 1;
		
		for (PubColumn col: PubColumn.values()) {
			Object v = col.prop.apply((PubType_MM) pub).getValue();
			if (col.type == ColDataType.INT) {
				if (v == null) {
					insertPub_PrepStmt.setNull(parameterIndex, java.sql.Types.INTEGER);
				} else {
					insertPub_PrepStmt.setInt(parameterIndex, (int) v);
				}
			}
			if (col.type == ColDataType.STRING) {
				if (v == null) {
					insertPub_PrepStmt.setNull(parameterIndex, java.sql.Types.VARCHAR);
				} else {
					insertPub_PrepStmt.setString(parameterIndex, (String) v);
				}
			}
			parameterIndex++;
		}
		
		insertPub_PrepStmt.execute();		
		
	}

}

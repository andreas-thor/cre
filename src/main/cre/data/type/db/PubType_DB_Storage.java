package main.cre.data.type.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import main.cre.data.type.abs.CRType_ColumnView;
import main.cre.data.type.abs.CRType_ColumnView.ColDataType;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.abs.PubType_ColumnView;
import main.cre.data.type.abs.PubType_ColumnView.PubColumn;
import main.cre.data.type.mm.PubType_MM;

public class PubType_DB_Storage {

	
	private Connection dbCon;
	private final int insertPub_BATCH_SIZE_MAX = 1000;
	private int insertPub_BatchCountCounter;
	private PreparedStatement insertPub_PrepStmt;

	
	public PubType_DB_Storage(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		this.dbCon = dbCon;

		Statement stmt = dbCon.createStatement();
		
		StringBuffer sql = new StringBuffer();
		sql.append ("DROP TABLE IF EXISTS PUB;");
		sql.append ("CREATE TABLE PUB ( ");
		sql.append (Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> { return col.getSQLCreateTable(); } ).collect(Collectors.joining(", ")));
		sql.append (", PRIMARY KEY (pub_" + PubType_ColumnView.PubColumn.ID.id + "));");
		stmt.execute(sql.toString());
		
		sql = new StringBuffer();
		sql.append ("INSERT INTO PUB (");
		sql.append (Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> { return "pub_" + col.id; } ).collect(Collectors.joining(", ")));
		sql.append (") VALUES (");
		sql.append (Arrays.stream(PubType_ColumnView.PubColumn.values()).map(col -> { return "?"; } ).collect(Collectors.joining(", ")));
		sql.append (");");
		insertPub_PrepStmt = dbCon.prepareStatement(sql.toString());
		
		insertPub_BatchCountCounter = 0;
		
		stmt.close();
		
	}
	
	public void insertPub (PubType pub) throws SQLException {
		
		insertPub_PrepStmt.clearParameters();
		int parameterIndex = 1;
		
		for (PubType_ColumnView.PubColumn col: PubType_ColumnView.PubColumn.values()) {
			Object v = col.prop.apply(pub).getValue();
			if (col.type == CRType_ColumnView.ColDataType.INT) {
				if (v == null) {
					insertPub_PrepStmt.setNull(parameterIndex, java.sql.Types.INTEGER);
				} else {
					insertPub_PrepStmt.setInt(parameterIndex, (int) v);
				}
			}
			if (col.type == CRType_ColumnView.ColDataType.STRING) {
				if (v == null) {
					insertPub_PrepStmt.setNull(parameterIndex, java.sql.Types.VARCHAR);
				} else {
					insertPub_PrepStmt.setString(parameterIndex, (String) v);
				}
			}
			parameterIndex++;
		}
		
		insertPub_PrepStmt.addBatch();
		insertPub_BatchCountCounter++;
		if (insertPub_BatchCountCounter>=insertPub_BATCH_SIZE_MAX) {
			insertPub_PrepStmt.executeBatch();
			insertPub_BatchCountCounter = 0;
		}
		
	}

	public void finishInsertPub() throws SQLException {
		if (insertPub_BatchCountCounter>0) {
			insertPub_PrepStmt.executeBatch();
			insertPub_BatchCountCounter = 0;
		}
		
	}

}

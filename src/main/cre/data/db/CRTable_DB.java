package main.cre.data.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.CRTable;
import main.cre.data.type.CRType;
import main.cre.data.type.PubType;
import main.cre.data.type.PubType.PubColumn;
import main.cre.ui.CRTableView.ColDataType;

public class CRTable_DB extends CRTable {

	private static CRTable_DB crTab = null;

	private Connection dbCon;
	private PreparedStatement insertPub_PrepStmt;
	private PreparedStatement insertCR_PrepStmt;
	private int insertPub_BatchSize;

	private int numberOfPubs; 
	private int numberOfCRs;
	
	private CRType_DB crTypeDB;
	
	public static CRTable_DB get() {
		if (crTab == null) {
			crTab = new CRTable_DB();
		}
		return crTab;
	}
	
	
	private CRTable_DB () { 
		
		
		try {
			Class.forName("org.h2.Driver" );
			dbCon = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");

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
			
			insertPub_BatchSize = 0;
			
			stmt.close();
			
			crTypeDB = new CRType_DB(dbCon);
			
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
	public Stream<CRType> getCR() {
		// TODO Auto-generated method stub
		Stream<CRType> emptyStr = Stream.of();
		return emptyStr;
	}

	@Override
	public Stream<PubType> getPub(boolean includePubsWithoutCRs) {
		// TODO Auto-generated method stub
		Stream<PubType> emptyStr = Stream.of();
		return emptyStr;
	}

	@Override
	public Stream<PubType> getPub() {
		// TODO Auto-generated method stub
		Stream<PubType> emptyStr = Stream.of();
		return emptyStr;
	}

	@Override
	public CRType addCR(CRType cr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CRType addCR(CRType cr, boolean checkForDuplicatesAndSetId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PubType addPub(PubType pub, boolean addCRs) {
		
		return addPub (pub, addCRs, false);


	}

	@Override
	public PubType addPub(PubType pub, boolean addCRs, boolean checkForDuplicates) {

		/* TODO: Check for Duplicates */
		this.numberOfPubs++;
		pub.setID(this.numberOfPubs);
		

		try {
			insertPub_PrepStmt.clearParameters();
			int parameterIndex = 1;
			
			for (PubColumn col: PubColumn.values()) {
				Object v = col.prop.apply(pub).getValue();
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
			
			
			pub.getCR().forEach(cr -> {
				
				try {
					int id = crTypeDB.insertCR(cr, this.numberOfCRs+1);
					if (id > this.numberOfCRs) {
						this.numberOfCRs++;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
			
			
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
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCR(Predicate<CRType> cond) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCR(List<CRType> toDelete) {
		// TODO Auto-generated method stub

	}

	@Override
	public void retainCR(List<CRType> toRetain) {
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
	public void removePubByCR(List<CRType> selCR) {
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
	public void filterByCluster(List<CRType> sel) {
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
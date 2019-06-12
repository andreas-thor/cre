package main.cre.data.type.db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRType;

public class CRType_DB extends CRType<PubType_DB> {

	
	private int N_CR;
	private int c1;
	private int c2;
	private int clusterSize;
	
	
	/**
	 * Iterator to generate a stream of CRType_DB from SQL result set
	 * @author Andreas
	 *
	 */
	public static class CRType_ResultSet implements Iterator<CRType_DB> {
	
		private ResultSet rs; 
		
		public CRType_ResultSet(ResultSet rs) throws IOException, SQLException {
			this.rs = rs;
		}
		
		public void close() throws IOException, SQLException {
			this.rs.close();
		}
		
		@Override
		public boolean hasNext() {
			try {
				return this.rs.next();
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
				if (rs.wasNull()) { cr.setRPY(null);}
				cr.N_CR = rs.getInt("CR_N_CR");
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
				cr.c1 = rs.getInt("CR_ClusterId1");
				cr.c2 = rs.getInt("CR_ClusterId2");
				cr.clusterSize = rs.getInt("CR_ClusterSize");
				cr.setVI(rs.getBoolean("CR_VI"));
				
				cr.setPERC_YR(rs.getDouble("CR_PERC_YR"));
				cr.setPERC_ALL(rs.getDouble("CR_PERC_ALL"));
				cr.setN_PYEARS(rs.getInt("CR_N_PYEARS"));
				cr.setPYEAR_PERC(rs.getDouble("CR_PYEAR_PERC"));
				
				int[] N_PCT = { rs.getInt("CR_N_PCT_P50"), rs.getInt("CR_N_PCT_P75"), rs.getInt("CR_N_PCT_P90"), rs.getInt("CR_N_PCT_P99"), rs.getInt("CR_N_PCT_P999") };
				if (!rs.wasNull()) { cr.setN_PCT(N_PCT); }
				int[] N_PCT_AboveAverage = { rs.getInt("CR_N_PCT_AboveAverage_P50"), rs.getInt("CR_N_PCT_AboveAverage_P75"), rs.getInt("CR_N_PCT_AboveAverage_P90"), rs.getInt("CR_N_PCT_AboveAverage_P99"), rs.getInt("CR_N_PCT_AboveAverage_P999") };
				if (!rs.wasNull()) { cr.setN_PCT_AboveAverage(N_PCT_AboveAverage); }
				
				cr.setSEQUENCE(rs.getString("CR_SEQUENCE"));
				cr.setTYPE(rs.getString("CR_TYPE"));
				
				return cr;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public Iterable<CRType_DB> getIterable () { 
			return () -> this;
		}
		
	}


	

	
	
	/**
	 * Add cr to the batch of pst that eventually inserts the cr into the database
	 * @param pst
	 * @param cr
	 * @param pubId
	 * @throws SQLException
	 */
	
	public static void addToBatch (PreparedStatement pst, CRType<?> cr, int pubId) throws SQLException {
		
		pst.clearParameters();
		pst.setInt		(1, cr.getID());
		pst.setString	(2, cr.getCR());
		
		if (cr.getRPY() == null) {
			pst.setNull	(3, java.sql.Types.INTEGER);
		} else {
			pst.setInt 	(3, cr.getRPY());
		}
		
		pst.setString	( 4, cr.getAU());
		pst.setString	( 5, cr.getAU_L());
		pst.setString	( 6, cr.getAU_F());
		pst.setString	( 7, cr.getAU_A());
		pst.setString	( 8, cr.getTI());
		pst.setString	( 9, cr.getJ());
		pst.setString	(10, cr.getJ_N());
		pst.setString	(11, cr.getJ_S());
		pst.setString	(12, cr.getVOL());
		pst.setString	(13, cr.getPAG());
		pst.setString	(14, cr.getDOI());
		pst.setInt		(15, cr.getClusterC1());
		pst.setInt		(16, cr.getClusterC2());
		pst.setInt		(17, cr.getClusterSize());
		pst.setBoolean	(18, cr.getVI());
		pst.setInt		(19, pubId);
		pst.addBatch();
	}
	

	@Override
	public int getN_CR() {
		return this.N_CR;
	}


	@Override
	public int getClusterC1() {
		return c1;
	}

	@Override
	public int getClusterC2() {
		return c2;
	}
	
	@Override
	public int getClusterSize() {
		return clusterSize;
	}
	
	@Override
	public Stream<PubType_DB> getPub() {
		return CRTable_DB.get().getDBStore().selectPub(String.format("WHERE Pub_ID IN (SELECT PUB_ID FROM PUB_CR WHERE CR_ID = %d)", this.getID()));
	}
	
}

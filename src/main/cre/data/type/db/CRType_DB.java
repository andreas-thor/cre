package main.cre.data.type.db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRType;

public class CRType_DB extends CRType<PubType_DB> {

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
				cr.setVI(rs.getBoolean("CR_VI"));
				return cr;
			} catch (Exception e) {
				return null;
			}
		}
		
		public Iterable<CRType_DB> getIterable () { 
			return () -> this;
		}
		
	}

	private int N_CR;
	private int c1;
	private int c2;
	private int clusterSize;
	
	public CRType_DB() {
		super();
		this.N_CR = 0;
	}
	
	
	public static void addToBatch (PreparedStatement pst, CRType<?> cr, int pubId) throws SQLException {
		
		int start = 0;
		pst.clearParameters();
		pst.setInt		(start+ 1, cr.getID());
		pst.setString	(start+ 2, cr.getCR());
		
		if (cr.getRPY() == null) {
			pst.setNull(start+ 3, java.sql.Types.INTEGER);
		} else {
			pst.setInt (start+ 3, cr.getRPY());
		}
		
		pst.setString	(start+ 4, cr.getAU());
		pst.setString	(start+ 5, cr.getAU_L());
		pst.setString	(start+ 6, cr.getAU_F());
		pst.setString	(start+ 7, cr.getAU_A());
		pst.setString	(start+ 8, cr.getTI());
		pst.setString	(start+ 9, cr.getJ());
		pst.setString	(start+10, cr.getJ_N());
		pst.setString	(start+11, cr.getJ_S());
		pst.setString	(start+12, cr.getVOL());
		pst.setString	(start+13, cr.getPAG());
		pst.setString	(start+14, cr.getDOI());
		pst.setInt		(start+15, cr.getClusterC1());
		pst.setInt		(start+16, cr.getClusterC2());
		pst.setInt		(start+17, cr.getClusterSize());
		pst.setBoolean	(start+18, cr.getVI());
		pst.setInt		(start+19, pubId);
		pst.addBatch();
	}
	

	public void setCluster(int c1, int c2, int size) {
		this.c1 = c1;
		this.c2 = c2;
		this.clusterSize = size;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPub(PubType_DB pubType, boolean b) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void removeAllPubs(boolean inverse) {
		// TODO Auto-generated method stub
		
	}

	
	public void setN_CR(int n_CR) {
		this.N_CR = n_CR;
	}
	
	@Override
	public int getN_CR() {
		return this.N_CR;
	}




	
	
	
}

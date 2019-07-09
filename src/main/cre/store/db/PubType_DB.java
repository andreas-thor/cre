package main.cre.store.db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.type.abs.PubType;

public class PubType_DB extends PubType<CRType_DB> {

	public final static CharSequence arraySeparator1 = "\n"; 
	public final static CharSequence arraySeparator2 = "\t"; 
	
	public final static Collector<? super String, ?, String> join = Collectors.joining(arraySeparator1);
	public final static Function<String, Stream<String>> split = s -> (s==null) ? Stream.empty() : Arrays.stream(s.split(arraySeparator1.toString()));
	
	public static class PubType_ResultSet implements Iterator<PubType_DB> {
		
		private ResultSet rs; 
		
		public PubType_ResultSet(ResultSet rs) throws IOException, SQLException {
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
		public PubType_DB next() {
			try {
				PubType_DB pub = new PubType_DB();
				pub.setID(rs.getInt("PUB_ID"));
				
				pub.setID(rs.getInt("PUB_ID"));
				pub.setPT(rs.getString("PUB_PT"));
				split.apply(rs.getString("PUB_AU")).forEach(it -> pub.addAU(it));
				split.apply(rs.getString("PUB_AF")).forEach(it -> pub.addAF(it));
				split.apply(rs.getString("PUB_C1")).forEach(it -> pub.addC1(it.split(arraySeparator2.toString())));
				split.apply(rs.getString("PUB_EM")).forEach(it -> pub.addEM(it));
				split.apply(rs.getString("PUB_AA")).forEach(it -> pub.addAA(it));
				pub.setTI(rs.getString("PUB_TI"));
				pub.setPY(rs.getInt("PUB_PY"));		if (rs.wasNull()) pub.setPY(null);
				pub.setSO(rs.getString("PUB_SO"));
				pub.setVL(rs.getString("PUB_VL"));
				pub.setIS(rs.getString("PUB_IS"));
				pub.setAR(rs.getString("PUB_AR"));
				pub.setBP(rs.getInt("PUB_BP"));		if (rs.wasNull()) pub.setBP(null);
				pub.setEP(rs.getInt("PUB_EP"));		if (rs.wasNull()) pub.setEP(null);	
				pub.setPG(rs.getInt("PUB_PG"));		if (rs.wasNull()) pub.setPG(null);
				pub.setTC(rs.getInt("PUB_TC"));		if (rs.wasNull()) pub.setTC(null);
				pub.setDI(rs.getString("PUB_DI"));
				pub.setLI(rs.getString("PUB_LI"));
				pub.setAB(rs.getString("PUB_AB"));
				pub.setDE(rs.getString("PUB_DE"));
				pub.setDT(rs.getString("PUB_DT"));
				pub.setFS(rs.getString("PUB_FS"));
				pub.setUT(rs.getString("PUB_UT"));
				
				return pub;
			} catch (Exception e) {
				return null;
			}
		}
		
		public Iterable<PubType_DB> getIterable () { 
			return () -> this;
		}
		
	}	
	
	public static void addToBatch (PreparedStatement pst, PubType<?> pub) throws SQLException {

		pst.clearParameters();
		
		pst.setInt 		( 1, pub.getID()); 
		pst.setString 	( 2, pub.getPT()); 
		
		if (pub.getAU().count()==0) { pst.setNull( 3, Types.VARCHAR); } else { pst.setString ( 3, pub.getAU().collect(join)); } 
		if (pub.getAF().count()==0) { pst.setNull( 4, Types.VARCHAR); } else { pst.setString ( 4, pub.getAF().collect(join)); } 
		if (pub.getC1().count()==0) { pst.setNull( 5, Types.VARCHAR); } else { pst.setString ( 5, pub.getC1().map(it -> String.join(arraySeparator2, it)).collect(join)); } 
		if (pub.getEM().count()==0) { pst.setNull( 6, Types.VARCHAR); } else { pst.setString ( 6, pub.getEM().collect(join)); } 
		if (pub.getAA().count()==0) { pst.setNull( 7, Types.VARCHAR); } else { pst.setString ( 7, pub.getAA().collect(join)); } 
		
		if ((pub.getID()==161) || (pub.getID()==447) || (pub.getID()==488)) {
			System.out.println("============== " + pub.getID());
			System.out.println("EM count = " + pub.getEM().count());
			System.out.println("AA count = " + pub.getAA().count());
		}
		
		pst.setString 	( 8, pub.getTI()); 
		if (pub.getPY()==null) { pst.setNull( 9, Types.INTEGER); } else { pst.setInt ( 9, pub.getPY()); } 
		pst.setString 	(10, pub.getSO()); 
		pst.setString 	(11, pub.getVL()); 
		pst.setString 	(12, pub.getIS()); 
		pst.setString 	(13, pub.getAR()); 
		if (pub.getBP()==null) { pst.setNull(14, Types.INTEGER); } else { pst.setInt (14, pub.getBP()); } 
		if (pub.getEP()==null) { pst.setNull(15, Types.INTEGER); } else { pst.setInt (15, pub.getEP()); } 
		if (pub.getPG()==null) { pst.setNull(16, Types.INTEGER); } else { pst.setInt (16, pub.getPG()); } 
		if (pub.getTC()==null) { pst.setNull(17, Types.INTEGER); } else { pst.setInt (17, pub.getTC()); } 
		pst.setString 	(18, pub.getDI()); 
		pst.setString 	(19, pub.getLI()); 
		pst.setString 	(20, pub.getAB()); 
		pst.setString 	(21, pub.getDE()); 
		pst.setString 	(22, pub.getDT()); 
		pst.setString 	(23, pub.getFS()); 
		pst.setString 	(24, pub.getUT());
		pst.addBatch();
	}
	
	@Override
	public Stream<CRType_DB> getCR(boolean sortById) {
		String order = sortById ? "ORDER BY CR_ID" : "";
		return CRTable_DB.get().getDBStore().selectCR(String.format("WHERE CR_ID IN (SELECT CR_ID FROM PUB_CR WHERE PUB_ID = %d) %s", this.getID(), order));
	}

	@Override
	public int getSizeCR() {
		return CRTable_DB.get().getDBStore().getNumber(String.format("SELECT COUNT(*) FROM PUB_CR WHERE PUB_ID = %d", this.getID()));
	}



}

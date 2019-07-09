package main.cre.data.type.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import main.cre.data.type.db.DB_Store.Queries;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.PubType_MM;
import main.cre.format.cre.Reader;

public class Reader_DB extends Reader {


	
	private Connection dbCon;
	
	private PreparedStatement pst_OnNewCR;
	private int pst_OnNewCR_Counter;
	private PreparedStatement pst_OnNewPub;
	private int pst_OnNewPub_Counter;
	private PreparedStatement pst_OnNewPub_CR;
	private int pst_OnNewPub_CR_Counter;

	
	private PreparedStatement pst_OnNew_MatchPair_Manu;
	private int pst_OnNew_MatchPair_Manu_Counter;
	private PreparedStatement pst_OnNew_MatchPair_Auto;
	private int pst_OnNew_MatchPair_Auto_Counter;
	
	private int numberOfPubs;
	
	public Reader_DB(Connection dbCon) throws SQLException {
		this.dbCon = dbCon;
		
		pst_OnNewCR = dbCon.prepareStatement(Queries.getQuery("reader/on_new_cr.sql"));
		pst_OnNewCR_Counter = 0;
		
		pst_OnNewPub = dbCon.prepareStatement(Queries.getQuery("reader/on_new_pub.sql"));
		pst_OnNewPub_Counter = 0;
		
		pst_OnNewPub_CR = dbCon.prepareStatement(Queries.getQuery("reader/on_new_pub_cr.sql"));
		pst_OnNewPub_CR_Counter = 0;
		
		pst_OnNew_MatchPair_Manu = dbCon.prepareStatement(Queries.getQuery("reader/on_new_matchpair_manu.sql"));
		pst_OnNew_MatchPair_Manu_Counter = 0;
		
		pst_OnNew_MatchPair_Auto = dbCon.prepareStatement(Queries.getQuery("reader/on_new_matchpair_auto.sql"));
		pst_OnNew_MatchPair_Auto_Counter = 0;

		
	}

	@Override
	public void onBeforeLoad() {
		this.numberOfPubs = 0;
	}

	
	@Override
	public void onNewCR(CRType_MM cr) {
		
		try {
			// INSERT INTO CR ...
			pst_OnNewCR.clearParameters();
			pst_OnNewCR.setInt		(1, cr.getID());
			pst_OnNewCR.setString	(2, cr.getCR());
			if (cr.getRPY() == null) { pst_OnNewCR.setNull(3, java.sql.Types.INTEGER); } else { pst_OnNewCR.setInt (3, cr.getRPY()); }
			pst_OnNewCR.setString	( 4, cr.getAU());
			pst_OnNewCR.setString	( 5, cr.getAU_L());
			pst_OnNewCR.setString	( 6, cr.getAU_F());
			pst_OnNewCR.setString	( 7, cr.getAU_A());
			pst_OnNewCR.setString	( 8, cr.getTI());
			pst_OnNewCR.setString	( 9, cr.getJ());
			pst_OnNewCR.setString	(10, cr.getJ_N());
			pst_OnNewCR.setString	(11, cr.getJ_S());
			pst_OnNewCR.setString	(12, cr.getVOL());
			pst_OnNewCR.setString	(13, cr.getPAG());
			pst_OnNewCR.setString	(14, cr.getDOI());
			pst_OnNewCR.setBoolean	(15, cr.getVI());
			pst_OnNewCR.setString	(16, cr.getFormatType().toString());
			pst_OnNewCR.addBatch();
			pst_OnNewCR_Counter++;
			
			if (pst_OnNewCR_Counter>100) {
				pst_OnNewCR.executeBatch();
				pst_OnNewCR_Counter = 0;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onNewPub(PubType_MM pub, List<Integer> crIds) {
		
		this.numberOfPubs++;
		pub.setID(this.numberOfPubs);
		
		try {
			// INSERT INTO PUB ...
			pst_OnNewPub.clearParameters();
			pst_OnNewPub.setInt 	( 1, pub.getID()); 
			pst_OnNewPub.setString 	( 2, pub.getPT()); 
			pst_OnNewPub.setString 	( 3, pub.getAU().count() == 0 ? null : pub.getAU().collect(PubType_DB.join)); 
			pst_OnNewPub.setString 	( 4, pub.getAF().count() == 0 ? null : pub.getAF().collect(PubType_DB.join)); 
			pst_OnNewPub.setString 	( 5, pub.getC1().count() == 0 ? null : pub.getC1().map(it -> String.join(PubType_DB.arraySeparator2, it)).collect(PubType_DB.join)); 
			pst_OnNewPub.setString 	( 6, pub.getEM().count() == 0 ? null : pub.getEM().collect(PubType_DB.join)); 
			pst_OnNewPub.setString 	( 7, pub.getAA().count() == 0 ? null : pub.getAA().collect(PubType_DB.join)); 
			pst_OnNewPub.setString 	( 8, pub.getTI()); 
			if (pub.getPY()==null) { pst_OnNewPub.setNull( 9, Types.INTEGER); } else { pst_OnNewPub.setInt ( 9, pub.getPY()); } 
			pst_OnNewPub.setString 	(10, pub.getSO()); 
			pst_OnNewPub.setString 	(11, pub.getVL()); 
			pst_OnNewPub.setString 	(12, pub.getIS()); 
			pst_OnNewPub.setString 	(13, pub.getAR()); 
			if (pub.getBP()==null) { pst_OnNewPub.setNull(14, Types.INTEGER); } else { pst_OnNewPub.setInt (14, pub.getBP()); } 
			if (pub.getEP()==null) { pst_OnNewPub.setNull(15, Types.INTEGER); } else { pst_OnNewPub.setInt (15, pub.getEP()); } 
			if (pub.getPG()==null) { pst_OnNewPub.setNull(16, Types.INTEGER); } else { pst_OnNewPub.setInt (16, pub.getPG()); } 
			if (pub.getTC()==null) { pst_OnNewPub.setNull(17, Types.INTEGER); } else { pst_OnNewPub.setInt (17, pub.getTC()); } 
			pst_OnNewPub.setString 	(18, pub.getDI()); 
			pst_OnNewPub.setString 	(19, pub.getLI()); 
			pst_OnNewPub.setString 	(20, pub.getAB()); 
			pst_OnNewPub.setString 	(21, pub.getDE()); 
			pst_OnNewPub.setString 	(22, pub.getDT()); 
			pst_OnNewPub.setString 	(23, pub.getFS()); 
			pst_OnNewPub.setString 	(24, pub.getUT());
			pst_OnNewPub.addBatch();		
			pst_OnNewPub_Counter++;
			
			if (pst_OnNewPub_Counter>100) {
				pst_OnNewPub.executeBatch();
				pst_OnNewPub_Counter = 0;
			}
			
			// INSERT INTO PUB_CR ...
			for (int crId: crIds) {
				pst_OnNewPub_CR.clearParameters();
				pst_OnNewPub_CR.setInt(1, pub.getID());
				pst_OnNewPub_CR.setInt(2, crId);
				pst_OnNewPub_CR.addBatch();
				pst_OnNewPub_CR_Counter++;
			}
			
			if (pst_OnNewPub_CR_Counter>100) {
				pst_OnNewPub_CR.executeBatch();
				pst_OnNewPub_CR_Counter = 0;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}

	@Override
	public void onNewMatchPair(int crId1, int crId2, double sim, boolean isManual) {
		// TODO Auto-generated method stub
		try {
			if (isManual) {
				pst_OnNew_MatchPair_Manu.clearParameters();
				pst_OnNew_MatchPair_Manu.setInt		(1, crId1);		
				pst_OnNew_MatchPair_Manu.setInt		(2, crId2);		
				pst_OnNew_MatchPair_Manu.setDouble	(3, sim);
				pst_OnNew_MatchPair_Manu.addBatch();
				pst_OnNew_MatchPair_Manu_Counter++;

				if (pst_OnNew_MatchPair_Manu_Counter>100) {
					pst_OnNew_MatchPair_Manu.executeBatch();
					pst_OnNew_MatchPair_Manu_Counter = 0;
				}
				
			} else {
				pst_OnNew_MatchPair_Auto.clearParameters();
				pst_OnNew_MatchPair_Auto.setInt		(1, crId1);		
				pst_OnNew_MatchPair_Auto.setInt		(2, crId2);		
				pst_OnNew_MatchPair_Auto.setDouble	(3, sim);
				pst_OnNew_MatchPair_Auto.addBatch();
				pst_OnNew_MatchPair_Auto_Counter++;
				
				if (pst_OnNew_MatchPair_Auto_Counter>100) {
					pst_OnNew_MatchPair_Auto.executeBatch();
					pst_OnNew_MatchPair_Auto_Counter = 0;
				}
				
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}

	@Override
	public void onAfterLoad() {
		try {
			
			if (pst_OnNewCR_Counter>0) {
				pst_OnNewCR.executeBatch();
				pst_OnNewCR_Counter = 0;
			}
			
			if (pst_OnNewPub_Counter>0) {
				pst_OnNewPub.executeBatch();
				pst_OnNewPub_Counter = 0;
			}
			
			if (pst_OnNewPub_CR_Counter>0) {
				pst_OnNewPub_CR.executeBatch();
				pst_OnNewPub_CR_Counter = 0;
			}
			
			if (pst_OnNew_MatchPair_Manu_Counter>0) {
				pst_OnNew_MatchPair_Manu.executeBatch();
				pst_OnNew_MatchPair_Manu_Counter = 0;
			}
			
			if (pst_OnNew_MatchPair_Auto_Counter>0) {
				pst_OnNew_MatchPair_Auto.executeBatch();
				pst_OnNew_MatchPair_Auto_Counter = 0;
			}		
			
			this.dbCon.createStatement().execute(Queries.getQuery("reader/on_after_load.sql"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}

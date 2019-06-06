package main.cre.data.type.extern;

import java.util.function.Function;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.CRType.PERCENTAGE;

public class CRType_ColumnView {

	// Column Information
	public static enum ColGroup { CR, INDICATOR, CLUSTER, SEARCH }

	public static enum ColDataType { INT, DOUBLE, STRING /*, CRCLUSTER*/ }

	public static enum CRColumn {
			
			ID 	("ID", "ID", ColGroup.CR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getID())),
			CR 	("CR", "Cited Reference", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getCR())),
			RPY ("RPY", "Reference Publication Year", ColGroup.CR, ColDataType.INT, cr -> new SimpleObjectProperty<Integer>(cr.getRPY())),
			N_CR ("N_CR", "Number of Cited References", ColGroup.CR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_CR())),
			PERC_YR ("PERC_YR", "Percent in Year", ColGroup.INDICATOR, ColDataType.DOUBLE, cr -> new SimpleDoubleProperty(cr.getPERC_YR())),
			PERC_ALL ("PERC_ALL", "Percent over all Years", ColGroup.INDICATOR, ColDataType.DOUBLE, cr -> new SimpleDoubleProperty(cr.getPERC_ALL())),
			AU ("AU", "Author", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU())),
			AU_L ("AU_L", "Last Name", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU_L())),
			AU_F ("AU_F", "First Name Initial", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU_F())),
			AU_A ("AU_A", "Authors", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU_A())),
			TI ("TI", "Title", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getTI())),
			J ("J", "Source", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getJ())),
			J_N ("J_N", "Source Title", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getJ_N())),
			J_S ("J_S", "Title short", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getJ_S())),
			VOL ("VOL", "Volume", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty (cr.getVOL())),
			PAG ("PAG", "Page", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getPAG())),
			DOI ("DOI", "DOI", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getDOI())),
			CID2 ("CID2", "ClusterID", ColGroup.CLUSTER, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getClusterId())),
			CID_S ("CID_S", "ClusterSize", ColGroup.CLUSTER, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getClusterSize())),
			
			N_PYEARS ("N_PYEARS", "Number of Citing Years", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PYEARS())),
			PYEAR_PERC ("PERC_PYEARS", "Percentage of Citing Years", ColGroup.INDICATOR, ColDataType.DOUBLE, cr -> new SimpleDoubleProperty(cr.getPYEAR_PERC())),
			
			N_PCT50 ("N_TOP50", "Top 50% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P50))),
			N_PCT75 ("N_TOP25", "Top 25% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P75))),
			N_PCT90 ("N_TOP10", "Top 10% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P90))),
			N_PCT99 ("N_TOP1", "Top 1% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P99))),
			N_PCT999 ("N_TOP0_1", "Top 0.1% Cited Reference", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P999))),
			
			N_PCT50_AboveAverage ("N_TOP50+", "Top 50% Cited Reference & Above Average", ColGroup.INDICATOR, ColDataType.INT,  cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P50))),
			N_PCT75_AboveAverage ("N_TOP25+", "Top 25% Cited Reference & Above Average", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P75))),
			N_PCT90_AboveAverage ("N_TOP10+", "Top 10% Cited Reference & Above Average", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P90))),
			N_PCT99_AboveAverage ("N_TOP1+", "Top 1% Cited Reference & Above Average", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P99))),
			N_PCT999_AboveAverage ("N_TOP0_1+", "Top 0.1% Cited Reference & Above Average", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P999))),
			
			SEQUENCE  ("SEQUENCE", "Sequence", ColGroup.INDICATOR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getSEQUENCE())),
			TYPE  ("TYPE", "Type", ColGroup.INDICATOR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getTYPE())),
			SEARCH_SCORE  ("SEARCH_SCORE", "Score from Search Process", ColGroup.SEARCH, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getSEARCH_SCORE()))
			
			
			
	//		CO  ("CO", "CO", ColGroup.INVISIBLE, ColDataType.INT, CRType::getCOProp), 
	
			;
	//		N_PYEARS2 ("N_PYEARS2", "N_PYEARS2", ColGroup.INDICATOR, ColDataType.INT, CRType::getN_PYEARS2Prop);
			
			public String id;
			public String title;
			public ColGroup group;	
			public ColDataType type;
			public Function<CRType, ObservableValue<?>> prop;
			
			CRColumn(String id, String title, ColGroup group, ColDataType type, Function<CRType, ObservableValue<?>> prop) {
				this.id = id;
				this.title = title;
				this.group = group;
				this.type = type;
				this.prop = prop;
			}
		}

}

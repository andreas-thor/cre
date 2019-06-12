DROP TABLE IF EXISTS CR;

CREATE TABLE CR ( 
	CR_ID 	int, 
	CR_CR 	varchar , 
	CR_RPY 	int, 
	CR_N_CR int, 
	CR_AU 	varchar, 
	CR_AU_L varchar, 
	CR_AU_F varchar, 
	CR_AU_A varchar, 
	CR_TI 	varchar, 
	CR_J 	varchar, 
	CR_J_N 	varchar, 
	CR_J_S 	varchar, 
	CR_VOL 	varchar, 
	CR_PAG 	varchar, 
	CR_DOI 	varchar, 
	CR_ClusterId1 int, 
	CR_ClusterId2 int,  
	CR_ClusterSize int,  
	CR_VI boolean, 
	
	CR_PERC_YR double, 
	CR_PERC_ALL double, 
	CR_N_PYEARS int,	
	CR_PYEAR_PERC double,
	CR_N_PCT_P50 int,
	CR_N_PCT_P75 int,
	CR_N_PCT_P90 int,
	CR_N_PCT_P99 int,
	CR_N_PCT_P999 int,
	CR_N_PCT_AboveAverage_P50 int,
	CR_N_PCT_AboveAverage_P75 int,
	CR_N_PCT_AboveAverage_P90 int,
	CR_N_PCT_AboveAverage_P99 int,
	CR_N_PCT_AboveAverage_P999 int,
	CR_SEQUENCE varchar,
	CR_TYPE varchar,		
	
	PRIMARY KEY (CR_ID)  
);


/* we use a temp table for efficient import */

DROP TABLE IF EXISTS CR_Temp;

CREATE TABLE CR_Temp ( 
	CR_ID 	int, 
	CR_CR 	varchar , 
	CR_RPY 	int, 
	CR_AU 	varchar, 
	CR_AU_L varchar, 
	CR_AU_F varchar, 
	CR_AU_A varchar, 
	CR_TI 	varchar, 
	CR_J 	varchar, 
	CR_J_N 	varchar, 
	CR_J_S 	varchar, 
	CR_VOL 	varchar, 
	CR_PAG 	varchar, 
	CR_DOI 	varchar, 
	CR_ClusterId1 int, 
	CR_ClusterId2 int,  
	CR_ClusterSize int, 
	CR_VI boolean,	
	PUB_ID int, 
	PRIMARY KEY (CR_ID)  
);


DROP TABLE IF EXISTS PUB;

CREATE TABLE PUB ( 
	PUB_ID int, 
	PUB_PT varchar, 
	PUB_AU varchar,  
	PUB_AF varchar,
	PUB_C1 varchar,
	PUB_EM varchar,
	PUB_AA varchar,
	PUB_TI varchar,
	PUB_PY int, 
	PUB_SO varchar,
	PUB_VL varchar,
	PUB_IS varchar,
	PUB_AR varchar,
	PUB_BP int, 
	PUB_EP int, 
	PUB_PG int, 
	PUB_TC int, 
	PUB_DI varchar,
	PUB_LI varchar,
	PUB_AB varchar,
	PUB_DE varchar,
	PUB_DT varchar,
	PUB_FS varchar,
	PUB_UT varchar,
	PRIMARY KEY (PUB_ID)  
);


/* CREATE UNIQUE INDEX CRSTRING ON CR(CR_CR); */  

DROP TABLE IF EXISTS PUB_CR;

CREATE TABLE PUB_CR ( 
	PUB_ID	int, 
	CR_ID 	int, 
	PRIMARY KEY (PUB_ID, CR_ID)  
);


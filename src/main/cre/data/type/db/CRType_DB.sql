DROP TABLE IF EXISTS CR;

CREATE TABLE CR ( 
	CR_ID 	int, 
	CR_CR 	varchar UNIQUE, 
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
	PRIMARY KEY (CR_ID)  
);

/* CREATE UNIQUE INDEX CRSTRING ON CR(CR_CR); */  

DROP TABLE IF EXISTS PUB_CR;

CREATE TABLE PUB_CR ( 
	PUB_ID	int, 
	CR_ID 	int, 
	PRIMARY KEY (PUB_ID, CR_ID)  
);

###

MERGE INTO CR 
USING (SELECT CAST(? AS varchar) AS CR_CR) AS T
ON (CR.CR_CR = T.CR_CR)
WHEN MATCHED THEN
  UPDATE SET CR.CR_N_CR = CR.CR_N_CR +1
WHEN NOT MATCHED THEN
INSERT ( 
	CR_ID, 
	CR_CR, 
	CR_RPY, 
	CR_N_CR, 
	CR_AU, 
	CR_AU_L, 
	CR_AU_F, 
	CR_AU_A, 
	CR_TI, 
	CR_J, 
	CR_J_N, 
	CR_J_S, 
	CR_VOL, 
	CR_PAG, 
	CR_DOI, 
	CR_ClusterId1, 
	CR_ClusterId2,
	CR_ClusterSize
) 
VALUES (
	?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
)

###

INSERT INTO PUB_CR 
(PUB_ID, CR_ID)
VALUES 
(?, ?)


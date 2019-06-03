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
	PRIMARY KEY (CR_ID)  
);

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


/* CREATE UNIQUE INDEX CRSTRING ON CR(CR_CR); */  

DROP TABLE IF EXISTS PUB_CR;

CREATE TABLE PUB_CR ( 
	PUB_ID	int, 
	CR_ID 	int, 
	PRIMARY KEY (PUB_ID, CR_ID)  
);

###

INSERT INTO CR_Temp  ( 
	CR_ID, 
	CR_CR, 
	CR_RPY, 
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
	CR_ClusterSize,
	CR_VI,	
	PUB_ID
) 
VALUES (
	?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
)

###

INSERT INTO PUB_CR (CR_ID, PUB_ID)
SELECT first_value (CR_ID) over (partition by CR_CR order by CR_ID), PUB_ID
FROM CR_Temp;

INSERT INTO CR 
(       CR_ID, CR_CR, CR_RPY, CR_N_CR, CR_AU, CR_AU_L, CR_AU_F, CR_AU_A, CR_TI, CR_J, CR_J_N, CR_J_S, CR_VOL, CR_PAG, CR_DOI, 
       	CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_VI)
SELECT CR_Temp.CR_ID, CR_CR, CR_RPY, T.PUBCOUNT, CR_AU, CR_AU_L, CR_AU_F, CR_AU_A, CR_TI, CR_J, CR_J_N, CR_J_S, CR_VOL, CR_PAG, CR_DOI, 
       CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_VI
FROM CR_Temp
JOIN (
	SELECT CR_ID, COUNT(*) AS PUBCOUNT
	FROM PUB_CR
	GROUP BY CR_ID
) AS T
ON (CR_Temp.CR_ID = T.CR_ID);

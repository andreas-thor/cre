DROP TABLE IF EXISTS CR;

CREATE TABLE CR ( 
	CR_ID 	int, 
	CR_CR 	varchar, 
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

INSERT INTO CR ( 
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
	CR_DOI
) VALUES (
	?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
)


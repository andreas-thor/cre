

INSERT INTO PUB_CR (CR_ID, PUB_ID)
SELECT DISTINCT first_value (CR_ID) over (partition by CR_CR order by CR_ID), PUB_ID
FROM CR_Temp;

INSERT INTO CR 
(       CR_ID, CR_CR, CR_RPY, CR_N_CR, CR_AU, CR_AU_L, CR_AU_F, CR_AU_A, CR_TI, CR_J, CR_J_N, CR_J_S, CR_VOL, CR_PAG, CR_DOI, 
       	CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_VI, CR_Format)
SELECT CR_Temp.CR_ID, CR_CR, CR_RPY, T.PUBCOUNT, CR_AU, CR_AU_L, CR_AU_F, CR_AU_A, CR_TI, CR_J, CR_J_N, CR_J_S, CR_VOL, CR_PAG, CR_DOI, 
       CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_VI, CR_Format
FROM CR_Temp
JOIN (
	SELECT CR_ID, COUNT(*) AS PUBCOUNT
	FROM PUB_CR
	GROUP BY CR_ID
) AS T
ON (CR_Temp.CR_ID = T.CR_ID);

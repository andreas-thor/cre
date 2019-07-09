
-- merged_into = CR of the cluster with the highest number of citations
MERGE INTO CR
(CR_ID, CR_MERGED_INTO)
SELECT 
  CR_ID, FIRST_VALUE(CR_ID) OVER (PARTITION BY CR_ClusterId1, CR_ClusterId2 ORDER BY CR_N_CR DESC, CR_ID) AS V
FROM CR;

-- adjust the pub_cr relationship
MERGE INTO PUB_CR
(PUB_ID, CR_ID)
SELECT DISTINCT PUB_ID, CR_MERGED_INTO
FROM PUB_CR JOIN CR ON (PUB_CR.CR_ID = CR.CR_ID);

DELETE
FROM PUB_CR
WHERE CR_ID NOT IN (
	SELECT CR_MERGED_INTO FROM CR
);

-- remove CRs that are merged into other CR
DELETE 
FROM CR
WHERE CR_ID != CR_MERGED_INTO;

-- update remainings CRs (sum of N_CR; re-init Clustering)
MERGE INTO CR
(CR_ID, CR_N_CR, CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_MERGED_INTO)
SELECT CR_ID, COUNT(*), CR_ID, CR_ID, 1, NULL 
FROM PUB_CR
GROUP BY CR_ID;

-- remove match results
TRUNCATE TABLE CR_MATCH_AUTO;
TRUNCATE TABLE CR_MATCH_MANU;


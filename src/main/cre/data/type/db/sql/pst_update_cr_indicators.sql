UPDATE CR 
SET
	CR_N_PYEARS = ?,	
	CR_PYEAR_PERC = ?,
	CR_PERC_YR = ?, 
	CR_PERC_ALL = ?, 
	CR_N_PCT_P50 = ?,
	CR_N_PCT_P75 = ?,
	CR_N_PCT_P90 = ?,
	CR_N_PCT_P99 = ?,
	CR_N_PCT_P999 = ?,
	CR_N_PCT_AboveAverage_P50 = ?,
	CR_N_PCT_AboveAverage_P75 = ?,
	CR_N_PCT_AboveAverage_P90 = ?,
	CR_N_PCT_AboveAverage_P99 = ?,
	CR_N_PCT_AboveAverage_P999 = ?,
	CR_SEQUENCE = ?,
	CR_TYPE = ?
WHERE
	CR_ID = ?


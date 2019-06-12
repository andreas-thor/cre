package main.cre.data.type.abs;

@FunctionalInterface
public interface CRIndicatorsUpdate {
		
	public void update (int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE);
	
}

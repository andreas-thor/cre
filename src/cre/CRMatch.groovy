package cre 

import groovy.transform.CompileStatic;
import groovy.transform.stc.ClosureParams;

@CompileStatic
class CRMatch {


	class Pair {
		Integer id1
		Integer id2
		Double s
		public Pair(Integer id1, Integer id2, Double s) {
			super();
			this.id1 = id1;
			this.id2 = id2;
			this.s = s;
		}
	}
	
	public Map <Boolean, Map<Integer, Map<Integer, Double>>> match = [:]
	
	public TreeMap <Long, ArrayList<Pair>> timestampedPairs
	
	public CRMatch () {
		match = [:]
		match[false] = [:]		// automatic match result
		match[true] = [:]		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<Pair>>()
	}



	public void eachPair (Closure c) {
		HashSet<Integer> id = new HashSet<Integer>()
		id.addAll(match[false].keySet())
		id.addAll(match[true].keySet())
		eachPair (id, c)
	}

	public void eachPair (Collection<Integer> id, Closure c) {

		if (id == null) { 
			eachPair (c)
		} else {
			id.each { Integer id1 ->

				Map<Integer, Double> tmpMap1 = match[false][id1]?:(Map<Integer, Double>)[:]
				Map<Integer, Double> tmpMap2 = match[true][id1]?:(Map<Integer, Double>)[:]
				(tmpMap1.keySet()+tmpMap2.keySet()).each { Integer id2 ->
					
					if ((tmpMap2[id2]!=null) || (tmpMap1[id2]!=null)) {	// due to undo operations: sim value can be null 
						c (id1, id2, tmpMap2[id2]?:tmpMap1[id2])	// manual mapping [true] overrides automatic [false]
					}
				}
			}
		}
	}


	/**
	 * Restrict match result to list of given ids
	 * Called when user deletes CRs
	 * @param id
	 */
	public void restrict (List<Integer> id) {
		[true, false].each { boolean tf ->
			match[tf].keySet().removeAll { Integer it -> !id.contains(it) }
			match[tf].each { Integer k, Map<Integer, Double> map ->
				match[tf][k].keySet().removeAll { Integer it -> !id.contains(it) }
			}
		}
	}


	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add, Long timestamp=null) {

		if (id1.equals(id2)) return

			// swap if needed so that always holds id1<id2
			if (id1.compareTo(id2)>0) {
				(id1,id2) = [id2, id1]
			}

		if (match[isManual][id1] == null) {
			match[isManual][id1] = [:]
		}

		if (match[!isManual][id1] == null) {
			match[!isManual][id1] = [:]
		}

		// store old value for undo operation of manual mappings
		if ((isManual) && (timestamp!=null)) {
			if (timestampedPairs.get(timestamp)==null) {
				timestampedPairs.put(timestamp, new ArrayList<Pair>())
			}
			timestampedPairs.get(timestamp).add(new Pair(id1, id2, match[isManual][id1][id2]))
		}
		
		double v = 0d
		if ((add) && (match[isManual][id1][id2] != null)) {
			v = match[isManual][id1][id2]
		}

		match[isManual][id1][id2] = (s==null) ? null : s+v
		
		
		
		
	}



	public Map getMapping (Integer id1, Boolean isManual) {
		match[isManual][id1]?:[:]
	}


	public void clear (boolean isManual) {
		match[isManual] = [:]
	}

	public int size (boolean isManual) {
		(int) match[isManual].inject (0) { int res, Integer k, Map<Integer, Double> v -> res + v.size() }
	}





}

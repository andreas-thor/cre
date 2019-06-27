package main.cre.data.type.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import main.cre.data.type.abs.MatchPairGroup;

public class MatchPairGroup_Resultset implements Iterator<MatchPairGroup> {
	
	private ResultSet rs;
	private boolean hasNextBlock;
	
	public MatchPairGroup_Resultset(ResultSet rs) throws IOException, SQLException {
		this.rs = rs;
		hasNextBlock = rs.next();
	}
	
	public void close() throws IOException, SQLException {
		this.rs.close();
	}
	
	@Override
	public boolean hasNext() {
		return hasNextBlock;
	}

	@Override
	public MatchPairGroup next() {
		
		try {
			MatchPairGroup res = new MatchPairGroup(rs.getInt(1));
			res.addMatch(rs.getInt(2), rs.getDouble(3));
			
			while (rs.next()) {
				if (rs.getInt(1) == res.getCrId1()) {
					res.addMatch(rs.getInt(2), rs.getDouble(3));
				} else {
					hasNextBlock = true;
					return res;
				}
			}

			hasNextBlock = false;
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Iterable<MatchPairGroup> getIterable () { 
		return () -> this;
	}
	
}		

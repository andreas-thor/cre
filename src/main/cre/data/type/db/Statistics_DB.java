package main.cre.data.type.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.cre.data.type.abs.Statistics;

public class Statistics_DB implements Statistics {

	private Connection dbCon;
	
	public Statistics_DB(Connection dbCon) {
		this.dbCon = dbCon;
	}
	
	
	private long[] executeSelect (String sql) {
		
		try {
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			int colCount = rs.getMetaData().getColumnCount();
			long[] res = new long[colCount];
			for (int i=1; i<=colCount; i++) {
				res[i-1] = rs.getLong(i);
			}
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return new long[] { -2, -2 };
		}
	}
	
	
	@Override
	public long getNumberOfCRs() {
		return executeSelect("SELECT COUNT(*) FROM CR")[0];
	}

	@Override
	public long getNumberOfPubs() {
		return getNumberOfPubs(false);
	}

	@Override
	public long getNumberOfPubs(boolean includePubsWithoutCRs) {
		String sql = includePubsWithoutCRs ? "SELECT COUNT(*) FROM PUB" : "SELECT COUNT (DISTINCT PUB_ID) FROM PUB_CR";
		return executeSelect(sql)[0];
	}

	@Override
	public int[] getMaxRangePY() {
		long[] res = executeSelect("SELECT MIN(PUB_PY), MAX(PUB_PY) FROM PUB WHERE PUB_ID IN (SELECT PUB_ID FROM PUB_CR)");
		return new int[] { (int)res[0], (int)res[1] };
	}

	@Override
	public int getNumberOfDistinctPY() {
		return (int) executeSelect("SELECT COUNT (DISTINCT PUB_PY) FROM PUB WHERE PUB_ID IN (SELECT PUB_ID FROM PUB_CR)")[0];
	}

	@Override
	public int[] getMaxRangeNCR() {
		long[] res = executeSelect("SELECT MIN(CR_N_CR), MAX(CR_N_CR) FROM CR");
		return new int[] { (int)res[0], (int)res[1] };
	}

	@Override
	public int[] getMaxRangeRPY() {
		long[] res = executeSelect("SELECT MIN(CR_RPY), MAX(CR_RPY) FROM CR");
		return new int[] { (int)res[0], (int)res[1] };
	}

	@Override
	public int[] getMaxRangeRPY(boolean visibleOnly) {
		long[] res = executeSelect("SELECT MIN(CR_RPY), MAX(CR_RPY) FROM CR WHERE CR_VI = 1");
		return new int[] { (int)res[0], (int)res[1] };
	}

	@Override
	public int getNumberOfDistinctRPY() {
		return (int) executeSelect("SELECT COUNT (DISTINCT CR_RPY) FROM CR")[0];
	}

	@Override
	public int getNumberOfCRsByVisibility(boolean visible) {
		return (int) executeSelect(String.format("SELECT COUNT(*) FROM CR WHERE CR_VI = %d", visible?1:0))[0];
	}

	@Override
	public long getNumberOfCRsByNCR(int[] range) {
		return executeSelect(String.format("SELECT COUNT(*) FROM CR WHERE %d <= CR_N_CR AND CR_N_CR <= %d", range[0], range[1]))[0];
	}

	@Override
	public long getNumberOfCRsByPercentYear(String comp, double threshold) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public long getNumberOfCRsByRPY(int[] range) {
		return executeSelect(String.format("SELECT COUNT(*) FROM CR WHERE NOT(CR_RPY IS NULL) AND %d <= CR_RPY AND CR_RPY <= %d", range[0], range[1]))[0];
	}

	@Override
	public long getNumberOfPubsByCitingYear(int[] range) {
		return executeSelect(String.format("SELECT COUNT(*) FROM PUB WHERE NOT(PUB_PY IS NULL) AND %d <= PUB_PY AND PUB_PY <= %d AND PUB_ID IN (SELECT PUB_ID FROM PUB_CR)", range[0], range[1]))[0];
	}

	@Override
	public int getNumberOfCRsWithoutRPY() {
		return (int) executeSelect("SELECT COUNT(*) FROM CR WHERE CR_RPY IS NULL")[0];
	}
	
	
}

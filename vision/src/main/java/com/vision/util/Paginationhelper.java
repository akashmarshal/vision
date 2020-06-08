/**
 * 
 */
package com.vision.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kiran-kumar.karra
 *
 */
public class Paginationhelper<E> {

	public static Logger logger = LoggerFactory.getLogger(Paginationhelper.class);

	// Record total
	private long totalRows;
	
	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	public List<E> fetchPage(final JdbcTemplate jt, final String sqlFetchRows, final Object args[], final int startIndex,
			final int lastIndex, final RowMapper rowMapper) {
		List<E> result = null;
		// Total number of records
		StringBuffer totalSQL = new StringBuffer("SELECT count (1) FROM (");
		totalSQL.append(sqlFetchRows);
		totalSQL.append(") totalTable");
		if (args == null) {
			setTotalRows(jt.queryForObject(totalSQL.toString(), Integer.class));
		} else {
			setTotalRows(jt.queryForObject(totalSQL.toString(), args, Integer.class));
		}
		if (getTotalRows() <= 0)
			return new ArrayList<E>(0);
		// Oracle database structure paging statement
		StringBuffer paginationSQL = new StringBuffer("SELECT * FROM (");
		paginationSQL.append("SELECT temp.*, ROWNUM num FROM (");
		paginationSQL.append(sqlFetchRows);
		int lastInd =lastIndex+1;
		paginationSQL.append(") temp where ROWNUM <= " + lastInd);
		paginationSQL.append(") WHERE num>" + startIndex);
		if (args == null) {
			result = jt.query(paginationSQL.toString(), rowMapper);
		} else {
			result = jt.query(paginationSQL.toString(), args, rowMapper);
		}
		return result;
	}

	public List<E> fetchPage(final JdbcTemplate jt, final String sqlFetchRows, final Object args[], final int startIndex,
			final int lastIndex, final Long totalRows, final RowMapper rowMapper) {

		List<E> result = null;
		// Total number of records
		setTotalRows(totalRows);

		// Oracle database structure paging statement
		StringBuffer paginationSQL = new StringBuffer("SELECT * FROM (");
		paginationSQL.append("SELECT temp.*, ROWNUM num FROM (");
		paginationSQL.append(sqlFetchRows);
		int lastInd =lastIndex+1;
		paginationSQL.append(") temp where ROWNUM <= " + lastInd);
		paginationSQL.append(") WHERE num > " + startIndex);
		long currentTime = System.currentTimeMillis();
		if (args == null) {
			result = jt.query(paginationSQL.toString(), rowMapper);
		} else {
			result = jt.query(paginationSQL.toString(), args, rowMapper);
		}
		// logger.info("Time taken to execute query ["+paginationSQL.toString()+"] is "+
		// (System.currentTimeMillis()-currentTime)+" ms." );
		return result;
	}

}
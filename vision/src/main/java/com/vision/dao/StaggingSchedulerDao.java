package com.vision.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vision.util.ValidationUtil;

@Component
public class StaggingSchedulerDao extends AbstractCommonDao {

	public void doBulkDelete() {
		List<String> tableNameList = getJdbcTemplate().queryForList(
				"SELECT TABLE_NAME FROM VWC_STAGGING_TABLE_LOGGING WHERE  DATE_LAST_MODIFIED  < (SYSDATE -  (15/(24*60)))",
				String.class);
		if(!ValidationUtil.isValidList(tableNameList)) {
          return;
		}
		String deleteQuery = formDeleteQuery(tableNameList);
		getJdbcTemplate().execute(deleteQuery);
		for (String tableName : tableNameList) {
			try {
				getJdbcTemplate().execute("DROP TABLE " + tableName + " PURGE");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/*FORMING STAGGING DELETE QUERY CONDITIONS*/
	private String formDeleteQuery(List<String> tableNameList) {
		StringBuffer deleteQuery = new StringBuffer("DELETE FROM VWC_STAGGING_TABLE_LOGGING WHERE  TABLE_NAME in ( ");
		tableNameList.forEach(tableName -> deleteQuery.append("'"+tableName.trim()+"',"));
		return deleteQuery.substring(0, deleteQuery.lastIndexOf(","))+")";
	}	
}

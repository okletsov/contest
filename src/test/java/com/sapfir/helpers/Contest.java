package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;

public class Contest {

	private DatabaseOperations dbOp = new DatabaseOperations();
	private DateTimeOperations dtOp = new DateTimeOperations();

	private static final Logger Log = LogManager.getLogger(Contest.class.getName());
	private Connection conn;
	private String contestId;

	public Contest(Connection conn, String contestId) {
		this.conn = conn;
		this.contestId = contestId;
	}

	public LocalDateTime getSeasStartDate() {
		String sql = "select start_date from contest where id = '" + contestId + "';";
		String stringStartDate = dbOp.getSingleValue(conn, "start_date", sql);
		return dtOp.convertToDateTimeFromString(stringStartDate);
	}

	public LocalDateTime getSeasEndDate() {
		String sql = "select end_date from contest where id = '" + contestId + "';";
		String stringEndDate = dbOp.getSingleValue(conn, "end_date", sql);
		return dtOp.convertToDateTimeFromString(stringEndDate);
	}

	public LocalDateTime getMonStartDate(int monthIndex){
		String sql = "select c2.start_date" +
				"from contest c1" +
				"join contest c2 on c1.year = c2.year" +
				"and c1.season = c2.season" +
				"where c1.id = '" + contestId + "'" +
				"and c2.month = " + monthIndex + "; ";

		String stringStartDate = dbOp.getSingleValue(conn, "start_date", sql);
		return dtOp.convertToDateTimeFromString(stringStartDate);
	}

	public LocalDateTime getMonEndDate(int monthIndex){
		String sql = "select c2.end_date" +
				"from contest c1" +
				"join contest c2 on c1.year = c2.year" +
				"and c1.season = c2.season" +
				"where c1.id = '" + contestId + "'" +
				"and c2.month = " + monthIndex + "; ";

		String stringStartDate = dbOp.getSingleValue(conn, "end_date", sql);
		return dtOp.convertToDateTimeFromString(stringStartDate);
	}

	/*Add following methods:
		getSeasEndDate24
		getMonEndDate23
	*/

}

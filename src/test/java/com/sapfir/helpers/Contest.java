package com.sapfir.helpers;

import java.sql.Connection;
import java.time.LocalDateTime;

public class Contest {

	private final DatabaseOperations dbOp = new DatabaseOperations();
	private final DateTimeOperations dtOp = new DateTimeOperations();

	private final Connection conn;
	private final String contestId;

	public Contest(Connection conn, String contestId) {
		this.conn = conn;
		this.contestId = contestId;
	}

	public String getMonContestId(int month) {
		DatabaseOperations dbOp = new DatabaseOperations();

		String sql = "select c1.id \n" +
				"from contest c1\n" +
				"	join contest c2 \n" +
				"		on c1.year = c2.year \n" +
				"		and c1.season = c2.season\n" +
				"where c1.type = 'monthly' \n" +
				"	and c2.type = 'seasonal'\n" +
				"	and c2.id = '" + contestId + "'\n" +
				"	and c1.month = " + month + ";";

		return dbOp.getSingleValue(conn, "id", sql);
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

	public LocalDateTime getSeasEndDate24() {
		LocalDateTime seasEndDate = getSeasEndDate();
		return seasEndDate.plusHours(24);
	}

	public LocalDateTime getSeasLastDayStart() {
		LocalDateTime seasEndDate = getSeasEndDate();
		return seasEndDate.minusHours(24);
	}

	public LocalDateTime getMonStartDate(int monthIndex){
		String sql = "select c2.start_date " +
				"from contest c1 " +
				"join contest c2 on c1.year = c2.year " +
				"and c1.season = c2.season " +
				"where c1.id = '" + contestId + "' " +
				"and c2.month = " + monthIndex + "; ";

		String stringStartDate = dbOp.getSingleValue(conn, "start_date", sql);
		return dtOp.convertToDateTimeFromString(stringStartDate);
	}

	public LocalDateTime getMonEndDate(int monthIndex){
		String sql = "select c2.end_date " +
				"from contest c1 " +
				"join contest c2 on c1.year = c2.year " +
				"and c1.season = c2.season " +
				"where c1.id = '" + contestId + "' " +
				"and c2.month = " + monthIndex + "; ";

		String stringStartDate = dbOp.getSingleValue(conn, "end_date", sql);
		return dtOp.convertToDateTimeFromString(stringStartDate);
	}

	public LocalDateTime getMonEndDate24(int monthIndex) {
		// This method adds 24h to month end date
		LocalDateTime monEndDate = getMonEndDate(monthIndex);
		return monEndDate.plusHours(24);
	}

	public LocalDateTime getMonLastDayStart(int monthIndex) {
		//This method subtracts 23hr 59m 59s from month end date
		LocalDateTime monEndDate = getMonEndDate(monthIndex);
		return monEndDate.minusSeconds(86399);
	}
}

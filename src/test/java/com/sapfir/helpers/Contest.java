package com.sapfir.helpers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;

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

	public String getSeasContestId() {
//		Method finds seasonal contest id if monthly contest id was provided

		String sql = "select \n" +
				"\tc2.id \n" +
				"from contest c \n" +
				"\tjoin contest c2 on \n" +
				"\t\tc.season = c2.season\n" +
				"\t\tand c.`year` = c2.`year` \n" +
				"where 1=1\n" +
				"\tand c.id = '" + contestId + "'\n" +
				"\tand c2.`type` = 'seasonal';";

		return dbOp.getSingleValue(conn, "id", sql);
	}

	public String getContestType() {
		String sql = "select type from contest where id = '" + contestId + "';";
		return dbOp.getSingleValue(conn, "type", sql);
	}

	public BigDecimal getEntranceFee() {
		String sql = "select entrance_fee from contest where id = '" + contestId + "';";
		return new BigDecimal(dbOp.getSingleValue(conn, "entrance_fee", sql));
	}

	public int getParticipantsCount(String contestId) {

		Contest c = new Contest(conn, contestId);
		String contestType = c.getContestType();

		String sql;

		if(contestType.equals("seasonal")) {

			sql = "select \n" +
					"\tcount(uscp.id) as participants_count\n" +
					"from contest c \n" +
					"\tjoin user_seasonal_contest_participation uscp on uscp.contest_id = c.id \n" +
					"\tjoin user_nickname un on un.user_id = uscp.user_id \n" +
					"where 1=1 \n" +
					"\tand c.id = '" + contestId + "'\n" +
					"\tand un.is_active = 1;";
		} else {
			sql = "select \n" +
					"\tcount(t1.user_id) as participants_count\n" +
					"from (\n" +
					"\tselect \n" +
					"\t\tcount(p.id) as predictions\n" +
					"\t\t, p.user_id \n" +
					"\tfrom prediction p\n" +
					"\t\tjoin validity_statuses vs on vs.status = p.monthly_validity_status \n" +
					"\twhere 1=1\n" +
					"\t\tand monthly_contest_id = '" + contestId + "'\n" +
					"\t\tand vs.count_in_contest \n" +
					"\tgroup by \n" +
					"\t\tp.user_id \n" +
					"\t) t1\n" +
					"where 1=1\n" +
					"\tand predictions >= 30\n" +
					";";
		}

		return Integer.parseInt(dbOp.getSingleValue(conn, "participants_count", sql));
	}

	public LocalDateTime getStartDate() {
		String sql = "select start_date from contest where id = '" + contestId + "';";
		String stringStartDate = dbOp.getSingleValue(conn, "start_date", sql);
		return dtOp.convertToDateTimeFromString(stringStartDate);
	}

	public LocalDateTime getEndDate() {
		String sql = "select end_date from contest where id = '" + contestId + "';";
		String stringEndDate = dbOp.getSingleValue(conn, "end_date", sql);
		return dtOp.convertToDateTimeFromString(stringEndDate);
	}

	public LocalDateTime getStartOfLastDay() {
		return getEndDate().minusHours(24);
	}

	public LocalDateTime getEndDatePlus24hrs() {
		return getEndDate().plusHours(24);
	}

	public ArrayList<String> getPredictionsToValidate() {
		String sql = "select \n" +
				"\tt2.id\n" +
				"from (\n" +
				"\tselect \n" +
				"\t\tt1.id\n" +
				"\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
				"\t\t, t1.date_predicted\n" +
				"\tfrom (\n" +
				"\t\tselect \n" +
				"\t\t\tp.id \n" +
				"\t\t\t, p.date_scheduled\n" +
				"\t\t\t, p.date_predicted \n" +
				"\t\tfrom prediction p\n" +
				"\t\twhere 1=1\n" +
				"\t\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
				"\t\t\n" +
				"\t\tunion all\n" +
				"\t\t\n" +
				"\t\tselect \n" +
				"\t\t\tpsc.prediction_id\n" +
				"\t\t\t, psc.previous_date_scheduled\n" +
				"\t\t\t, p2.date_predicted \n" +
				"\t\tfrom prediction_schedule_changes psc\n" +
				"\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
				"\t\twhere 1=1 \n" +
				"\t\t\tand p2.seasonal_contest_id = '" + contestId + "'\n" +
				"\t\t) t1\n" +
				"\twhere 1=1\n" +
				"\tgroup by \n" +
				"\t\tt1.id\n" +
				"\t\t, t1.date_predicted\n" +
				"\t) t2\n" +
				"where 1=1\n" +
				"order by \n" +
				"\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
				"\t, t2.initial_date_scheduled asc\n" +
				"\t, t2.date_predicted asc;";
		return dbOp.getArray(conn, "id", sql);
	}
}
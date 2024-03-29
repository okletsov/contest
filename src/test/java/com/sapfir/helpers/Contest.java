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

	public Contest(Connection conn) {
		this.conn = conn;
		this.contestId = null;
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

	public String getMonContestIdByPredictionId(String predictionId) {

		String sql = "SELECT\n" +
				"\tc.id\n" +
				"from\n" +
				"\tcontest c\n" +
				"where 1 = 1\n" +
				"\tand c.`type` = 'monthly'\n" +
				"\tand c.start_date < (\n" +
				"\tselect\n" +
				"\t\tmin(t1.date_scheduled) as initial_date_scheduled\n" +
				"\tfrom\n" +
				"\t\t(\n" +
				"\t\tselect\n" +
				"\t\t\tp.id\n" +
				"\t\t\t, p.date_scheduled\n" +
				"\t\t\t, p.date_predicted\n" +
				"\t\tfrom\n" +
				"\t\t\tprediction p\n" +
				"\t\twhere 1 = 1\n" +
				"\t\t\tand p.id = '" + predictionId + "'\n" +
				"\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
				"\t\tselect\n" +
				"\t\t\tpsc.prediction_id\n" +
				"\t\t\t, psc.previous_date_scheduled\n" +
				"\t\t\t, p2.date_predicted\n" +
				"\t\tfrom\n" +
				"\t\t\tprediction_schedule_changes psc\n" +
				"\t\tjoin prediction p2 on\n" +
				"\t\t\tpsc.prediction_id = p2.id\n" +
				"\t\twhere 1 = 1\n" +
				"\t\t\tand psc.prediction_id = '" + predictionId + "' ) t1\n" +
				"\twhere 1 = 1\n" +
				"\tgroup by\n" +
				"\t\tt1.id )\n" +
				"\tand c.end_date > (\n" +
				"\tselect\n" +
				"\t\tmin(t1.date_scheduled) as initial_date_scheduled\n" +
				"\tfrom\n" +
				"\t\t(\n" +
				"\t\tselect\n" +
				"\t\t\tp.id\n" +
				"\t\t\t, p.date_scheduled\n" +
				"\t\t\t, p.date_predicted\n" +
				"\t\tfrom\n" +
				"\t\t\tprediction p\n" +
				"\t\twhere 1 = 1\n" +
				"\t\t\tand p.id = '" + predictionId + "'\n" +
				"\tunion all\n" +
				"\t\t-- to combine date_scheduled and previous_date_scheduled\n" +
				"\t\tselect\n" +
				"\t\t\tpsc.prediction_id\n" +
				"\t\t\t, psc.previous_date_scheduled\n" +
				"\t\t\t, p2.date_predicted\n" +
				"\t\tfrom\n" +
				"\t\t\tprediction_schedule_changes psc\n" +
				"\t\tjoin prediction p2 on\n" +
				"\t\t\tpsc.prediction_id = p2.id\n" +
				"\t\twhere 1 = 1\n" +
				"\t\t\tand psc.prediction_id = '" + predictionId + "' ) t1\n" +
				"\twhere 1 = 1\n" +
				"\tgroup by t1.id );";

		DatabaseOperations dbOp = new DatabaseOperations();
		return dbOp.getSingleValue(conn, "id", sql);
	}

	public String getSeasContestIdByMonContestId() {
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

	public String getAnnContestIdByYear(int year) {

		String sql = "select \n" +
				"\tc.id \n" +
				"from contest c \n" +
				"where 1=1\n" +
				"\tand c.year = " + year + "\n" +
				"    and c.type = 'annual';";

		return dbOp.getSingleValue(conn, "id", sql);
	}

	public String getSeasContestIdByYearAndSeason(int year, String season) {

		String sql = "select \n" +
				"\tc.id \n" +
				"from contest c \n" +
				"where 1=1\n" +
				"\tand c.type = 'seasonal'\n" +
				"\tand c.year = " + year + "\n" +
				"    and c.season = '" + season + "';";

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

		String sql = null;

		if(contestType.equals("seasonal")) {

			sql = "select \n" +
					"\tcount(uscp.id) as participants_count\n" +
					"from contest c \n" +
					"\tjoin user_seasonal_contest_participation uscp on uscp.contest_id = c.id \n" +
					"\tjoin user_nickname un on un.user_id = uscp.user_id \n" +
					"where 1=1 \n" +
					"\tand c.id = '" + contestId + "'\n" +
					"\tand un.is_active = 1;";
		} else if (contestType.equals("monthly")) {
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
		String sql = "SELECT \n" +
				"\tp.id \n" +
				"\t, p.seasonal_validity_status as status\n" +
				"\t, p.competitors \n" +
				"\t, ids.initial_date_scheduled\n" +
				"\t, ids.date_predicted \n" +
				"\t, p.date_updated\n" +
				"\t, p.date_validated\n" +
				"\t, p.result\n" +
				"from prediction p \n" +
				"\tjoin initial_date_scheduled ids on ids.id = p.id \n" +
				"where 1=1\n" +
				"\tand p.seasonal_contest_id = '" + contestId + "'\n" +
				"\tand (ids.initial_date_scheduled is null \n" +
				"\t\tor ids.initial_date_scheduled >=\n" +
				"\t\t\t(\n" +
				"\t\t\t\tSELECT \n" +
				"\t\t\t\t\tmin(ids2.initial_date_scheduled)\n" +
				"\t\t\t\tfrom prediction p2 \n" +
				"\t\t\t\t\tjoin initial_date_scheduled ids2 on ids2.id = p2.id\n" +
				"\t\t\t\twhere 1=1\n" +
				"\t\t\t\t\tand p2.seasonal_contest_id = '" + contestId + "'\n" +
				"\t\t\t\t\tand (\n" +
				"\t\t\t\t\t\tp2.date_validated is null\n" +
				"\t\t\t\t\t\tor p2.date_updated >= p2.date_validated\n" +
				"\t\t\t\t\t)\n" +
				"\t\t\t)\n" +
				"\t\t)\n" +
				"order by \n" +
				"\tcase when ids.initial_date_scheduled is null then 1 else 0 end\n" +
				"\t, ids.initial_date_scheduled asc\n" +
				"\t, ids.date_predicted asc;";
		return dbOp.getArray(conn, "id", sql);
	}

	public ArrayList<String> getSeasIdsForAnnContest() {

		String sql = "select \n" +
				"\taxsc.seasonal_contest_id \n" +
				"from annual_x_seasonal_contest axsc \n" +
				"where 1=1\n" +
				"\tand axsc.annual_contest_id = '" + contestId + "';";

		return dbOp.getArray(conn, "seasonal_contest_id", sql);

	}
}

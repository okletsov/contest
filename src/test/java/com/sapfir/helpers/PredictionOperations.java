package com.sapfir.helpers;

import com.sapfir.apiUtils.ApiHelpers;
import com.sapfir.apiUtils.PredictionParser;
import com.sapfir.pageClasses.PredictionsInspection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class PredictionOperations {

    private final Connection conn;
//    private WebDriver driver;
    private PredictionParser parser;
    private String predictionID;

    private static final Logger Log = LogManager.getLogger(PredictionOperations.class.getName());

    /*
    public PredictionOperations(WebDriver driver, Connection conn) {
        this.conn = conn;
        this.driver = driver;
    }
     */

    public PredictionOperations(Connection conn) {
        this.conn = conn;
    }

    public PredictionOperations(Connection conn, ApiHelpers apiHelpers, String json, String predictionIdJson) {
        this.conn = conn;
        this.parser = new PredictionParser(json, predictionIdJson, apiHelpers);
        this.predictionID = predictionIdJson;
    }

    public String getDbPredictionResult(String predictionID) {
        Log.debug("Getting result written to database for prediction id " + predictionID + "...");
        String sql = "select result from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        String dbPredictionResult =  dbOp.getSingleValue(conn, "result", sql);
        Log.debug("Result = " + dbPredictionResult);
        return  dbPredictionResult;
    }

    public String getDbDateScheduled(String predictionID) {
        String sql = "SELECT DATE_FORMAT(date_scheduled,'%Y-%m-%d %H:%i:%s') as date_scheduled from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "date_scheduled", sql);
    }

    public String getDbDatePredicted(String predictionID) {
        String sql = "SELECT DATE_FORMAT(date_predicted,'%Y-%m-%d %H:%i:%s') as date_predicted from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "date_predicted", sql);
    }

    public String getDbInitialDateScheduled(String predictionId) {
        String sql = "select \n" +
                "\tmin(DATE_FORMAT(t1.date_scheduled,'%Y-%m-%d %H:%i:%s')) as initial_date_scheduled\n" +
                "from (\n" +
                "\tselect \n" +
                "\t\tp.id \n" +
                "\t\t, p.date_scheduled\n" +
                "\t\t, p.date_predicted \n" +
                "\tfrom prediction p\n" +
                "\twhere 1=1\n" +
                "\t\tand p.id = '" + predictionId + "'\n" +
                "\t\n" +
                "\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\n" +
                "\tselect \n" +
                "\t\tpsc.prediction_id\n" +
                "\t\t, psc.previous_date_scheduled\n" +
                "\t\t, p2.date_predicted \n" +
                "\tfrom prediction_schedule_changes psc\n" +
                "\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\twhere 1=1 \n" +
                "\t\tand psc.prediction_id = '" + predictionId + "'\n" +
                "\t) t1\n" +
                "where 1=1\n" +
                "group by t1.id;";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "initial_date_scheduled", sql);
    }

    public String getDbUserId(String predictionId) {
        String sql = "select user_id from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "user_id", sql);
    }

    public String getDbSeasContestId(String predictionId) {
        String sql = "select seasonal_contest_id from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "seasonal_contest_id", sql);
    }

    public String getDbMarket(String predictionId) {
        String sql = "select market from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "market", sql);
    }

    public String getDbMainScore(String predictionID) {
        String sql = "select main_score from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "main_score", sql);
    }

    public String getDbEventIdentifier(String predictionID) {
        String sql = "select event_identifier from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "event_identifier", sql);
    }

    public String getDbUserPickName(String predictionId) {
        String sql = "select user_pick_name from prediction p where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "user_pick_name", sql);
    }

    public String getDbCompetitors(String predictionId) {
        String sql = "select competitors from prediction p where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "competitors", sql);
    }

    public int getPredictionIndexOnGivenDayByUser(String predictionId) {
        String contestId = getDbSeasContestId(predictionId);
        String userId = getDbUserId(predictionId);
        String initialDateScheduled = getDbInitialDateScheduled(predictionId);

        String sql = "select \n" +
                "\tt3.row_num\n" +
                "from (\n" +
                "\tselect\n" +
                "\t\trow_number() over (\n" +
                "\t\t\torder by \n" +
                "\t\t\t\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t\t\t, date_predicted asc\n" +
                "\t\t\t\t, t2.id asc\n" +
                "\t\t) row_num\n" +
                "\t\t, t2.id\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tp.id \n" +
                "\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t, p.date_predicted \n" +
                "\t\t\tfrom prediction p\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p.seasonal_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tor p.id = '" + predictionId + "'\n" +
                "\t\t\t\n" +
                "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t, p2.date_predicted \n" +
                "\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\twhere 1=1 \n" +
                "\t\t\t\tand p2.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p2.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p2.seasonal_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs2.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs2\n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs2.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tor p2.id = '" + predictionId + "'\n" +
                "\t\t\t) t1\n" +
                "\t\twhere 1=1\n" +
                "\t\tgroup by \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\t) t2\n" +
                "\twhere 1=1\n" +
                "\t\tand date(date(convert_tz(t2.initial_date_scheduled, 'UTC', 'Europe/Kiev'))) = date(date(convert_tz('" + initialDateScheduled + "', 'UTC', 'Europe/Kiev')))\n" +
                "\t) t3\n" +
                "where t3.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getPredictionIndexInContest(String predictionId, String contestId) {

        Contest contest = new Contest(conn, contestId);
        String contestType = contest.getContestType();

        String userId = getDbUserId(predictionId);

        String sql = "select \n" +
                "\tt3.row_num\n" +
                "from (\t\n" +
                "\tselect \n" +
                "\t\trow_number() over (\n" +
                "\t\t\t\t\torder by \n" +
                "\t\t\t\t\t\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t                    , date_predicted asc\n" +
                "\t                    , t2.id asc\n" +
                "\t\t\t\t\t) row_num\n" +
                "\t\t, t2.id\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tp.id \n" +
                "\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t, p.date_predicted \n" +
                "\t\t\tfrom prediction p\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand p." + contestType + "_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p." + contestType + "_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tor p.id = '" + predictionId + "'\n" +
                "\t\t\t\n" +
                "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t, p2.date_predicted \n" +
                "\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\twhere 1=1 \n" +
                "\t\t\t\tand p2." + contestType + "_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p2.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p2." + contestType + "_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs2.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs2 \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs2.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tor p2.id = '" + predictionId + "'\n" +
                "\t\t\t) t1\n" +
                "\t\twhere 1=1\n" +
                "\t\tgroup by \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\t) t2\n" +
                "\t) t3\n" +
                "where t3.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getPredictionIndexInSeasContest(String predictionId) {

        String contestId = getDbSeasContestId(predictionId);
        String userId = getDbUserId(predictionId);

        String sql = "select \n" +
                "\tt3.row_num\n" +
                "from (\t\n" +
                "\tselect \n" +
                "\t\trow_number() over (\n" +
                "\t\t\t\t\torder by \n" +
                "\t\t\t\t\t\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t                    , date_predicted asc\n" +
                "\t                    , t2.id asc\n" +
                "\t\t\t\t\t) row_num\n" +
                "\t\t, t2.id\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tp.id \n" +
                "\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t, p.date_predicted \n" +
                "\t\t\tfrom prediction p\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p.seasonal_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tor p.id = '" + predictionId + "'\n" +
                "\t\t\t\n" +
                "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t, p2.date_predicted \n" +
                "\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\twhere 1=1 \n" +
                "\t\t\t\tand p2.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p2.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p2.seasonal_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs2.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs2 \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs2.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tor p2.id = '" + predictionId + "'\n" +
                "\t\t\t) t1\n" +
                "\t\twhere 1=1\n" +
                "\t\tgroup by \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\t) t2\n" +
                "\t) t3\n" +
                "where t3.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getPredictionIndexWithOddsBetween10And15InMonth(String predictionId) {

        String contestId = getDbSeasContestId(predictionId);
        String userId = getDbUserId(predictionId);
        String initialDateScheduled = getDbInitialDateScheduled(predictionId);

        String sql = "select \n" +
                "\tt3.row_num\n" +
                "from (\n" +
                "\tselect\n" +
                "\t\trow_number() over (\n" +
                "\t\t\torder by \n" +
                "\t\t\t\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t\t\t, date_predicted asc\n" +
                "\t\t\t\t, t2.id asc\n" +
                "\t\t) row_num\n" +
                "\t\t, t2.id\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tp.id \n" +
                "\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t, p.date_predicted \n" +
                "\t\t\tfrom prediction p\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p.seasonal_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tand p.user_pick_value > 10\n" +
                "\t\t\t\tand p.user_pick_value <= 15\n" +
                "\t\t\t\tor p.id = '" + predictionId + "'\n" +
                "\t\t\t\n" +
                "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t, p2.date_predicted \n" +
                "\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\twhere 1=1 \n" +
                "\t\t\t\tand p2.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p2.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p2.seasonal_validity_status in (\n" +
                "\t\t\t\t\tselect\n" +
                "\t\t\t\t\t\tvs2.status\n" +
                "\t\t\t\t\tfrom validity_statuses vs2 \n" +
                "\t\t\t\t\twhere 1=1\n" +
                "\t\t\t\t\t\tand vs2.count_in_contest = 1\n" +
                "\t\t\t\t\t)\n" +
                "\t\t\t\tand p2.user_pick_value > 10\n" +
                "\t\t\t\tand p2.user_pick_value <= 15\n" +
                "\t\t\t\tor p2.id = '" + predictionId + "'\n" +
                "\t\t\t) t1\n" +
                "\t\twhere 1=1\n" +
                "\t\tgroup by \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\t) t2\n" +
                "\twhere 1=1\n" +
                "\t\tand month(date(convert_tz(t2.initial_date_scheduled, 'UTC', 'Europe/Kiev'))) = month(date(convert_tz('" + initialDateScheduled + "', 'UTC', 'Europe/Kiev')))\n" +
                "\t) t3\n" +
                "where t3.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getPredictionIndexPerEventPerUser(String predictionId) {
        String contestId = getDbSeasContestId(predictionId);
        String userId = getDbUserId(predictionId);
        String eventIdentifier = getDbEventIdentifier(predictionId);

        String sql = "select \n" +
                "\tt1.row_num\n" +
                "from (\n" +
                "\tselect\n" +
                "\t\trow_number() over(order by p.date_predicted asc) row_num\n" +
                "\t\t, p.id\n" +
                "\tfrom prediction p \n" +
                "\twhere 1=1\n" +
                "\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\tand p.user_id = '" + userId + "'\n" +
                "\t\tand event_identifier = '" + eventIdentifier + "'\n" +
                "\t\tand p.seasonal_validity_status in (\n" +
                "\t\t\tselect\n" +
                "\t\t\t\tvs.status\n" +
                "\t\t\tfrom validity_statuses vs \n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand vs.count_in_contest = 1\n" +
                "\t\t\t)\n" +
                "\t\tor p.id = '" + predictionId + "'\n" +
                "\t) t1\n" +
                "where 1=1\n" +
                "\tand t1.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getPredictionIndexPerEventMarketUserPickNameCompetitors (String predictionId) {
        String eventIdentifier = getDbEventIdentifier(predictionId);
        String market = getDbMarket(predictionId);
        String userPickName = getDbUserPickName(predictionId);
        String competitors = getDbCompetitors(predictionId).replace("'", "''");

        String sql = "select \n" +
                "\tt1.row_num \n" +
                "from (\n" +
                "\tselect \n" +
                "\t\trow_number() over(order by date_predicted asc) row_num\n" +
                "\t\t, id\n" +
                "\tfrom prediction p \n" +
                "\t\tleft join validity_statuses vs on vs.status = p.seasonal_validity_status \n" +
                "\twhere 1=1\n" +
                "\t\tand vs.count_in_contest = 1\n" +
                "\t\tand p.event_identifier = '" + eventIdentifier + "'\n" +
                "\t\tand p.market = '" + market + "'\n" +
                "\t\tand p.user_pick_name = '" + userPickName + "'\n" +
                "\t\tand p.competitors = '" + competitors + "'\n" +
                "\t\tor p.id = '" + predictionId + "'\n" +
                "\t) t1\n" +
                "where t1.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getIndexOfDuplPrediction (String predictionId) {
        String contestId = getDbSeasContestId(predictionId);
        String userId = getDbUserId(predictionId);

        String sql = "select \n" +
                "\tt3.row_num\n" +
                "from (\n" +
                "\tselect \n" +
                "\t\trow_number() over (\n" +
                "\t\t\t\t\t\torder by \n" +
                "\t\t\t\t\t\t\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t                    , date_predicted asc\n" +
                "\t\t                    , t2.id asc\n" +
                "\t\t\t\t\t\t) row_num\n" +
                "\t\t, t2.id\n" +
                "\t\t, t2.initial_date_scheduled\n" +
                "\t\t, t2.date_predicted\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tp.id \n" +
                "\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t, p.date_predicted \n" +
                "\t\t\tfrom prediction p\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p.seasonal_warning_status in ('1', '2')\n" +
                "\t\t\t\tor p.id = '" + predictionId + "'\n" +
                "\t\t\t\n" +
                "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t, p2.date_predicted \n" +
                "\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\twhere 1=1 \n" +
                "\t\t\t\tand p2.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p2.user_id = '" + userId + "'\n" +
                "\t\t\t\tand p2.seasonal_warning_status in ('1', '2')\n" +
                "\t\t\t\tor p2.id = '" + predictionId + "'\n" +
                "\t\t\t) t1\n" +
                "\t\twhere 1=1\n" +
                "\t\tgroup by \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\t) t2\n" +
                "\t) t3\n" +
                "where 1=1\n" +
                "\tand t3.id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "row_num", sql));
    }

    public int getRemainingPredictionsCount(String predictionId, String contestId) {
        /*
            Getting the count of remaining predictions in contest that:
                - scheduled after currently being inspected prediction AND
                - whose initial_date_scheduled belong to current contest AND
                - include predictions with unknown initial_date_scheduled only if contest is not over
                - regardless of future predictions' validity status
         */

        Contest contest = new Contest(conn, contestId);
        DateTimeOperations dtOp = new DateTimeOperations();

        String contestType = contest.getContestType();
        String endDate = dtOp.convertToStringFromDateTime(contest.getEndDate());
        String userId = getDbUserId(predictionId);
        String initialDateScheduled = getDbInitialDateScheduled(predictionId);
        String datePredicted = getDbDatePredicted(predictionId);

        String sql = "select \n" +
                "\tcount(t3.id) as count\n" +
                "from (\n" +
                "\tselect \n" +
                "\t\tt2.id\n" +
                "\t\t, t2.initial_date_scheduled\n" +
                "\t\t, t2.date_predicted\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tp.id \n" +
                "\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t, p.date_predicted \n" +
                "\t\t\tfrom prediction p\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand p." + contestType + "_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p.user_id = '" + userId + "'\n" +
                "\t\t\t\n" +
                "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t, p2.date_predicted \n" +
                "\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\twhere 1=1 \n" +
                "\t\t\t\tand p2." + contestType + "_contest_id = '" + contestId + "'\n" +
                "\t\t\t\tand p2.user_id = '" + userId + "'\n" +
                "\t\t\t) t1\n" +
                "\t\twhere 1=1\n" +
                "\t\tgroup by \n" +
                "\t\t\tt1.id\n" +
                "\t\t\t, t1.date_predicted\n" +
                "\t\t) t2\n" +
                "\twhere 1=1 \n" +
                "\t-- only including predictions scheduled after currently being inspected prediction\n" +
                "\t\tand t2.initial_date_scheduled > '" + initialDateScheduled + "' -- current prediction's initial_date_scheduled\n" +
                "\t\tand t2.initial_date_scheduled <= '" + endDate + "' -- contest end date\n" +
                "\t\tand t2.id != '" + predictionId + "' -- exclude current prediction from result set\n" +
                "\t\t-- including predictions with unknown initial_date_scheduled only if contest is not over yet\n" +
                "\t\tor (\n" +
                "\t\t\tt2.initial_date_scheduled is null\n" +
                "\t\t\tand now() <= '" + endDate + "' -- contest end date\n" +
                "\t\t\t)\n" +
                "-- including predictions with same initial_date_scheduled but greater date_predicted\n" +
                "\t\tor (\n" +
                "\t\t\tCASE \n" +
                "\t\t\t\twhen t2.initial_date_scheduled = '" + initialDateScheduled + "'\n" +
                "\t\t\t\tthen t2.date_predicted > '" + datePredicted + "'\n" +
                "\t\t\tEND\n" +
                "\t\t)" +
                "\torder by\n" +
                "\t\tcase when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t, t2.date_predicted asc\n" +
                "\t\t, t2.id asc\n" +
                "\t) t3;";

        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, "count", sql));
    }

    public int getDbValidityStatus(String predictionID, String contestId) {
        Contest contest = new Contest(conn,contestId);
        String contestType = contest.getContestType();

        String sql = "select " + contestType + "_validity_status from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return Integer.parseInt(dbOp.getSingleValue(conn, contestType + "_validity_status", sql));
    }

    public float getDbUserPickValue(String predictionId) {
        String sql = "select user_pick_value from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String stringUserPickValue = dbOp.getSingleValue(conn, "user_pick_value", sql);
        return Float.parseFloat(stringUserPickValue);
    }

    public float getDbOption2Value(String predictionId) {
        String sql = "select option2_value from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String stringOption2Value = dbOp.getSingleValue(conn, "option2_value", sql);

        if (stringOption2Value != null) {
            return Float.parseFloat(stringOption2Value);
        } else {
            return 0;
        }
    }

    public float getPayout(String predictionId) {
        String sql = "select \n" +
                "round(" +
                "if(market != 'DC', 1, 2)/(1/option1_value + 1/option2_value + if(option3_value is null, 0, 1/option3_value))" +
                ", 4) as payout\n" +
                "from prediction\n" +
                "where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String payout =  dbOp.getSingleValue(conn, "payout", sql);

        return Float.parseFloat(payout);
    }

    public ArrayList<String> getInPlayPredictionsByUsername(String username) {

        // Returns predictions with dateScheduled greater than current timestamp

        String sql = "SELECT \n" +
                "\tp.id\n" +
                "from prediction p \n" +
                "\tjoin user_nickname un on un.user_id = p.user_id \n" +
                "where 1=1\n" +
                "\tand un.is_active = 1\n" +
                "\tand un.nickname = '" + username + "'\n" +
                "\tand p.`result` = 'not-played'\n" +
                "\tand p.date_scheduled <= utc_timestamp()\n" +
                "\tand p.seasonal_contest_id = (\n" +
                "\t\tselect\n" +
                "\t\t\tc.id\n" +
                "\t\tfrom contest c \n" +
                "\t\twhere c.is_active = 1\n" +
                "\t\tand c.`type` = 'seasonal'\n" +
                "\t);";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getArray(conn, "id", sql);
    }

    private boolean resultDifferent() {
        /*
            This method compare web prediction result vs db prediction result and returns:
                true - if result different
                false - if results match
         */
        String webPredictionResult = parser.getResult();
        String dbPredictionResult = getDbPredictionResult(predictionID);

        boolean resultDifferent;
        resultDifferent = !webPredictionResult.equals(dbPredictionResult);
        return resultDifferent;
    }

    private boolean dateScheduledDifferent() {
        /*
            This method compare web prediction date scheduled vs db prediction date scheduled and returns:
                true - if result different
                false - if result match
         */
        String webDateScheduled = parser.getDateScheduled();
        String dbDateScheduled = getDbDateScheduled(predictionID);

        boolean dateScheduledDifferent = false;
        if(dbDateScheduled != null && webDateScheduled != null) {
            dateScheduledDifferent = !webDateScheduled.equals(dbDateScheduled);
        } else if (dbDateScheduled == null && webDateScheduled != null) {
            dateScheduledDifferent = true;
        } else if (dbDateScheduled != null) {
            dateScheduledDifferent = true;
        }

        return dateScheduledDifferent;
    }

    public boolean eventPostponed(String predictionId) {
        boolean wasPostponed;

        String sql = "select count(id) as count " +
                "from prediction_schedule_changes " +
                "where prediction_id = '" + predictionId + "' " +
                "group by prediction_id;";

        DatabaseOperations dbOp = new DatabaseOperations();
        String count = dbOp.getSingleValue(conn, "count", sql);

        wasPostponed = count != null;
        return wasPostponed;
    }

    public boolean isDbValidityStatusOverruled(String predictionId, String contestId) {

        Contest contest = new Contest(conn, contestId);
        String contestType = contest.getContestType();

        String sql = "select " + contestType + "_validity_status_overruled from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String stringValue = dbOp.getSingleValue(conn, contestType + "_validity_status_overruled", sql);
        return stringValue.equals("1");
    }

    public boolean isDbDateScheduledKnown(String predictionId) {
        String dateScheduled = getDbDateScheduled(predictionId);
        return dateScheduled != null;
    }

    public boolean isMonContestIdKnown(String predictionId) {
        Contest contest = new Contest(conn);

        String monContestId = contest.getMonContestIdByPredictionId(predictionId);
        return monContestId != null;
    }

    public boolean isQuarterGoal(String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);
        String market = predOp.getDbMarket(predictionId);

        if (market.startsWith("AH ") || market.startsWith("O/U " )) {
            return  market.contains(".25") || market.contains(".75");
        } else {
            return false;
        }
    }

    private void updateMainScore() {
        Log.debug("Updating main score for prediction " + predictionID + "...");
        String webMainScore = parser.getMainScore();

        String sql = "update prediction set main_score = '" + webMainScore + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.info("Updated main_score for " + predictionID + ". New main_score: " + webMainScore);
    }

    private void updateDetailedScore() {
        Log.debug("Updating detailed score for prediction " + predictionID + "...");
        String webDetailedScore = parser.getDetailedScore();

        String sql =
                "update prediction set detailed_score = '" + webDetailedScore + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.info("Updated detailed_score for " + predictionID + ". New detailed_score: " + webDetailedScore);
    }

    private void updateResult() {
        Log.debug("Updating prediction result for prediction " + predictionID + "...");

        String webPredictionResult = parser.getResult();

        String sql = "update prediction set result = '" + webPredictionResult + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.info("Updated result for " + predictionID + ". New result: " + webPredictionResult);
    }

    private void logPreviousDateScheduled(String predictionID) {
        String dbDateScheduled = getDbDateScheduled(predictionID);
        if (dbDateScheduled != null) {
            String sql =
                    "insert into prediction_schedule_changes (id, prediction_id, previous_date_scheduled) " +
                            "values (uuid(), '" + predictionID + "', '" + dbDateScheduled + "');";
            ExecuteQuery executeInsert = new ExecuteQuery(conn, sql);
            executeInsert.cleanUp();
            Log.info("Previous date logged for prediction: " + predictionID + ". Previous date: " + dbDateScheduled);
        }
    }

    private void updateDateScheduled() {
        String webDateScheduled = parser.getDateScheduled();

        String updateDateScheduled =
                "update prediction set date_scheduled = '" + webDateScheduled + "' where id = '" + predictionID + "';";
        ExecuteQuery executeUpdate = new ExecuteQuery(conn, updateDateScheduled);
        executeUpdate.cleanUp();
        Log.info("Updated date_scheduled for prediction: " + predictionID + ". New date: " + webDateScheduled);
    }

    private void updateDateUpdated() {
        DateTimeOperations dateOp = new DateTimeOperations();
        String timestamp = dateOp.getTimestamp();

        String updateDateScheduled =
                "update prediction set date_updated = '" + dateOp.getTimestamp() + "' where id = '" + predictionID + "';";
        ExecuteQuery executeUpdate = new ExecuteQuery(conn, updateDateScheduled);
        executeUpdate.cleanUp();
        Log.debug("Updated date_updated for prediction: " + predictionID + ". New date: " + timestamp);
    }

    public void updateDateValidated(String predictionId) {
        DateTimeOperations dateOp = new DateTimeOperations();
        String timestamp = dateOp.getTimestamp();

        String updateDateScheduled =
                "update prediction set date_validated = '" + dateOp.getTimestamp() + "' where id = '" + predictionId + "';";
        ExecuteQuery executeUpdate = new ExecuteQuery(conn, updateDateScheduled);
        executeUpdate.cleanUp();
        Log.debug("Updated date_validated for prediction: " + predictionId + ". New date: " + timestamp);
    }

    private void updateUnitOutcome() {
        Log.debug("Updating unit outcome for prediction " + predictionID + "...");

        BigDecimal unitOutcome = parser.getUnitOutcome();

        String sql = "update prediction set unit_outcome = '" + unitOutcome + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.info("Updated unit outcome for " + predictionID + ". New unit outcome: " + unitOutcome);
    }

    public void updateMonthlyContestId(String predictionId, String monthlyContestId) {
        Log.debug("Updating monthly_contest_id for prediction " + predictionId);

        String sql = "update prediction \n" +
                    "set monthly_contest_id = '" + monthlyContestId + "' \n" +
                    "where id = '" + predictionId + "';";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();

        Log.debug("Success. New monthly_contest_id: " + monthlyContestId);
    }

    public void addPrediction(String username) {
        Log.debug("Adding prediction to database...");

        ContestOperations cop = new ContestOperations(conn);
        String contestID = cop.getActiveSeasonalContestID();

        if (contestID != null) {
            UserOperations uo = new UserOperations(conn);
            DateTimeOperations dateOp = new DateTimeOperations();

            PreparedStatement sql = null;
            try {
                sql = conn.prepareStatement(
                        "insert into prediction \n" +
                                "(id, seasonal_contest_id, seasonal_validity_status_overruled, " +
                                "monthly_validity_status_overruled, user_id, event_identifier, sport, region, \n" +
                                "tournament_name, main_score, detailed_score, result, date_scheduled, \n" +
                                "date_predicted, competitors, market, option1_name, option1_value, \n" +
                                "option2_name, option2_value, option3_name, option3_value, user_pick_name, \n" +
                                "user_pick_value, unit_outcome, date_created, market_url, feed_url) \n" +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );
                sql.setString(1, predictionID);
                sql.setString(2, cop.getActiveSeasonalContestID());
                sql.setInt(3, 0);
                sql.setInt(4, 0);
                sql.setString(5, uo.getUserID(username));
                sql.setString(6, parser.getEventIdForDatabase());
                sql.setString(7, parser.getSport());
                sql.setString(8, parser.getRegion());
                sql.setString(9, parser.getTournamentName());
                sql.setString(10, parser.getMainScore());
                sql.setString(11, parser.getDetailedScore());
                sql.setString(12, parser.getResult());
                sql.setString(13, parser.getDateScheduled());
                sql.setString(14, parser.getDatePredicted());
                sql.setString(15, parser.getCompetitors());
                sql.setString(16, parser.getMarket());
                sql.setString(17, parser.getOptionName(1));
                sql.setBigDecimal(18, parser.getOptionValue(1));
                sql.setString(19, parser.getOptionName(2));
                sql.setBigDecimal(20, parser.getOptionValue(2));
                sql.setString(21, parser.getOptionName(3));
                sql.setBigDecimal(22, parser.getOptionValue(3));
                sql.setString(23, parser.getUserPickName());
                sql.setBigDecimal(24, parser.getUserPickValue());
                sql.setBigDecimal(25, parser.getUnitOutcome());
                sql.setString(26, dateOp.getTimestamp());
                sql.setString(27, parser.getMarketUrl());
                sql.setString(28, parser.getFeedUrl());

                sql.executeUpdate();
                sql.close();

                Log.info(username + ": inserted prediction with id: " + predictionID);
            } catch (SQLException ex) {
                Log.error("SQLException: " + ex.getMessage());
                Log.error("SQLState: " + ex.getSQLState());
                Log.error("VendorError: " + ex.getErrorCode());
                Log.trace("Stack trace: ", ex);
                Log.error("Failing sql statement: " + sql);
            }

        } else {
            Log.error("There are no active seasonal contests in database");
        }
    }

    public boolean checkIfExist(String predictionID) {
        /*
            This method determines if prediction already exist in database
         */
        Log.debug("Checking if prediction " + predictionID + " exist on database...");

        String sql = "select id from prediction where id = '" + predictionID + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String id = dbOp.getSingleValue(conn, "id", sql);

        boolean predictionExist;
        predictionExist = id != null;
        Log.debug("Result: " + predictionExist);
        return predictionExist;
    }

    public boolean predictionFinalized(String predictionID, String username) {
        /*
            Prediction is finalized when the event was completed and prediction was written to database after that.
            It means there are no other changes to prediction can happen on website
            Prediction is finalized if the value in result column is not "not-played"
         */

        Log.debug("Checking if prediction " + predictionID + " is finalized...");
        boolean predictionFinalized;
        String dbPredictionResult = getDbPredictionResult(predictionID);
        predictionFinalized = !dbPredictionResult.equals("not-played");
        Log.info(username + ": prediction " + predictionID + " finalized? - " + predictionFinalized);
        return predictionFinalized;
    }

    public void updatePrediction() {
        /*
            This method will determine and perform an update if it is needed for:
                - date_scheduled
                - result
                - main_score
                - detailed_score
                - unit_outcome
         */

        Log.debug("Updating date_scheduled for prediction " + predictionID + "...");
        if (dateScheduledDifferent()) {
            logPreviousDateScheduled(predictionID);
            updateDateScheduled();
            updateDateUpdated();
        } else { Log.debug("No update needed"); }

        Log.debug("Updating result for prediction " + predictionID + "...");
        if (resultDifferent()) {
            updateResult();
            updateMainScore();
            updateDetailedScore();
            updateUnitOutcome();
            updateDateUpdated();
        } else {Log.debug("No update needed"); }
    }

    public void updateValidityStatus(String predictionId, int status, String contestType) {
        String sql = "update prediction p\n" +
                "set " + contestType + "_validity_status = " + status + "\n" +
                "where id = '" + predictionId + "';";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
    }

    public void updateWarningStatus(String predictionId, int status, String contestType) {

        String sql = "update prediction p\n" +
                "set " + contestType + "_warning_status = " + status + "\n" +
                "where id = '" + predictionId + "';";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
    }

}

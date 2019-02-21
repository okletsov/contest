package com.sapfir.helpers;

import com.sapfir.pageClasses.PredictionsInspection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PredictionOperations {

    private static final Logger Log = LogManager.getLogger(PredictionOperations.class.getName());
    public PredictionOperations(WebDriver driver, Connection conn) {
        this.conn = conn;
        this.driver = driver;
    }
    public PredictionOperations(Connection conn) {
        this.conn = conn;
    }

    private Connection conn;
    private WebDriver driver;

    public String getDbPredictionResult(String predictionID) {
        Log.debug("Getting result written to database for prediction id " + predictionID + "...");
        String sql = "select result from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        String dbPredictionResult =  dbOp.getSingleValue(conn, "result", sql);
        Log.debug("Result = " + dbPredictionResult);
        return  dbPredictionResult;
    }

    public String getDbDateScheduled(String predictionID) {
        String sql = "select date_scheduled from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "date_scheduled", sql);
    }

    public String getDbDatePredicted(String predictionID) {
        String sql = "select date_predicted from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "date_predicted", sql);
    }

    public String getDbOriginalDateScheduled(String predictionId) {
        String sql = "select min(previous_date_scheduled) as original_date_scheduled " +
                "from prediction_schedule_changes " +
                "where prediction_id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "original_date_scheduled", sql);
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

    public String getFirstPredictionByUserForEvent(String eventIdentifier, String userId) {
        // Update sql to include validity statuses !!!

        String sql = "select id\n" +
                "from prediction\n" +
                "where date_predicted = (\n" +
                "                        select min(date_predicted)\n" +
                "                        from prediction \n" +
                "                        where event_identifier = '" + eventIdentifier + "'\n" +
                "                        and user_id = '" + userId + "'\n" +
                "                        and (validity_status is null or validity_status not in (10))\n" +
                "                        )\n" +
                ";";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "id", sql);
    }

    public int getPredictionIndexOnGivenDayByUser(String predictionId, String dateScheduled) {
        // update sql to include validity statuses !!!

        int predictionIndex = -1;
        String userId = getDbUserId(predictionId);

        String sql = "select id\n" +
                "from prediction\n" +
                "where user_id = '" + userId + "'\n" +
                "and (validity_status is null or validity_status not in (10))\n" +
                "and date(convert_tz(date_scheduled, 'UTC', 'Europe/Kiev')) = " +
                "date(convert_tz('" + dateScheduled + "', 'UTC', 'Europe/Kiev'))\n" +
                "order by date_scheduled, date_predicted;";

        DatabaseOperations dbOp = new DatabaseOperations();
        ArrayList<String> predictionOnDayByUser = dbOp.getArray(conn, "id", sql);

        for (int i = 0; i < predictionOnDayByUser.size(); i++) {
            String prediction = predictionOnDayByUser.get(i);
            if (prediction.equals(predictionId)) {
                predictionIndex = i + 1;
            }
        }
        return predictionIndex;
    }

    public int getPredictionIndexInSeasContest(String predictionId, String contestId) {
        // update sql to include validity statuses !!!
        // make sure to include status for void due to canc etc.

        int predictionIndex = -1;
        String userId = getDbUserId(predictionId);

        String sql = "select id\n" +
                "from prediction\n" +
                "where user_id = '" + userId + "'\n" +
                "and seasonal_contest_id = '" + contestId + "'\n" +
                "and (validity_status is null or validity_status not in (10))\n" +
                "order by date_scheduled, date_predicted;";

        DatabaseOperations dbOp = new DatabaseOperations();
        ArrayList<String> predictionsInContestByUser = dbOp.getArray(conn, "id", sql);

        for (int i = 0; i < predictionsInContestByUser.size(); i++) {
            String prediction = predictionsInContestByUser.get(i);
            if (prediction.equals(predictionId)) {
                predictionIndex = i + 1;
            }
        }
        return predictionIndex;
    }

    public int getDbValidityStatus(String predictionID) {
        String sql = "select validity_status from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();

        return Integer.parseInt(dbOp.getSingleValue(conn, "validity_status", sql));
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
                "(1-((1/option1_value + 1/option2_value + if(option3_value is null, 0, 1/option3_value)) - 1))" +
                ", 4) as payout\n" +
                "from prediction\n" +
                "where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String payout =  dbOp.getSingleValue(conn, "payout", sql);

        return Float.parseFloat(payout);
    }

    private boolean resultDifferent(String predictionID) {
        /*
            This method compare web prediction result vs db prediction result and returns:
                true - if result different
                false - if result match
         */
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webPredictionResult = pi.getResult(predictionID);
        String dbPredictionResult = getDbPredictionResult(predictionID);

        boolean resultDifferent;
        resultDifferent = !webPredictionResult.equals(dbPredictionResult);
        return resultDifferent;
    }

    private boolean dateScheduledDifferent(String predictionID) {
        /*
            This method compare web prediction date scheduled vs db prediction date scheduled and returns:
                true - if result different
                false - if result match
         */
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webDateScheduled = pi.getDateScheduled(predictionID);
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

    private void updateMainScore(String predictionID) {
        Log.debug("Updating main score for prediction " + predictionID + "...");
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webMainScore = pi.getMainScore(predictionID);

        String sql = "update prediction set main_score = '" + webMainScore + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.info("Updated main_score for " + predictionID + ". New main_score: " + webMainScore);
    }

    private void updateDetailedScore(String predictionID) {
        Log.debug("Updating detailed score for prediction " + predictionID + "...");
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webDetailedScore = pi.getDetailedScore(predictionID);

        String sql =
                "update prediction set detailed_score = '" + webDetailedScore + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.info("Updated detailed_score for " + predictionID + ". New detailed_score: " + webDetailedScore);
    }

    private void updateResult(String predictionID) {
        Log.debug("Updating prediction result for prediction " + predictionID + "...");

        PredictionsInspection pi = new PredictionsInspection(driver);
        String webPredictionResult = pi.getResult(predictionID);

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

    private void updateDateScheduled(String predictionID) {
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webDateScheduled = pi.getDateScheduled(predictionID);

        String updateDateScheduled =
                "update prediction set date_scheduled = '" + webDateScheduled + "' where id = '" + predictionID + "';";
        ExecuteQuery executeUpdate = new ExecuteQuery(conn, updateDateScheduled);
        executeUpdate.cleanUp();
        Log.info("Updated date_scheduled for prediction: " + predictionID + ". New date: " + webDateScheduled);
    }

    private void updateUnitOutcome(String predictionID) {
        Log.debug("Updating unit outcome for prediction " + predictionID + "...");

        PredictionsInspection pi = new PredictionsInspection(driver);
        BigDecimal unitOutcome = pi.getUnitOutcome(predictionID);

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

    public void addPrediction(String predictionID, String username) {
        Log.debug("Adding prediction to database...");

        ContestOperations cop = new ContestOperations(conn);
        String contestID = cop.getActiveSeasonalContestID();

        if (contestID != null) {
            PredictionsInspection pred = new PredictionsInspection(driver);
            UserOperations uo = new UserOperations(conn);
            DateTimeOperations dateOp = new DateTimeOperations();

            int userPick = pred.getUserPick(predictionID);
            PreparedStatement sql = null;
            try {
                sql = conn.prepareStatement(
                        "insert into prediction \n" +
                                "(id, seasonal_contest_id, user_id, event_identifier, sport, region, \n" +
                                "tournament_name, main_score, detailed_score, result, date_scheduled, \n" +
                                "date_predicted, competitors, market, option1_name, option1_value, \n" +
                                "option2_name, option2_value, option3_name, option3_value, user_pick_name, \n" +
                                "user_pick_value, unit_outcome, date_created) \n" +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );
                sql.setString(1, predictionID);
                sql.setString(2, cop.getActiveSeasonalContestID());
                sql.setString(3, uo.getUserID(username));
                sql.setString(4, pred.getEventIdentifier(predictionID));
                sql.setString(5, pred.getSport(predictionID));
                sql.setString(6, pred.getRegion(predictionID));
                sql.setString(7, pred.getTournament(predictionID));
                sql.setString(8, pred.getMainScore(predictionID));
                sql.setString(9, pred.getDetailedScore(predictionID));
                sql.setString(10, pred.getResult(predictionID));
                sql.setString(11, pred.getDateScheduled(predictionID));
                sql.setString(12, pred.getDatePredicted(predictionID));
                sql.setString(13, pred.getCompetitorsText(predictionID));
                sql.setString(14, pred.getMarket(predictionID));
                sql.setString(15, pred.getOptionName(predictionID, 1));
                sql.setBigDecimal(16, pred.getOptionValue(predictionID, 1));
                sql.setString(17, pred.getOptionName(predictionID, 2));
                sql.setBigDecimal(18, pred.getOptionValue(predictionID, 2));
                sql.setString(19, pred.getOptionName(predictionID, 3));
                sql.setBigDecimal(20, pred.getOptionValue(predictionID, 3));
                sql.setString(21, pred.getOptionName(predictionID, userPick));
                sql.setBigDecimal(22, pred.getOptionValue(predictionID, userPick));
                sql.setBigDecimal(23, pred.getUnitOutcome(predictionID));
                sql.setString(24, dateOp.getTimestamp());

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

    public boolean predictionFinalized(String predictionID) {
        /*
            Prediction is finalized when the event was completed and prediction was written to database after that.
            It means there are no other changes to prediction can happen on website
            Prediction is finalized if the value in result column is not "not-played"
         */

        Log.debug("Checking if prediction " + predictionID + " is finalized...");
        boolean predictionFinalized;
        String dbPredictionResult = getDbPredictionResult(predictionID);
        predictionFinalized = !dbPredictionResult.equals("not-played");
        Log.info("Prediction " + predictionID + " finalized? - " + predictionFinalized);
        return predictionFinalized;
    }

    public void updatePrediction(String predictionID) {
        /*
            This method will determine and perform an update if it is needed for:
                - date_scheduled
                - result
                - main_score
                - detailed_score
                - unit_outcome
         */

        Log.debug("Updating date_scheduled for prediction " + predictionID + "...");
        if (dateScheduledDifferent(predictionID)) {
            logPreviousDateScheduled(predictionID);
            updateDateScheduled(predictionID);
        } else { Log.debug("No update needed"); }

        Log.debug("Updating result for prediction " + predictionID + "...");
        if (resultDifferent(predictionID)) {
            updateResult(predictionID);
            updateMainScore(predictionID);
            updateDetailedScore(predictionID);
            updateUnitOutcome(predictionID);
        } else {Log.debug("No update needed"); }
    }

    public void setMonthContestIdToNull(String predictionId) {
        String sql = "update prediction set monthly_contest_id = null where id = '" + predictionId + "';";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
    }

    public ArrayList<String> getPredictionsToValidate(String contestId) {
        String sqlToGetPredictionsToInspect =
                "select p.id\n" +
                "from prediction p\n" +
                "join contest c on c.id = p.seasonal_contest_id\n" +
                "where c.id = '" + contestId + "'\n" +
                "order by user_id, date_scheduled, date_predicted;";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getArray(conn, "id", sqlToGetPredictionsToInspect);
    }
}

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

    public float getDbUserPickValue(String predictionId) {
        String sql = "select user_pick_value from prediction where id = '" + predictionId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        String stringUserPickValue = dbOp.getSingleValue(conn, "user_pick_value", sql);
        return Float.parseFloat(stringUserPickValue);
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

    private String getMonthlyContestIdOfPrediction(String predictionID) {
        PredictionsInspection pi = new PredictionsInspection(driver);
        String dateScheduled = pi.getDateScheduled(predictionID);

        String sql = "select \n" +
                "c1.id\n" +
                "from contest c1\n" +
                "join contest c2\n" +
                "on c1.year = c2.year\n" +
                "            and c1.season = c2.season\n" +
                "where \n" +
                "    c1.type = 'monthly'\n" +
                "    and c2.type = 'seasonal'\n" +
                "    and c2.is_active = 1\n" +
                "    and '" + dateScheduled + "' between c1.start_date and c1.end_date;";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getSingleValue(conn, "id", sql);
    }

    private void updateMonthlyContestId(String predictionId) {
        Log.debug("Updating monthly_contest_id for prediction " + predictionId);

        String monthlyContestId = getMonthlyContestIdOfPrediction(predictionId);
        if (monthlyContestId != null) {
            String sql = "update prediction \n" +
                    "set monthly_contest_id = '" + monthlyContestId + "' \n" +
                    "where id = '" + predictionId + "';";

            ExecuteQuery eq = new ExecuteQuery(conn, sql);
            eq.cleanUp();
            Log.debug("Success. New monthly_contest_id: " + monthlyContestId);
        } else {
            Log.debug("No monthly_contest_id found for prediction " + predictionId);
        }
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
                                "(id, seasonal_contest_id, monthly_contest_id, user_id, event_identifier, sport, region, \n" +
                                "tournament_name, main_score, detailed_score, result, date_scheduled, \n" +
                                "date_predicted, competitors, market, option1_name, option1_value, \n" +
                                "option2_name, option2_value, option3_name, option3_value, user_pick_name, \n" +
                                "user_pick_value, unit_outcome, date_created) \n" +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );
                sql.setString(1, predictionID);
                sql.setString(2, cop.getActiveSeasonalContestID());
                sql.setString(3, getMonthlyContestIdOfPrediction(predictionID));
                sql.setString(4, uo.getUserID(username));
                sql.setString(5, pred.getEventIdentifier(predictionID));
                sql.setString(6, pred.getSport(predictionID));
                sql.setString(7, pred.getRegion(predictionID));
                sql.setString(8, pred.getTournament(predictionID));
                sql.setString(9, pred.getMainScore(predictionID));
                sql.setString(10, pred.getDetailedScore(predictionID));
                sql.setString(11, pred.getResult(predictionID));
                sql.setString(12, pred.getDateScheduled(predictionID));
                sql.setString(13, pred.getDatePredicted(predictionID));
                sql.setString(14, pred.getCompetitorsText(predictionID));
                sql.setString(15, pred.getMarket(predictionID));
                sql.setString(16, pred.getOptionName(predictionID, 1));
                sql.setBigDecimal(17, pred.getOptionValue(predictionID, 1));
                sql.setString(18, pred.getOptionName(predictionID, 2));
                sql.setBigDecimal(19, pred.getOptionValue(predictionID, 2));
                sql.setString(20, pred.getOptionName(predictionID, 3));
                sql.setBigDecimal(21, pred.getOptionValue(predictionID, 3));
                sql.setString(22, pred.getOptionName(predictionID, userPick));
                sql.setBigDecimal(23, pred.getOptionValue(predictionID, userPick));
                sql.setBigDecimal(24, pred.getUnitOutcome(predictionID));
                sql.setString(25, dateOp.getTimestamp());

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
                - monthly_contest_id
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
            updateMonthlyContestId(predictionID);
        } else {Log.debug("No update needed"); }
    }

    public ArrayList<String> getPredictionsToValidate(String contestId) {
        String sqlToGetPredictionsToInspect =
                "select p.id" +
                "from prediction p" +
                "join contest c on c.id = p.seasonal_contest_id" +
                "where c.id = '" + contestId + "';";

        DatabaseOperations dbOp = new DatabaseOperations();
        return dbOp.getArray(conn, "id", sqlToGetPredictionsToInspect);
    }
}

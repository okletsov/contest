package com.sapfir.helpers;

import com.sapfir.pageClasses.PredictionsInspection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PredictionOperations {

    private Connection conn;
    private WebDriver driver;

    private String dbPredictionResult;

    public PredictionOperations(WebDriver driver, Connection conn) {
        this.conn = conn;
        this.driver = driver;
    }
    private static final Logger Log = LogManager.getLogger(PredictionOperations.class.getName());

    private void getDbPredictionResult(String predictionID) {
        Log.debug("Getting result written to database for prediction id " + predictionID + "...");
        String sql = "select result from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        dbPredictionResult = dbOp.getSingleValue(conn, "result", sql);
        Log.debug("Result = " + dbPredictionResult);
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
                                "user_pick_value, date_created) \n" +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
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
                sql.setString(23, dateOp.getTimestamp());

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
        getDbPredictionResult(predictionID);
        predictionFinalized = !dbPredictionResult.equals("not-played");
        Log.info("Prediction finalized? - " + predictionFinalized);
        return predictionFinalized;
    }

    public void updateResult(String predictionID) {
        /*
            This method will compare result written in database for given predictionID
            with the prediction result from website and update database if needed
         */

        Log.debug("Updating prediction result for prediction " + predictionID + "...");
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webPredictionResult = pi.getResult(predictionID);

        if (!dbPredictionResult.equals(webPredictionResult)) {
            String sql =
                    "update prediction set result = '" + webPredictionResult + "' where id = '" + predictionID + "';";
            ExecuteQuery eq = new ExecuteQuery(conn, sql);
            eq.cleanUp();
            Log.debug("Updated: " + webPredictionResult);
        } else {
            Log.debug("No update is needed");
        }
    }

    public void updateMainScore(String predictionID) {
        Log.debug("Updating main score for prediction " + predictionID + "...");
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webMainScore = pi.getMainScore(predictionID);

        String sql = "update prediction set main_score = '" + webMainScore + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.debug("Updated: " + webMainScore);
    }

    public void updateDetailedScore(String predictionID) {
        Log.debug("Updating detailed score for prediction " + predictionID + "...");
        PredictionsInspection pi = new PredictionsInspection(driver);
        String webDetailedScore = pi.getMainScore(predictionID);

        String sql =
                "update prediction set detailed_score = '" + webDetailedScore + "' where id = '" + predictionID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
        Log.debug("Updated: " + webDetailedScore);
    }

    public void updateDateScheduled(String predictionID) {
        Log.debug("Updating date_scheduled for prediction " + predictionID + "...");

        PredictionsInspection pi = new PredictionsInspection(driver);
        String webDateScheduled = pi.getDateScheduled(predictionID);

        String sql = "select date_scheduled from prediction where id = '" + predictionID + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        String dbDateScheduled = dbOp.getSingleValue(conn, "date_scheduled", sql);
    }
}

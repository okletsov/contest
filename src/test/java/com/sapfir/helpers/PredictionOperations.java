package com.sapfir.helpers;

import com.sapfir.pageClasses.PredictionsInspection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PredictionOperations {

    private Connection conn;
    private WebDriver driver;

    public PredictionOperations(WebDriver driver, Connection conn) {
        this.conn = conn;
        this.driver = driver;
    }
    private static final Logger Log = LogManager.getLogger(PredictionOperations.class.getName());

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
}

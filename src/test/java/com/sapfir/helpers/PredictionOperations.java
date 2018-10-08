package com.sapfir.helpers;

import com.sapfir.pageClasses.PredictionsInspection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;

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

            String sport = pred.getSport(predictionID);
            String region = pred.getRegion(predictionID);
            String tournament_name = pred.getTournament(predictionID);
            String result = pred.getResult(predictionID);
            String dateScheduled = pred.getDateScheduled(predictionID);
            String datePredicted = pred.getDatePredicted(predictionID);
            String competitors = pred.getCompetitorsText(predictionID);
            String market = pred.getMarket(predictionID);
            String mainScore = pred.getMainScore(predictionID);
            String detailedScore = pred.getDetailedScore(predictionID);
            String eventIdentifier = pred.getEventIdentifier(predictionID);
            String option1Name = pred.getOptionName(predictionID, 1);
            String option2Name = pred.getOptionName(predictionID, 2);
            String option3Name = pred.getOptionName(predictionID, 3);
            BigDecimal option1Value = pred.getOptionValue(predictionID, 1);
            BigDecimal option2Value = pred.getOptionValue(predictionID, 2);
            BigDecimal option3Value = pred.getOptionValue(predictionID, 3);
            int userPick = pred.getUserPick(predictionID);
            String userPickName = pred.getOptionName(predictionID, userPick);
            BigDecimal userPickValue = pred.getOptionValue(predictionID, userPick);

            UserOperations uo = new UserOperations(conn);
            String userID = uo.getUserID(username);

            DateTimeOperations dateOp = new DateTimeOperations();
            String dateCreated = dateOp.getTimestamp();

            String sql = "insert into prediction \n" +
                    "(id, seasonal_contest_id, user_id, event_identifier, sport, region, tournament_name, " +
                    "main_score, detailed_score, " +
                    "result, date_scheduled, date_predicted, competitors, market, option1_name, option1_value, " +
                    "option2_name, option2_value, option3_name, option3_value, user_pick_name, user_pick_value, " +
                    "date_created) \n" +
                    "values \n" +
                    "('" + predictionID + "', '" + contestID + "', '" + userID + "', '" + eventIdentifier +
                    "', '" + sport + "', '" + region + "', '" + tournament_name + "', '" + mainScore +
                    "', '" + detailedScore + "', '" + result + "', '" + dateScheduled + "', '" + datePredicted +
                    "', '" + competitors + "', '" + market + "', '" + option1Name + "', " + option1Value +
                    ", '" + option2Name + "', " + option2Value + ", '" + option3Name + "', " + option3Value +
                    ", '" + userPickName + "', " + userPickValue + ", '" + dateCreated + "');";

            ExecuteQuery eq = new ExecuteQuery(conn, sql);
            Log.info(eq.getRowsAffected() + " prediction with id " + predictionID + " successfully inserted into db");

        } else {
            Log.error("There are no active seasonal contests in database");
        }
    }
}

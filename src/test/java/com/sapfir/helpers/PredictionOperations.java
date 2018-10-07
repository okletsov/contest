package com.sapfir.helpers;

import com.sapfir.pageClasses.PredictionsInspection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.sql.Connection;

public class PredictionOperations {

    private Connection conn;
    private WebDriver driver;

    public PredictionOperations(WebDriver driver, Connection conn) {
        this.conn = conn;
        this.driver = driver;
    }
    private static final Logger Log = LogManager.getLogger(PredictionOperations.class.getName());

    public void addPrediction(String predictionID) {
        Log.debug("Adding prediction to database...");

        ContestOperations cop = new ContestOperations(conn);
        String contestID = cop.getActiveSeasonalContestID();

        if (contestID != null) {
            PredictionsInspection pred = new PredictionsInspection(driver);

            String sport = pred.getSport(predictionID);
            String region = pred.getRegion(predictionID);
            String tournament = pred.getTournament(predictionID);
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


        } else {
            Log.error("There are no active seasonal contests in database");
        }
    }
}

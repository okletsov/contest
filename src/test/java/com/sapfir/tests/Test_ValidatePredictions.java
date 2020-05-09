package com.sapfir.tests;

import com.sapfir.helpers.Contest;
import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.PredictionValidationTier1;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;

public class Test_ValidatePredictions {

    private static final Logger Log = LogManager.getLogger(Test_ValidatePredictions.class.getName());

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;

    @BeforeClass
    public void setUp() {
        conn = dbOp.connectToDatabase();
    }

    @AfterClass
    public void tearDown() {
        dbOp.closeConnection(conn);
    }
    
    @Test
    public void testPredictions() {
        // Get active seasonal contest id
//        ContestOperations contOp = new ContestOperations(conn);
//        String contestId = contOp.getActiveSeasonalContestID();
        String contestId = "5b6dd5a7-29ea-11ea-9120-74852a015562";

        // Get the list of predictions to validate
//        Contest contest = new Contest(conn, contestId);
//        ArrayList<String> predictionsToValidate = contest.getPredictionsToValidate();

        ArrayList<String> predictionsToValidate = new ArrayList<>();
        predictionsToValidate.add("feed_item_4022981003");

        // Individually validate each prediction
        for (String predictionId : predictionsToValidate) {

            // Have individual prediction inspected for season
            PredictionValidationTier1 t1 = new PredictionValidationTier1(conn, contestId, predictionId);
            int result = t1.getSeasStatus();
            System.out.println("Validity status for prediction " + predictionId + ": " + result);

            // Have individual prediction inspected for month 1
            // Have individual prediction inspected for month 2
        }
    }
}

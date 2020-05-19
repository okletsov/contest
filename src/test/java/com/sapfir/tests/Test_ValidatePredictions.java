package com.sapfir.tests;

import com.sapfir.helpers.*;
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
        // Step 1: Get active seasonal contest id
        Log.info("Starting to validate predictions...");
        ContestOperations contOp = new ContestOperations(conn);
        String seasContestId = contOp.getActiveSeasonalContestID();
//        String seasContestId = "3fd1fd5d-7913-11e9-a98a-74852a015562";

        Contest contest = new Contest(conn, seasContestId);
        String mon1ContestId = contest.getMonContestId(1);
        String mon2ContestId = contest.getMonContestId(2);

        // Step 2: Get the list of predictions to validate
        ArrayList<String> predictionsToValidate = contest.getPredictionsToValidate();
//        ArrayList<String> predictionsToValidate = new ArrayList<>();
//        predictionsToValidate.add("feed_item_3805148303");
//        predictionsToValidate.add("feed_item_3804796803");
//        predictionsToValidate.add("feed_item_3804797703");

        // Step 3: Individually validate each prediction
        for (String predictionId : predictionsToValidate) {

            // Step 3.1: Have individual prediction inspected for season
            PredictionValidationTier1 predValSeas = new PredictionValidationTier1(conn, seasContestId, predictionId);
            PredictionOperations predOp = new PredictionOperations(conn);
            ValidityStatuses vs = new ValidityStatuses(conn);

            int seasStatus = predValSeas.getStatus();
            if (seasStatus > 1) { Log.warn("Status for prediction " + predictionId + ": " + seasStatus + " - " + vs.getDescription(seasStatus)); }
            predOp.updateValidityStatus(predictionId, seasStatus, "seasonal");

            // Step 3.2: Have individual prediction inspected for month 1
            // Step 3.3: Have individual prediction inspected for month 2

        }

        Log.info("Predictions validation finished");
    }
}

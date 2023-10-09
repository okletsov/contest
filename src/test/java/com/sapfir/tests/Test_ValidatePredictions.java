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

        ValidityStatuses vs = new ValidityStatuses(conn);
        PredictionOperations predOp = new PredictionOperations(conn);

        // Step 1: Get active seasonal contest id
        Log.info("Starting to validate predictions...");
        ContestOperations contOp = new ContestOperations(conn);
        String seasContestId = contOp.getActiveSeasonalContestID();
//        String seasContestId = "8d570ed0-de73-11e9-92ec-74852a015562";

        Contest contest = new Contest(conn, seasContestId);
        String mon1ContestId = contest.getMonContestId(1);
        String mon2ContestId = contest.getMonContestId(2);

        // Step 2: Get the list of predictions to validate
         ArrayList<String> predictionsToValidate = contest.getPredictionsToValidate();
//        ArrayList<String> predictionsToValidate = new ArrayList<>();
//        predictionsToValidate.add("feed_item_4784094303");

        // Step 3: Individually validate each prediction
        for (String predictionId : predictionsToValidate) {

            Log.info("Validating: " + predictionId);

            // Step 3.1: Have individual prediction inspected for season
            PredictionValidation predValSeas = new PredictionValidation(conn, seasContestId, predictionId);
            int seasStatus = predValSeas.getStatus();
            if (seasStatus > 1 && seasStatus != 41) { Log.warn("Status for prediction " + predictionId + ": " + seasStatus + " - " + vs.getDescription(seasStatus)); }
            predOp.updateValidityStatus(predictionId, seasStatus, "seasonal");

            // Step 3.2: Insert monthly contest id if known
            boolean monContestIdKnown = predOp.isMonContestIdKnown(predictionId);

            if (monContestIdKnown && seasStatus != 13) {
                String monContestId = contest.getMonContestIdByPredictionId(predictionId);
                predOp.updateMonthlyContestId(predictionId, monContestId);
            }

            // Step 3.3: Have individual prediction inspected for month 1
            PredictionValidation predValMon1 = new PredictionValidation(conn, mon1ContestId, predictionId);

            int mon1Status = predValMon1.getStatus();
            if (mon1Status != 11 && mon1Status != 12) { predOp.updateValidityStatus(predictionId, mon1Status, "monthly"); }

            // Step 3.4: Have individual prediction inspected for month 2
            PredictionValidation predValMon2 = new PredictionValidation(conn, mon2ContestId, predictionId);

            int mon2Status = predValMon2.getStatus();
            if (mon2Status != 11 && mon2Status != 12) { predOp.updateValidityStatus(predictionId, mon2Status, "monthly"); }
        }

//        Step 4: insert background job timestamp
        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_ValidatePredictions.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);

        Log.info("Predictions validation finished");
    }
}

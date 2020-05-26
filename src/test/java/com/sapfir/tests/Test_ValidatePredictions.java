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

        // Step 3: Individually validate each prediction
        for (String predictionId : predictionsToValidate) {

            // Step 3.1: Have individual prediction inspected for season
            PredictionValidationTier1 predValSeas = new PredictionValidationTier1(conn, seasContestId, predictionId);
            int seasStatus = predValSeas.getStatus();
            if (seasStatus > 1 && seasStatus != 41) { Log.warn("Status for prediction " + predictionId + ": " + seasStatus + " - " + vs.getDescription(seasStatus)); }
            predOp.updateValidityStatus(predictionId, seasStatus, "seasonal");

            // Step 3.2: Have individual prediction inspected for month 1
            PredictionValidationTier1 predValMon1 = new PredictionValidationTier1(conn, mon1ContestId, predictionId);
            int mon1Status = predValMon1.getStatus();

            if ( // update monthly status only if prediction belongs to monthly contest
                    mon1Status != 11 &&
                    mon1Status != 12 &&
                    mon1Status != 13
            ) {
                if ( // log a warning only if prediction status is specific to monthly contest
                        mon1Status == 3 ||
                        mon1Status == 51 ||
                        mon1Status == 52 ||
                        mon1Status == 53
                ) {
                    Log.warn("Month 1 status for prediction " + predictionId + ": " + mon1Status + " - " + vs.getDescription(mon1Status));
                }
                predOp.updateValidityStatus(predictionId, mon1Status, "monthly");
            }

            // Step 3.3: Have individual prediction inspected for month 2
            PredictionValidationTier1 predValMon2 = new PredictionValidationTier1(conn, mon2ContestId, predictionId);
            int mon2Status = predValMon2.getStatus();

            if ( // update monthly status only if prediction belongs to monthly contest
                    mon2Status != 11 &&
                    mon2Status != 12 &&
                    mon2Status != 13
            ) {
                if ( // log a warning only if prediction status is specific to monthly contest
                        mon2Status == 3 ||
                        mon2Status == 51 ||
                        mon2Status == 52 ||
                        mon2Status == 53
                ) {
                    Log.warn("Month 2 status for prediction " + predictionId + ": " + mon2Status + " - " + vs.getDescription(mon2Status));
                }
                predOp.updateValidityStatus(predictionId, mon2Status, "monthly");
            }

        }

        Log.info("Predictions validation finished");
    }
}

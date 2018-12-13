package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PredictionValidation {

    private static final Logger Log = LogManager.getLogger(PredictionValidation.class.getName());
    private DatabaseOperations dbOp = new DatabaseOperations();

    public PredictionValidation(Connection conn) {
        this.conn = conn;

        String sqlToGetPredictionsToInspect =
                        "select \n" +
                        "p.id\n" +
                        "from prediction p\n" +
                        "join contest c on c.id = p.seasonal_contest_id\n" +
                        "where\n" +
                        "c.is_active = 1;";
        this.predictionsToInspect = dbOp.getArray(conn, "id", sqlToGetPredictionsToInspect);
    }

    private Connection conn;
    private ArrayList<String> predictionsToInspect;

    private boolean validateDateScheduled(String predictionId) {
        Log.debug("Validating date_scheduled for " + predictionId + "...");
        boolean dateScheduledValid = true;

        PredictionOperations predOp = new PredictionOperations(conn);
        ContestOperations contOp = new ContestOperations(conn);
        DateTimeOperations dtOp = new DateTimeOperations();

        String dateScheduled = predOp.getDbDateScheduled(predictionId);
        LocalDateTime seasonalContestEndDate = dtOp.convertToDateTimeFromString(contOp.getActiveSeasonalContestEndDate());

        if (dateScheduled == null) {
            LocalDateTime timestamp = dtOp.convertToDateTimeFromString(dtOp.getTimestamp());
            if (timestamp.isAfter(seasonalContestEndDate)) {

                // Implement updating validity status here

                dateScheduledValid = false;
                Log.warn("Prediction " + predictionId + " is not valid. Status 10");
                Log.warn("Additional info: prediction does not have date scheduled and seasonal contest is already over");
            }
        } else {
            // Implement all crazy logic here
        }
        if (dateScheduledValid) { Log.debug("Date scheduled valid"); }
        return dateScheduledValid;
    }

    public boolean validatePredictions() {
        boolean newInvalidPredictionsFound = false;

        for (String predictionId: predictionsToInspect ) {
            // add individual validation methods here
            // check if prediction validity status differs from db. This is needed to determine if it's a new invalid prediction or not
        }

        return newInvalidPredictionsFound;
    }
}

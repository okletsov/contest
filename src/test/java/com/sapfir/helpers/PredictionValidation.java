package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PredictionValidation {

    private static final Logger Log = LogManager.getLogger(PredictionValidation.class.getName());
    private Connection conn;
    private String contestId;

    public PredictionValidation(Connection conn) {
        this.conn = conn;

        ContestOperations contOp = new ContestOperations(conn);
        this.contestId = contOp.getActiveSeasonalContestID();

    }

    private boolean validateDateScheduled(String predictionId) {
        Log.debug("Validating date_scheduled for " + predictionId + "...");
        boolean dateScheduledValid = true;

        PredictionOperations predOp = new PredictionOperations(conn);
        DateTimeOperations dtOp = new DateTimeOperations();
        Contest cont = new Contest(conn, contestId);

        String dateScheduled = predOp.getDbDateScheduled(predictionId);
        LocalDateTime seasContEndDate = cont.getSeasEndDate();

        if (dateScheduled == null) {
            LocalDateTime timestamp = dtOp.convertToDateTimeFromString(dtOp.getTimestamp());
            if (timestamp.isAfter(seasContEndDate)) {

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

        PredictionOperations predOp = new PredictionOperations(conn);
        ArrayList<String> predictionsToValidate = predOp.getPredictionsToValidate(contestId);


        for (String predictionId: predictionsToValidate ) {
            // add individual validation methods here
            // check if prediction validity status differs from db. This is needed to determine if it's a new invalid prediction or not
        }

        return newInvalidPredictionsFound;
    }
}

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

    private void updateValidityStatus(String predictionId, int validityStatus) {
        String sql = "update prediction " +
                "set validity_status = " + validityStatus + " " +
                "where id = '" + predictionId + "';";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
    }

    private void validateUnknownDateScheduled(String predictionId) {
        /*
            When user places a bet on tournament winner, date_scheduled will be null until the bet is settled
            date_scheduled can be determined only after that

            This method checks if contest is already over to see if predictions without date_scheduled should be marked invalid
         */

        DateTimeOperations dtOp = new DateTimeOperations();
        LocalDateTime timestamp = dtOp.convertToDateTimeFromString(dtOp.getTimestamp());

        Contest cont = new Contest(conn, contestId);
        LocalDateTime seasContEndDate = cont.getSeasEndDate();

        if (timestamp.isAfter(seasContEndDate)) {
            updateValidityStatus(predictionId,10);
            Log.warn("Prediction " + predictionId + " is not valid. Status 10");
            Log.warn("Additional info: prediction does not have date scheduled and seasonal contest is already over");
        } else {
            Log.debug("date_scheduled unknown, but contest is not over yet");
        }

    }

    private boolean dateScheduledWithinSeasLimit(String stringDateScheduled) {
        boolean isWithinLimit;

        Contest cont = new Contest(conn, contestId);
        LocalDateTime seasStartDate = cont.getSeasStartDate();
        LocalDateTime seasEndDate = cont.getSeasEndDate();

        DateTimeOperations dtOp = new DateTimeOperations();
        LocalDateTime dateScheduled = dtOp.convertToDateTimeFromString(stringDateScheduled);

        isWithinLimit = seasEndDate.isAfter(dateScheduled) && seasStartDate.isBefore(dateScheduled);
        return isWithinLimit;
    }

    private void validateDateScheduled(String predictionId) {
        Log.debug("Validating date_scheduled for " + predictionId + "...");

        PredictionOperations predOp = new PredictionOperations(conn);
        String dateScheduled = predOp.getDbDateScheduled(predictionId);

        if (dateScheduled == null) {
            validateUnknownDateScheduled(predictionId);
        } else {
            // Implement all crazy logic here
        }

        // In the end see if there is any debug logging needed for notifying about validity (try to have that in sub-methods)
        // might need debug logging to say that date_scheduled is valid. Need to think about it because validating unknownDateScheduled
        // method already have some logging for successful validation
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

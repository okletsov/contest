package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;

public class PredictionValidationTier1 {

    LocalDateTime todayDateTime;

    // Prediction metadata:

    boolean seasValidityStatusOverruled;
    boolean wasPostponed;
    boolean dateScheduledKnown;
    int seasValidityStatus;
    int indexInSeasContest;
    LocalDateTime dateScheduled;
    LocalDateTime originalDateScheduled;

    // Contest metadata:

    LocalDateTime seasStartDate;
    LocalDateTime seasEndDate;
    LocalDateTime seasEndDate24;

    public PredictionValidationTier1(Connection conn, String contestId, String predictionId) {

        PredictionOperations predOp = new PredictionOperations(conn);
        Contest contest = new Contest(conn, contestId);
        DateTimeOperations dtOp = new DateTimeOperations();

        // Getting prediction metadata:

        this.seasValidityStatusOverruled = predOp.isDbValidityStatusOverruled(predictionId, contestId);
        if (seasValidityStatusOverruled) { this.seasValidityStatus = predOp.getDbValidityStatus(predictionId, contestId); }
        this.dateScheduledKnown = predOp.isDbDateScheduledKnown(predictionId);
        this.wasPostponed = predOp.eventPostponed(predictionId);
        if (dateScheduledKnown) { this.dateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbDateScheduled(predictionId)); }
        if (!dateScheduledKnown) { this.todayDateTime = dtOp.convertToDateTimeFromString(dtOp.getTimestamp()); }
        if (wasPostponed) { this.originalDateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbOriginalDateScheduled(predictionId)); }
        this.indexInSeasContest = predOp.getPredictionIndexInContest(predictionId, contestId);

        // Getting contest metadata:

        this.seasStartDate = contest.getSeasStartDate();
        this.seasEndDate = contest.getSeasEndDate();
        this.seasEndDate24 = contest.getSeasEndDate24();
    }

    private boolean eventDateBelongsToSeasContest() {
        if (wasPostponed &&
                (originalDateScheduled.isBefore(seasStartDate) ||
                 originalDateScheduled.isAfter(seasEndDate))) {
            return false;
        }

        if (!wasPostponed && dateScheduledKnown &&
                (dateScheduled.isBefore(seasStartDate) ||
                 dateScheduled.isAfter(seasEndDate))) {
            return false;
        }

        return true;
    }

    // Get seasonal validity status
    public int getSeasStatus() {

        /*
            Step 0: check is seasonal_validity_status was overruled
            Step 1: check if prediction should at all belong to current contest
                    1.1 Checking if event was originally scheduled within contest time frame
                    1.2 If date_scheduled is unknown check if contest is already over
                    1.3 Check if user already has 100 valid predictions
         */

        if (seasValidityStatusOverruled) { return seasValidityStatus; } // Step 0

        if (!eventDateBelongsToSeasContest()) { return 11; } // Step 1.1
        if (!dateScheduledKnown && todayDateTime.isAfter(seasEndDate)) { return 12; } // Step 1.2
        if (indexInSeasContest > 100) { return 13; } // Step 1.3

        return 1;
    }

}

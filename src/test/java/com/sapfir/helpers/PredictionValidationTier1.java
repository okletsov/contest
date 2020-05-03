package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;

public class PredictionValidationTier1 {

    // Prediction metadata:

    boolean seasValidityStatusOverruled;
    boolean wasPostponed;
    boolean dateScheduledKnown;
    int seasValidityStatus;
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
        if (seasValidityStatusOverruled) { this.seasValidityStatus = predOp.getDbSeasValidityStatus(predictionId); }
        this.dateScheduledKnown = predOp.isDbDateScheduledKnown(predictionId);
        this.wasPostponed = predOp.eventPostponed(predictionId);
        if (dateScheduledKnown) { this.dateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbDateScheduled(predictionId)); }
        if (wasPostponed) { this.originalDateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbOriginalDateScheduled(predictionId)); }

        // Getting contest metadata:

        this.seasStartDate = contest.getSeasStartDate();
        this.seasEndDate = contest.getSeasEndDate();
        this.seasEndDate24 = contest.getSeasEndDate24();
    }

    // Get seasonal validity status
    public int getSeasStatus() {

        // Step 0: check if overruled flag is = 1
        if (seasValidityStatusOverruled) { return seasValidityStatus; }

        // Step 1: check if prediction should at all belong to current contest

         // 1.1 Check if date_scheduled or original date_scheduled is within contest time frame
         // (in other words checking if event was originally scheduled within contest time frame)

        if (wasPostponed &&
                (originalDateScheduled.isBefore(seasStartDate) ||
                originalDateScheduled.isAfter(seasEndDate))) {
            return 11;
        }

        if (!wasPostponed && dateScheduledKnown &&
                (dateScheduled.isBefore(seasStartDate) ||
                dateScheduled.isAfter(seasEndDate))) {
            return 11;
        }

        // If date_scheduled is unknown check if contest should already be over
        // (in other words check if a bet for a tournament winner should belong to contest)

        return 1;
    }

}

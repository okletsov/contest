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
    boolean predictionQuarterGoal;
    int seasValidityStatus;
    int indexInSeasContest;
    int indexWithOddsBetween10And15InMonth;
    int indexPerEventPerUser;
    int indexOnGivenDayByUser;
    LocalDateTime dateScheduled;
    LocalDateTime initialDateScheduled;
    float userPickValue;

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
        if (dateScheduledKnown) { this.initialDateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbInitialDateScheduled(predictionId)); }
        if (!dateScheduledKnown) { this.todayDateTime = dtOp.convertToDateTimeFromString(dtOp.getTimestamp()); }
        this.indexInSeasContest = predOp.getPredictionIndexInContest(predictionId, contestId);
        this.userPickValue = predOp.getDbUserPickValue(predictionId);
        this.predictionQuarterGoal = predOp.isQuarterGoal(predictionId);
        if (dateScheduledKnown) {this.indexWithOddsBetween10And15InMonth = predOp.getPredictionIndexWithOddsBetween10And15InMonth(predictionId); }
        this.indexPerEventPerUser = predOp.getPredictionIndexPerEventPerUser(predictionId);
        if (dateScheduledKnown) { this.indexOnGivenDayByUser = predOp.getPredictionIndexOnGivenDayByUser(predictionId); }

        // Getting contest metadata:

        this.seasStartDate = contest.getSeasStartDate();
        this.seasEndDate = contest.getSeasEndDate();
        this.seasEndDate24 = contest.getSeasEndDate24();
    }

    private boolean eventDateBelongsToSeasContest() {

        if (dateScheduledKnown &&
                (initialDateScheduled.isBefore(seasStartDate) ||
                initialDateScheduled.isAfter(seasEndDate))) {
            return false;
        } else {
            return true;
        }

    }

    // Get seasonal validity status
    public int getSeasStatus() {

        /*
            Step 0: check is seasonal_validity_status was overruled
            Step 1: check if prediction should at all belong to current contest
                    1.1 Checking if event was originally scheduled within contest time frame
                    1.2 If date_scheduled is unknown check if contest is already over
                    1.3 Check if user already has 100 valid predictions
            Step 2: if step 1 is ok check if user violated any rules for which prediction should be count lost:
                    2.1 Check if user_pick_value is less than 1.5 or more than 15
                    2.2 Check if prediction is quarter goal and user_pick_value is less than 2
                    2.3 Check if user made more than 1 prediction with user_pick_value between 10.01 and 15 in a given month
                    2.4 Check if user made more than 1 prediction for the same event
                    2.5 Check if user made predictions on more than 10 events per day
         */

        if (seasValidityStatusOverruled) { return seasValidityStatus; } // Step 0

        if (!eventDateBelongsToSeasContest()) { return 11; } // Step 1.1
        if (!dateScheduledKnown && todayDateTime.isAfter(seasEndDate)) { return 12; } // Step 1.2
        if (indexInSeasContest > 100) { return 13; } // Step 1.3

        if (userPickValue < 1.5 || userPickValue > 15) { return 21; } // Step 2.1
        if (userPickValue >= 1.5 && userPickValue < 2 && predictionQuarterGoal)  { return 22; } // Step 2.2
        if (dateScheduledKnown && indexWithOddsBetween10And15InMonth > 1) { return 23; } // Step 2.3
        if (indexPerEventPerUser > 1) { return 24; } // Step 2.4
        if (dateScheduledKnown && indexOnGivenDayByUser > 10) { return 25; } // Step 2.5

        return 1;
    }

}

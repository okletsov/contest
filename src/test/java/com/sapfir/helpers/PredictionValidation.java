package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;

public class PredictionValidation {

    private static final Logger Log = LogManager.getLogger(PredictionValidation.class.getName());

    Connection conn;
    LocalDateTime todayDateTime;

    // Prediction metadata:

    boolean validityStatusOverruled;
    boolean wasPostponed;
    boolean dateScheduledKnown;
    boolean predictionQuarterGoal;
    int validityStatus;
    int indexInContest;
    int indexInSeasContest;
    int indexWithOddsBetween10And15InMonth;
    int indexPerEventPerUser;
    int indexOnGivenDayByUser;
    int indexPerEventMarketUserPickNameCompetitors;
    int indexOfDuplPrediction;
    LocalDateTime dateScheduled;
    LocalDateTime initialDateScheduled;
    float userPickValue;
    float payout;
    float option2Value;
    String userPickName;
    String userId;
    String predictionId;
    String mainScore;

    // Contest metadata:

    String contestId;
    String contestType;
    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime startOfLastDay;
    LocalDateTime endDatePlus24hrs;

    public PredictionValidation(Connection conn, String contestId, String predictionId) {

        PredictionOperations predOp = new PredictionOperations(conn);
        DateTimeOperations dtOp = new DateTimeOperations();
        this.conn = conn;
        Contest contest = new Contest(conn, contestId);

        // Getting prediction metadata:

        this.validityStatusOverruled = predOp.isDbValidityStatusOverruled(predictionId, contestId);
        if (validityStatusOverruled) { this.validityStatus = predOp.getDbValidityStatus(predictionId, contestId); }
        this.dateScheduledKnown = predOp.isDbDateScheduledKnown(predictionId);
        this.wasPostponed = predOp.eventPostponed(predictionId);
        if (dateScheduledKnown) { this.dateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbDateScheduled(predictionId)); }
        if (dateScheduledKnown) { this.initialDateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbInitialDateScheduled(predictionId)); }
        if (!dateScheduledKnown) { this.todayDateTime = dtOp.convertToDateTimeFromString(dtOp.getTimestamp()); }
        this.indexInContest = predOp.getPredictionIndexInContest(predictionId, contestId);
        this.indexInSeasContest = predOp.getPredictionIndexInSeasContest(predictionId);
        this.userPickValue = predOp.getDbUserPickValue(predictionId);
        this.predictionQuarterGoal = predOp.isQuarterGoal(predictionId);
        if (dateScheduledKnown) {this.indexWithOddsBetween10And15InMonth = predOp.getPredictionIndexWithOddsBetween10And15InMonth(predictionId); }
        this.indexPerEventPerUser = predOp.getPredictionIndexPerEventPerUser(predictionId);
        if (dateScheduledKnown) { this.indexOnGivenDayByUser = predOp.getPredictionIndexOnGivenDayByUser(predictionId); }
        this.userPickName = predOp.getDbUserPickName(predictionId);
        this.indexPerEventMarketUserPickNameCompetitors = predOp.getPredictionIndexPerEventMarketUserPickNameCompetitors(predictionId);
        this.userId = predOp.getDbUserId(predictionId);
        this.predictionId = predictionId;
        this.mainScore = predOp.getDbMainScore(predictionId);
        this.option2Value = predOp.getDbOption2Value(predictionId);
        if (option2Value > 0) { this.payout = predOp.getPayout(predictionId); }
        this.indexOfDuplPrediction = predOp.getIndexOfDuplPrediction(predictionId);

        // Getting contest metadata:

        this.contestId = contestId;
        this.contestType = contest.getContestType();
        this.startDate = contest.getStartDate();
        this.endDate = contest.getEndDate();
        this.startOfLastDay = contest.getStartOfLastDay();
        this.endDatePlus24hrs = contest.getEndDatePlus24hrs();
    }

    private boolean eventDateBelongsToContest() {

        if (dateScheduledKnown &&
                (initialDateScheduled.isBefore(startDate) ||
                initialDateScheduled.isAfter(endDate))) {
            return false;
        } else {
            return true;
        }

    }

    private boolean isBeforeLastDay(LocalDateTime date) {
        return date.isBefore(startOfLastDay) &&
               date.isAfter(startDate.plusSeconds(-1)) ;
    }

    private boolean isOnLastDay(LocalDateTime date) {
        return date.isAfter(startOfLastDay.plusSeconds(-1)) &&
               date.isBefore(endDate.plusSeconds(1));
    }

    private boolean isOnLastDayPlus24hrs(LocalDateTime date) {
        return date.isAfter(endDate) &&
               date.isBefore(endDatePlus24hrs);
    }

    private boolean isAfterLastDayPlus24hrs(LocalDateTime date) {
        return date.isAfter(endDatePlus24hrs.plusSeconds(-1));
    }

    private boolean eventCancelled() {
        if (mainScore != null) {
            return mainScore.contains("abn.") ||
                    mainScore.contains(" w.o.") ||
                    mainScore.contains(" ret.") ||
                    mainScore.contains("canc.") ||
                    mainScore.contains("award.");
        } else return false;
    }

    private boolean extraPredictionMade() {
        // Method named and invented by Inga :)

        PredictionOperations predOp = new PredictionOperations(conn);
        int countRemainingPredictions = predOp.getRemainingPredictionsCount(predictionId, contestId);
        int predictionsAmountRequired = 0;

        if (contestType.equals("seasonal")) {
            predictionsAmountRequired = 100;
        } else if (contestType.equals("monthly")) {
            predictionsAmountRequired = 30;
        }

        return (indexInContest + countRemainingPredictions) > predictionsAmountRequired;
    }

    private void updateWarningStatus(int status) {
        if (contestType.equals("seasonal")) { Log.warn("Warning for prediction " + predictionId + ": " + indexOfDuplPrediction + " occurrence for user"); }
        PredictionOperations predOp = new PredictionOperations(conn);
        predOp.updateWarningStatus(predictionId, status, contestType);
    }

    // Get validity status
    public int getStatus() {

        /*
            Step 0: check is validity_status was overruled
            Step 1: check if prediction should at all belong to current contest
                    1.1 Checking if event was originally scheduled within contest time frame
                    1.2 If date_scheduled is unknown check if contest is already over
                    1.3 Check if user already has 100 valid predictions for seasonal contest
            Step 2: if step 1 is ok check if user violated any rules for which prediction should be count lost:
                    2.1 Check if user_pick_value is less than 1.5 or more than 15
                    2.2 Check if prediction is quarter goal and user_pick_value is less than 2
                    2.3 Check if user made more than 1 prediction with user_pick_value between 10.01 and 15 in a given month
                    2.4 Check if user made more than 1 prediction for the same event
                    2.5 Check if user made predictions on more than 10 events per day
                    2.6 Check if market is Odd/Even (implemented via inspection of user_pick_name)
                    2.7 Check if user made a duplicated prediction
                        2.7.1 Warning for the first occurrence
                        2.7.2 Warning for the second occurrence
                        2.7.3 Count-lost starting from the third occurrence
                    2.8 Check for anomaly odds

             Step 3: if steps 1 and 2 are ok check other special conditions that may apply
                    3.1 Initial_date_scheduled before last day, date_scheduled before last day and event cancelled - doesn't count
                    3.2 Initial_date_scheduled before last day, date_scheduled on the last day and event cancelled - doesn't count
                    3.3 Initial_date_scheduled before last day, date_scheduled on the last + 24hrs - doesn't count
                    3.4 Initial_date_scheduled before last day, date_scheduled after the last day + 24hrs - doesn't count

                 If inspecting for seasonal contest:
                    3.5 Initial_date_scheduled on the last day, date_scheduled on the last day, event cancelled and extra prediction made instead - doesn't count
                    3.6 Initial_date_scheduled on the last day, date_scheduled on the last day, event cancelled and no extra prediction made instead - count void
                    3.7 Initial_date_scheduled on the last day, date_scheduled on the last day + 24hrs and extra prediction made instead - doesn't count
                    3.8 Initial_date_scheduled on the last day, date_scheduled on the last day + 24hrs, event cancelled and no extra prediction made instead - count void
                    3.9 Initial_date_scheduled on the last day, date_scheduled on the last day + 24hrs, event not cancelled and no extra prediction made instead - valid prediction
                    3.10 Initial_date_scheduled on the last day, date_scheduled after the last day + 24hrs and extra prediction made instead - doesn't count
                    3.11 Initial_date_scheduled on the last day, date_scheduled after the last day + 24hrs and no extra prediction made instead - count void

                 If inspecting for monthly contest:
                    3.12 Initial_date_scheduled on the last day, date_scheduled on the last day and event cancelled - doesn't count
                    3.13 Initial_date_scheduled on the last day, date_scheduled on the last + 24hrs day and event cancelled - count void
                    3.14 Initial_date_scheduled on the last day, date_scheduled on the last + 24hrs day and event not cancelled - valid prediction
                    3.15 Initial_date_scheduled on the last day, date_scheduled after last dat + 24hrs

         */

        if (validityStatusOverruled) { return validityStatus; } // Step 0

        if (!eventDateBelongsToContest()) { return 11; } // Step 1.1
        if (!dateScheduledKnown && todayDateTime.isAfter(endDate)) { return 12; } // Step 1.2
        if (contestType.equals("seasonal") && indexInSeasContest > 100) { return 13; } // Step 1.3

        if (userPickValue < 1.5 || userPickValue > 15) { return 21; } // Step 2.1
        if (userPickValue >= 1.5 && userPickValue < 2 && predictionQuarterGoal)  { return 22; } // Step 2.2
        if (dateScheduledKnown && userPickValue > 10 && userPickValue <= 15 && indexWithOddsBetween10And15InMonth > 1) { return 23; } // Step 2.3
        if (indexPerEventPerUser > 1) { return 24; } // Step 2.4
        if (dateScheduledKnown && indexOnGivenDayByUser > 10) { return 25; } // Step 2.5
        if (userPickName.contains("Odd") || userPickName.contains("Even")) { return 26; } // Step 2.6

        if (indexPerEventMarketUserPickNameCompetitors > 1 && indexOfDuplPrediction == 1) { updateWarningStatus(1); } // Step 2.7.1
        if (indexPerEventMarketUserPickNameCompetitors > 1 && indexOfDuplPrediction == 2) { updateWarningStatus(2); } // Step 2.7.2
        if (indexPerEventMarketUserPickNameCompetitors > 1 && indexOfDuplPrediction > 2) { return 27; } // Step 2.7.3

        if (option2Value > 0 && payout >= 1.05) { return 60; } // Step 2.8

        if (dateScheduledKnown && isBeforeLastDay(initialDateScheduled)) {

            if (isBeforeLastDay(dateScheduled) && eventCancelled()) { return 41; } // Step 3.1
            if (isOnLastDay(dateScheduled) && eventCancelled()) { return 42; } // Step 3.2
            if (isOnLastDayPlus24hrs(dateScheduled)) { return 43; } // Step 3.3
            if (isAfterLastDayPlus24hrs(dateScheduled)) { return 44; } // Step 3.4
        }

        if (contestType.equals("seasonal") && dateScheduledKnown && isOnLastDay(initialDateScheduled)) {

            if (isOnLastDay(dateScheduled) && eventCancelled() && extraPredictionMade()) { return 45; } // Step 3.5
            if (isOnLastDay(dateScheduled) && eventCancelled() && !extraPredictionMade()) { return 46; } // Step 3.6
            if (isOnLastDayPlus24hrs(dateScheduled) && extraPredictionMade()) { return 47; } // Step 3.7
            if (isOnLastDayPlus24hrs(dateScheduled) && !extraPredictionMade() && eventCancelled()) { return 48; } // Step 3.8
            if (isOnLastDayPlus24hrs(dateScheduled) && !extraPredictionMade() && !eventCancelled()) { return 2; } // Step 3.9
            if (isAfterLastDayPlus24hrs(dateScheduled) && extraPredictionMade()) { return 49; } // Step 3.10
            if (isAfterLastDayPlus24hrs(dateScheduled) && !extraPredictionMade()) { return 50; } // Step 3.11
        }

        if (contestType.equals("monthly") && dateScheduledKnown && isOnLastDay(initialDateScheduled)) {

            if (isOnLastDay(dateScheduled) && eventCancelled()) { return 51; } // Step 3.12
            if (isOnLastDayPlus24hrs(dateScheduled) && eventCancelled()) { return 52; } // Step 3.13
            if (isOnLastDayPlus24hrs(dateScheduled) && !eventCancelled()) { return 3; } // Step 3.14
            if (isAfterLastDayPlus24hrs(dateScheduled)) { return 53; } // Step 3.15

        }

        return 1;
    }

}

package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;

public class PredictionValidationTier1 {

    public static HashMap<String, Integer> warnings = new HashMap<>();
    Connection conn;
    LocalDateTime todayDateTime;

    // Prediction metadata:

    boolean validityStatusOverruled;
    boolean wasPostponed;
    boolean dateScheduledKnown;
    boolean predictionQuarterGoal;
    int validityStatus;
    int indexInContest;
    int indexWithOddsBetween10And15InMonth;
    int indexPerEventPerUser;
    int indexOnGivenDayByUser;
    int indexPerEventMarketUserPickNameCompetitors;
    LocalDateTime dateScheduled;
    LocalDateTime initialDateScheduled;
    float userPickValue;
    String userPickName;
    String userId;
    String predictionId;

    // Contest metadata:

    String contestId;

    public PredictionValidationTier1(Connection conn, String contestId, String predictionId) {

        PredictionOperations predOp = new PredictionOperations(conn);
        DateTimeOperations dtOp = new DateTimeOperations();
        this.conn = conn;

        // Getting prediction metadata:

        this.validityStatusOverruled = predOp.isDbValidityStatusOverruled(predictionId, contestId);
        if (validityStatusOverruled) { this.validityStatus = predOp.getDbValidityStatus(predictionId, contestId); }
        this.dateScheduledKnown = predOp.isDbDateScheduledKnown(predictionId);
        this.wasPostponed = predOp.eventPostponed(predictionId);
        if (dateScheduledKnown) { this.dateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbDateScheduled(predictionId)); }
        if (dateScheduledKnown) { this.initialDateScheduled = dtOp.convertToDateTimeFromString(predOp.getDbInitialDateScheduled(predictionId)); }
        if (!dateScheduledKnown) { this.todayDateTime = dtOp.convertToDateTimeFromString(dtOp.getTimestamp()); }
        this.indexInContest = predOp.getPredictionIndexInContest(predictionId, contestId);
        this.userPickValue = predOp.getDbUserPickValue(predictionId);
        this.predictionQuarterGoal = predOp.isQuarterGoal(predictionId);
        if (dateScheduledKnown) {this.indexWithOddsBetween10And15InMonth = predOp.getPredictionIndexWithOddsBetween10And15InMonth(predictionId); }
        this.indexPerEventPerUser = predOp.getPredictionIndexPerEventPerUser(predictionId);
        if (dateScheduledKnown) { this.indexOnGivenDayByUser = predOp.getPredictionIndexOnGivenDayByUser(predictionId); }
        this.userPickName = predOp.getDbUserPickName(predictionId);
        this.indexPerEventMarketUserPickNameCompetitors = predOp.getPredictionIndexPerEventMarketUserPickNameCompetitors(predictionId);
        this.userId = predOp.getDbUserId(predictionId);
        this.predictionId = predictionId;

        // Getting contest metadata:

        this.contestId = contestId;
    }

    private boolean contestIsOverForUnknownDateScheduled(String contestId) {
        Contest contest = new Contest(conn, contestId);
        LocalDateTime endDate = contest.getEndDate();
        return todayDateTime.isAfter(endDate);
    }

    private boolean eventDateBelongsToContest(String contestId) {

        Contest contest = new Contest(conn, contestId);
        LocalDateTime startDate = contest.getStartDate();
        LocalDateTime endDate = contest.getEndDate();

        if (dateScheduledKnown &&
                (initialDateScheduled.isBefore(startDate) ||
                initialDateScheduled.isAfter(endDate))) {
            return false;
        } else {
            return true;
        }

    }

    private int countDuplicatedPredictions() {
        int count = 0;

        for (String id: warnings.keySet()) {
            PredictionOperations predOpDupl = new PredictionOperations(conn);

            String userIdDupl = predOpDupl.getDbUserId(id);
            int warningId = warnings.get(id);

            if (userIdDupl.equals(userId) && (warningId == 1 || warningId == 2)) {
                count++;
            }
        }

        return count;
    }

    // Get validity status
    public int getStatus() {

        int countDuplPredictions = countDuplicatedPredictions();

        /*
            Step 0: check is validity_status was overruled
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
                    2.6 Check if market is Odd/Even (implemented via inspection of user_pick_name)
                    2.7 Check if user made a duplicated prediction
                        2.7.1 Warning for the first occurrence
                        2.7.2 Warning for the second occurrence
                        2.7.3 Count-lost starting from the third occurrence
         */

        if (validityStatusOverruled) { return validityStatus; } // Step 0

        if (!eventDateBelongsToContest(contestId)) { return 11; } // Step 1.1
        if (!dateScheduledKnown && contestIsOverForUnknownDateScheduled(contestId)) { return 12; } // Step 1.2
        if (indexInContest > 100) { return 13; } // Step 1.3

        if (userPickValue < 1.5 || userPickValue > 15) { return 21; } // Step 2.1
        if (userPickValue >= 1.5 && userPickValue < 2 && predictionQuarterGoal)  { return 22; } // Step 2.2
        if (dateScheduledKnown && indexWithOddsBetween10And15InMonth > 1) { return 23; } // Step 2.3
        if (indexPerEventPerUser > 1) { return 24; } // Step 2.4
        if (dateScheduledKnown && indexOnGivenDayByUser > 10) { return 25; } // Step 2.5
        if (userPickName.contains("Odd") || userPickName.contains("Even")) { return 26; } // Step 2.6

        if (indexPerEventMarketUserPickNameCompetitors > 1 && countDuplPredictions == 0) { warnings.put(predictionId, 1); } // Step 2.7.1
        if (indexPerEventMarketUserPickNameCompetitors > 1 && countDuplPredictions == 1) { warnings.put(predictionId, 2); } // Step 2.7.2
        if (indexPerEventMarketUserPickNameCompetitors > 1 && countDuplPredictions > 1) { return 27; } // Step 2.7.3

        return 1;
    }

}

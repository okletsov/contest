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
            Log.debug("Prediction " + predictionId + " does not count with status 10:\n" +
                    "- date_scheduled unknown\n" +
                    "- contest is already over");
        } else {
            Log.debug("Count: date_scheduled unknown, but contest is not over yet");
        }

    }

    private void validateKnownDateScheduled(String dateScheduled, String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);

        if (!dateScheduledWithinSeasLimit(dateScheduled)){
            if (predOp.eventPostponed(predictionId)){
                String origDateScheduled = predOp.getDbOriginalDateScheduled(predictionId);
                if (dateScheduledOnLastSeasDate(origDateScheduled)){
                    if (getCountValidPredictionsExclCurrent(predictionId) >= 100) {
                        /* Does not count:
                            - date_scheduled not in range
                            - event was postponed
                            - original_date_scheduled is on the last day of seasonal contest
                            - user made additional prediction instead of this one
                         */
                        updateValidityStatus(predictionId, 13);
                        Log.debug("Prediction " + predictionId + " does not count with status 13:\n" +
                                "- date_scheduled NOT in range\n" +
                                "- event was postponed\n" +
                                "- original_date_scheduled is on the last day of contest\n" +
                                "- user made additional prediction instead of this one");
                    } else {
                        if (dateScheduledWithinSeasEndDate24(dateScheduled)) {
                            /* Count:
                                - date_scheduled not in range
                                - event was postponed
                                - original_date_scheduled on last day of seasonal contest
                                - user DID NOT make additional prediction instead of this one
                                - new date_scheduled within 24 hours of contest end date
                                - !!! prediction settles according to event result, just like any usual prediction
                             */
                            updateValidityStatus(predictionId, 2);
                            Log.debug("Prediction count with status 2:\n" +
                                    "- date_scheduled NOT in range\n" +
                                    "- event was postponed\n" +
                                    "- original_date_scheduled is on the last day of contest\n" +
                                    "- user DID NOT make additional prediction instead of this one\n" +
                                    "- new date_scheduled within 24 hours of contest end date\n" +
                                    "- !!! prediction settles according to event result, just like a usual prediction");
                        } else {
                            /* Count:
                                - date_scheduled not in range
                                - event was postponed
                                - original_date_scheduled on last day of seasonal contest
                                - user DID NOT make additional prediction instead of this one
                                - new date_scheduled is NOT within 24 hours of contest end date
                                - !!! prediction should be VOID no matter the result
                             */
                            updateValidityStatus(predictionId, 3);
                            Log.debug("Prediction count with status 3:\n" +
                                    "- date_scheduled NOT in range\n" +
                                    "- event was postponed\n" +
                                    "- original_date_scheduled is on the last day of contest\n" +
                                    "- user DID NOT make additional prediction instead of this one\n" +
                                    "- new date_scheduled is NOT within 24 hours of contest end date\n" +
                                    "- !!! prediction should be VOID no matter the result");
                        }
                    }
                } else {
                    /* Does not count:
                        - date_scheduled NOT in range
                        - event was postponed
                        - original_date_scheduled was NOT on the last day of contest
                     */
                    updateValidityStatus(predictionId, 12);
                    Log.debug("Prediction " + predictionId + " does not count with status 12:\n" +
                            "- date_scheduled NOT in range\n" +
                            "- event was postponed\n" +
                            "- original_date_scheduled was NOT on the last day of contest");
                }
            } else {
                /* Status 11: does not count:
                    - date_scheduled NOT in range
                    - event was NOT postponed
                 */
                updateValidityStatus(predictionId,11);
                Log.debug("Prediction " + predictionId + " does not count with status 11:\n" +
                        "- date_scheduled NOT in range\n" +
                        "- event was NOT postponed");
            }
        } else {
            // Count: date_scheduled in range
            Log.debug("Count: date_scheduled is within range");
        }
    }

    private void validateVoidResult(String predictionId) {

        if (isVoidDueToCancellation(predictionId)){
            PredictionOperations predOp = new PredictionOperations(conn);
            String dateScheduled = predOp.getDbDateScheduled(predictionId);

            if (dateScheduledOnLastSeasDate(dateScheduled)) {
                if (getCountValidPredictionsExclCurrent(predictionId) >= 100) {
                    updateValidityStatus(predictionId, 15);
                    Log.debug("Prediction " + predictionId + " does not count with status 15:\n" +
                            "- result is void due to cancellation, retirement etc\n" +
                            "- date_scheduled is on the last day of contest\n" +
                            "- user made additional prediction instead of this one");
                } else {
                    updateValidityStatus(predictionId, 4);
                    Log.debug("result is valid:\n" +
                            "- result is void due to cancellation, retirement etc\n" +
                            "- date_scheduled is on the last day of contest\n" +
                            "- user DID NOT make additional prediction instead of this one");
                }
            } else {
                updateValidityStatus(predictionId, 14);
                Log.debug("Prediction " + predictionId + " does not count with status 14:\n" +
                        "- result is void due to cancellation, retirement etc\n" +
                        "- date_scheduled is NOT on the last day of contest");
                // bet invalid
            }
        } else {
            Log.debug("result is valid");
        }
    }

    private boolean dateScheduledWithinSeasLimit(String stringDateScheduled) {
        boolean isWithinLimit;

        Contest cont = new Contest(conn, contestId);
        LocalDateTime seasStartDate = cont.getSeasStartDate();
        LocalDateTime seasEndDate = cont.getSeasEndDate();

        DateTimeOperations dtOp = new DateTimeOperations();
        LocalDateTime dateScheduled = dtOp.convertToDateTimeFromString(stringDateScheduled);

        isWithinLimit = !seasStartDate.isAfter(dateScheduled) && !seasEndDate.isBefore(dateScheduled);
        return isWithinLimit;
    }

    private boolean dateScheduledWithinSeasEndDate24(String stringDateScheduled) {
        boolean isWithinLimit;

        Contest cont = new Contest(conn, contestId);
        LocalDateTime seasEndDate = cont.getSeasEndDate();
        LocalDateTime seasEndDate24 = cont.getSeasEndDate24();

        DateTimeOperations dtOp = new DateTimeOperations();
        LocalDateTime dateScheduled = dtOp.convertToDateTimeFromString(stringDateScheduled);

        isWithinLimit = !seasEndDate.isAfter(dateScheduled) && !seasEndDate24.isBefore(dateScheduled);
        return isWithinLimit;
    }

    private boolean dateScheduledOnLastSeasDate(String stringDateScheduled) {

        Contest cont = new Contest(conn, contestId);
        LocalDateTime seasLastDayStart = cont.getSeasLastDayStart();
        LocalDateTime seasEndDate = cont.getSeasEndDate();

        DateTimeOperations dtOp = new DateTimeOperations();
        LocalDateTime dateScheduled = dtOp.convertToDateTimeFromString(stringDateScheduled);

        return  !seasLastDayStart.isAfter(dateScheduled) && !seasEndDate.isBefore(dateScheduled);
    }

    private boolean isPredictionQuarterGoal(String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);
        String market = predOp.getDbMarket(predictionId);

        if (market.startsWith("AH ") || market.startsWith("O/U " )) {
            return  market.contains(".25") || market.contains(".75");
        } else {
            return false;
        }
    }

    private boolean isVoidDueToCancellation(String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);
        String mainScore = predOp.getDbMainScore(predictionId);

        return mainScore.contains("abn.") ||
                mainScore.contains(" w.o.") ||
                mainScore.contains(" ret.") ||
                mainScore.contains("canc.");
    }

    private int getCountValidPredictionsExclCurrent(String predictionId) {
        // !!! Add other invalid statuses for "not in" clause or replace "not in" only with valid statuses !!!
        PredictionOperations predOp = new PredictionOperations(conn);

        String contestId = predOp.getDbSeasContestId(predictionId);
        String userId = predOp.getDbUserId(predictionId);

        String sql = "select count(id) as count " +
                "from prediction " +
                "where seasonal_contest_id = '" + contestId + "' " +
                "and user_id = '" + userId + "' " +
                "and id != '" + predictionId + "' " +
                "and (validity_status is null or validity_status not in (10)) " +
                "group by user_id;";

        DatabaseOperations dbOp = new DatabaseOperations();
        String stringCount = dbOp.getSingleValue(conn, "count", sql);
        return Integer.parseInt(stringCount);
    }

    private int getCountValidPredictionsOver10ExclCurrent(String predictionId) {
        /*
            !!! Add other invalid statuses for "not in" clause or replace "not in" only with valid statuses !!!

            This method returns count of valid predictions made by user with user_pick_value < 10 and <=15
            made before current prediction in month prediction being inspected is placed (kiev time zone)
         */
        PredictionOperations predOp = new PredictionOperations(conn);
        String contestId = predOp.getDbSeasContestId(predictionId);
        String userId = predOp.getDbUserId(predictionId);
        String datePredicted = predOp.getDbDatePredicted(predictionId);

        String sql = "select count(id) as count\n" +
                "from prediction\n" +
                "where seasonal_contest_id = '" + contestId + "'\n" +
                "and user_id = '" + userId + "'\n" +
                "and id != '" + predictionId + "'\n" +
                "and month(date(convert_tz(date_predicted, 'UTC', 'Europe/Kiev'))) = " +
                "month(date(convert_tz('" + datePredicted + "', 'UTC', 'Europe/Kiev')))\n" +
                "and (validity_status is null or validity_status not in (10))\n" +
                "and user_pick_value > 10\n" +
                "and user_pick_value <= 15\n" +
                "and date_predicted < '" + datePredicted + "'\n" +
                "group by user_id;";

        DatabaseOperations dbOp = new DatabaseOperations();
        String stringCount = dbOp.getSingleValue(conn, "count", sql);

        if (stringCount == null){
            return 0;
        } else {
            return Integer.parseInt(stringCount);
        }
    }

    private void validateDateScheduled(String predictionId) {
        Log.debug("Validating date_scheduled for " + predictionId + "...");

        PredictionOperations predOp = new PredictionOperations(conn);
        String dateScheduled = predOp.getDbDateScheduled(predictionId);

        if (dateScheduled == null) {
            validateUnknownDateScheduled(predictionId);
        } else {
            validateKnownDateScheduled(dateScheduled, predictionId);
        }
    }

    private void validateUserPickValue(String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);
        float userPickValue = predOp.getDbUserPickValue(predictionId);

        if (userPickValue < 1.5) {
            updateValidityStatus(predictionId, 20);
            Log.debug("Prediction " + predictionId + " count-lost with status 20: \n- user_pick_value < 1.5");
        } else if(userPickValue >= 1.5 && userPickValue < 2) {
            if (isPredictionQuarterGoal(predictionId)) {
                updateValidityStatus(predictionId, 21);
                Log.debug("Prediction " + predictionId + " count-lost with status 21: \n- quarter-goal user_pick_value < 2");
            } else {
                Log.debug("user_pick_value is within range");
            }
        } else if (userPickValue >= 2 && userPickValue <= 10) {
            Log.debug("user_pick_value is within range");
        } else if (userPickValue > 10 && userPickValue <= 15) {
            int predictionsOver10 = getCountValidPredictionsOver10ExclCurrent(predictionId);
            if (predictionsOver10 > 0) {
                updateValidityStatus(predictionId, 22);
                Log.debug("Prediction " + predictionId + " count-lost with status 22: \n" +
                        "- prediction with user_pick_value > 10 and <= 15 was already placed this month");
            } else {
                Log.debug("user_pick_value is within range. First prediction with user_pick_value > 10 this month");
            }
        } else if(userPickValue > 15) {
            updateValidityStatus(predictionId, 23);
            Log.debug("Prediction " + predictionId + " count-lost with status 23: \n- user_pick_value > 15");
        } else {
            Log.error("Unknown user_pick_value!");
        }
    }

    private void validateResult(String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);
        String result = predOp.getDbPredictionResult(predictionId);

        if (result.equals("void")) {
            validateVoidResult(predictionId);
        } else {
            Log.debug("result is valid");
        }
    }

    private void checkForAnomalyOdd(String predictionId) {
        /*
            Anomaly odd = bagovaya stavka
            Until a better solution is implemented we will manually inspect odds with payout > 105%

            To know what bets have a payout value we will need to see the db value for option2_value
            if it exists (> 0) the payout value can be calculated
         */

        PredictionOperations predOp = new PredictionOperations(conn);
        float option2Value = predOp.getDbOption2Value(predictionId);

        if (option2Value > 0) {
            float payout = predOp.getPayout(predictionId);
            if (payout > 1.05) {
                updateValidityStatus(predictionId, 16);
                Log.warn("potentially does not count. Status 16:\n" +
                        "- payout > 1.05. Check prediction for anomaly odd");
            } else {
                Log.debug("Payout is ok");
            }
        } else {
            Log.debug("Single value prediction. Payout N/A");
        }
    }

    private void checkIfOneBetForEventByUser(String predictionId) {
        PredictionOperations predOp = new PredictionOperations(conn);

        String userId = predOp.getDbUserId(predictionId);
        String eventIdentifier = predOp.getDbEventIdentifier(predictionId);
        String firstPredictionId = predOp.getFirstPredictionByUserForEvent(eventIdentifier, userId);

        if (predictionId.equals(firstPredictionId)) {
            Log.debug("It is the first prediction made by user for this event");
        } else {
            updateValidityStatus(predictionId, 24);
            Log.debug("Prediction " + predictionId + " count-lost with status 24:\n" +
                    "- current prediction is not the first valid prediction made by user for this event");
        }
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

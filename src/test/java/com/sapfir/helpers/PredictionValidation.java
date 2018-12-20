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
            Log.warn("Prediction " + predictionId + " does not count with status 10:\n" +
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
                if (origDateScheduledOnLastSeasDate(origDateScheduled)){
                    if (getCountValidPredictionsExclCurrent(predictionId) >= 100) {
                        /* Does not count:
                            - date_scheduled not in range
                            - event was postponed
                            - original_date_scheduled is on the last day of seasonal contest
                            - user made additional prediction instead of this one
                         */
                        updateValidityStatus(predictionId, 13);
                        Log.warn("Prediction " + predictionId + " does not count with status 13:\n" +
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
                            Log.warn("Prediction count with status 2:\n" +
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
                            Log.warn("Prediction count with status 3:\n" +
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
                    Log.warn("Prediction " + predictionId + " does not count with status 12:\n" +
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
                Log.warn("Prediction " + predictionId + " does not count with status 11:\n" +
                        "- date_scheduled NOT in range\n" +
                        "- event was NOT postponed");
            }
        } else {
            // Count: date_scheduled in range
            Log.debug("Count: date_scheduled is within range");
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

    private boolean origDateScheduledOnLastSeasDate(String stringDateScheduled) {
        boolean isOnLastDay;

        Contest cont = new Contest(conn, contestId);
        LocalDateTime seasLastDayStart = cont.getSeasLastDayStart();
        LocalDateTime seasEndDate = cont.getSeasEndDate();

        DateTimeOperations dtOp = new DateTimeOperations();
        LocalDateTime dateScheduled = dtOp.convertToDateTimeFromString(stringDateScheduled);

        isOnLastDay = !seasLastDayStart.isAfter(dateScheduled) && !seasEndDate.isBefore(dateScheduled);
        return isOnLastDay;
    }

//    private boolean isQuarterGoal(String predictionId) {
//        PredictionOperations predOp = new PredictionOperations(conn);
//
//        String market
//    }

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

            This method returns count of valid predictions made by user with odds < 10 and <=15
            in month prediction being inspected is placed (kiev time zone)
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

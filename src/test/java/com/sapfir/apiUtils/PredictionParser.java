package com.sapfir.apiUtils;

import com.sapfir.helpers.DateTimeOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PredictionParser {

    private final JsonHelpers jsonHelpers = new JsonHelpers();

    private final String json;
    private final String feedItemId;
    private final String predictionResultId;

    private final String feedFieldsPath;
    private final String infoFieldsPath;
    private List<String> outcomeNames = new ArrayList<>();

    private final ApiHelpers apiHelpers;

    public PredictionParser(String jsonWithPredictions, String feedItemId, ApiHelpers apiHelpers) {
        this.apiHelpers = apiHelpers;

        this.json = jsonWithPredictions;
        this.feedItemId = feedItemId;
        this.feedFieldsPath = "/d/feed/" + this.feedItemId;

        String predictionInfoId = getPredictionInfoId();
        this.infoFieldsPath = "/d/info/" + predictionInfoId;

        this.predictionResultId = getPredictionResultId();

        this.outcomeNames = getOutcomeNames();
        Collections.sort(this.outcomeNames);
    }

    private List<String> getOutcomeNames() {
        return jsonHelpers.getParentFieldNames(json, infoFieldsPath + "/outcomes");
    }

    public String getFeedItemIdForDatabase() {
        return "feed_item_" + feedItemId;
    }

    public String getPredictionInfoId() {
        return jsonHelpers.getFieldValueByPathAndName(json, feedFieldsPath, "SubjectUID");
    }

    public String getEventIdForDatabase() {
        String eventIdFromJson = jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "EventID");

        // Winner bets don't have a value for EventId, but have it for TournamentID field
        if (eventIdFromJson.equals("null")) {
            return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "TournamentID");
        }
        return "status-" + eventIdFromJson;
    }

    public String getSport() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "sport-name");
    }

    public String getRegion() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "country-name");
    }

    public String getTournamentName() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "tournament-name");
    }

    public String getMainScore() {
        /*
            Note: it is possible for main score:
                - to not be present (Winner bets)
                - to equal to null
                - or to be an empty string
         */
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "Result");
    }

    public String getDetailedScore() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "partialresult");
    }

    public String getPredictionResultId() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "PredictionResultID");
    }

    private String getSportId() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "sport-id");
    }

    private String getEncodeTournamentId() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "encodeTournamentID");
    }

    public String getDateScheduled() {
        String unixValue = jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "Time");
        DateTimeOperations dateTimeOperations = new DateTimeOperations();

        if (!unixValue.equals("null")) {
            // e.g. if dateScheduled is known
            return dateTimeOperations.convertFromUnix(unixValue);
        } else if (!predictionResultId.equals("null")) {
            // if dateScheduled is unknown but prediction outcome is known assuming the bet is for Winner market

            String sportId = getSportId();
            String encodeTournamentId = getEncodeTournamentId();

            TournamentResults tournamentResults = new TournamentResults(apiHelpers, sportId, encodeTournamentId);
            return tournamentResults.getDateScheduledByTeam("replace");
            // Convert that time to easy to read format
        } else {
            /*
                if both dateScheduled and prediction outcome are unknown,
                then assuming the bet is for Winner market but result is still unknown
             */
            return null;
        }
    }
}

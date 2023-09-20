package com.sapfir.apiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PredictionParser {

    private final JsonHelpers jsonHelpers = new JsonHelpers();

    private final String json;
    private final String feedItemId;

    private final String feedFieldsPath;
    private final String infoFieldsPath;
    private List<String> outcomeNames = new ArrayList<>();

    public PredictionParser(String jsonWithPredictions, String feedItemId) {
        this.json = jsonWithPredictions;
        this.feedItemId = feedItemId;
        this.feedFieldsPath = "/d/feed/" + this.feedItemId;

        String predictionInfoId = getPredictionInfoId();
        this.infoFieldsPath = "/d/info/" + predictionInfoId;

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
}

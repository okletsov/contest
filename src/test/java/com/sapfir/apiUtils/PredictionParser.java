package com.sapfir.apiUtils;

public class PredictionParser {

    private final JsonHelpers jsonHelpers = new JsonHelpers();

    private final String json;
    private final String feedItemId;

    private final String feedFieldsPath;
    private final String infoFieldsPath;
    private String outcomesObjectsPath;

    public PredictionParser(String jsonWithPredictions, String feedItemId) {
        this.json = jsonWithPredictions;
        this.feedItemId = feedItemId;
        this.feedFieldsPath = "/d/feed/" + this.feedItemId;

        String predictionInfoId = getPredictionInfoId();
        this.infoFieldsPath = "/d/info/" + predictionInfoId;
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
}

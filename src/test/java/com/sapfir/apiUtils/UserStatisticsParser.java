package com.sapfir.apiUtils;

import com.sapfir.helpers.Properties;

public class UserStatisticsParser {

    private final JsonHelpers jsonHelpers = new JsonHelpers();

    private final String username;
    private final ApiHelpers apiHelpers;

    Properties props = new Properties();
    private final String siteUrl = props.getSiteUrl();

    public UserStatisticsParser(String username, ApiHelpers apiHelpers) {
        this.username = username;
        this.apiHelpers = apiHelpers;
    }

    public int getTotalSettledPredictions() {

        String url = siteUrl + "myPredictions/overallStats/" + username + "/";
        String json = apiHelpers.makeApiRequest(url);

        String pathToField = "/d/total";
        String rawFieldValue = jsonHelpers.getFieldValueByPathAndName(json, pathToField, "total");

        if (rawFieldValue != null) {
            return Integer.parseInt(rawFieldValue);
        }
        return 0;

    }

    public int getTotalNextPredictions() {

        String url = siteUrl + "myPredictions/next/" + username + "/";
        String json = apiHelpers.makeApiRequest(url);

        String pathToField = "/d";

        String rawFieldValue = jsonHelpers.getFieldValueByPathAndName(json, pathToField, "total");

        if (rawFieldValue != null) {
            return Integer.parseInt(rawFieldValue);
        }

        return 0;

    }
}

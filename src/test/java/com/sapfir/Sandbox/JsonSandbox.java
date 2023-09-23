package com.sapfir.Sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapfir.apiUtils.ApiHelpers;
import com.sapfir.apiUtils.PredictionParser;

import java.io.File;
import java.io.IOException;

public class JsonSandbox {

    public static void main(final String... args) throws IOException {

        File jsonFile = new File("jsonExample.json");
        String json = "";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            json = rootNode.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiHelpers apiHelpers = new ApiHelpers();
        PredictionParser predictionParser = new PredictionParser(json, "6246498203", apiHelpers);

        System.out.println(predictionParser.getFeedItemIdForDatabase());
        System.out.println(predictionParser.getPredictionInfoId());
        System.out.println(predictionParser.getEventIdForDatabase());
        System.out.println(predictionParser.getSport());
        System.out.println(predictionParser.getRegion());
        System.out.println(predictionParser.getTournamentName());
        System.out.println(predictionParser.getMainScore());
        System.out.println(predictionParser.getDetailedScore());
        System.out.println(predictionParser.getDateScheduled());
        System.out.println(predictionParser.getPredictionResultId());
    }
}

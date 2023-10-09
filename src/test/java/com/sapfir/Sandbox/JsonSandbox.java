package com.sapfir.Sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapfir.apiUtils.ApiHelpers;
import com.sapfir.apiUtils.JsonHelpers;
import com.sapfir.apiUtils.PredictionParser;
import com.sapfir.apiUtils.TournamentResultsParser;
import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.PredictionOperations;
import com.sapfir.helpers.TournamentResultsHelpers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class JsonSandbox {

    public static void main(final String... args) throws IOException {

        // Connect to database
        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

        // Add necessary helpers
        JsonHelpers jsonHelpers = new JsonHelpers();
        ApiHelpers apiHelpers = new ApiHelpers();

        // Get json from a file
        File jsonFile = new File("jsonExample.json");
        String json = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            json = rootNode.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Code to test
        PredictionOperations predOp = new PredictionOperations(conn, apiHelpers, json, "6288602303-test");
//        predOp.addPrediction("Deagle");
        predOp.updatePrediction();

        // Close database connection
        dbOp.closeConnection(conn);

        System.out.println("stop here");
    }
}

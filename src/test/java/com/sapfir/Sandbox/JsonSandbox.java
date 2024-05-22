package com.sapfir.Sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapfir.apiUtils.ApiHelpers;
import com.sapfir.apiUtils.JsonHelpers;
import com.sapfir.apiUtils.PredictionParser;
import com.sapfir.apiUtils.TournamentResultsParser;
import com.sapfir.helpers.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
//        PredictionOperations predOp = new PredictionOperations(conn, apiHelpers, json, "6652239303");
        PredictionParser parser = new PredictionParser(json, "6652239303");
        boolean isBroken = parser.getPredictionInfoId().equals("2");
        System.out.println(isBroken);

        // Close database connection
        dbOp.closeConnection(conn);

        System.out.println("stop here");
    }
}

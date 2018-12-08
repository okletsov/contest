package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;

public class PredictionValidation {

    private static final Logger Log = LogManager.getLogger(PredictionValidation.class.getName());
    private DatabaseOperations dbOp = new DatabaseOperations();

    public PredictionValidation(Connection conn) {
        this.conn = conn;

        String sqlToGetPredictionsToInspect =
                        "select \n" +
                        "p.id\n" +
                        "from prediction p\n" +
                        "join contest c on c.id = p.seasonal_contest_id\n" +
                        "where\n" +
                        "c.is_active = 1;";
        this.predictionsToInspect = dbOp.getArray(conn, "id", sqlToGetPredictionsToInspect);
    }

    private Connection conn;
    private ArrayList<String> predictionsToInspect;

    public boolean validatePredictions() {
        boolean newInvalidPredictionsFound = false;

        for (String predictionId: predictionsToInspect ) {
            // add individual validation methods here
        }

        return newInvalidPredictionsFound;
    }
}

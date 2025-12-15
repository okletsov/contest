package com.sapfir.Sandbox;

import com.sapfir.helpers.ContestResults;
import com.sapfir.helpers.DatabaseOperations;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Practice {

    public static void main(final String... args) {
//        Connect to database
        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

//        Add necessary helpers
        ContestResults contRes = new ContestResults(conn);

//        Code to test
        String seasContestId = "36d6dcb0-ce59-11f0-8b23-000017024a87";
        String monContestId = "36da5a22-ce59-11f0-8b23-000017024a87";
        String annualContestId = "2cdc9aed-84fd-11f0-8b23-000017024a87";

        List<HashMap<String,Object>> resultsToWrite = contRes.getGeneralContestResultsToWrite(annualContestId);
        System.out.println("Stop Here");

//        Close database connection
        dbOp.closeConnection(conn);

    }
}

package com.sapfir.Sandbox;

import com.sapfir.helpers.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

public class JsonSandbox {

    public static void main(final String... args) throws IOException {

        // Connect to database
        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

        // Add necessary helpers


        // Code to test
        String sql = "SELECT * from contest c where is_active = 1;";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet rs = eq.getSelectResult();


        String json = dbOp.sqlToJson(conn, sql);



        // Close database connection
        dbOp.closeConnection(conn);

        System.out.println("stop here");
    }
}

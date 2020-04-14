package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {

    private static final Logger Log = LogManager.getLogger(DatabaseOperations.class.getName());

    public Connection connectToDatabase() {

        Properties prop = new Properties();
        Connection conn = null;

        try {
            Log.trace("Connecting to database...");
            conn = DriverManager.getConnection(
                    prop.getDatabaseURL(),
                    prop.getDatabaseUsername(),
                    prop.getDatabasePassword());
            Log.trace("Successfully connected to database...");

        } catch (SQLException ex) {

            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
        return conn;
    }

    public void closeConnection(Connection conn){
        try {
            Log.trace("Closing connection...");
            if (conn != null){
                conn.close();
                Log.trace("Connection closed successfully");
            } else {
                Log.error("Unable to close connection because connection is null");
            }
        } catch (SQLException ex){
            Log.error("SQL Exception: " + ex.getMessage());
        }
    }

    public String getSingleValue (Connection conn, String columnLabel, String sql){
        String value = null;
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet rs = eq.getSelectResult();
        try {
            while (rs.next()) {
                value = rs.getString(columnLabel);
                Log.trace("Successfully found " + columnLabel);
            }
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
        eq.cleanUp();
        return value;
    }

    public ArrayList<String> getArray(Connection conn, String columnLabel, String sql){
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet rs = eq.getSelectResult();
        ArrayList<String> result = new ArrayList<>();
        try {
            while (rs.next()) {
                String value = rs.getString(columnLabel);
                result.add(value);
            }
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
        eq.cleanUp();
        Log.trace("Successfully retrieved array for column " + columnLabel);
        return result;
    }

}

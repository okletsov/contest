package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    public String arrayToStringForInClause(ArrayList<String> array) {
        String result = "(";

        for (int i = 0; i < array.size(); i++) {
           result = result + "'" + array.get(i) + "'";

           if (i == array.size() - 1) {
               result = result + ")";
           } else {
               result = result + ", ";
           }
        }

        return result;
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

    public List<HashMap<String,Object>> getListOfHashMaps (Connection conn, String sql) {

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet rs = eq.getSelectResult();
        List<HashMap<String,Object>> list = new ArrayList<>();

        try {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            while (rs.next()) {
                HashMap<String,Object> row = new HashMap<String, Object>(columns);
                for(int i=1; i<=columns; ++i) {
                    row.put(md.getColumnName(i),rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
        eq.cleanUp();
        Log.trace("Successfully retrieved sql result as a list");
        return list;
    }

}

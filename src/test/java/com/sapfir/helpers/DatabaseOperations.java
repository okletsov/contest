package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DatabaseOperations {

    private static final Logger Log = LogManager.getLogger(DatabaseOperations.class.getName());

    public Connection connectToDatabase() {

        Properties prop = new Properties();
        Connection connection = null;

        try {
            Log.debug("Connecting to database...");
            connection = DriverManager.getConnection(
                    prop.getDatabaseURL(),
                    prop.getDatabaseUsername(),
                    prop.getDatabasePassword());
            Log.info("Successfully connected to database...");

        } catch (SQLException ex) {

            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
        return connection;
    }

    public void closeConnection(Connection connection){
        try {
            Log.debug("Closing connection...");
            if (connection != null){
                connection.close();
                Log.info("Connection closed successfully...");
            } else {
                Log.error("Unable to close connection because connection is null");
            }
        } catch (SQLException ex){
            Log.error("SQL Exception: " + ex.getMessage());
        }
    }

    public void closeStatement(Statement statement){
        try {
            Log.trace("Closing statement...");
            if (statement != null){
                statement.close();
                Log.trace("Statement closed successfully...");
            } else {
                Log.warn("Unable to close statement because statement is null");
            }
        } catch (SQLException ex){
            Log.error("SQL Exception: " + ex.getMessage());
        }
    }

    public int updateDatabase(Connection conn, String sql) {

        Statement statement = null;
        int resultSet = 0;

        Log.debug("Executing sql statement...");
        try{
            statement = conn.createStatement();
            resultSet = statement.executeUpdate(sql);
            Log.debug("Success. Rows affected: " + resultSet);

        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        } finally {
            closeStatement(statement);
        }
        return resultSet;
    }

    public ResultSet selectFromDatabase(Connection conn, String sql){
        Statement statement = null;
        ResultSet resultSet = null;

        Log.debug("Executing SELECT statement...");
        try{
            statement = conn.createStatement();
            resultSet = statement.executeQuery(sql);
            Log.debug("Statement executed successfully");
        } catch (SQLException ex){
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        } finally {
            closeStatement(statement);
        }
        return resultSet;
    }
}

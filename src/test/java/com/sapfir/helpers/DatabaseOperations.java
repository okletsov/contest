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

    public void closeConnection(Connection conn){
        try {
            Log.debug("Closing connection...");
            if (conn != null){
                conn.close();
                Log.info("Connection closed successfully...");
            } else {
                Log.error("Unable to close connection because connection is null");
            }
        } catch (SQLException ex){
            Log.error("SQL Exception: " + ex.getMessage());
        }
    }

}

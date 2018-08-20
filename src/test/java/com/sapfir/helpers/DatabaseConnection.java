package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private Properties prop = new Properties();
    private static final Logger Log = LogManager.getLogger(DatabaseConnection.class.getName());
    private Connection connection;

    public Connection connectToDatabase() {

        try {
            Log.info("Connecting to database...");
            this.connection = DriverManager.getConnection(
                    prop.getDatabaseURL(),
                    prop.getDatabaseUsername(),
                    prop.getDatabasePassword());
            Log.info("Connection successful...");

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
            Log.info("Closing connection...");
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
            Log.info("Closing statement...");
            if (statement != null){
                statement.close();
                Log.info("Statement closed successfully...");
            } else {
                Log.error("Unable to close statement because statement is null");
            }
        } catch (SQLException ex){
            Log.error("SQL Exception: " + ex.getMessage());
        }
    }
}

package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Logger Log = LogManager.getLogger(DatabaseConnection.class.getName());
    private Connection conn;

    public Connection getConnection (String dbUrl, String dbUsername, String dbPassword) {

        try {
            Log.info("Connecting to database...");
            this.conn = DriverManager.getConnection(dbUrl, dbUsername ,dbPassword);
            Log.info("Connection successful...");

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
            Log.info("Closing connection...");
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

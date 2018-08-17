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

            Log.error("SQLException: " + ex.getMessage());
            Log.error("SQLState: " + ex.getSQLState());
            Log.error("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
        }
        return conn;
    }
}

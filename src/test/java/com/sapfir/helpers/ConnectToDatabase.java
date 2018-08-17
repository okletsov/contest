package com.sapfir.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectToDatabase {

    public ConnectToDatabase(String dbUrl, String dbUsername, String dbPassword) {
        System.out.println("Connecting database...");

        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername ,dbPassword);
            System.out.println("Connection successful!");

        } catch (SQLException ex) {

            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
}

package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ContestOperations {

    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());

    private DatabaseConnection conn = new DatabaseConnection();
    private Connection connection;
    private Statement statement;
    private String sql;
    private String start_date;
    private String end_date;
    private int resultSet;

    public void addSeasonalContest(String year, String season) {
        connection = conn.connectToDatabase();

        // Determining start_date and end_date
        if (season.equals("Autumn")) {
            start_date = year + "-09-01 00:00:00";
            end_date = year + "-11-30 23:59:59";
        } else if (season.equals("Spring")) {
            start_date = year + "-03-01 00:00:00";
            end_date = year + "-5-31 23:59:59";
        } else if (season.equals("Winter")) {
            start_date = year + "-12-01 00:00:00";
            int nextYear = Integer.parseInt(year) + 1;
            year = Integer.toString(nextYear);
            end_date = year + "-02-28 23:59:59";
        } else if (season.equals("Summer")) {
            start_date = year + "-06-01 00:00:00";
            end_date = year + "-08-31 23:59:59";
        } else {
            Log.fatal("Incorrect season entered: " + season);
            System.exit(0);
        }

        try{
            sql = "INSERT INTO contest " +
                    "(id, type, year, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'seasonal', '" + year + "', '" + season + "', '" + start_date + "', '" + end_date + "', 1);";
            statement = connection.createStatement();
            resultSet = statement.executeUpdate(sql);
            Log.info("Rows updated: " + resultSet);
        } catch (SQLException ex) {
            Log.fatal("\nSQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        } finally {
            conn.closeConnection(connection);
        }

    }

}

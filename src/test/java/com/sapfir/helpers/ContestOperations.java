package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ContestOperations {

    // Implement a check to see if there are active contests exist
    // Implement visokosny year determination
    // Implement monthly contest
    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());

    private DatabaseConnection conn = new DatabaseConnection();
    private Statement statement;

    public void addSeasonalContest(String year, String season) {
        String sql;
        String start_date;
        String end_date;
        int resultSet;

        Connection connection = conn.connectToDatabase();

        Log.info("Determining start_date and end_date...");
        switch (season) {
            case "Autumn":
                start_date = year + "-09-01 00:00:00";
                end_date = year + "-11-30 23:59:59";
                break;
            case "Spring":
                start_date = year + "-03-01 00:00:00";
                end_date = year + "-5-31 23:59:59";
                break;
            case "Winter":
                start_date = year + "-12-01 00:00:00";
                int nextYear = Integer.parseInt(year) + 1;
                end_date = Integer.toString(nextYear) + "-02-28 23:59:59";
                break;
            case "Summer":
                start_date = year + "-06-01 00:00:00";
                end_date = year + "-08-31 23:59:59";
                break;
            default:
                start_date = null;
                end_date = null;
                Log.fatal("Incorrect season entered: " + season);
                System.exit(0);
        }

        Log.info("Inserting new seasonal contest into database...");
        try{
            sql = "INSERT INTO contest " +
                    "(id, type, year, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'seasonal', '" + year + "', '" + season + "', '" + start_date + "', '" + end_date + "', 1);";
            statement = connection.createStatement();
            resultSet = statement.executeUpdate(sql);
            Log.info("Successfully added contest. Rows added: " + resultSet);
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        } finally {
            conn.closeStatement(statement);
            conn.closeConnection(connection);
        }
    }
}

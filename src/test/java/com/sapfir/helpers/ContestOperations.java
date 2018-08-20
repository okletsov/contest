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
        String sql_seasonal;
        String sql_monthly_1;
        String sql_monthly_2;
        String seasonal_start_date;
        String seasonal_end_date;
        String month_1_start_date;
        String month_1_end_date;
        String month_2_start_date;
        String month_2_end_date;
        int resultSet_seasonal;
        int resultSet_monthly_1;
        int resultSet_monthly_2;

        Connection connection = conn.connectToDatabase();

        Log.info("Determining start and end dates...");
        switch (season) {
            case "Autumn":
                seasonal_start_date = year + "-09-01 00:00:00";
                seasonal_end_date = year + "-11-30 23:59:59";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-09-30 23:59:59";
                month_2_start_date =  year + "-10-01 00:00:00";
                month_2_end_date = year + "-10-31 23:59:59";
                break;
            case "Spring":
                seasonal_start_date = year + "-03-01 00:00:00";
                seasonal_end_date = year + "-5-31 23:59:59";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-03-31 23:59:59";
                month_2_start_date =  year + "-04-01 00:00:00";
                month_2_end_date = year + "-04-30 23:59:59";
                break;
            case "Winter":
                int nextYearInt = Integer.parseInt(year) + 1;
                String nextYear = Integer.toString(nextYearInt);

                seasonal_start_date = year + "-12-01 00:00:00";
                seasonal_end_date = nextYear + "-02-28 23:59:59";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-12-31 23:59:59";
                month_2_start_date =  nextYear + "-01-01 00:00:00";
                month_2_end_date = nextYear + "-01-31 23:59:59";
                break;
            case "Summer":
                seasonal_start_date = year + "-06-01 00:00:00";
                seasonal_end_date = year + "-08-31 23:59:59";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-06-30 23:59:59";
                month_2_start_date =  year + "-07-01 00:00:00";
                month_2_end_date = year + "-07-31 23:59:59";
                break;
            default:
                seasonal_start_date = null;
                seasonal_end_date = null;
                month_1_start_date = null;
                month_1_end_date = null;
                month_2_start_date = null;
                month_2_end_date = null;
                Log.fatal("Incorrect season entered: " + season);
                System.exit(0);
        }

        Log.info("Inserting new seasonal and monthly contests into database...");
        try{
            sql_seasonal = "INSERT INTO contest " +
                    "(id, type, year, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'seasonal', '" + year + "', '" + season + "'," +
                    " '" + seasonal_start_date + "', '" + seasonal_end_date + "', 1);";
            statement = connection.createStatement();
            resultSet_seasonal = statement.executeUpdate(sql_seasonal);
            Log.info("Successfully added seasonal contest. Rows added: " + resultSet_seasonal);

            sql_monthly_1 = "INSERT INTO contest " +
                    "(id, type, year, month, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'monthly', '" + year + "', '"+ "'1'," + season + "'," +
                    " '" + month_1_start_date + "', '" + month_1_end_date + "', 1);";
            statement = connection.createStatement();
            resultSet_monthly_1 = statement.executeUpdate(sql_monthly_1);
            Log.info("Successfully added month 1 contest. Rows added: " + resultSet_monthly_1);

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

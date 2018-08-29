package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

public class ContestOperations {

    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());

    public void addContest(Connection conn, String year, String season) {

        String sql_seasonal;
        String sql_monthly_1;
        String sql_monthly_2;
        String seasonal_start_date;
        String seasonal_end_date;
        String month_1_start_date;
        String month_1_end_date;
        String month_2_start_date;
        String month_2_end_date;


        Log.debug("Determining start and end dates...");
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
                //Increasing year by 1
                int nextYearInt = Integer.parseInt(year) + 1;
                String nextYear = Integer.toString(nextYearInt);

                //Set days in February to 29 for leap years
                String februaryDays = "28";
                if (Integer.parseInt(year) % 4 == 0){
                    februaryDays = "29";
                }

                seasonal_start_date = year + "-12-01 00:00:00";
                seasonal_end_date = nextYear + "-02-" + februaryDays + " 23:59:59";
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

        sql_seasonal = "INSERT INTO contest " +
                    "(id, type, year, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'seasonal', '" + year + "', '" + season + "'," +
                    " '" + seasonal_start_date + "', '" + seasonal_end_date + "', 1);";

        Log.debug("Adding seasonal contest...");
        ExecuteQuery eq1 = new ExecuteQuery(conn, sql_seasonal);
        eq1.cleanUp();
        Log.info("Successfully added " + season + " " + year + " contest");

        sql_monthly_1 = "INSERT INTO contest " +
                    "(id, type, year, month, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'monthly', '" + year + "', '1', '" + season + "'," +
                    " '" + month_1_start_date + "', '" + month_1_end_date + "', 1);";

        Log.debug("Adding month 1 contest");
        ExecuteQuery eq2 = new ExecuteQuery(conn, sql_monthly_1);
        eq2.cleanUp();
        Log.info("Successfully added month 1 contest");

        sql_monthly_2 = "INSERT INTO contest " +
                    "(id, type, year, month, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'monthly', '" + year + "', '2', '" + season + "'," +
                    " '" + month_2_start_date + "', '" + month_2_end_date + "', 0);";

        Log.debug("Adding month 2 contest");
        ExecuteQuery eq3 = new ExecuteQuery(conn, sql_monthly_2);
        eq3.cleanUp();
        Log.info("Successfully added month 2 contest");
    }

    public void deactivateContest(Connection conn, String contestType) {
        int resultSet;
        String sql = "UPDATE contest SET is_active = 0 " +
                "     WHERE type = '" + contestType + "' AND is_active = 1;";

        Log.debug("Deactivating contest...");
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        resultSet = eq.getRowsAffected();
        eq.cleanUp();

        if (resultSet > 0){
            Log.info( contestType + " contest successfully deactivated");
        } else {
            Log.info("Contest deactivation: no active " + contestType +" contests found");
        }
    }

    public void activateMonth2contest (Connection conn) {

        String deactivate_month1 = "UPDATE contest SET is_active = 0 where type = 'monthly' and is_active = 1;";
        String activate_month2 = "UPDATE contest c1 " +
                                 "JOIN contest c2 ON c1.year = c2.year " +
                                 "AND c1.season = c2.season " +
                                 "SET c1.is_active = 1 " +
                                 "WHERE c2.type = 'seasonal' " +
                                 "AND c2.is_active = 1 " +
                                 "AND c1.month = 2;";

        Log.debug("Deactivating month 1 contest...");
        ExecuteQuery eq1 = new ExecuteQuery(conn, deactivate_month1);
        eq1.cleanUp();
        Log.info("Month 1 contest successfully deactivated");

        Log.debug("Activating month 2 contest");
        ExecuteQuery eq2 = new ExecuteQuery(conn, activate_month2);
        eq2.cleanUp();
        Log.info("Month 2 contest successfully activated");
    }
}

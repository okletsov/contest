package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

public class ContestOperations {

    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());

    public void addContest(Connection conn, String year, String season) {

        DatabaseOperations dbOp = new DatabaseOperations();

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
        dbOp.updateDatabase(conn, sql_seasonal);
        Log.info("Successfully added " + season + " " + year + " contest");

        sql_monthly_1 = "INSERT INTO contest " +
                    "(id, type, year, month, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'monthly', '" + year + "', '1', '" + season + "'," +
                    " '" + month_1_start_date + "', '" + month_1_end_date + "', 1);";
        Log.debug("Adding month 1 contest");
        dbOp.updateDatabase(conn, sql_monthly_1);
        Log.info("Successfully added month 1 contest");

        sql_monthly_2 = "INSERT INTO contest " +
                    "(id, type, year, month, season, start_date, end_date, is_active)" +
                    " VALUES" +
                    " (UUID(), 'monthly', '" + year + "', '2', '" + season + "'," +
                    " '" + month_2_start_date + "', '" + month_2_end_date + "', 0);";
        Log.debug("Adding month 2 contest");
        dbOp.updateDatabase(conn, sql_monthly_2);
        Log.info("Successfully added month 2 contest");
    }

    public void deactivateContest(Connection conn, String contestType) {
        DatabaseOperations dbOp = new DatabaseOperations();
        int resultSet;
        String sql = "UPDATE contest SET is_active = 0 " +
                "     WHERE type = '" + contestType + "' AND is_active = 1;";
        Log.debug("Deactivating contest...");
        resultSet = dbOp.updateDatabase(conn, sql);
        if (resultSet > 0){
            Log.info( contestType + " contest successfully deactivated");
        } else {
            Log.info("Contest deactivation: no active " + contestType +" contests found");
        }
    }

    public void activateMonth2contest (Connection conn) {

        DatabaseOperations dbOp = new DatabaseOperations();

        String deactivate_month1 = "UPDATE contest SET is_active = 0 where type = 'monthly' and is_active = 1;";
        String activate_month2 = "UPDATE contest c1 " +
                            "SET c1.is_active = 1 " +
                            "WHERE c1.type = 'monthly' " +
                            "AND c1.is_active = 0 " +
                            "AND c1.year = (SELECT c2.year from contest c2 " +
                                            "WHERE c2.type = 'seasonal' " +
                                            "AND c2.is_active = 1);";

        Log.debug("Deactivating month 1 contest...");
        dbOp.updateDatabase(conn, deactivate_month1);
        Log.info("Month 1 contest successfully deactivated");

        Log.debug("Activating month 2 contest");
        dbOp.updateDatabase(conn, activate_month2);
        Log.info("Month 2 contest successfully activated");
    }
}

package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContestOperations {

    private Connection conn;

    public ContestOperations(Connection conn) {
        this.conn = conn;
    }

    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());

    public void addContest(String year, String season) {
        /*
            This method will check if seasonal contest for a given year and season already exist:
            if YES - display a message
            if NO - proceed with method execution

            Method does the following:
             - deactivates any existing seasonal and monthly contests
             - inserts seasonal contest and two monthly contests
             - seasonal and Month 1 contests inserted in active state
         */

        String sql_find_contest = "select id from contest " +
                                  "where year = " + year +
                                  " and season = '" + season +
                                  "' and type = 'seasonal';";
        DatabaseOperations dbOp = new DatabaseOperations();
        String existingContestID =  dbOp.getSingleValue(conn,"id", sql_find_contest);

        //Checking if contest already exist
        if (existingContestID == null) {

            String sql_seasonal;
            String sql_monthly_1;
            String sql_monthly_2;
            String seasonal_start_date;
            String seasonal_end_date;
            String month_1_start_date;
            String month_1_end_date;
            String month_2_start_date;
            String month_2_end_date;

            //Deactivating all seasonal and monthly contests (if any)
            deactivateContest("seasonal");
            deactivateContest("monthly");

            //Determining start and end dates depending on contest season
            Log.debug("Determining start and end dates...");
            switch (season) {
                case "Autumn":
                    seasonal_start_date = year + "-09-01 00:00:00";
                    seasonal_end_date = year + "-11-30 23:59:59";
                    month_1_start_date = seasonal_start_date;
                    month_1_end_date = year + "-09-30 23:59:59";
                    month_2_start_date = year + "-10-01 00:00:00";
                    month_2_end_date = year + "-10-31 23:59:59";
                    break;
                case "Spring":
                    seasonal_start_date = year + "-03-01 00:00:00";
                    seasonal_end_date = year + "-5-31 23:59:59";
                    month_1_start_date = seasonal_start_date;
                    month_1_end_date = year + "-03-31 23:59:59";
                    month_2_start_date = year + "-04-01 00:00:00";
                    month_2_end_date = year + "-04-30 23:59:59";
                    break;
                case "Winter":
                    //Increasing year by 1
                    int nextYearInt = Integer.parseInt(year) + 1;
                    String nextYear = Integer.toString(nextYearInt);

                    //Set days in February to 29 for leap years
                    String februaryDays = "28";
                    if (Integer.parseInt(year) % 4 == 0) {
                        februaryDays = "29";
                    }

                    seasonal_start_date = year + "-12-01 00:00:00";
                    seasonal_end_date = nextYear + "-02-" + februaryDays + " 23:59:59";
                    month_1_start_date = seasonal_start_date;
                    month_1_end_date = year + "-12-31 23:59:59";
                    month_2_start_date = nextYear + "-01-01 00:00:00";
                    month_2_end_date = nextYear + "-01-31 23:59:59";
                    break;
                case "Summer":
                    seasonal_start_date = year + "-06-01 00:00:00";
                    seasonal_end_date = year + "-08-31 23:59:59";
                    month_1_start_date = seasonal_start_date;
                    month_1_end_date = year + "-06-30 23:59:59";
                    month_2_start_date = year + "-07-01 00:00:00";
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
        } else {
            Log.error("Adding contest: " + year + " " + season +
                         " contest already exist in database with id " + existingContestID);
        }
    }

    public void deactivateContest(String contestType) {
        int resultSet;
        String sql = "UPDATE contest SET is_active = 0 " +
                     "WHERE type = '" + contestType + "' AND is_active = 1;";

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

    public void activateMonth2contest() {

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

    public String getActiveSeasonalContestID() {

        Log.debug("Getting active seasonal contest ID...");
        String sql = "select * from contest where is_active = 1 and type = 'seasonal';";
        DatabaseOperations dbOp = new DatabaseOperations();
        String contestID = dbOp.getSingleValue(conn,"id", sql);

        if (contestID != null) {Log.debug("Successfully got active seasonal contest ID"); }
        else { Log.debug("There are no active seasonal contests in database"); }

        return contestID;
    }
}

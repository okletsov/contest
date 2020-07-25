package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ContestOperations {

    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());
    private final Connection conn;

    public ContestOperations(Connection conn) {
        this.conn = conn;
    }

    public void addContest(int year, String season) {
        /*
            Method does the following:
             - inserts seasonal contest and two monthly contests
             - seasonal and Month 1 contests inserted in active state
         */

        Contest c = new Contest(conn);
        String annContestId;
        String seasContestId;

        PreparedStatement sql = null;
        String seasonal_start_date;
        String seasonal_end_date;
        String month_1_start_date;
        String month_1_end_date;
        String month_2_start_date;
        String month_2_end_date;

        //Determining start and end dates depending on contest season
        Log.trace("Determining start and end dates...");

        //Set days in February to 29 for leap years
        String februaryDays = "28";
        if (year % 4 == 0) {
            februaryDays = "29";
        }

        switch (season) {
            case "Autumn":
                seasonal_start_date = year + "-08-31 21:00:01";
                seasonal_end_date = year + "-11-30 22:00:00";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-09-30 20:59:59";
                month_2_start_date = year + "-09-30 21:00:00";
                month_2_end_date = year + "-10-31 21:59:59";
                break;
            case "Spring":
                seasonal_start_date = year + "-02-" + februaryDays + " 22:00:01";
                seasonal_end_date = year + "-05-31 21:00:00";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-03-31 20:59:59";
                month_2_start_date = year + "-03-31 21:00:00";
                month_2_end_date = year + "-04-30 20:59:59";
                break;
            case "Winter":
                //Increasing year by 1
                int nextYearInt = year + 1;
                String nextYear = Integer.toString(nextYearInt);

                seasonal_start_date = year + "-11-30 22:00:01";
                seasonal_end_date = nextYear + "-02-" + februaryDays + " 22:00:00";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-12-31 21:59:59";
                month_2_start_date = year + "-12-31 22:00:00";
                month_2_end_date = nextYear + "-01-31 21:59:59";
                break;
            case "Summer":
                seasonal_start_date = year + "-05-31 21:00:01";
                seasonal_end_date = year + "-08-31 21:00:00";
                month_1_start_date = seasonal_start_date;
                month_1_end_date = year + "-06-30 20:59:59";
                month_2_start_date = year + "-06-30 21:00:00";
                month_2_end_date = year + "-07-31 20:59:59";
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

        try {
            DateTimeOperations dtOp = new DateTimeOperations();

            sql = conn.prepareStatement("INSERT INTO contest \n" +
                    "(id, type, year, month, season, start_date, end_date, is_active, date_created, entrance_fee)\n" +
                    "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?);");

            Log.debug("Adding seasonal contest...");
            sql.setString(1, "seasonal");
            sql.setInt(2, year);
            sql.setString(3, null);
            sql.setString(4, season);
            sql.setString(5, seasonal_start_date);
            sql.setString(6, seasonal_end_date);
            sql.setInt(7, 1);
            sql.setString(8, dtOp.getTimestamp());
            sql.setInt(9, 250);
            sql.executeUpdate();
            Log.info("Successfully added " + season + " " + year + " contest");

            Log.debug("Adding month 1 contest...");
            sql.setString(1, "monthly");
            sql.setInt(2, year);
            sql.setString(3, "1");
            sql.setString(4, season);
            sql.setString(5, month_1_start_date);
            sql.setString(6, month_1_end_date);
            sql.setInt(7, 1);
            sql.setString(8, dtOp.getTimestamp());
            sql.setInt(9, 0);
            sql.executeUpdate();
            Log.info("Successfully added month 1 contest");

            Log.debug("Adding month 2 contest...");
            sql.setString(1, "monthly");
            sql.setInt(2, year);
            sql.setString(3, "2");
            sql.setString(4, season);
            sql.setString(5, month_2_start_date);
            sql.setString(6, month_2_end_date);
            sql.setInt(7, 0);
            sql.setString(8, dtOp.getTimestamp());
            sql.setInt(9, 0);
            sql.executeUpdate();
            Log.info("Successfully added month 2 contest");

            if (season.equals("Autumn")) {

//                Insert new annual contest (in active state)

                Log.debug("Adding annual contest...");

                sql.setString(1, "annual");
                sql.setInt(2, year);
                sql.setString(3, null);
                sql.setString(4, null);
                sql.setString(5, seasonal_start_date);
                sql.setString(6, seasonal_end_date);
                sql.setInt(7, 1);
                sql.setString(8, dtOp.getTimestamp());
                sql.setInt(9, 0);

                sql.executeUpdate();

//                  Add relationship between seasonal and annual contest

                annContestId = c.getAnnContestIdByYear(year);
                seasContestId = c.getSeasContestIdByYearAndSeason(year, season);
                addAnnXSeasRelationship(annContestId, seasContestId);

                Log.info("Successfully added " + year + " annual contest");

            } else if (season.equals("Winter") || season.equals("Spring")) {
//                Update end date for annual contest
//                Add relationship between seasonal and annual contests

                annContestId = c.getAnnContestIdByYear(year);
                seasContestId = c.getSeasContestIdByYearAndSeason(year, season);

                addAnnXSeasRelationship(annContestId, seasContestId);
                updateEndDate(annContestId, seasonal_end_date);
            }

            sql.close();

        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            Log.fatal("Failing sql statement: " + sql);
            System.exit(0);
        }
    }

    public void deactivateContestByType(String contestType) {
        Log.debug("Deactivating contest...");
        int resultSet;
        String sql = "UPDATE contest SET is_active = 0 " +
                     "WHERE type = '" + contestType + "' AND is_active = 1;";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        resultSet = eq.getRowsAffected();
        eq.cleanUp();

        if (resultSet > 0){ Log.info( contestType + " contest successfully deactivated"); }
        else { Log.info("Contest deactivation: no active " + contestType +" contests found"); }
    }

    public void deactivateContest(String contestId) {

        String sql = "update `main`.`contest`\n" +
                "set `is_active` = '0'\n" +
                "where (`id` = '" + contestId + "');";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
    }

    public void activateMonth2contest() {
        /*
            This method does two things:
                - deactivates any month 1 active contests
                - activates month 2 contest that belong to currently active seasonal contest
         */

        Log.debug("Deactivating month 1 contest...");
        String deactivate_month1 = "UPDATE contest SET is_active = 0 where type = 'monthly' and is_active = 1;";
        ExecuteQuery eq1 = new ExecuteQuery(conn, deactivate_month1);
        eq1.cleanUp();
        Log.info("Month 1 contest successfully deactivated");

        Log.debug("Activating month 2 contest");
        String activate_month2 = "UPDATE contest c1 " +
                "JOIN contest c2 ON c1.year = c2.year " +
                "AND c1.season = c2.season " +
                "SET c1.is_active = 1 " +
                "WHERE c2.type = 'seasonal' " +
                "AND c2.is_active = 1 " +
                "AND c1.month = 2;";
        ExecuteQuery eq2 = new ExecuteQuery(conn, activate_month2);
        eq2.cleanUp();
        Log.info("Month 2 contest successfully activated");
    }

    private void addAnnXSeasRelationship(String annContestId, String seasContestId) {

        PreparedStatement sql = null;

        try {
            sql = conn.prepareStatement(
                    "INSERT INTO `main`.`annual_x_seasonal_contest` (`id`, `annual_contest_id`, `seasonal_contest_id`) " +
                            "VALUES (uuid(), ?, ?);"
            );

            sql.setString(1, annContestId);
            sql.setString(2, seasContestId);

            sql.executeUpdate();
            sql.close();

            Log.info("Added relationship between annual and seasonal contests");

        } catch (SQLException ex) {
            Log.error("SQLException: " + ex.getMessage());
            Log.error("SQLState: " + ex.getSQLState());
            Log.error("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            Log.error("Failing sql statement: " + sql);
        }
    }

    private void updateEndDate(String contestId, String endDate) {

        Contest c = new Contest(conn);

        String sql = "UPDATE `main`.`contest` " +
                "SET `end_date` = '" + endDate + "' WHERE (`id` = '" + contestId + "');";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        eq.cleanUp();
    }

    public String getActiveSeasonalContestID() {

        Log.trace("Getting active seasonal contest ID...");
        String sql = "select id from contest where is_active = 1 and type = 'seasonal';";
        DatabaseOperations dbOp = new DatabaseOperations();
        String contestID = dbOp.getSingleValue(conn,"id", sql);

        if (contestID != null) {Log.trace("Successfully got active seasonal contest ID"); }
        else { Log.trace("There are no active seasonal contests in database"); }

        return contestID;
    }

}

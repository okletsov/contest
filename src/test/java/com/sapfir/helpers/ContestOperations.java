package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class ContestOperations {

    private static final Logger Log = LogManager.getLogger(ContestOperations.class.getName());
    private final Connection conn;

    public ContestOperations(Connection conn) {
        this.conn = conn;
    }

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

            PreparedStatement sql = null;
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
            Log.trace("Determining start and end dates...");

            //Set days in February to 29 for leap years
            String februaryDays = "28";
            if (Integer.parseInt(year) % 4 == 0) {
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
                    int nextYearInt = Integer.parseInt(year) + 1;
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
                        "(id, type, year, month, season, start_date, end_date, is_active, date_created)\n" +
                        "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?);");

                Log.debug("Adding seasonal contest...");
                sql.setString(1, "seasonal");
                sql.setString(2, year);
                sql.setString(3, null);
                sql.setString(4, season);
                sql.setString(5, seasonal_start_date);
                sql.setString(6, seasonal_end_date);
                sql.setInt(7, 1);
                sql.setString(8, dtOp.getTimestamp());
                sql.executeUpdate();
                Log.info("Successfully added " + season + " " + year + " contest");

                Log.debug("Adding month 1 contest...");
                sql.setString(1, "monthly");
                sql.setString(2, year);
                sql.setString(3, "1");
                sql.setString(4, season);
                sql.setString(5, month_1_start_date);
                sql.setString(6, month_1_end_date);
                sql.setInt(7, 1);
                sql.setString(8, dtOp.getTimestamp());
                sql.executeUpdate();
                Log.info("Successfully added month 1 contest");

                Log.debug("Adding month 2 contest...");
                sql.setString(1, "monthly");
                sql.setString(2, year);
                sql.setString(3, "2");
                sql.setString(4, season);
                sql.setString(5, month_2_start_date);
                sql.setString(6, month_2_end_date);
                sql.setInt(7, 0);
                sql.setString(8, dtOp.getTimestamp());
                sql.executeUpdate();
                Log.info("Successfully added month 2 contest");

                sql.close();

            } catch (SQLException ex) {
                Log.fatal("SQLException: " + ex.getMessage());
                Log.fatal("SQLState: " + ex.getSQLState());
                Log.fatal("VendorError: " + ex.getErrorCode());
                Log.trace("Stack trace: ", ex);
                Log.fatal("Failing sql statement: " + sql);
                System.exit(0);
            }

        } else {
            Log.error("Adding contest: " + year + " " + season +
                         " contest already exist in database with id " + existingContestID);
        }
    }

    public void deactivateContest(String contestType) {
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

    public void writeGeneralContestResults(List<HashMap<String,Object>> results) {

        PreparedStatement sql = null;
        int lastValidPlace = 0;

        for (int i = 0; i < results.size(); i++) {

            String nickname = results.get(i).get("nickname").toString();

            Log.info("Writing general contest results for " + nickname);

//            Step 1: getting data to insert from the result set

            String userId = results.get(i).get("user_id").toString();
            String contestId = results.get(i).get("contest_id").toString();
            int place = Integer.parseInt(results.get(i).get("place").toString());
            int finalBetsCount = Integer.parseInt(results.get(i).get("final_bets_count").toString());
            int origBetsCount = Integer.parseInt(results.get(i).get("orig_bets_count").toString());
            int activeDays = Integer.parseInt(results.get(i).get("active_days").toString());
            BigDecimal won = new BigDecimal(results.get(i).get("won").toString());
            BigDecimal lost = new BigDecimal(results.get(i).get("lost").toString());
            BigDecimal units = new BigDecimal(results.get(i).get("units").toString());
            BigDecimal roi = new BigDecimal(results.get(i).get("roi").toString());

            if (place == 0) {
                    place = lastValidPlace + 1;
            } else {
                lastValidPlace = place;
            }

//            Step 2: find annual points corresponding to a place
            int annualPoints = getAnnualPointsByPlace(place);

//            Step 3: generate and execute update statement

            try {
                sql = conn.prepareStatement(
                        "INSERT INTO `main`.`cr_general` (`id`, `user_id`, `contest_id`, `annual_points`, `nickname`, `place`, `final_bets_count`, `orig_bets_count`, `won`, `lost`, `units`, `roi`, `active_days`) \n" +
                        "VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );

                sql.setString(1, userId);
                sql.setString(2, contestId);
                sql.setInt(3, annualPoints);
                sql.setString(4, nickname);
                sql.setInt(5, place);
                sql.setInt(6, finalBetsCount);
                sql.setInt(7, origBetsCount);
                sql.setBigDecimal(8, won);
                sql.setBigDecimal(9, lost);
                sql.setBigDecimal(10, units);
                sql.setBigDecimal(11, roi);
                sql.setInt(12, activeDays);

                sql.executeUpdate();
                sql.close();

                Log.info("Done");

            }catch (SQLException ex) {
                Log.error("SQLException: " + ex.getMessage());
                Log.error("SQLState: " + ex.getSQLState());
                Log.error("VendorError: " + ex.getErrorCode());
                Log.trace("Stack trace: ", ex);
                Log.error("Failing sql statement: " + sql);
            }
        }

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

    private int getAnnualPointsByPlace(int place) {
        if (place == 1) {
            return 10;
        } else if (place == 2) {
            return 8;
        } else if (place == 3) {
            return 6;
        } else if (place == 4) {
            return 5;
        } else if (place == 5) {
            return 4;
        } else if (place == 6) {
            return 3;
        } else if (place == 7) {
            return 2;
        } else if (place == 8) {
            return 1;
        } else {
            return 0;
        }
    }

    public List<HashMap<String,Object>> getGeneralContestResults(String contestId) {

        DatabaseOperations dbOp = new DatabaseOperations();

        String seasResultSql = "select \n" +
                "\t(\n" +
                "\t\tcase \n" +
                "\t\t\twhen t5.active_days >=30 then (row_number() over (order by \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tcase when t5.active_days >= 30 then 0 else 1 end\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t, t5.units desc\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t)\n" +
                "\t\t\t\t\t\t\t\t\t\t)\n" +
                "\t\telse 0 end\t\t\n" +
                "\t) as place\n" +
                "\t, t5.nickname\n" +
                "\t, t5.user_id\n" +
                "\t, t5.seasonal_contest_id as contest_id\n" +
                "\t, t5.final_bets_count\n" +
                "\t, t5.orig_bets_count\n" +
                "\t, t5.active_days\n" +
                "\t, t5.won\n" +
                "\t, t5.lost\n" +
                "\t, t5.units\n" +
                "\t, t5.roi\n" +
                "from (\n" +
                "\tselect \n" +
                "\t\tt4.nickname\n" +
                "\t\t, t4.user_id\n" +
                "\t\t, t4.seasonal_contest_id\n" +
                "\t\t, (case when t4.bets < 100 then 100 else t4.bets end) as final_bets_count\n" +
                "\t\t, t4.bets as orig_bets_count\n" +
                "\t\t, t4.active_days\n" +
                "\t\t, t4.won\n" +
                "\t\t, (case when t4.bets < 100 then (t4.lost + (100 - t4.bets)) else t4.lost end) as lost\n" +
                "\t\t, (case when t4.bets < 100 then (t4.units - (100 - t4.bets)) else t4.units end) as units\n" +
                "\t\t, cast((case when t4.bets < 100 then (t4.units - (100 - t4.bets)) else t4.units end) as decimal(5,2)) as roi\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt3.nickname\n" +
                "\t\t\t, t3.user_id\n" +
                "\t\t\t, t3.seasonal_contest_id\n" +
                "\t\t\t, sum(case when t3.result = 'not-played' then 0 else 1 end) as bets\n" +
                "\t\t\t, count(distinct date(t3.kiev_date_predicted)) as active_days\n" +
                "\t\t\t, cast(sum(\n" +
                "\t\t\t\tcase \n" +
                "\t\t\t\t\twhen t3.count_lost = 1 then '0'\n" +
                "\t\t\t\t\twhen t3.count_void = 1 then '1'\n" +
                "\t\t\t\t\twhen t3.result = 'void' then '1'\n" +
                "\t\t\t\t\twhen t3.result = 'won' then t3.user_pick_value\n" +
                "\t\t\t\t\twhen t3.result = 'void-won' then t3.user_pick_value\t\t\t\n" +
                "\t\t\t\tend \n" +
                "\t\t\t) as decimal(5,2)) as won\n" +
                "\t\t\t, sum(case when t3.unit_outcome < 0 then t3.unit_outcome end) * -1 as lost\n" +
                "\t\t\t, cast(sum(t3.unit_outcome) as decimal(5,2)) as units\n" +
                "\t\t\t, cast((sum(t3.unit_outcome) / sum(case when t3.result = 'not-played' then 0 else 1 end)) * 100 as decimal(5,2)) as roi\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\tun.nickname \n" +
                "\t\t\t\t, p3.user_id \n" +
                "\t\t\t\t, p3.seasonal_contest_id \n" +
                "\t\t\t\t, p3.seasonal_validity_status as seas_st\n" +
                "\t\t\t\t, p3.seasonal_validity_status_overruled as seas_st_over\n" +
                "\t\t\t\t, p3.monthly_validity_status as mon_st\n" +
                "\t\t\t\t, p3.monthly_validity_status_overruled as mon_st_over\n" +
                "\t\t\t\t, vs.count_lost \n" +
                "\t\t\t\t, vs.count_void \n" +
                "\t\t\t\t, convert_tz(t2.date_predicted, 'UTC', 'Europe/Kiev') as kiev_date_predicted\n" +
                "\t\t\t\t, p3.user_pick_value \n" +
                "\t\t\t\t, p3.`result`\n" +
                "\t\t\t\t, (\n" +
                "\t\t\t\t\tcase \n" +
                "\t\t\t\t\t\twhen vs.count_lost = 1 then '-1'\n" +
                "\t\t\t\t\t\twhen vs.count_void = 1 then '0'\n" +
                "\t\t\t\t\t\telse p3.unit_outcome \n" +
                "\t\t\t\t\tend\n" +
                "\t\t\t\t) as unit_outcome\n" +
                "\t\t\tfrom (\n" +
                "\t\t\t\tselect \n" +
                "\t\t\t\t\tt1.id\n" +
                "\t\t\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t\t\t, t1.date_predicted\n" +
                "\t\t\t\tfrom (\n" +
                "\t\t\t\t\tselect \n" +
                "\t\t\t\t\t\tp.id \n" +
                "\t\t\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t\t\t, p.date_predicted \n" +
                "\t\t\t\t\tfrom prediction p\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tselect \n" +
                "\t\t\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t\t\t, psc.previous_date_scheduled \n" +
                "\t\t\t\t\t\t, p2.date_predicted \n" +
                "\t\t\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\t\t\tjoin prediction p2 on p2.id = psc.prediction_id \n" +
                "\t\t\t\t\t) t1 -- finding all date_scheduled, including postponed\n" +
                "\t\t\t\twhere 1=1\n" +
                "\t\t\t\tgroup by \n" +
                "\t\t\t\t\tt1.id\n" +
                "\t\t\t\t\t, t1.date_predicted\n" +
                "\t\t\t\t) t2 -- finding initial date_scheduled per prediction\n" +
                "\t\t\t\tjoin prediction p3 on p3.id = t2.id\n" +
                "\t\t\t\tjoin user u on u.id = p3.user_id \n" +
                "\t\t\t\tjoin user_nickname un on un.user_id = u.id \n" +
                "\t\t\t\tjoin validity_statuses vs on vs.status = p3.seasonal_validity_status  \n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand un.is_active = 1\n" +
                "\t\t\t\tand p3.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\t \t\tand vs.count_in_contest = 1\n" +
                "\t\t\torder by \n" +
                "\t\t\t\tun.nickname \n" +
                "\t\t\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t\t\t, t2.date_predicted asc\n" +
                "\t\t\t) t3 -- all predictions that count in contest with correct unit_outcome based on status \n" +
                "\t\tgroup by \n" +
                "\t\t\tt3.nickname\n" +
                "\t\t\t, t3.user_id\n" +
                "\t\t\t, t3.seasonal_contest_id\n" +
                "\t\t) t4 -- calculating raw contest result measures\n" +
                "\t) t5 -- applying rules for user who did no make 100 predictions\n" +
                "; -- applying rules for users with less than 30 active days";

        return dbOp.getListOfHashMaps(conn, seasResultSql);

    }

}

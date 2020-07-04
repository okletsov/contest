package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class ContestResultsOperations {

    private static final Logger Log = LogManager.getLogger(ContestResultsOperations.class.getName());
    private final Connection conn;

    public ContestResultsOperations(Connection conn) {
        this.conn = conn;
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
            ContestResults cr = new ContestResults(conn);
            int annualPoints = cr.getAnnualPointsByPlace(place);

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

            } catch (SQLException ex) {
                Log.error("SQLException: " + ex.getMessage());
                Log.error("SQLState: " + ex.getSQLState());
                Log.error("VendorError: " + ex.getErrorCode());
                Log.trace("Stack trace: ", ex);
                Log.error("Failing sql statement: " + sql);
            }
        }

    }

    public void writeContestResultsWinningStrick(List<HashMap<String,Object>> results) {

        PreparedStatement sql = null;

        for (int i = 0; i < results.size(); i++) {

            String nickname = results.get(i).get("nickname").toString();

            Log.info("Writing winning strick results for " + nickname);

//            Step 1: getting data to insert from the result set

            String userId = results.get(i).get("user_id").toString();
            String contestId = results.get(i).get("contest_id").toString();
            BigDecimal strickAvgOdds = new BigDecimal(results.get(i).get("strick_avg_odds").toString());
            int strickLength = Integer.parseInt(results.get(i).get("strick_length").toString());

//            Step 2: generate and execute update statement

            try {
                sql = conn.prepareStatement(
                        "INSERT INTO `main`.`cr_winning_strick` (`id`, `user_id`, `contest_id`, `nickname`, `strick_length`, `strick_avg_odds`) \n" +
                                "VALUES (uuid(), ?, ?, ?, ?, ?);"
                );

                sql.setString(1, userId);
                sql.setString(2, contestId);
                sql.setString(3, nickname);
                sql.setInt(4, strickLength);
                sql.setBigDecimal(5, strickAvgOdds);

                sql.executeUpdate();
                sql.close();

                Log.info("Done");

            } catch (SQLException ex) {
                Log.error("SQLException: " + ex.getMessage());
                Log.error("SQLState: " + ex.getSQLState());
                Log.error("VendorError: " + ex.getErrorCode());
                Log.trace("Stack trace: ", ex);
                Log.error("Failing sql statement: " + sql);
            }
        }
    }

    public void writeContestResultsBiggestOdds(List<HashMap<String,Object>> results) {

        PreparedStatement sql = null;

        for (int i = 0; i < results.size(); i++) {

            String nickname = results.get(i).get("nickname").toString();

            Log.info("Writing biggest odds for " + nickname);

//            Step 1: getting data to insert from the result set

            String userId = results.get(i).get("user_id").toString();
            String contestId = results.get(i).get("contest_id").toString();
            BigDecimal userPickValue = new BigDecimal(results.get(i).get("user_pick_value").toString());

//            Step 2: generate and execute update statement

            try {
                sql = conn.prepareStatement(
                        "INSERT INTO `main`.`cr_biggest_odds` (`id`, `user_id`, `contest_id`, `nickname`, `user_pick_value`) \n" +
                                "VALUES (uuid(), ?, ?, ?, ?);"
                );

                sql.setString(1, userId);
                sql.setString(2, contestId);
                sql.setString(3, nickname);
                sql.setBigDecimal(4, userPickValue);

                sql.executeUpdate();
                sql.close();

                Log.info("Done");

            } catch (SQLException ex) {
                Log.error("SQLException: " + ex.getMessage());
                Log.error("SQLState: " + ex.getSQLState());
                Log.error("VendorError: " + ex.getErrorCode());
                Log.trace("Stack trace: ", ex);
                Log.error("Failing sql statement: " + sql);
            }
        }
    }

}

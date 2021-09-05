package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class ContestFinanceOperations {

    private static final Logger Log = LogManager.getLogger(ContestFinanceOperations.class.getName());
    private final Connection conn;

    public ContestFinanceOperations(Connection conn) {
        this.conn = conn;
    }

    public void writeContestPlacementAwards(List<HashMap<String,Object>> results) {

        PreparedStatement sql = null;

        for (int i = 0; i < results.size(); i++) {

            String nickname = results.get(i).get("nickname").toString();

            Log.info("Writing place award for " + nickname);

//            Step 1: getting data to insert from the result set

            String userId = results.get(i).get("user_id").toString();
            String contestId = results.get(i).get("contest_id").toString();
            int place = Integer.parseInt(results.get(i).get("place").toString());

            ContestFinance cf = new ContestFinance(conn, contestId);

            int financeActionId = cf.getFinanceActionId(place, contestId);
            BigDecimal actionValue = cf.getFinanceActionValue(place, contestId);

//            Step 2: generate and execute update statement

            try {
                sql = conn.prepareStatement(
                        "INSERT INTO `main`.`cr_finance` (`id`, `user_id`, `contest_id`, `finance_action_id`, `action_value`) " +
                                "VALUES (uuid(), ?, ?, ?, ?);"
                );

                sql.setString(1, userId);
                sql.setString(2, contestId);
                sql.setInt(3, financeActionId);
                sql.setBigDecimal(4, actionValue);

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

    public void writeContestBiggestOddsAwards(List<HashMap<String,Object>> results) {

        PreparedStatement sql = null;

        for (int i = 0; i < results.size(); i++) {

            String nickname = results.get(i).get("nickname").toString();

            Log.info("Writing biggest odds award for " + nickname);

//            Step 1: getting data to insert from the result set

            String userId = results.get(i).get("user_id").toString();
            String contestId = results.get(i).get("contest_id").toString();
            int financeActionId = 8;

            ContestFinance cf = new ContestFinance(conn, contestId);
            BigDecimal winners = BigDecimal.valueOf(results.size());
            BigDecimal actionValue = cf.getBiggestOddsAward().divide(winners, 2, RoundingMode.HALF_UP);

//            Step 2: generate and execute update statement

            try {
                sql = conn.prepareStatement(
                        "INSERT INTO `main`.`cr_finance` (`id`, `user_id`, `contest_id`, `finance_action_id`, `action_value`) " +
                                "VALUES (uuid(), ?, ?, ?, ?);"
                );

                sql.setString(1, userId);
                sql.setString(2, contestId);
                sql.setInt(3, financeActionId);
                sql.setBigDecimal(4, actionValue);

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

    public void writeContestWinningStrickAwards(List<HashMap<String,Object>> results) {

        PreparedStatement sql = null;

        for (int i = 0; i < results.size(); i++) {

            String nickname = results.get(i).get("nickname").toString();

            Log.info("Writing winning strick award for " + nickname);

//            Step 1: getting data to insert from the result set

            String userId = results.get(i).get("user_id").toString();
            String contestId = results.get(i).get("contest_id").toString();
            int financeActionId = 7;

            ContestFinance cf = new ContestFinance(conn, contestId);
            BigDecimal actionValue = cf.getWinningStrickAward();

//            Step 2: generate and execute update statement

            try {
                sql = conn.prepareStatement(
                        "INSERT INTO `main`.`cr_finance` (`id`, `user_id`, `contest_id`, `finance_action_id`, `action_value`) " +
                                "VALUES (uuid(), ?, ?, ?, ?);"
                );

                sql.setString(1, userId);
                sql.setString(2, contestId);
                sql.setInt(3, financeActionId);
                sql.setBigDecimal(4, actionValue);

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

    public void addEntranceFee(String contestId, String username) {

        PreparedStatement sql = null;

//        Step 1: get data to insert

        UserOperations uo = new UserOperations(conn);
        String userId = uo.getUserID(username);

        Contest c = new Contest(conn, contestId);
        BigDecimal entranceFee = c.getEntranceFee();
        BigDecimal entranceFeeToInsert = entranceFee.multiply(BigDecimal.valueOf(-1));

//        Step 2: generate and execute insert statement

        try {
            sql = conn.prepareStatement(
                    "INSERT INTO `main`.`cr_finance` (`id`, `user_id`, `contest_id`, `finance_action_id`, `action_value`) " +
                            "VALUES (uuid(), ?, ?, ?, ?);"
            );

            sql.setString(1, userId);
            sql.setString(2, contestId);
            sql.setInt(3, 12);
            sql.setBigDecimal(4, entranceFeeToInsert);

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

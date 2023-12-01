package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;

public class UserOperations {

    private final Connection conn;

    public UserOperations(Connection conn) {
        this.conn = conn;
    }

    private static final Logger Log = LogManager.getLogger(UserOperations.class.getName());

    public String getUserID(String username) {
        Log.trace("Getting userID for " + username);
        String userID;
        String sql = "select u.id from user u " +
                "left join user_nickname un on u.id = un.user_id " +
                "where u.username = '" + username + "' " +
                "or un.nickname = '" + username + "';";
        DatabaseOperations dbOp = new DatabaseOperations();
        userID = dbOp.getSingleValue(conn,"id", sql);

        if (userID != null) { Log.trace("Successfully got user ID for " + username); }
        else { Log.trace("User ID for " + username + " not found"); }

        return userID;
    }

    public void addUser(String nickname, String targetUser) {
        /*
        Notes:
            - to add brand new user to database just pass "New Participant" value for targetUser parameter
            - to add nickname for an existing user pass username of existing user for targetUser parameter

        How method works:
            Check if nickname already exist:
                if CAN find - do nothing and display a message
                if CANNOT find - check if brand new user needs to be added:
                    if YES - add to both 'user' and 'user_nickname' tables
                    if NO - add only to 'user_nickname' table
     */
        String userId = getUserID(nickname);
        if (userId == null) {
            if (targetUser.equals("New Participant")) {
                addToUserTable(nickname);
                addToUserNicknameTable(nickname, nickname);
            } else {
                addToUserNicknameTable(nickname, targetUser);
            }
        } else {
            Log.error("Adding user: user '" + nickname + "' already exist in database with id " + userId);
        }
    }

    private void addToUserTable(String username){
        Log.debug("Adding new participant " + username + " to 'user' table");
        String addUserSql = "insert into user (id, username) values (uuid(), '" + username + "');";
        ExecuteQuery eq = new ExecuteQuery(conn, addUserSql);
        eq.cleanUp();
        Log.info("Successfully added new participant " + username + " to 'user' table");
    }

    private void addToUserNicknameTable (String nickname, String targetUser){
        Log.debug("Adding nickaname '" + nickname + "' to 'user_nickname' table for '" + targetUser + "' user");
        deactivateNickname(targetUser);
        String userId = getUserID(targetUser);
        String addNicknameSql = "insert into user_nickname (id, user_id, nickname, is_active) " +
                "values (uuid(), '" + userId + "', '" + nickname + "', 1);";
        ExecuteQuery eq = new ExecuteQuery(conn, addNicknameSql);
        eq.cleanUp();
        Log.info("Successfully added " + nickname + " to 'user_nickname' table for '" + targetUser + "' user");
    }

    private void deactivateNickname(String targetUser){
        Log.debug("Deactivating nicknames for user '" + targetUser + "'");

        String userID = getUserID(targetUser);
        String sql = "update user_nickname set is_active = 0 where is_active = 1 and user_id = '" + userID + "';";
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        int resultSet = eq.getRowsAffected();
        eq.cleanUp();

        if (resultSet > 0){ Log.debug("Nicknames deactivated successfully"); }
        else { Log.debug("No active nicknames found for " + targetUser);}
    }

    public void inspectParticipants(ArrayList<String> participants) {
         /*
            Get active seasonal contest ID:
                if CANNOT find - display an error message
                if CAN find - proceed with method execution:
                  Get userID:
                    if CAN find:
                        - insert participation ID (if it doesn't exist) into `user_seasonal_contest_participation` table
                    if CANNOT find
                        - increase counter
                        - display an error message that user does not exist in database
                        - (optional) add user to database
                        - (optional) add participation ID to `user_seasonal_contest_participation` table
                        - (optional) add entrance fee to `cr_finance` table
             */

        Log.info("Inspecting participants...");
        ContestOperations co = new ContestOperations(conn);
        ContestFinanceOperations cfo = new ContestFinanceOperations(conn);
        String contestID = co.getActiveSeasonalContestID();
        if (contestID != null) {
            int counter = 0;
            for (String username : participants) {
                String userID = getUserID(username);
                if (userID != null) {
                    addParticipationID(contestID, username);
                } else {
                    counter = counter + 1;
                    Log.warn("User " + username + " does not exist in database");
                addUser(username, "Zizu");
                addParticipationID(contestID, username);
                cfo.addEntranceFee(contestID, username);
                }
            }
            if (counter == 0) { Log.info("Inspection complete: all participants exist and linked to contest"); }
            else { Log.info("Inspection complete: see messages above"); }
        } else { Log.error("There are no active seasonal contests in database"); }
    }

    private String getParticipationID(String contestID, String username) {
        Log.debug("Getting participatoin ID for " + username + "...");
        String userID = getUserID(username);
        String participationID;
        String sqlParticipationID = "select id from user_seasonal_contest_participation " +
                "where user_id = '" + userID + "' " +
                "and contest_id = '" + contestID + "';";
        DatabaseOperations dbOP = new DatabaseOperations();
        participationID = dbOP.getSingleValue(conn,"id", sqlParticipationID);
        Log.debug("Successfully got participation ID for " + username);
        return participationID;
    }

    private void addParticipationID(String contestID, String username) {
        /*
            This method inserts a record (if it does not exist) into 'user_seasonal_contest_participation' table
            That table indicates seasonal contests user participated in
         */
        Log.debug("Adding Participation ID and entrance fee for " + username + "...");
        String participationID = getParticipationID(contestID, username);
        if (participationID == null) {
            ContestFinanceOperations cfo = new ContestFinanceOperations(conn);
            cfo.addEntranceFee(contestID, username);
            String userID = getUserID(username);
            String sqlParticipant_x_Contest = "insert into user_seasonal_contest_participation" +
                    "(id, user_id, contest_id) " +
                    "values (UUID(), '" + userID + "', '" + contestID + "');";
            ExecuteQuery eq = new ExecuteQuery(conn, sqlParticipant_x_Contest);
            eq.cleanUp();
            Log.info("Successfully added Participation ID for " + username);
        } else {
            Log.debug("User " + username + " is already participating in current contest");
        }

    }
}
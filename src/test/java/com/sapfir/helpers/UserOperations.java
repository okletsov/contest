package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserOperations {

    private Connection conn;

    public UserOperations(Connection conn) {
        this.conn = conn;
    }

    private static final Logger Log = LogManager.getLogger(UserOperations.class.getName());

    public String getUserID(String username) {
        String userID = null;
        String sql = "select u.id from user u " +
                "left join user_nickname un on u.id = un.user_id " +
                "where u.username = '" + username + "' " +
                "or un.nickname = '" + username + "';";

        Log.debug("Getting userID for " + username);
        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet resultSet = eq.getSelectResult();

        try {
            while (resultSet.next()) {
                userID = resultSet.getString("id");
                Log.debug("Successfully got userID for " + username);
            }
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }

        if (userID == null) {
            Log.info("User ID for " + username + " not found");
        }
        eq.cleanUp();
        return userID;
    }

    public void addUser(String nickname, String targetUser) {
        /*
        Notes:
            - to add brand new user to database just pass "New Participant" value for targetUser parameter
            - to add nickname for an existing user pass username of existing user for targetUser parameter

        How method works:
            Check if nickname already exist:
                if CAN find - do nothing and dusplay a message
                if CANNOT find - check if brand new user needs to be added:
                    if YES - add to both 'user' and 'user_nickname' tables
                    if NO - add only to 'user_nickname' table
     */
        String userId = getUserID(nickname);
        if (userId == null) {
            if (targetUser.equals("New Participant")) {
                addToUserTable(nickname);
                addToUserNicknameTable(nickname, targetUser);
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
        ExecuteQuery eq1 = new ExecuteQuery(conn, addUserSql);
        eq1.cleanUp();
        Log.info("Successfully added new participant " + username + " to 'user' table");
    }

    private void addToUserNicknameTable (String nickname, String targetUser){
        Log.debug("Adding nickaname '" + nickname + "' to 'user_nickname' table for '" + targetUser + "' user");
        String userId = getUserID(targetUser);
        String addNicknameSql = "insert into user_nickname (id, user_id, nickname) " +
                "values (uuid(), '" + userId + "', '" + nickname + "');";

        ExecuteQuery eq2 = new ExecuteQuery(conn, addNicknameSql);
        eq2.cleanUp();
        Log.info("Successfully added " + nickname + " to 'user_nickname' table for '" + targetUser + "' user");
    }

    public void inspectParticipants(ArrayList<String> participants) {
         /*
            Get active seasonal contest ID:
                if CANNOT find - display an error message
                if CAN find - proceed with method execution:
                  Get userID:
                    if CAN find: check if userID and contestID pair exist in `user_seasonal_contest_participation` table:
                            if CAN find - do nothing (debug message logged)
                            if CANNOT find - insert participation ID into `user_seasonal_contest_participation` table
                    if CANNOT find
                        - increase counter
                        - display an error message that user does not exist in database
                        - (optional) add user to database
                        - (optional) add participation ID to `user_seasonal_contest_participation` table
             */

        Log.info("Inspecting participants...");
        ContestOperations co = new ContestOperations(conn);
        String contestID = co.getActiveSeasonalContestID();
        if (contestID != null) {
            int counter = 0;
            for (String username : participants) {
                String participationID;
                String userID = getUserID(username);
                if (userID != null) {
                    participationID = getParticipationID(contestID, username);
                    if (participationID != null) {
                        Log.debug("User " + username + " is already participating in current contest");
                    } else { addParticipationID(contestID, username); }
                } else {
                    counter = counter + 1;
                    Log.warn("User " + username + " does not exist in database");
//                addUser(username, "New Participant");
//                addParticipationID(contestID, username);
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
        participationID = dbOP.getSingleValue("id", sqlParticipationID);
        Log.debug("Successfully got participation ID for " + username);
        return participationID;
    }

    private void addParticipationID(String contestID, String username) {
        String userID = getUserID(username);
        Log.debug("Adding Participation ID for " + username + "...");
        String sqlParticipant_x_Contest = "insert into user_seasonal_contest_participation" +
                "(id, user_id, contest_id) " +
                "values (UUID(), '" + userID + "', '" + contestID + "');";
        ExecuteQuery eq = new ExecuteQuery(conn, sqlParticipant_x_Contest);
        eq.cleanUp();
        Log.info("Added Participation ID for " + username);
    }
}
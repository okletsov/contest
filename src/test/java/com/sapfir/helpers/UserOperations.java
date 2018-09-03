package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserOperations {

    private Connection conn;

    public UserOperations(Connection conn){
        this.conn = conn;
    }

    private static final Logger Log = LogManager.getLogger(UserOperations.class.getName());

    public String getUserID(String username){
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

        if (userID == null){
            Log.info("User ID for " + username + " not found");
        }
        eq.cleanUp();
        return userID;
    }

    /*
        To add new user to database just pass "New Participant" value for targetUser parameter
        To add nickname for an existing user pass username of existing user for targetUser parameter
     */
    public void addUser(String nickname, String targetUser){
        String userId = getUserID(nickname);

        /*  Check if user already exist in database:
            - if YES - display a message and do nothing
            - if NO - proceed with method execution
         */
        if (userId == null) {

            /*  Check if brand new user needs to be added:
                - if YES - add it to both 'user' and 'user_nickname' tables
                - if NO - just get userID for a user we are trying to add nickname for*/
            if (targetUser.equals("New Participant")) {

                String addUserSql = "insert into user (id, username) values (uuid(), '" + nickname + "');";

                Log.debug("Adding new participant " + nickname + " to 'user' table");
                ExecuteQuery eq1 = new ExecuteQuery(conn, addUserSql);
                eq1.cleanUp();
                Log.info("Successfully added new participant " + nickname + " to 'user' table");

                userId = getUserID(nickname);
            } else {
                userId = getUserID(targetUser);
            }

            String addNicknameSql = "insert into user_nickname (id, user_id, nickname) " +
                    "values (uuid(), '" + userId + "', '" + nickname + "');";

            Log.debug("Adding " + nickname + " to 'user_nickname' table");
            ExecuteQuery eq2 = new ExecuteQuery(conn, addNicknameSql);
            eq2.cleanUp();
            Log.info("Successfully added " + nickname + " to 'user_nickname' table");
        } else {
            Log.error("Adding user: user '" + nickname + "' already exist in database with id " + userId);
        }
    }

    public void inspectParticipants(ArrayList <String> participants){

        Log.info("Inspecting participants...");
        ContestOperations co = new ContestOperations(conn);
        String contestID = co.getActiveSeasonalContestID();

        if (contestID != null){
            /*
                  Get userID
                    if CAN find:
                        - check if given userID and contestID pair exist in `user_seasonal_contest_participation` table (new method):
                            if YES - do nothing (maybe display debug message)
                            if NO - implement insertion of given userID into `user_seasonal_contest_participation` table (new method)
                    if CANNOT find
                        - increase counter
                        - display an error message that user does not exist in database
             */
            int counter = 0;
            for (String username : participants) {
                String participationID = null;
                String userID = getUserID(username);
                if (userID != null){
                    String sqlParticipationID = "select id from user_seasonal_contest_participation " +
                            "where user_id = '" + userID + "' " +
                            "and contest_id = '" + contestID + "';";
                    ExecuteQuery eq1 = new ExecuteQuery(conn, sqlParticipationID);
                    ResultSet rs = eq1.getSelectResult();
                    try {
                        while (rs.next()) {
                            participationID = rs.getString("id");
                            Log.debug("Successfully got participationID for " + username);
                        }
                    } catch (SQLException ex) {
                        Log.fatal("SQLException: " + ex.getMessage());
                        Log.fatal("SQLState: " + ex.getSQLState());
                        Log.fatal("VendorError: " + ex.getErrorCode());
                        Log.trace("Stack trace: ", ex);
                        System.exit(0);
                    }
                    eq1.cleanUp();
                    if (participationID != null){
                        Log.debug("User " + username + " is already participating in current contest");
                    } else {
                        Log.debug("Inserting user_x_contest link for " + username + "...");
                        String sqlParticipant_x_Contest = "insert into user_seasonal_contest_participation" +
                                "(id, user_id, contest_id) " +
                                "values (UUID(), '" + userID + "', '" + contestID + "');";
                        ExecuteQuery eq2 = new ExecuteQuery(conn, sqlParticipant_x_Contest);
                        eq2.cleanUp();
                        Log.info("Inserted user_x_contest link for " + username);
                    }
                } else {
                    counter = counter + 1;
                    Log.warn("User " + username + " does not exist in database");
//                addUser(username, "New Participant");
                }
            }
            if (counter == 0) {Log.info("Inspection complete: all participants exist and linked to contest");}
            else { Log.info("Inspection complete"); }
        } else {
            Log.error("There are no active seasonal contests in database");
        }
    }
}

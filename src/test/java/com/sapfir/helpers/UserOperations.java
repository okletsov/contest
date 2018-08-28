package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserOperations {

    private static final Logger Log = LogManager.getLogger(UserOperations.class.getName());

    public String getUserID(Connection conn, String username){
        String userID = null;
        String sql = "SELECT id FROM user WHERE username = '" + username + "';";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet resultSet = eq.getSelectResult();

        try {
            while (resultSet.next()) {
                userID = resultSet.getString("id");
            }
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }

        if (userID == null){
            Log.error("Unable to get userID for " + username);
            System.exit(0);
        }
        eq.cleanUp();
        return userID;
    }

    // To add new user to database just pass "New Participant" value for targetUser parameter
    public void addNickname(Connection conn, String nickname, String targetUser){
        String userId;
        String addNicknameSql;
        String addUserSql;

        if (targetUser.equals("New Participant")){
            addUserSql = "insert into user (id, username) values (uuid(), '" + nickname + "');";

            ExecuteQuery eq1 = new ExecuteQuery(conn, addUserSql);
            eq1.cleanUp();

            userId = getUserID(conn, nickname);
        } else {
            userId = getUserID(conn, targetUser);
        }

        addNicknameSql = "insert into user_nickname (id, user_id, nickname) " +
                "values (uuid(), '" + userId + "', '" + nickname + "');";

        ExecuteQuery eq2 = new ExecuteQuery(conn, addNicknameSql);
        eq2.cleanUp();
    }
}

package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserOperations {

    private static final Logger Log = LogManager.getLogger(UserOperations.class.getName());
    private DatabaseOperations dbOp = new DatabaseOperations();

    public String getUserID(Connection conn, String username){
        String userID = null;
        String sql = "SELECT id FROM user WHERE username = '" + username + "';";
        ResultSet resultSet = dbOp.selectFromDatabase(conn, sql);


        try {
            while (resultSet.next()) {
                System.out.println("Success 2");
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
        return userID;
    }

    public void addNickname(Connection conn, String nickname, String targetUser){
        String userId;
        String addNicknameSql;
        String addUserSql;

        if (targetUser.equals("")){
            addUserSql = "insert into user (id, username) values (uuid(), '" + nickname + "');";
            dbOp.updateDatabase(conn, addUserSql);
            System.out.println("Success 1");
            userId = getUserID(conn, nickname);
        } else {
            userId = getUserID(conn, targetUser);
        }

        addNicknameSql = "insert into user_nickname (id, user_id, nickname) " +
                "values (uuid(), '" + userId + "', '" + nickname + "');";
        dbOp.updateDatabase(conn, addNicknameSql);
    }

    public void addUsers(Connection conn, ArrayList<String> usernames){

        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            String sql = "";
        }
    }
}

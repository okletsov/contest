package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;

public class UserOperations {

    private static final Logger Log = LogManager.getLogger(UserOperations.class.getName());

    public void addUsers(Connection conn, ArrayList<String> usernames){

        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            String sql = "";
        }
    }
}

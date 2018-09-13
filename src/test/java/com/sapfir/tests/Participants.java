package com.sapfir.tests;

import com.sapfir.helpers.DatabaseOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;

import java.sql.Connection;
import java.util.ArrayList;

public class Participants {

    private static final Logger Log = LogManager.getLogger(Participants.class.getName());

    @DataProvider(name = "participants")
    public static Object[][] getData(){
        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

        String sql = "SELECT un.nickname FROM user_nickname un " +
                     "JOIN user u ON u.id = un.user_id " +
                     "JOIN user_seasonal_contest_participation uscp ON uscp.user_id = u.id " +
                     "JOIN contest c ON c.id = uscp.contest_id " +
                     "WHERE un.is_active = 1 " +
                     "AND c.is_active = 1;";
        ArrayList<String> participants = dbOp.getArray(conn, "nickname", sql);

        Object[][] testData = new Object[1] [participants.size()];
        for (int i = 0; i < testData.length; i++) {
            for (int j = 0; j < testData[i].length; j++) {
                testData[i][j] = participants.get(j);
            }
        }

        //        for (int i = 1; i < participants.size(); i++) {
//            testData[i][1] = participants.get(i);
//        }

        dbOp.closeConnection(conn);
        return testData;


//        return new Object[][]{
//                {"Cap4ik"},
//                {"Deagle"}
//        };
    }
}

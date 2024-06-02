package com.sapfir.helpers;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Workshop {
    public static void main(String[] args) {

        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

//        DateTimeOperations dtOp = new DateTimeOperations();
        PredictionOperations predOp = new PredictionOperations(conn);
//        Contest contest = new Contest(conn, "c4ae5d83-d84f-11ee-a16d-0d0b0e289ea2");

        int index = predOp.getPredictionIndexInContest("6770166303", "c4ae5d83-d84f-11ee-a16d-0d0b0e289ea2");
        int remaining = predOp.getRemainingPredictionsCount("6770166303", "c4ae5d83-d84f-11ee-a16d-0d0b0e289ea2");

        System.out.println("stop here");

        dbOp.closeConnection(conn);
    }
}

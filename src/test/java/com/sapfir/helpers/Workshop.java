package com.sapfir.helpers;


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

        DateTimeOperations dtOp = new DateTimeOperations();
        PredictionOperations predOp = new PredictionOperations(conn);
        Contest contest = new Contest(conn, "2deb734e-ce85-11e8-8022-74852a015562");

        LocalDateTime predictionTime = dtOp.convertToDateTimeFromString("2018-12-01 21:59:59");

        System.out.println("stop here");

        dbOp.closeConnection(conn);
    }
}

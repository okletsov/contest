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

        DateTimeOperations dtOp = new DateTimeOperations();
        PredictionOperations predOp = new PredictionOperations(conn);
        Contest contest = new Contest(conn, "d28a83e2-0e33-11ec-ab01-288316e63e84");

        ContestResults cr = new ContestResults(conn);
        List<HashMap<String,Object>> streak = cr.getContestResultsWinningStrickToWrite("d28a83e2-0e33-11ec-ab01-288316e63e84");

        System.out.println("stop here");

        dbOp.closeConnection(conn);
    }
}

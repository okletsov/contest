package com.sapfir.helpers;


import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Workshop {
    public static void main(String[] args) {

        DateTimeOperations dtOp = new DateTimeOperations();
        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();
        Contest cont = new Contest(conn, "2deb734e-ce85-11e8-8022-74852a015562");
        PredictionValidation predVal = new PredictionValidation(conn);

        PredictionOperations predOp = new PredictionOperations(conn);
//        String dbDateTime = predOp.getDbDateScheduled("feed_item_3191037203");
//        String timeStamp = dtOp.getTimestamp();

//        String dbDateTime = "2018-11-30 21:59";
//        String timeStamp = "2018-11-30 22:00:00";
//
//        LocalDateTime convertedDateTime = dtOp.convertToDateTimeFromString(dbDateTime);
//        LocalDateTime convertedTimeStamp = dtOp.convertToDateTimeFromString(timeStamp);
//
//        System.out.println(convertedDateTime);
//        System.out.println(convertedTimeStamp);
//
//        boolean timestampAfter = convertedTimeStamp.isAfter(convertedDateTime);
//        System.out.println(timestampAfter);

//        System.out.println(cont.getMonEndDate(1));
//        System.out.println(cont.getMonEndDate24(1));
//        System.out.println(cont.getMonLastDayStart(1));

       // System.out.println(predOp.getDbOriginalDateScheduled("feed_item_3191037203"));
        //System.out.println(predVal.dateScheduledWithinSeasLimit("2018-08-31 21:00:00"));

//        System.out.println(predOp.getDbUserId("feed_item_3241285903"));
//        System.out.println(predVal.getCountValidPredictionsExclCurrent("feed_item_3088105003"));
//
//        System.out.println(cont.getSeasEndDate());
//        System.out.println(cont.getSeasEndDate24());
//        System.out.println(predVal.dateScheduledWithinSeasEndDate24(dbDateTime));

//        System.out.println(cont.getSeasLastDayStart());
//        System.out.println(cont.getSeasEndDate());
//        System.out.println(cont.getSeasEndDate24());
//        System.out.println(predVal.origDateScheduledOnLastSeasDate("2018-11-29 22:00:00"));

//        predVal.validateDateScheduled("feed_item_2954314703");
//        System.out.println(predVal.getCountValidPredictionsExclCurrent("feed_item_16123403"));

//        System.out.println(predVal.getCountPredictionsOver10ExclCurrent("feed_item_3239678003"));
//        System.out.println(predVal.isPredictionQuarterGoal("feed_item_3009903303"));

//        predVal.validateUserPickValue("feed_item_3016017903");

//        System.out.println(predVal.isVoidDueToCancellation("feed_item_1058014903"));
//        predVal.validateResult("feed_item_3014605903");

        dbOp.closeConnection(conn);
    }
}

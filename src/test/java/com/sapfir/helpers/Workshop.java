package com.sapfir.helpers;


import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Workshop {
    public static void main(String[] args) {

        DateTimeOperations dtOp = new DateTimeOperations();
        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

        PredictionOperations predOp = new PredictionOperations(conn);
//        String dbDateTime = predOp.getDbDateScheduled("feed_item_3191037203");
//        String timeStamp = dtOp.getTimestamp();

        String dbDateTime = "2018-11-30 22:00:00";
        String timeStamp = "2018-11-30 22:00:00";

        LocalDateTime convertedDateTime = dtOp.convertToDateTimeFromString(dbDateTime);
        LocalDateTime convertedTimeStamp = dtOp.convertToDateTimeFromString(timeStamp);

        System.out.println(convertedDateTime);
        System.out.println(convertedTimeStamp);

        boolean timestampAfter = convertedTimeStamp.isAfter(convertedDateTime);
        System.out.println(timestampAfter);

        System.out.println(predOp.eventPostponed("feed_item_3191037203"));


    }
}

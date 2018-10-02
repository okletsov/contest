package com.sapfir.helpers;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Workshop {
    public static void main(String[] args) {

        String unixDate = "1538131200";
        Long longDate = Long.parseLong(unixDate);
        Instant instantDateTime = Instant.ofEpochSecond(longDate);
        ZonedDateTime zonedDateTime = instantDateTime.atZone(ZoneOffset.UTC);
        String finalDateTime = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

        String date =
                Instant.ofEpochSecond(Long.parseLong(unixDate)).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

        System.out.println(instantDateTime);
        System.out.println(zonedDateTime);
        System.out.println(finalDateTime);

        System.out.println(date);


    }
}

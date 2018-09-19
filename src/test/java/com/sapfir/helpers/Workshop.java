package com.sapfir.helpers;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Workshop {
    public static void main(String[] args) {
        long unixDate = 1536073200;
        Date date = Date.from(Instant.ofEpochSecond(unixDate));
        LocalDateTime localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println(localDate);

        System.out.println(localDate.getYear());
        System.out.println(localDate.getMonthValue());
        System.out.println(localDate.getDayOfMonth());
        System.out.println(localDate.getHour());
        System.out.println(localDate.getMinute());
    }
}

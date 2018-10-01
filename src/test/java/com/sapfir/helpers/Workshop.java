package com.sapfir.helpers;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Workshop {
    public static void main(String[] args) {

        Long timestamp = Long.parseLong("1451748600");
        Date date = Date.from(Instant.ofEpochSecond(timestamp));
        LocalDateTime localDate = date.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        System.out.println(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        System.out.println(ZonedDateTime.now().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));


    }
}

package com.sapfir.helpers;

import java.util.Date;

public class Workshop {
    public static void main(String[] args) {
        long unixDate = 1536073200;

        Date date = new Date(unixDate*1000);
        System.out.println(date);
    }
}

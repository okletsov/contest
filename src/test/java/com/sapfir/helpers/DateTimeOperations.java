package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeOperations {

	private static final Logger Log = LogManager.getLogger(DateTimeOperations.class.getName());

	public String convertFromUnix(String unixTimeStamp){
		Long timestamp = Long.parseLong(unixTimeStamp);
		Date date = Date.from(Instant.ofEpochSecond(timestamp));
		LocalDateTime localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return localDate + ":00";
	}
}

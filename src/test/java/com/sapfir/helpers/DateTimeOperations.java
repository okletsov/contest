package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeOperations {

	private static final Logger Log = LogManager.getLogger(DateTimeOperations.class.getName());

	public String convertFromUnix(String unixTimeStamp){
		return Instant.ofEpochSecond(Long.parseLong(unixTimeStamp)).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
}

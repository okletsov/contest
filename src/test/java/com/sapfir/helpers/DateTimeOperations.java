package com.sapfir.helpers;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeOperations {

	private String datePattern = "yyyy-MM-dd HH:mm:ss";

	public String convertFromUnix(String unixTimeStamp){
		return Instant.ofEpochSecond(Long.parseLong(unixTimeStamp)).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(datePattern));
	}

	public String getTimestamp() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(datePattern));
	}
}

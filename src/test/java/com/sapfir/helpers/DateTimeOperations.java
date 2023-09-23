package com.sapfir.helpers;

import org.openqa.selenium.WebElement;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeOperations {

	private final String datePattern = "yyyy-MM-dd HH:mm:ss";

	public String convertFromUnix(String unixTimeStamp){
		return Instant.ofEpochSecond(Long.parseLong(unixTimeStamp)).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(datePattern));
	}

	public String getTimestamp() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(datePattern));
	}

	public String getDateTimeFromClassName(WebElement element) {
		String className = element.getAttribute("class");
		int startIndex = className.indexOf(" t") + 2;
		int endIndex = className.indexOf("-", startIndex);
		String unixDate = className.substring(startIndex, endIndex);
		return convertFromUnix(unixDate);
	}

	public LocalDateTime convertToDateTimeFromString (String stringDateTime) {
		String convertableString = stringDateTime.replace(" ", "T");
		return LocalDateTime.parse(convertableString);
	}

	public String convertToStringFromDateTime (LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return dateTime.format(formatter);
	}

	public String getUnixTimestamp() {
		long unixTimestampMillis = System.currentTimeMillis() / 1000;
		return String.valueOf(unixTimestampMillis);
	}
}

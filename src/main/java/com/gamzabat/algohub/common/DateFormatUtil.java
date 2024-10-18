package com.gamzabat.algohub.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateFormatUtil {
	public static String formatDate(LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		return date.format(formatter);
	}
}
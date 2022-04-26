package org.ssi.util;


import java.text.SimpleDateFormat;

public class DateUtil {
	
	public static final SimpleDateFormat PATTERN_YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat PATTERN_YYYYMM = new SimpleDateFormat("yyyyMM");
	public static final SimpleDateFormat PATTERN_YYYYMMDD_SLASH = new SimpleDateFormat("yyyy/MM/dd");
	public static final SimpleDateFormat PATTERN_YYYYMMDD_HHMMSS = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	public static String toString(SimpleDateFormat format) {
			String reportDate = format.format(System.currentTimeMillis());
			return reportDate;
	}
	public static String toString(long ms, SimpleDateFormat format) {
			String reportDate = format.format(ms);
			return reportDate;
	}
	public static int toInt() {
		try {
			String date = toString(PATTERN_YYYYMMDD);
			return Integer.parseInt(date);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	public static int toInt(long ms) {
		try {
			String date = toString(ms, PATTERN_YYYYMMDD);
			return Integer.parseInt(date);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public static void main (String[] args) {
		long day1 = 24l * 60l * 60l * 1000l;
		System.out.println(toInt(System.currentTimeMillis() - day1));
	}
}

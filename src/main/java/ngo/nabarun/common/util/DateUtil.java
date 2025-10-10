package ngo.nabarun.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

/**
 * Date utility helpers for common date calculations and formatting.
 *
 * <p>Provides convenience methods to obtain specific days of the current month, the last day
 * of the month, formatting/parsing with time zones, checking whether a date falls into the
 * current month, enumerating months between two dates, and adding time to a Date.
 *
 * <p>All formatting/parsing methods use java.text.SimpleDateFormat and are not thread-safe
 * when a shared formatter instance is used; callers should avoid sharing returned formatters.
 */
public class DateUtil {
	/**
	 * Return a Date representing the given day-of-month for the current month (relative to now).
	 *
	 * @param day day-of-month (1-31)
	 * @return Date with the provided day set on current month
	 */
	public static Date getDayOfCurrentMonth(int day) {
		return getDayOfCurrentMonth(day,new Date());
	}
	
	/**
	 * Return a Date representing the given day-of-month for the month of the provided date.
	 *
	 * @param day day-of-month (1-31)
	 * @param today reference date used to determine the month/year
	 * @return Date with the provided day set on the month of {@code today}
	 */
	public static Date getDayOfCurrentMonth(int day,Date today) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}

	/**
	 * Return the last day (Date) of the current month.
	 *
	 * @return Date representing the last day of the current month
	 */
	public static Date getLastDayOfCurrentMonth() {
		return getLastDayOfCurrentMonth(new Date());
	}
	
	/**
	 * Return the last day (Date) of the month containing the provided reference date.
	 *
	 * @param today reference date used to determine the month/year
	 * @return Date representing the last day of the month containing {@code today}
	 */
	public static Date getLastDayOfCurrentMonth(Date today) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
	
	/**
	 * Format a Date into a string using the provided format and time zone.
	 *
	 * @param date the Date to format (may be null)
	 * @param format the date format pattern (e.g. "yyyy-MM-dd'T'HH:mm:ss")
	 * @param timeZone time zone ID (e.g. "UTC"); if null or empty the system default zone is used
	 * @return formatted date string or null if {@code date} is null
	 */
	public static String formatDateToString(Date date, String format, String timeZone) {
		if (date == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		if (timeZone == null || "".equalsIgnoreCase(timeZone.trim())) {
			timeZone = Calendar.getInstance().getTimeZone().getID();
		}
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		return sdf.format(date);
	}
	
	/**
	 * Check if the provided date falls in the same calendar month as now.
	 *
	 * @param givenDate date to check
	 * @return true if {@code givenDate} is in the current month
	 */
	public static boolean isCurrentMonth(Date givenDate) {
		return isCurrentMonth(givenDate,new Date());
	}

	/**
	 * Check if the provided date falls in the same calendar month as the reference date.
	 *
	 * @param givenDate date to check
	 * @param today reference date
	 * @return true if {@code givenDate} and {@code today} are in the same month and year
	 */
	public static boolean isCurrentMonth(Date givenDate,Date today) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTime(givenDate);
		cal2.setTime(today);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

	/**
	 * Return a list of formatted month strings between two dates (inclusive of start, exclusive of end).
	 *
	 * @param startDate inclusive start
	 * @param endDate exclusive end
	 * @param format date format for each month string
	 * @return list of formatted month strings
	 */
	public static List<String> getMonthsBetween(Date startDate, Date endDate, String format) {
		List<String> list = new ArrayList<String>();
		Calendar beginCalendar = Calendar.getInstance();
		Calendar finishCalendar = Calendar.getInstance();
		beginCalendar.setTime(startDate);
		finishCalendar.setTime(endDate);
		DateFormat formaterYd = new SimpleDateFormat(format);
		while (beginCalendar.before(finishCalendar)) {
			list.add(formaterYd.format(beginCalendar.getTime()));
			beginCalendar.add(Calendar.MONTH, 1);
		}
		return list;
	}

	/**
	 * Convenience overload using default format "MMMM yyyy".
	 *
	 * @param startDate inclusive start
	 * @param endDate exclusive end
	 * @return list of formatted month strings
	 */
	public static List<String> getMonthsBetween(Date startDate, Date endDate) {

		return getMonthsBetween(startDate, endDate, "MMMM yyyy");
	}

	/**
	 * Parse a date string using the provided pattern. Returns null on parse failure or if input
	 * is null/blank.
	 *
	 * @param dateStr input date string
	 * @param format parsing pattern
	 * @return parsed Date or null on failure
	 */
	public static Date getFormattedDate(String dateStr, String format) {
		if (dateStr == null || StringUtils.isBlank(dateStr)) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		try {
			return formaterYd.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Format a Date to a string using the provided pattern. Returns null if date is null.
	 *
	 * @param date date to format
	 * @param format pattern
	 * @return formatted string or null
	 */
	public static String getFormattedDateString(Date date, String format) {
		if (date == null) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		return formaterYd.format(date);
	}

	/**
	 * Convenience formatting with default pattern "MMMM yyyy".
	 *
	 * @param date date to format
	 * @return formatted string or null
	 */
	public static String getFormattedDateString(Date date) {
		return getFormattedDateString(date, "MMMM yyyy");
	}

	/**
	 * Add a number of days to a date. Internally implemented as seconds addition.
	 *
	 * @param date base date (may be null)
	 * @param days number of days to add (may be negative)
	 * @return new Date with days added, or null if input date was null
	 */
	public static Date addDaysToDate(Date date, int days) {
		return addSecondsToDate(date, days * 86400); // One day to 86400 seconds
	}

	/**
	 * Add seconds to a date.
	 *
	 * @param date base date (may be null)
	 * @param seconds seconds to add (may be negative)
	 * @return new Date with seconds added, or null if input date was null
	 */
	public static Date addSecondsToDate(Date date, int seconds) {
		if (date == null) {
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}
}
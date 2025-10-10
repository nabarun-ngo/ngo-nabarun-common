package ngo.nabarun.common.util;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilTest {

    @Test
    public void testGetDayOfCurrentMonth() {
        Date d = DateUtil.getDayOfCurrentMonth(5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetLastDayOfCurrentMonth() {
        Date d = DateUtil.getLastDayOfCurrentMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int last = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        assertEquals(last, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testFormatAndParse() {
        Date now = new Date(0); // epoch
        String s = DateUtil.formatDateToString(now, "yyyy-MM-dd'T'HH:mm:ss'Z'", "UTC");
        assertNotNull(s);
        Date parsed = DateUtil.getFormattedDate(s, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        assertNotNull(parsed);
    }

    @Test
    public void testMonthsBetween() {
        Date start = DateUtil.getFormattedDate("2025-01-01", "yyyy-MM-dd");
        Date end = DateUtil.getFormattedDate("2025-04-01", "yyyy-MM-dd");
        List<String> months = DateUtil.getMonthsBetween(start, end, "MMMM yyyy");
        assertEquals(3, months.size());
        assertTrue(months.get(0).toLowerCase().contains("january"));
    }

    @Test
    public void testAddDays() {
        Date base = DateUtil.getFormattedDate("2025-10-10", "yyyy-MM-dd");
        Date plus3 = DateUtil.addDaysToDate(base, 3);
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DATE, 3);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), DateUtil.getDayOfCurrentMonth(cal.get(Calendar.DAY_OF_MONTH), plus3).getDate());
    }
}

package ngo.nabarun.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionUtilTest {

    @Test
    public void testGetExceptionDetailsNull() {
        String out = ExceptionUtil.getExceptionDetails(null);
        assertNotNull(out);
        assertTrue(out.contains("No exception provided") || out.contains("No exception"));
    }

    @Test
    public void testGetExceptionDetailsContents() {
        IllegalStateException cause = new IllegalStateException("inner cause");
        RuntimeException ex = new RuntimeException("boom", cause);

        String details = ExceptionUtil.getExceptionDetails(ex);
        assertNotNull(details);
        assertTrue(details.contains("RuntimeException"));
        assertTrue(details.contains("IllegalStateException"));
        assertTrue(details.contains("boom") || details.contains("inner cause"));
        assertTrue(details.contains("Stack Trace") || details.contains("at "));
    }
}
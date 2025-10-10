package ngo.nabarun.common.util;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility helpers for working with exceptions.
 *
 * <p>This class provides formatting helpers that produce a readable, multi-line string
 * representation of an exception including its type, message, cause chain and full stack
 * trace. The output is intended for logging or debugging purposes.
 */
public class ExceptionUtil {

    /**
     * Produce a human-friendly, multi-line representation of the provided throwable.
     *
     * <p>The returned string contains a header, the exception type and message, an ordered
     * listing of causes (if present), and the full stack trace. If {@code ex} is null a
     * short message is returned instead.
     *
     * @param ex the throwable to format (may be null)
     * @return formatted exception details suitable for logging
     */
    public static String getExceptionDetails(Throwable ex) {
        if (ex == null) {
            return "⚠️ No exception provided.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n==============================\n");
        sb.append("🚨 Exception Details\n");
        sb.append("==============================\n");

        // Basic Info
        sb.append("Type: ").append(ex.getClass().getName()).append("\n");
        sb.append("Message: ").append(ex.getMessage() != null ? ex.getMessage() : "(no message)").append("\n");

        // Root cause chain
        Throwable cause = ex.getCause();
        if (cause != null) {
            sb.append("\n🔁 Cause Chain:\n");
            int level = 1;
            while (cause != null) {
                sb.append("   ").append(level++).append(". ")
                  .append(cause.getClass().getName()).append(": ")
                  .append(cause.getMessage() != null ? cause.getMessage() : "(no message)")
                  .append("\n");
                cause = cause.getCause();
            }
        }

        // Stack Trace
        sb.append("\n🧩 Stack Trace:\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        sb.append(sw.toString());

        sb.append("==============================\n");

        return sb.toString();
    }
}
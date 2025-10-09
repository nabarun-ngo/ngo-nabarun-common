package ngo.nabarun.common.util;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

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

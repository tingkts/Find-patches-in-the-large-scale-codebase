package tiger.patch.finder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private static final String DEFAULT_TAG = "tag";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

    private static boolean sIsDebug = true;
    private static boolean sHasError = false;

    private static ByteArrayOutputStream mErrorLogCache = new ByteArrayOutputStream();
    private static PrintStream mErrorLogPrintStream = new PrintStream(mErrorLogCache);

    public static boolean isDebug() {
        return sIsDebug;
    }

    public static void setDebug(boolean isDubug) {
        sIsDebug = isDubug;
    }

    public static boolean hasError() {
        return sHasError;
    }

    public static void d(String message) {
        d(DEFAULT_TAG, message, null);
    }

    public static void d(String tag, String message) {
        d(tag, message, null);
    }

    public static void d(String message, Exception e) {
        d(DEFAULT_TAG, message, e);
    }

    public static void d(String tag, String message, Exception e) {
        print(false, tag, message, System.out, e);
    }

    public static void e(String message) {
        e(DEFAULT_TAG, message, null);
    }

    public static void e(String tag, String message) {
        e(tag, message, null);
    }

    public static void e(String message, Exception e) {
        e(DEFAULT_TAG, message, e);
    }

    public static void e(String tag, String message, Exception e) {
        print(true, tag, message, System.out, e);
    }

    public static void dumpErrorLog() {
        System.out.print(mErrorLogCache.toString());
    }

    private static void print(boolean isError, String tag, String message, PrintStream stream, Exception e) {
        if (sIsDebug || isError) {
            String log = String.format("%s 0 %s %s [%s] %s", DATE_FORMAT.format(new Date()), Thread.currentThread().getId(), isError ? "E" : "D", tag, message);
            stream.println(log);
            if (isError) {
                mErrorLogPrintStream.println(log);
            }
            if (e != null) {
                e.printStackTrace(stream);
                if (isError) {
                    e.printStackTrace(mErrorLogPrintStream);
                }
            }
        }
        if (isError && sHasError != isError) {
            sHasError = isError;
        }
    }

}

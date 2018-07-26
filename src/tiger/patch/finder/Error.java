package tiger.patch.finder;

public class Error {

    public static final int SEVERITY_NONE = 0;
    public static final int SEVERITY_LOW = 1;
    public static final int SEVERITY_HIGH = 2;

    public final Object cause;
    public final int severity;
    public final String message;

    private Error(Object cause, int severity, String message) {
        this.cause = cause;
        this.severity = severity;
        this.message = message;
    }

    @Override
    public int hashCode() {
        return severity + message.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Error) {
            Error other = (Error) object;
            return severity == other.severity && message.equals(other.message);
        }
        return false;
    }

    public static Error obtainError(Object cause, int severity, String message) {
        // TODO: Cache all the errors for ErrorPage
        return new Error(cause, severity, message);
    }

}

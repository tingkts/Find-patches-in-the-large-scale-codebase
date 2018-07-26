package tiger.patch.finder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Artifact {

    protected String mName;
    protected int mSeverity = Error.SEVERITY_NONE;
    protected Set<Error> mErrorSet = new HashSet<Error>();

    public Artifact(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public int getSeverity() {
        return mSeverity;
    }

    public Collection<Error> getErrors() {
        return mErrorSet;
    }

    public Error addError(int severity, String message) {
        if (mSeverity < severity) {
            mSeverity = severity;
        }
        Error error = Error.obtainError(this, severity, message);
        mErrorSet.add(error);
        return error;
    }

}

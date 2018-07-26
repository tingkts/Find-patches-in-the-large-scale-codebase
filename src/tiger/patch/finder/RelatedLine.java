package tiger.patch.finder;

public class RelatedLine {

    private SourceLine mSourceLine;
    private boolean mIsInPatch;

    public RelatedLine(SourceLine sourceLine, boolean isInPatch) {
        mSourceLine = sourceLine;
        mIsInPatch = isInPatch;
    }

    public boolean isInPatch() {
        return mIsInPatch;
    }

    public String getSourceFile() {
        return mSourceLine.getSourceFile();
    }

    public int getIndex() {
        return mSourceLine.getIndex();
    }

    public String getContent() {
        return mSourceLine.getContent();
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject instanceof RelatedLine) {
            return mSourceLine == ((RelatedLine) anotherObject).mSourceLine;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mSourceLine.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s %s", mIsInPatch ? "M" : " ", mSourceLine);
    }

}

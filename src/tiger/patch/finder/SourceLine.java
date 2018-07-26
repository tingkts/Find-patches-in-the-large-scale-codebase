package tiger.patch.finder;

public class SourceLine {

    private String mSourceFile;
    private int mIndex;
    private String mContent;

    public SourceLine(String sourceFile, int index, String content) {
        mSourceFile = sourceFile;
        mIndex = index;
        mContent = content;
    }

    public String getSourceFile() {
        return mSourceFile;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getContent() {
        return mContent;
    }

    @Override
    public String toString() {
        return String.format("Line %5s: %s", mIndex, mContent);
    }

}

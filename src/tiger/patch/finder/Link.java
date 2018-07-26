package tiger.patch.finder;

public class Link {

    private final String mName;
    private final String mHref;
    private final int mHash;

    public Link(String name, String href) {
        mName = name.trim();
        mHref = href.trim();
        mHash = mName.hashCode();
    }

    public String getName() {
        return mName;
    }

    public String getHref() {
        return mHref;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject instanceof Link) {
            return mHash == ((Link) anotherObject).mHash;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public String toString() {
        return mName + "-->" + mHref;
    }

    public String toHtmlString(String attributes) {
        if (attributes == null) {
            attributes = "";
        }
        return String.format("<a href='%s' %s>%s</a>", mHref.replaceAll("\\'", "\\&\\#39\\;").replaceAll("\\\"", "\\&quot\\;"), attributes, mName);
    }

}

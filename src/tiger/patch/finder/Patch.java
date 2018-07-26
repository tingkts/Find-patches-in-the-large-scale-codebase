package tiger.patch.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Patch extends Artifact {

    public static final int STRING_LEVEL_LITE = 1;
    public static final int STRING_LEVEL_NORMAL = 2;
    public static final int STRING_LEVEL_FULL = 3;

    private int mHash;
    private int mSectionCount = 0;
    private String mCategory;
    private String mAuthor;
    private String mDate;
    private Module mModule;
    private String mTitle;
    private Set<Subject> mSubjectSet = new HashSet<Subject>();
    private StringBuilder mDescription = new StringBuilder();
    private List<Link> mLinkList = new ArrayList<Link>();
    private Map<String, ModifiedFile> mModifiedFileMap = new HashMap<String, ModifiedFile>();

    public Patch(String category, String author, String date, Module module, String title) {
        super(title);
        mCategory = category;
        mAuthor = author;
        mDate = date;
        mTitle = title;
        mModule = module;
        mHash = getHash(category, author, date, module.toString(), title);
        module.addPatch(this);
    }

    public void addSubject(Subject subject) {
        mSubjectSet.add(subject);
        subject.addPatch(this);
        mModule.addSubject(subject);
    }

    public void addDescription(String description) {
        mDescription.append(description);
        mDescription.append("\n");
    }

    public void addLink(Link link) {
        if (!mLinkList.contains(link)) {
            mLinkList.add(link);
        }
    }

    public void addRelatedLine(String file, RelatedLine line) {
        if (!mModifiedFileMap.containsKey(file)) {
            mModifiedFileMap.put(file, new ModifiedFile(file));
        }
        List<RelatedLine> relatedLineList = mModifiedFileMap.get(file).getRelatedLineList();
        if (!relatedLineList.contains(line)) {
            relatedLineList.add(line);
        }
    }

    public void increaseSectionCount() {
        mSectionCount++;
    }

    public int getSectionCount() {
        return mSectionCount;
    }

    public String getCategoty() {
        return mCategory;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDate() {
        return mDate;
    }

    public Module getModule() {
        return mModule;
    }

    public String getTitle() {
        return mTitle;
    }

    public Collection<Subject> getSubjects() {
        return mSubjectSet;
    }

    public String getDescription() {
        return mDescription.toString();
    }

    public Collection<Link> getLinks() {
        return mLinkList;
    }

    public Collection<ModifiedFile> getModifiedFiles() {
        return mModifiedFileMap.values();
    }

    public String getId() {
        return Integer.toHexString(mHash);
    }

    public void addError(int severity, String message, String file, int index) {
        Error error = addError(severity, message);
        if (file != null) {
            if (!mModifiedFileMap.containsKey(file)) {
                mModifiedFileMap.put(file, new ModifiedFile(file));
            }
            mModifiedFileMap.get(file).addError(index, error);
        }
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject instanceof Patch) {
            return hashCode() == anotherObject.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public String toString() {
        return toString(STRING_LEVEL_NORMAL);
    }

    public String toString(int level) {
        StringBuilder stringBuilder = new StringBuilder(String.format("[%s][%s][%s][%s] %s", mCategory, mAuthor, mDate, mModule, mTitle));
        if (level == STRING_LEVEL_LITE) {
            return stringBuilder.toString();
        }
        stringBuilder.append("\n");
        stringBuilder.append("Subject:");
        boolean firstElement = true;
        for (Subject subject : mSubjectSet) {
            if (!firstElement) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(subject);
            firstElement = false;
        }
        stringBuilder.append("\n");
        stringBuilder.append(mDescription);
        stringBuilder.append("Modified:\n");
        for (ModifiedFile modifiedFile : mModifiedFileMap.values()) {
            stringBuilder.append("    ");
            stringBuilder.append(modifiedFile.getName());
            if (level == STRING_LEVEL_FULL) {
                stringBuilder.append(":");
            }
            stringBuilder.append("\n");
            if (level == STRING_LEVEL_FULL) {
                List<RelatedLine> relatedLineList = modifiedFile.getRelatedLineList();
                RelatedLine lastRelatedLine = null;
                for (RelatedLine relatedLine : relatedLineList) {
                    if (lastRelatedLine != null && lastRelatedLine.getIndex() + 1 != relatedLine.getIndex()) {
                        stringBuilder.append("        +\n");
                    }
                    stringBuilder.append("        ");
                    stringBuilder.append(relatedLine);
                    stringBuilder.append("\n");
                    lastRelatedLine = relatedLine;
                }
            }
        }
        return stringBuilder.toString();
    }

    public static int getHash(String category, String author, String date, String module, String title) {
        return (category.toLowerCase() + author.toLowerCase() + date + module.toLowerCase() + title).hashCode();
    }

    public static class ModifiedFile extends Artifact implements Comparable<ModifiedFile> {

        private List<RelatedLine> mRelatedLineList = new ArrayList<RelatedLine>();
        private Map<Integer, Set<Error>> mIndexErrorMap = new HashMap<Integer, Set<Error>>();

        public ModifiedFile(String name) {
            super(name);
        }

        public List<RelatedLine> getRelatedLineList() {
            return mRelatedLineList;
        }

        public void addError(int index, Error error) {
            if (mSeverity < error.severity) {
                mSeverity = error.severity;
            }
            Set<Error> errorSet = mIndexErrorMap.get(index);
            if (errorSet == null) {
                errorSet = new HashSet<Error>();
                mIndexErrorMap.put(index, errorSet);
            }
            errorSet.add(error);
        }

        public Map<Integer, Set<Error>> getIndexErrorMap() {
            return mIndexErrorMap;
        }

        @Override
        public int compareTo(ModifiedFile another) {
            return mName.compareTo(another.mName);
        }

    }

}

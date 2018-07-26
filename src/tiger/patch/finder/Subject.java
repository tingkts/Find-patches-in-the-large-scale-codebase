package tiger.patch.finder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Subject extends Artifact {

    private static Map<String, Subject> sSubjectMap = new HashMap<String, Subject>();

    private Set<String> mCategories = new HashSet<String>();
    private Set<String> mAuthors = new HashSet<String>();
    private Set<String> mTitles = new HashSet<String>();
    private String mDate = "";
    private String mFirstModuleName = null;
    private boolean mIsCrossModule = false;
    private Set<Patch> mPatchSet = new HashSet<Patch>();

    public Subject(String name) {
        super(name);
    }

    public void addPatch(Patch patch) {
        if (mPatchSet.add(patch)) {
            mCategories.add(patch.getCategoty());
            mAuthors.add(patch.getAuthor());
            mTitles.add(patch.getTitle());
            if (mDate.compareTo(patch.getDate()) < 0) {
                mDate = patch.getDate();
            }
            if (mFirstModuleName == null) {
                mFirstModuleName = patch.getModule().getName();
            } else if (!mIsCrossModule && mFirstModuleName != patch.getModule().getName()) {
                mIsCrossModule = true;
            }
        }
    }

    public Collection<Patch> getPatches() {
        return mPatchSet;
    }

    public Collection<String> getCategoties() {
        return mCategories;
    }

    public Collection<String> getAuthors() {
        return mAuthors;
    }

    public Collection<String> getTitles() {
        return mTitles;
    }

    public String getDate() {
        return mDate;
    }

    public boolean isCrossModule() {
        return mIsCrossModule;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
        return anotherObject instanceof Subject && hashCode() == anotherObject.hashCode();
    }

    @Override
    public String toString() {
        return mName;
    }

    public static Subject fromName(String name) {
        Subject subject = sSubjectMap.get(name);
        if (subject == null) {
            subject = new Subject(name);
            sSubjectMap.put(name, subject);
        }
        return subject;
    }

    public static Collection<Subject> getSubjects() {
        return sSubjectMap.values();
    }

}

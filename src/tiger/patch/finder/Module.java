package tiger.patch.finder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Module extends Artifact {

    private static Map<String, Module> sModuleMap = new HashMap<String, Module>();

    private Set<Subject> mSubjectSet = new HashSet<Subject>();
    private Set<Patch> mPatchSet = new HashSet<Patch>();

    public Module(String name) {
        super(name);
    }

    public void addSubject(Subject subject) {
        mSubjectSet.add(subject);
    }

    public Collection<Subject> getSubjects() {
        return mSubjectSet;
    }

    public void addPatch(Patch patch) {
        mPatchSet.add(patch);
    }

    public Collection<Patch> getPatches() {
        return mPatchSet;
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

    public static Module fromName(String name) {
        Module module = sModuleMap.get(name);
        if (module == null) {
            module = new Module(name);
            sModuleMap.put(name, module);
        }
        return module;
    }

    public static Collection<Module> getModules() {
        return sModuleMap.values();
    }

}

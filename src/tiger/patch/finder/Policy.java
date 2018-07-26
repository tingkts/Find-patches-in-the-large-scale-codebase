package tiger.patch.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Policy {

    public static final List<String> VALID_CATEGORY_LIST = new ArrayList<String>();

    private static String TAG = Policy.class.getSimpleName();

    private static final String FORMAT_HAS_ERROR_PATCH = "%s @ %s";

    static {
        VALID_CATEGORY_LIST.add("Debug");
        VALID_CATEGORY_LIST.add("Issue Fix");
        VALID_CATEGORY_LIST.add("Feature");
        VALID_CATEGORY_LIST.add("Customization");
        VALID_CATEGORY_LIST.add("Optimization");
        VALID_CATEGORY_LIST.add("Tool");
    }

    public static void validate(Collection<Patch> patches) {
        validateModuleName();
        upForwardErrors(patches);
    }

    private static void validateModuleName() {
        Map<String, Module> map = new HashMap<String, Module>();
        for (Module module : Module.getModules()) {
            String key = module.getName().toLowerCase().replaceAll("\\s", "");
            Module moduleFromMap = map.get(key);
            if (moduleFromMap == null) {
                map.put(key, module);
            } else {
                Module badModule = moduleFromMap;
                if (module.getPatches().size() <= moduleFromMap.getPatches().size()) {
                    badModule = module;
                } else {
                    map.put(key, module);
                }
                badModule.addError(Error.SEVERITY_HIGH, "Duplicate module.");
                Log.e(TAG, "Duplicate module: " + badModule.getName());
            }
        }
    }

    private static void upForwardErrors(Collection<Patch> patches) {
        for (Patch patch : patches) {
            for (Error error : patch.getErrors()) {
                String message = String.format(FORMAT_HAS_ERROR_PATCH, error.message, patch.getTitle());
                patch.getModule().addError(error.severity, message);
                for (Subject subject : patch.getSubjects()) {
                    subject.addError(error.severity, message);
                }
            }
        }
    }

}

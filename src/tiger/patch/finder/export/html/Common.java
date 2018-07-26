package tiger.patch.finder.export.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tiger.patch.finder.Artifact;
import tiger.patch.finder.Log;
import tiger.patch.finder.Error;
import tiger.patch.finder.Module;
import tiger.patch.finder.Patch;
import tiger.patch.finder.Policy;
import tiger.patch.finder.StringUtility;
import tiger.patch.finder.Patch.ModifiedFile;
import tiger.patch.finder.Subject;

public class Common {

    private static final String FORMAT_SUMMARY = new StringBuilder()
    .append("<table class='tablesorter-default'>\n")
    .append("<tbody>\n")
    .append("<tr><td>Branch</td><td>%s</td></tr>\n") // branch
    .append("<tr><td>Generated</td><td>%s</td></tr>\n") // date-time
    .append("<tr><td>Modules</td><td>%s</td></tr>\n") // module-amount
    .append("<tr><td>Subjects</td><td><a class='expander' href='#'>%s (%s patches)</a><br/><div class='content'>%s</div></td></tr>\n") // patch-amount, subject-amount, category-list
    .append("</tbody>\n")
    .append("</table>\n")
    .toString();

    private static final String FORMAT_NAME_WITH_SEVERITY = "<span class='error severity%s' title='%s'>%s</span>"; // severity, message, name

    static final Comparator<Module> COMPARATOR_MODULE = new Comparator<Module>() {

        @Override
        public int compare(Module module1, Module module2) {
            return module2.getPatches().size() - module1.getPatches().size();
        }

    };

    static final Comparator<Patch> COMPARATOR_PATCH = new Comparator<Patch>() {

        @Override
        public int compare(Patch patch1, Patch patch2) {
            return -1 * patch1.getDate().compareTo(patch2.getDate());
        }

    };

    static final Comparator<ModifiedFile> COMPARATOR_MODIFIED_FILE = new Comparator<ModifiedFile>() {

        @Override
        public int compare(ModifiedFile file1, ModifiedFile file2) {
            return file1.getName().compareTo(file2.getName());
        }

    };

    static final Comparator<Map.Entry<String, Integer>> COMPARATOR_CATEGORY_AMOUNT = new Comparator<Map.Entry<String, Integer>>() {

        @Override
        public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
            Integer value1 = entry1.getValue();
            Integer value2 = entry2.getValue();
            if (value1 == null) {
                value1 = 0;
            }
            if (value2 == null) {
                value2 = 0;
            }
            return value2 - value1;
        }

    };

    static String getHtml(String tag, Class<?> clazz) {
        try {
            return StringUtility.fromStream(clazz.getResourceAsStream(clazz.getSimpleName() + ".html"), "UTF-8", true);
        } catch (IOException e) {
            Log.e(tag, "IOException: " + e.getMessage(), e);
            return null;
        }
    }

    static String getSummaryHtml(String branch) {
        // Subjects (includes stand alone patch)
        int patchAmount = 0;
        int standAlonePatchAmount = 0;
        Map<String, Integer> categoryAmountMap = new HashMap<String, Integer>();
        for (Module module : Module.getModules()) {
            patchAmount += module.getPatches().size();
            standAlonePatchAmount += Common.getStandAlonePatchAmount(module);
            for (Patch patch : module.getPatches()) {
                if (patch.getSubjects().size() == 0) { // stand alone patch
                    Common.increseMapValue(categoryAmountMap, patch.getCategoty());
                }
            }
        }
        for (Subject subject : Subject.getSubjects()) {
            for (String category: subject.getCategoties()) {
                Common.increseMapValue(categoryAmountMap, category);
            }
        }
        return String.format(FORMAT_SUMMARY,
                branch,
                new Date(),
                Module.getModules().size(),
                standAlonePatchAmount + Subject.getSubjects().size(), patchAmount,
                Common.getCategoryAmountHtml(categoryAmountMap.entrySet(), null));
    }

    static String getErrorListHtml(Collection<Error> errors) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Error error : errors) {
            stringBuilder.append(error.message);
            stringBuilder.append("&#013;");
        }
        return stringBuilder.toString();
    }

    static String getNameWithSeverityHtml(int severity, String name, String message) {
        return (severity == Error.SEVERITY_NONE) ? name : String.format(FORMAT_NAME_WITH_SEVERITY, severity, message.replace("'", "&#39;"), name);
    }

    static String getArtifactNameWithSeverityHtml(Artifact artifact) {
        final int severity = artifact.getSeverity();
        if (severity == Error.SEVERITY_NONE) {
            return artifact.getName();
        } else {
            return getNameWithSeverityHtml(severity, artifact.getName(), getErrorListHtml(artifact.getErrors()));
        }
    }

    static String getCategoryNameWithSeverityHtml(String name) {
        if (Policy.VALID_CATEGORY_LIST.contains(name)) {
            return name;
        } else {
            return getNameWithSeverityHtml(Error.SEVERITY_LOW, name, "Invalid category.");
        }
    }

    static String getModuleMenuHtml(Module activeModule) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Module> moduleList = new ArrayList<Module>(Module.getModules());
        Collections.sort(moduleList, COMPARATOR_MODULE);
        for (Module module : moduleList) {
            String attribute = module == activeModule ? " class='active'" : "";
            stringBuilder.append(String.format("\n<li%s><a href='%s'>%s</a></li>", attribute, getModuleOutputFileName(module), module.getName()));
        }
        return stringBuilder.toString();
    }

    static String getCategoryAmountHtml(Collection<Map.Entry<String, Integer>> categoryAmountEntries, Module module) {
        StringBuilder categorySummaryBuilder = new StringBuilder("<table class='grid'>");
        List<Map.Entry<String, Integer>> categoryAmountEntryList = new ArrayList<Map.Entry<String, Integer>>(categoryAmountEntries);
        Collections.sort(categoryAmountEntryList, Common.COMPARATOR_CATEGORY_AMOUNT);
        String format;
        if (module == null) {
            format = "<tr><td>%s</td><td><a class='external' href='search.html?category=%s'>%s</a></td></tr>\n";
        } else {
            format = "<tr><td>%s</td><td><a class='external' href='" + getModuleOutputFileName(module) + "?category=%s'>%s</a></td></tr>\n";
        }
        for (Map.Entry<String, Integer> entry : categoryAmountEntryList) {
            categorySummaryBuilder.append(String.format(format, entry.getValue(), entry.getKey(), getCategoryNameWithSeverityHtml(entry.getKey())));
        }
        categorySummaryBuilder.append("</table>");
        return categorySummaryBuilder.toString();
    }

    static List<String> getModifiedFileList(Module module) {
        List<String> modifiedFileList = new ArrayList<String>();
        for (Patch patch : module.getPatches()) {
            for (ModifiedFile modifiedFile : patch.getModifiedFiles()) {
                if (!modifiedFileList.contains(modifiedFile.getName())) {
                    modifiedFileList.add(modifiedFile.getName());
                }
            }
        }
        Collections.sort(modifiedFileList);
        return modifiedFileList;
    }

    static int getStandAlonePatchAmount(Module targetModule) {
        if (targetModule != null) {
            return getStandAlonePatchAmountInner(targetModule);
        } else {
            int amount = 0;
            for (Module module : Module.getModules()) {
                amount += getStandAlonePatchAmountInner(module);
            }
            return amount;
        }
    }

    private static int getStandAlonePatchAmountInner(Module module) {
        int amount = 0;
        for (Patch patch : module.getPatches()) {
            if (patch.getSubjects().isEmpty()) {
                amount++;
            }
        }
        return amount;
    }

    static void increseMapValue(Map<String, Integer> map, String key) {
        Integer count = map.get(key);
        if (count == null) {
            count = 0;
        }
        map.put(key, count + 1);
    }

    static String getModuleOutputFileName(Module module) {
        return module.getName().replaceAll("\\W+", "_") + ".html";
    }

    static String htmlFilter(String words) {
        if (words == null) {
            return "";
        }
        return words.replaceAll("<", "&lt;");
    }

    static String emptyFilter(String words) {
        if (words != null && words.trim().isEmpty()) {
            return "(empty)";
        }
        return words;
    }

    static String spaceFilter(String words) {
        if (words == null) {
            return "";
        }
        return words.replaceAll(" ", "&nbsp;");
    }

    static String newLineFilter(String words) {
        if (words == null) {
            return "";
        }
        return words.replaceAll("\n", "<br/>");
    }

    static String linkFilter(String words) {
        if (words == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher matcher = pattern.matcher(words);
        int charIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            int linkStart = matcher.start();
            if (charIndex < linkStart) {
                stringBuilder.append(words.substring(charIndex, linkStart));
            }
            String link = matcher.group();
            stringBuilder.append("<a class='external' href='");
            stringBuilder.append(link);
            stringBuilder.append("'>");
            stringBuilder.append(link);
            stringBuilder.append("</a>");
            charIndex = matcher.end();
        }
        if (charIndex < words.length()) {
            stringBuilder.append(words.substring(charIndex));
        }
        return stringBuilder.toString();
    }

    static String hpkbFilter(String words) {
        if (words == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("HPKB#([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(words);
        int charIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            int linkStart = matcher.start();
            if (charIndex < linkStart) {
                stringBuilder.append(words.substring(charIndex, linkStart));
            }
            String kbid = matcher.group(1);
            stringBuilder.append("<a class='external' href='http://masd.htc.com.tw:33007/Default.aspx?kbid=");
            stringBuilder.append(kbid);
            stringBuilder.append("'>");
            stringBuilder.append(matcher.group());
            stringBuilder.append("</a>");
            charIndex = matcher.end();
        }
        if (charIndex < words.length()) {
            stringBuilder.append(words.substring(charIndex));
        }
        return stringBuilder.toString();
    }

    static void saveToFile(File outputDirectory, String outputFileName, String html) throws FileNotFoundException {
        Log.d(HtmlExporter.TAG, "Writing output file: " + outputFileName);
        StringUtility.toFile(new File(outputDirectory, outputFileName), html);
    }

}

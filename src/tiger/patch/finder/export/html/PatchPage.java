package tiger.patch.finder.export.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.regex.Matcher;

import tiger.patch.finder.Module;
import tiger.patch.finder.Patch;
import tiger.patch.finder.Subject;

public class PatchPage {

    public static final String FILE_NAME = "patch.html";
    public static final String DATA_DIRECTORY_NAME = "data";

    private static final String TAG = PatchPage.class.getSimpleName();
    private static final String HTML = Common.getHtml(TAG, PatchPage.class);

    private static final String FORMAT_PATCH_DATA = new StringBuilder()
            .append("var p_title = '%s';\n")
            .append("var p_author = '%s';\n")
            .append("var p_category = '%s';\n")
            .append("var p_date = '%s';\n")
            .append("var p_module = '%s';\n")
            .append("var p_subject = '%s';\n")
            .append("var p_link = '%s';\n")
            .append("var p_dscription = '%s';\n")
            .append("var p_modified = '%s';\n")
            .toString();

    public static void export(File outputDirectory, String branch, String sourceSite, String resource) throws FileNotFoundException {
        if (HTML != null) {
            String html = HTML;
            html = html.replaceAll("\\$\\{BRANCH\\}", Matcher.quoteReplacement(branch));
            html = html.replaceAll("\\$\\{SOURCE_SITE\\}", Matcher.quoteReplacement(sourceSite));
            html = html.replaceAll("\\$\\{RES\\}", Matcher.quoteReplacement(resource));
            html = html.replaceAll("\\$\\{MODULE_MENU\\}", Matcher.quoteReplacement(Common.getModuleMenuHtml(null)));
            Common.saveToFile(outputDirectory, FILE_NAME, html);
            File dataOutputDirectory = new File(outputDirectory, DATA_DIRECTORY_NAME);
            if (!dataOutputDirectory.isDirectory()) {
                dataOutputDirectory.mkdirs();
            }
            for (Module module : Module.getModules()) {
                for (Patch patch : module.getPatches()) {
                    exportData(dataOutputDirectory, patch);
                }
            }
        }
    }

    private static void exportData(File outputDirectory, Patch patch) throws FileNotFoundException {
        String output = String.format(FORMAT_PATCH_DATA,
                escapeChars(patch.getTitle()),
                escapeChars(patch.getAuthor()),
                escapeChars(Common.getCategoryNameWithSeverityHtml(patch.getCategoty())),
                escapeChars(patch.getDate()),
                escapeChars(getModuleHtml(patch.getModule())),
                escapeChars(getSubjectsHtml(patch.getSubjects())),
                escapeChars(PatchTableHtml.getLinksHtml(patch)),
                escapeChars(patch.getTitle() + "<br/>" + PatchTableHtml.getPatchDescriptionHtml(patch)),
                escapeChars(PatchTableHtml.getPatchModifiedFileHtml(patch, "")));
        Common.saveToFile(outputDirectory, patch.getId() + ".js", output);
    }

    private static String escapeChars(String words) {
        return words.replace("\\", "\\\\").replaceAll("\n", "\\\\n").replace("'", "\\'");
    }

    private static String getModuleHtml(Module module) {
        return String.format("<a class='external' href='%s'>%s</a>", Common.getModuleOutputFileName(module), Common.getArtifactNameWithSeverityHtml(module));
    }

    private static String getSubjectsHtml(Collection<Subject> subjects) {
        if (subjects.isEmpty()) {
            return "(Stand alone patch)";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Subject subject : subjects) {
                stringBuilder.append(String.format("<div><a class='expander' href='#'><b>%s</b></a><br/>\n<div class='content'>%s</div></div>\n", Common.getArtifactNameWithSeverityHtml(subject), getPatchPageLinksHtml(subject.getPatches())));
            }
            return stringBuilder.toString();
        }
    }

    private static String getPatchPageLinksHtml(Collection<Patch> patches) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Patch patch : patches) {
            stringBuilder.append(String.format("<a class='external' href='%s?id=%s'>%s</a><br/>\n", FILE_NAME, patch.getId(), Common.getArtifactNameWithSeverityHtml(patch)));
        }
        return stringBuilder.toString();
    }

}

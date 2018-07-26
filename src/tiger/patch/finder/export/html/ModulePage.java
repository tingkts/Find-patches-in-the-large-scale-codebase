package tiger.patch.finder.export.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import tiger.patch.finder.Module;
import tiger.patch.finder.Patch;
import tiger.patch.finder.Subject;

public class ModulePage {

    private static final String TAG = ModulePage.class.getSimpleName();
    private static final String HTML = Common.getHtml(TAG, ModulePage.class);
    private static final String FORMAT_SUMMARY = new StringBuilder()
            .append("<table class='tablesorter-default'>\n")
            .append("<tbody>\n")
            .append("<tr><td>Module</td><td><a class='external' href='https://sptw.htc.com/Sites/MASD/Others/FWKB/Lists/%s/AllItems.aspx'>%s</a></td></tr>\n") // module, module
            .append("<tr><td>Branch</td><td>%s</td></tr>\n") // branch
            .append("<tr><td>Generated</td><td>%s</td></tr>\n") // date-time
            .append("<tr><td>Files</td><td><a class='expander' href='#'>%s</a><br/><div class='content'>%s</div></td></tr>\n") // file-amount, file-list
            .append("<tr><td>Subjects</td><td><a class='expander' href='#'>%s (%s patches)</a><br/><div class='content'>%s</div></td></tr>\n") // subject-amount, patch-amount, category-list
            .append("</tbody>\n")
            .append("</table>\n")
            .toString();

    public static void export(File outputDirectory, Module module, String branch, String sourceSite, String resource) throws FileNotFoundException {
        if (HTML != null) {
            String html = HTML;
            html = html.replaceAll("\\$\\{MODULE\\}", Matcher.quoteReplacement(module.getName()));
            html = html.replaceAll("\\$\\{SUMMARY\\}", Matcher.quoteReplacement(getSummaryHtml(module, branch)));
            html = html.replaceAll("\\$\\{PATCH_LIST\\}", Matcher.quoteReplacement(getPatchListHtml(module)));
            html = html.replaceAll("\\$\\{SOURCE_SITE\\}", Matcher.quoteReplacement(sourceSite));
            html = html.replaceAll("\\$\\{RES\\}", Matcher.quoteReplacement(resource));
            html = html.replaceAll("\\$\\{MODULE_MENU\\}", Matcher.quoteReplacement(Common.getModuleMenuHtml(module)));
            Common.saveToFile(outputDirectory, Common.getModuleOutputFileName(module), html);
        }
    }

    private static String getSummaryHtml(Module module, String branch) {
        // Files
        List<String> modifiedFileList = Common.getModifiedFileList(module);
        StringBuilder modifiedFileListBuilder = new StringBuilder();
        for (String modifiedFile : modifiedFileList) {
            modifiedFileListBuilder.append(modifiedFile);
            modifiedFileListBuilder.append("<br/>");
        }
        // Subjects (includes stand alone patch)
        Map<String, Integer> categoryAmountMap = new HashMap<String, Integer>();
        for (Patch patch : module.getPatches()) {
            if (patch.getSubjects().size() == 0) { // stand alone patch
                Common.increseMapValue(categoryAmountMap, patch.getCategoty());
            }
        }
        for (Subject subject : module.getSubjects()) {
            for (String category: subject.getCategoties()) {
                Common.increseMapValue(categoryAmountMap, category);
            }
        }
        return String.format(FORMAT_SUMMARY,
                module.getName(), Common.getArtifactNameWithSeverityHtml(module),
                branch,
                new Date(),
                modifiedFileList.size(), modifiedFileListBuilder,
                Common.getStandAlonePatchAmount(module) + module.getSubjects().size(), module.getPatches().size(),
                Common.getCategoryAmountHtml(categoryAmountMap.entrySet(), module));
    }

    private static String getPatchListHtml(Module module) {
        StringBuilder stringBuilder = new StringBuilder(PatchTableHtml.PATCH_TABLE_PREFIX);
        for (Subject subject : module.getSubjects()) {
            stringBuilder.append(PatchTableHtml.getSubjectRowHtml(subject));
        }
        for (Patch patch : module.getPatches()) {
            if (patch.getSubjects().isEmpty()) {
                stringBuilder.append(PatchTableHtml.getPatchRowHtml(patch, "" /* rowAttribute */, null /* parent */));
            }
        }
        stringBuilder.append(PatchTableHtml.PATCH_TABLE_POSTFIX);
        return stringBuilder.toString();
    }

}

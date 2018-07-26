package tiger.patch.finder.export.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Matcher;

import tiger.patch.finder.Module;
import tiger.patch.finder.Subject;

public class HomePage {

    private static final String TAG = HomePage.class.getSimpleName();
    private static final String HTML = Common.getHtml(TAG, HomePage.class);
    private static final String FILE_NAME = "index.html";
    private static final String FORMAT_MODULE_TABLE_PREFIX = new StringBuilder()
            .append("<table id='module' class='tablesorter-default fill'>")
            .append("<thead>")
            .append("<tr>")
            .append("<th width='20%' data-placeholder='Filter...' class='filter-match'>Module</th>")
            .append("<th width='10%' data-placeholder='Try >5'>Subjects</th>")
            .append("<th width='10%' data-placeholder='Try >5'>Patches</th>")
            .append("<th width='60%' data-placeholder='Filter key words...' class='filter-match'>Files</th>")
            .append("</tr>")
            .append("</thead>")
            .append("<tbody>\n")
            .toString();
    private static final String FORMAT_MODULE_ROW = "<tr><td><a class='external' href='%s'>%s</a></td><td>%s</td><td>%s</td><td><a class='expander' href='#'>%s</a><br/><div class='content'>%s</div></td></tr>\n"; // module-output-file-name, subject, subject-amount, patch-amount, modified-file-amount, modified-file
    private static final String FORMAT_MODULE_TABLE_POSTFIX = new StringBuilder()
            .append("</tbody>\n")
            .append("</table>\n")
            .toString();

    public static void export(File outputDirectory, String branch, String sourceSite, String resource) throws FileNotFoundException {
        if (HTML != null) {
            String html = HTML;
            html = html.replaceAll("\\$\\{SUMMARY\\}", Matcher.quoteReplacement(Common.getSummaryHtml(branch)));
            html = html.replaceAll("\\$\\{MODULE_LIST\\}", Matcher.quoteReplacement(getModuleListHtml()));
            html = html.replaceAll("\\$\\{CROSS_MODULE_SUBJECT_LIST\\}", Matcher.quoteReplacement(getCrossModuleSubjectListHtml()));
            html = html.replaceAll("\\$\\{SOURCE_SITE\\}", Matcher.quoteReplacement(sourceSite));
            html = html.replaceAll("\\$\\{RES\\}", Matcher.quoteReplacement(resource));
            html = html.replaceAll("\\$\\{MODULE_MENU\\}", Matcher.quoteReplacement(Common.getModuleMenuHtml(null)));
            Common.saveToFile(outputDirectory, FILE_NAME, html);
        }
    }

    private static String getModuleListHtml() {
        StringBuilder stringBuilder = new StringBuilder(FORMAT_MODULE_TABLE_PREFIX);
        for (Module module : Module.getModules()) {
            List<String> modifiedFileList = Common.getModifiedFileList(module);
            StringBuilder modifiedFileListBuilder = new StringBuilder();
            for (String modifiedFile : modifiedFileList) {
                modifiedFileListBuilder.append(modifiedFile);
                modifiedFileListBuilder.append("<br/>");
            }
            stringBuilder.append(String.format(FORMAT_MODULE_ROW,
                    Common.getModuleOutputFileName(module),
                    Common.getArtifactNameWithSeverityHtml(module),
                    Common.getStandAlonePatchAmount(module) + module.getSubjects().size(),
                    module.getPatches().size(),
                    modifiedFileList.size(),
                    modifiedFileListBuilder));
        }
        stringBuilder.append(FORMAT_MODULE_TABLE_POSTFIX);
        return stringBuilder.toString();
    }

    private static String getCrossModuleSubjectListHtml() {
        StringBuilder stringBuilder = new StringBuilder(PatchTableHtml.PATCH_TABLE_PREFIX);
        for (Subject subject : Subject.getSubjects()) {
            if (subject.isCrossModule()) {
                stringBuilder.append(PatchTableHtml.getSubjectRowHtml(subject));
            }
        }
        stringBuilder.append(PatchTableHtml.PATCH_TABLE_POSTFIX);
        return stringBuilder.toString();
    }

}

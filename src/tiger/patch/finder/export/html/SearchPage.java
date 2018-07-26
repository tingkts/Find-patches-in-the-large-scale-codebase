package tiger.patch.finder.export.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import tiger.patch.finder.Module;
import tiger.patch.finder.Patch;
import tiger.patch.finder.Patch.ModifiedFile;
import tiger.patch.finder.StringUtility;

public class SearchPage {

    private static final String TAG = SearchPage.class.getSimpleName();
    private static final String HTML = Common.getHtml(TAG, SearchPage.class);
    private static final String FILE_NAME = "search.html";
    static final String PATCH_TABLE_PREFIX = new StringBuilder()
            .append("<table id='patch' class='tablesorter-default fill'>")
            .append("<thead>")
            .append("<tr>")
            .append("<th width='10%' data-placeholder='Filter...' class='filter-match'>Category</th>")
            .append("<th width='10%' data-placeholder='Filter author1|author2|...' class='filter-match'>Author</th>")
            .append("<th width='10%' data-placeholder='Try >2014'>Date</th>")
            .append("<th width='10%' data-placeholder='Filter...' class='filter-match'>Module</th>")
            .append("<th width='50%' data-placeholder='Filter key words...' class='filter-match'>Subject: Patch</th>")
            .append("<th width='10%' data-placeholder='Filter...' class='filter-match'>File</th>")
            .append("</tr>")
            .append("</thead>")
            .append("<tbody>\n")
            .toString();
    private static final String FORMAT_PATCH_ROW = "<tr><td>%s</td><td>%s</td><td>%s</td><td><a class='external' href='%s'>%s</a></td><td class='wrap'><a class='external' href='%s'>%s</a></td><td><a class='expander' href='#'>%s</a><br/><div class='content'>%s</div></td></tr>\n"; // category, author, date, module-link, module, file-amount, files, patch-link, subject(s):title
    static final String PATCH_TABLE_POSTFIX = new StringBuilder()
            .append("</tbody>\n")
            .append("</table>\n")
            .toString();

    public static void export(File outputDirectory, String branch, String sourceSite, String resource) throws FileNotFoundException {
        if (HTML != null) {
            String html = HTML;
            html = html.replaceAll("\\$\\{SUMMARY\\}", Matcher.quoteReplacement(Common.getSummaryHtml(branch)));
            html = html.replaceAll("\\$\\{PATCH_LIST\\}", Matcher.quoteReplacement(getPatchListHtml()));
            html = html.replaceAll("\\$\\{RES\\}", Matcher.quoteReplacement(resource));
            html = html.replaceAll("\\$\\{MODULE_MENU\\}", Matcher.quoteReplacement(Common.getModuleMenuHtml(null)));
            Common.saveToFile(outputDirectory, FILE_NAME, html);
        }
    }

    private static String getPatchListHtml() {
        StringBuilder stringBuilder = new StringBuilder(PATCH_TABLE_PREFIX);
        for (Module module : Module.getModules()) {
            for (Patch patch : module.getPatches()) {
                // File
                List<ModifiedFile> modifiedFileList = new ArrayList<ModifiedFile>(patch.getModifiedFiles());
                Collections.sort(modifiedFileList, Common.COMPARATOR_MODIFIED_FILE);
                StringBuilder modifiedFileListBuilder = new StringBuilder();
                for (ModifiedFile modifiedFile : modifiedFileList) {
                    modifiedFileListBuilder.append(modifiedFile.getName());
                    modifiedFileListBuilder.append("<br/>");
                }
                // Subject: Patch
                String subjectTitle = Common.getArtifactNameWithSeverityHtml(patch);
                if (!patch.getSubjects().isEmpty()) {
                    subjectTitle = String.format("<b>%s</b>: %s", StringUtility.join(patch.getSubjects(), ", "), subjectTitle);
                }
                stringBuilder.append(String.format(FORMAT_PATCH_ROW,
                        Common.getCategoryNameWithSeverityHtml(patch.getCategoty()),
                        patch.getAuthor(),
                        patch.getDate(),
                        Common.getModuleOutputFileName(module), Common.getArtifactNameWithSeverityHtml(module),
                        PatchPage.FILE_NAME + "?id=" + patch.getId(),
                        subjectTitle,
                        modifiedFileList.size(), modifiedFileListBuilder));
            }
        }
        stringBuilder.append(PATCH_TABLE_POSTFIX);
        return stringBuilder.toString();
    }

}

package tiger.patch.finder.export.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tiger.patch.finder.Error;
import tiger.patch.finder.Link;
import tiger.patch.finder.Patch;
import tiger.patch.finder.RelatedLine;
import tiger.patch.finder.StringUtility;
import tiger.patch.finder.Subject;
import tiger.patch.finder.Patch.ModifiedFile;

public class PatchTableHtml {

    static final String PATCH_TABLE_PREFIX = new StringBuilder()
            .append("<table id='patch' class='tablesorter-default fill'>")
            .append("<thead>")
            .append("<tr>")
            .append("<th width='10%' data-placeholder='Filter...' class='filter-match'>Category</th>")
            .append("<th width='10%' data-placeholder='Filter author1|author2|...' class='filter-match'>Author</th>")
            .append("<th width='10%' data-placeholder='Try >2014'>Date</th>")
            .append("<th width='70%' data-placeholder='Filter key words...' class='filter-match'>Subject</th>")
            .append("</tr>")
            .append("</thead>")
            .append("<tbody>\n")
            .toString();

    private static final String FORMAT_SUBJECT_ROW = "<tr><td>%s</td><td>%s</td><td>%s</td><td><a href='#' class='toggle'><b>%s</b></a><div class='filter-data'>%s</div></td></tr>\n"; // category, author, date, subject, titles
    private static final String FORMAT_PATCH_ROW = "<tr%s><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n"; // row attribute, category, author, date, title with detail
    private static final String FORMAT_PATCH_DETAIL = "<a class='expander' href='#'>%s</a><div class='content'>%s\n%s\n<br/>Modified:<br/>%s\n</div>"; // title, description, links, modified
    private static final String FORMAT_PATCH_MODIFIED = "<div>%s<a class='expander' href='#'>%s</a><br/>\n<div class='content' data-file='%s'>%s</div></div>\n"; // prefix, file-with-severity, file, modified lines

    static final String PATCH_TABLE_POSTFIX = new StringBuilder()
            .append("</tbody>\n")
            .append("</table>\n")
            .toString();

    static String getSubjectRowHtml(Subject subject) {
        String category = getNameOrMultipleFilterDataHtml(subject.getCategoties());
        String author = getNameOrMultipleFilterDataHtml(subject.getAuthors());
        String patchTitles = StringUtility.join(subject.getTitles(), " | ");
        StringBuilder stringBuilder = new StringBuilder(String.format(FORMAT_SUBJECT_ROW, category, author, subject.getDate(), Common.getArtifactNameWithSeverityHtml(subject), patchTitles));
        // child rows
        List<Patch> orderedPatchList = new ArrayList<Patch>(subject.getPatches());
        Collections.sort(orderedPatchList, Common.COMPARATOR_PATCH);
        for (Patch patch : orderedPatchList) {
            stringBuilder.append(getPatchRowHtml(patch, " class='tablesorter-childRow'", subject));
        }
        return stringBuilder.toString();
    }

    private static String getNameOrMultipleFilterDataHtml(Collection<String> names) {
        return names.size() > 1 ?
                String.format("<div class='tip-holder'>Multiple</div><div class='filter-data tip'>%s</div>", Common.htmlFilter(StringUtility.join(names, " | "))) :
                Common.htmlFilter(names.iterator().next());
    }

    static String getPatchRowHtml(Patch patch, String rowAttribute, Subject parent) {
        return String.format(FORMAT_PATCH_ROW, rowAttribute, patch.getCategoty(), patch.getAuthor(), patch.getDate(), getPatchDetailHtml(patch, parent));
    }

    private static String getPatchDetailHtml(Patch patch, Subject parent) {
        return String.format(FORMAT_PATCH_DETAIL, getPatchTitleHtml(patch, parent), getPatchDescriptionHtml(patch), getPatchLinkHtml(patch), getPatchModifiedFileHtml(patch, "&nbsp;&nbsp;&nbsp;&nbsp;"));
    }

    private static String getPatchTitleHtml(Patch patch, Subject parent) {
        String title;
        if (parent != null && parent.isCrossModule()) {
            title = String.format("[%s] %s", patch.getModule().getName(), patch.getTitle());
        } else {
            title = patch.getTitle();
        }
        return Common.getNameWithSeverityHtml(patch.getSeverity(), Common.htmlFilter(title), Common.getErrorListHtml(patch.getErrors()));
    }

    static String getPatchDescriptionHtml(Patch patch) {
        StringBuilder stringBuilder = new StringBuilder();
        String description = patch.getDescription();
        if (!description.isEmpty()) {
            stringBuilder.append("<br/>");
            stringBuilder.append(Common.newLineFilter(Common.hpkbFilter(Common.linkFilter(Common.spaceFilter(Common.htmlFilter(description))))));
        }
        return stringBuilder.toString();
    }

    private static String getPatchLinkHtml(Patch patch) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!patch.getLinks().isEmpty()) {
            stringBuilder.append("<br/>Links: ");
            stringBuilder.append(getLinksHtml(patch));
            stringBuilder.append("<br/>");
        }
        return stringBuilder.toString();
    }

    static String getLinksHtml(Patch patch) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Link link : patch.getLinks()) {
            stringBuilder.append(link.toHtmlString("class='external'"));
            stringBuilder.append("&nbsp;&nbsp;");
        }
        return stringBuilder.toString();
    }

    static String getPatchModifiedFileHtml(Patch patch, String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        List<ModifiedFile> modifiedFileList = new ArrayList<ModifiedFile>(patch.getModifiedFiles());
        Collections.sort(modifiedFileList, Common.COMPARATOR_MODIFIED_FILE);
        for (ModifiedFile modifiedFile : modifiedFileList) {
            stringBuilder.append(String.format(FORMAT_PATCH_MODIFIED, prefix, getPatchModifiedFileNameHtml(modifiedFile), modifiedFile.getName(), getPatchRelatedLineHtml(modifiedFile)));
        }
        return stringBuilder.toString();
    }

    private static String getPatchModifiedFileNameHtml(ModifiedFile modifiedFile) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, Set<Error>> entry : modifiedFile.getIndexErrorMap().entrySet()) {
            stringBuilder.append("Line#");
            stringBuilder.append(entry.getKey());
            stringBuilder.append(":");
            for (Error error : entry.getValue()) {
                stringBuilder.append(" ");
                stringBuilder.append(error.message);
            }
            stringBuilder.append("&#013;");
        }
        return Common.getNameWithSeverityHtml(modifiedFile.getSeverity(), modifiedFile.getName(), stringBuilder.toString());
    }

    private static String getPatchRelatedLineHtml(ModifiedFile modifiedFile) {
        StringBuilder stringBuilder = new StringBuilder();
        int lineNumber = 0;
        boolean isCodeOpened = false;
        for (RelatedLine relatedLine : modifiedFile.getRelatedLineList()) {
            if (relatedLine.getIndex() != lineNumber) {
                if (isCodeOpened) {
                    stringBuilder.append("</pre>");
                    isCodeOpened = false;
                }
                lineNumber = relatedLine.getIndex();
                stringBuilder.append(String.format("<pre class='prettyprint %s linenums:%s' data-line-number='%s'>\n", getLanguage(modifiedFile.getName().toLowerCase()), lineNumber, lineNumber));
                isCodeOpened = true;
            }
            if (relatedLine.isInPatch()) {
                stringBuilder.append("<span class='patchline'>");
            }
            String relatedLineHtml = Common.htmlFilter(relatedLine.getContent());
            stringBuilder.append(relatedLineHtml);
            if (relatedLineHtml.trim().isEmpty()) {
                stringBuilder.append("<br/>");
            }
            if (relatedLine.isInPatch()) {
                stringBuilder.append("</span>");
            }
            stringBuilder.append("\n");
            lineNumber++;
        }
        if (isCodeOpened) {
            stringBuilder.append("</pre>");
            isCodeOpened = false;
        }
        return stringBuilder.toString();
    }

    private static String getLanguage(String fileName) {
        if (fileName.endsWith(".java")) {
            return "lang-java";
        } else if (fileName.endsWith(".cpp") || fileName.endsWith(".h")) {
            return "lang-cpp";
        }
        return "";
    }

}

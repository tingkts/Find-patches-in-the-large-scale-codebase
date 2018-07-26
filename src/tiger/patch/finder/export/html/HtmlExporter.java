package tiger.patch.finder.export.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import tiger.patch.finder.Module;

public class HtmlExporter {

    public static final String TAG = HtmlExporter.class.getSimpleName();

    private static final String PROPERTY_KEY_BRANCH = "branch";
    private static final String PROPERTY_KEY_SOURCE_SITE = "source_site";
    private static final String DEFAULT_SOURCE_SITE = "";
    private static final String PROPERTY_KEY_RESOURCE = "resource";
    private static final String DEFAULT_RESOURCE = "../res";

    public static void export(Properties properties, String branchDirectories, File outputDirectory) throws FileNotFoundException {
        String branch = properties.getProperty(PROPERTY_KEY_BRANCH);
        String sourceSite = properties.getProperty(PROPERTY_KEY_SOURCE_SITE, DEFAULT_SOURCE_SITE);
        String resource = properties.getProperty(PROPERTY_KEY_RESOURCE, DEFAULT_RESOURCE);
        if (branchDirectories != null) {
            String branchDirectory = branchDirectories.substring(0, branchDirectories.indexOf(":"));
            branch = String.format("%s (%s) <a class='expander' href='#'>(more)</a><br/><div class='content' id='more-branch'>%s</div>", branchDirectory, branch, branchDirectories);
        }
        if (!outputDirectory.isDirectory()) {
            outputDirectory.mkdirs();
        }
        for (Module module : Module.getModules()) {
            ModulePage.export(outputDirectory, module, branch, sourceSite, resource);
        }
        HomePage.export(outputDirectory, branch, sourceSite, resource);
        PatchPage.export(outputDirectory, branch, sourceSite, resource);
        SearchPage.export(outputDirectory, branch, sourceSite, resource);
    }

}

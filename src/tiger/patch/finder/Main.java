package tiger.patch.finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import tiger.patch.finder.export.html.HtmlExporter;
import tiger.patch.finder.export.html.PatchPage;

public class Main {

    private static final int ERROR_LEVEL_FILE_NOT_FOUND = 1;
    private static final int ERROR_LEVEL_PATCH_FORMAT = 2;

    private static final String TAG = Main.class.getSimpleName();

    private static final String DEFAULT_CONFIG_FILE_PATH = "config.properties";

    private static final int ARGUMENT_CONFIG = 0;
    private static final int ARGUMENT_BRANCH_DIRECTORIES = 1;

    private static final String PROPERTY_KEY_DEBUG = "debug";
    private static final String DEFAULT_DEBUG = "true";
    private static final String PROPERTY_KEY_CONTEXT_LINE_NUMBER = "context_line_number";
    private static final String DEFAULT_CONTEXT_LINE_NUMBER = "5";
    private static final String PROPERTY_KEY_OUTPUT = "output";
    private static final String DEFAULT_OUTPUT = "out";
    private static final String PROPERTY_KEY_EXCLUDE = "exclude";
    private static final String DEFAULT_EXCLUDE = ".git$|.repo$|.bmp$|.jpg$|.jpeg$|.png$|.raw$|.mp4$";
    private static final String PROPERTY_KEY_ROOT = "root";
    private static final String PROPERTY_KEY_FILES = "files";

    /**
     * @param args
     * @throws FileNotFoundException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // Read properties
        File configFile = loadConfigFile(args);
        Properties config = loadConfigurations(configFile);
        Log.setDebug(Boolean.parseBoolean(config.getProperty(PROPERTY_KEY_DEBUG, DEFAULT_DEBUG)));
        int contextLineNumber = getInt(config, PROPERTY_KEY_CONTEXT_LINE_NUMBER, DEFAULT_CONTEXT_LINE_NUMBER);
        File output = new File(configFile.getParent(), config.getProperty(PROPERTY_KEY_OUTPUT, DEFAULT_OUTPUT));
        String root = config.getProperty(PROPERTY_KEY_ROOT);
        String files = config.getProperty(PROPERTY_KEY_FILES);
        Pattern exclude = Pattern.compile(config.getProperty(PROPERTY_KEY_EXCLUDE, DEFAULT_EXCLUDE).replace(".", "\\.").replace("*", ".*"), Pattern.CASE_INSENSITIVE);
        if (root == null || files == null) {
            Log.e(TAG, "Fatal error! root=" + root + " files=" + files);
            return;
        }

        // Walk through files and find patches
        PatchReader patchReader = new PatchReader(contextLineNumber);
        File rootFile = new File(root);
        int validFiles = 0;
        if (files == null || files.isEmpty() || files.equals(".")) {
            validFiles += readFile(patchReader, rootFile, rootFile, exclude);
        } else {
            for (String fileName : files.split(" *[;,] *")) {
                fileName = fileName.trim();
                if (fileName != null && !fileName.isEmpty()) {
                    validFiles += readFile(patchReader, rootFile, new File(rootFile, fileName), exclude);
                }
            }
        }
        if (validFiles == 0) {
            Log.e(TAG, "No file is parsed. Please check the given file path.");
            System.exit(ERROR_LEVEL_FILE_NOT_FOUND);
            return;
        }

        // Add links for each patch
        for (Patch patch : patchReader.getPatches()) {
            String href = PatchPage.FILE_NAME + "?id=" + patch.getId();
            patch.addLink(new Link("Patch", href));
            String author = patch.getAuthor();
            String title = patch.getTitle();
            href = String.format("http://git.htc.com:8081/#/q/owner:%s+is:merged+message:\"%s\",n,z", author, title.replaceAll("[^\\w ]", "\\\\$0")); // insert '\' before each special character
            patch.addLink(new Link("Gerrit", href));
        }

        // Check and forward errors
        Policy.validate(patchReader.getPatches());

        // Generate reports
        HtmlExporter.export(config, getBranchDirectories(args), output);

        // Report status
        if (!Log.hasError()) {
            Log.d(TAG, "Done.");
        } else {
            if (Log.isDebug()) {
                Log.d(TAG, "----- DUMP ERROR LOG -----");
                Log.dumpErrorLog();
            }
            Log.e(TAG, "Exit with errors! Please check the error logs listed above.");
            System.exit(ERROR_LEVEL_PATCH_FORMAT);
        }
    }

    private static File loadConfigFile(String[] args) throws FileNotFoundException {
        String configFilePath = DEFAULT_CONFIG_FILE_PATH;
        if (args.length > ARGUMENT_CONFIG) {
            configFilePath = args[ARGUMENT_CONFIG];
        }
        File configFile = new File(configFilePath);
        if (configFile.isFile()) {
            return configFile;
        }
        Log.e(TAG, "Cannot access the config file: " + configFile);
        throw new FileNotFoundException();
    }

    private static Properties loadConfigurations(File configFile) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFile));
        Log.d(TAG, "Using config file: " + configFile);
        return properties;
    }

    private static String getBranchDirectories(String[] args) {
        if (args.length > ARGUMENT_BRANCH_DIRECTORIES) {
            String branchDirectories = args[ARGUMENT_BRANCH_DIRECTORIES];
            if (!"".equals(branchDirectories.trim())) {
                return branchDirectories;
            }
        }
        return null;
    }

    private static int readFile(PatchReader patchReader, File root, File file, Pattern exclude) {
        int count = 0;
        if (exclude != null && exclude.matcher(file.getAbsolutePath()).find()) {
            Log.d(TAG, "Exclude: " + file);
        } else if (isSymbolicLink(file)) {
            Log.d(TAG, "Skip symbolic link: " + file);
        } else if (file.isFile() && file.length() > 0) {
            try {
                patchReader.readFile(root, file);
                count++;
            } catch (IOException e) {
                Log.e(TAG, "Cannot read the file: " + file);
            }
        } else if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                count += readFile(patchReader, root, child, exclude);
            }
        } else if (!file.exists()) {
            Log.d(TAG, "File not found: " + file);
        }
        return count;
    }

    public static boolean isSymbolicLink(File file) {
        try {
            File canon;
            if (file.getParent() == null) {
                canon = file;
            } else {
                File canonDir = file.getParentFile().getCanonicalFile();
                canon = new File(canonDir, file.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (IOException e) {
            Log.e(TAG, "Cannot read the file: " + file);
            return true;
        }
    }

    private static int getInt(Properties properties, String key, String defaultValue) {
        String propertyValue = properties.getProperty(key, defaultValue);
        try {
            return Integer.decode(propertyValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, String.format("Error on decoding %s=%s", key, propertyValue), e);
            return Integer.decode(defaultValue);
        }
    }

}

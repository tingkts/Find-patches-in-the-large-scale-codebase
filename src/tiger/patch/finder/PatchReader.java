package tiger.patch.finder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchReader {

    private static String TAG = PatchReader.class.getSimpleName();

    private static final Pattern PATTERN_PATCH_OPEN = Pattern.compile("// ?\\+\\+\\[([ \\w\\.]+)\\]\\[([\\w\\.]+)\\]\\[([0-9/\\-]+)\\]\\[([ \\w\\.]+)\\] ?(.*)");
    private static final int GROUP_PATCH_CATEGORY = 1;
    private static final int GROUP_PATCH_AUTHOR = 2;
    private static final int GROUP_PATCH_DATE = 3;
    private static final int GROUP_PATCH_MODULE = 4;
    private static final int GROUP_PATCH_TITLE = 5;
    private static final Pattern PATTERN_PATCH_CLOSE = Pattern.compile("// ?[\\-\\~][\\-\\~]\\[([ \\w\\.]+)\\]\\[([\\w\\.]+)\\]\\[([0-9/\\-]+)\\]\\[([ \\w\\.]+)\\] ?(.*)");
    private static final Pattern PATTERN_SUBJECT = Pattern.compile("//\\$ *(.*)");
    private static final int GROUP_SUBJECT_CONTENT = 1;
    private static final Pattern PATTERN_DESCRIPTION = Pattern.compile("//\\: *(.*)");
    private static final int GROUP_DESCRIPTION_CONTENT = 1;

    private int mContextLineNumber;
    private Map<Integer, Patch> mPatchMap = new HashMap<Integer, Patch>();
    private List<SourceLine> mSourceLineCache = new ArrayList<SourceLine>();
    private List<Patch> mOpeningPatchList = new ArrayList<Patch>();
    private Patch mMainOpeningPatch = null;
    private Map<Patch, Integer> mClosingPatchMap = new HashMap<Patch, Integer>();

    public PatchReader(int contextLineNumber) {
        if (contextLineNumber >= 0) {
            mContextLineNumber = contextLineNumber;
        } else {
            throw new IllegalArgumentException("Argument error: contextLineNumber should >= 0");
        }
    }

    public void readFile(File root, File target) throws IOException {
        String source = StringUtility.fromFile(target, "UTF-8");
        String[] lines = source.split("\r?\n|\r");
        String fileName = target.getAbsolutePath().substring(root.getAbsolutePath().length() + 1).replace("\\", "/");
        Log.d(TAG, "Reading file: " + fileName);
        fileStart(fileName);
        for (int index = 0, length = lines.length; index < length; index++) {
            handleSourceLine(fileName, index + 1, lines[index]);
        }
        fileEnd(fileName);
    }

    public Collection<Patch> getPatches() {
        return mPatchMap.values();
    }

    private void fileStart(String file) {
        mSourceLineCache.clear();
        mOpeningPatchList.clear();
        mClosingPatchMap.clear();
        mMainOpeningPatch = null;
    }

    private void handleSourceLine(String file, int index, String line) {
        Patch openingPatch = getOpeningPatch(file, index, line);
        Patch closingPatch = getClosingPatch(file, index, line);
        if (openingPatch != null) {
            if (!mOpeningPatchList.contains(openingPatch)) {
                for (SourceLine cachedSourceLine : mSourceLineCache) {
                    openingPatch.addRelatedLine(file, new RelatedLine(cachedSourceLine, false));
                }
                mMainOpeningPatch = openingPatch;
                mOpeningPatchList.add(openingPatch);
            } else {
                openingPatch.addError(Error.SEVERITY_LOW, "Duplicate opening line before closing.", file, index);
                logErrorLine(file, index, line, "The opening patch has already in mOpeningPatchList: " + openingPatch.toString(Patch.STRING_LEVEL_LITE));
                Log.d(TAG, "Assuming it is a closing patch.");
                // Assume there is something wrong with ++/--
                closingPatch = openingPatch;
                mMainOpeningPatch = null;
                mOpeningPatchList.remove(closingPatch);
                mClosingPatchMap.put(closingPatch, index + mContextLineNumber);
            }
        } else if (closingPatch != null) {
            mMainOpeningPatch = null;
            if (!mOpeningPatchList.remove(closingPatch)) {
                closingPatch.addError(Error.SEVERITY_HIGH, "Cannot find the opening line.", file, index);
                logErrorLine(file, index, line, "The closing patch is not in mOpeningPatchList: " + closingPatch.toString(Patch.STRING_LEVEL_LITE));
            }
            mClosingPatchMap.put(closingPatch, index + mContextLineNumber);
        } else if (mMainOpeningPatch != null) {
            String[] subjectNames = getSubjectNames(line);
            String description = getDescription(line);
            if (subjectNames != null) {
                for (String subjectName : subjectNames) {
                    mMainOpeningPatch.addSubject(Subject.fromName(subjectName.trim()));
                }
            } else if (description != null) {
                mMainOpeningPatch.addDescription(description);
            } else {
                mMainOpeningPatch = null;
            }
        }
        SourceLine sourceLine = new SourceLine(file, index, line);
        if (mSourceLineCache.size() == mContextLineNumber) {
            mSourceLineCache.remove(0);
        }
        mSourceLineCache.add(sourceLine);
        RelatedLine relatedLine = new RelatedLine(sourceLine, true);
        for (Patch patch : mOpeningPatchList) {
            patch.addRelatedLine(file, relatedLine);
        }
        if (closingPatch != null) {
            closingPatch.addRelatedLine(file, relatedLine);
        }
        for (Patch patch : new HashSet<Patch>(mClosingPatchMap.keySet())) {
            if (mClosingPatchMap.get(patch) == index) {
                mClosingPatchMap.remove(patch);
                for (SourceLine cachedSourceLine : mSourceLineCache) {
                    patch.addRelatedLine(file, new RelatedLine(cachedSourceLine, false));
                }
            }
        }
    }

    private Patch getOpeningPatch(String file, int index, String line) {
        Matcher matcher = PATTERN_PATCH_OPEN.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        String category = matcher.group(GROUP_PATCH_CATEGORY);
        String author = matcher.group(GROUP_PATCH_AUTHOR);
        String date = matcher.group(GROUP_PATCH_DATE);
        String module = matcher.group(GROUP_PATCH_MODULE);
        String title = removeTail(matcher.group(GROUP_PATCH_TITLE));
        int hash = Patch.getHash(category, author, date, module, title);
        Patch patch = mPatchMap.get(hash);
        if (patch == null) {
            patch = new Patch(category, author, date, Module.fromName(module), title);
            mPatchMap.put(hash, patch);
            if (!Policy.VALID_CATEGORY_LIST.contains(category)) {
                patch.addError(Error.SEVERITY_LOW, "Invalid category.", null, 0);
                logErrorLine(file, index, line, "Invalid category: " + category);
            }
        }
        patch.increaseSectionCount();
        return patch;
    }

    private Patch getClosingPatch(String file, int index, String line) {
        Matcher matcher = PATTERN_PATCH_CLOSE.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        String category = matcher.group(GROUP_PATCH_CATEGORY);
        String author = matcher.group(GROUP_PATCH_AUTHOR);
        String date = matcher.group(GROUP_PATCH_DATE);
        String module = matcher.group(GROUP_PATCH_MODULE);
        String title = removeTail(matcher.group(GROUP_PATCH_TITLE));
        int hash = Patch.getHash(category, author, date, module, title);
        Patch patch = mPatchMap.get(hash);
        if (patch == null) {
            logErrorLine(file, index, line, "Cannot find the patch in mPatchMap from the closing line (opening line and closing line are not consistent)");
            Log.d(TAG, "Trying to find in mOpeningPatchList...");
            for (int i = mOpeningPatchList.size() - 1; i >= 0; i--) {
                Patch possiblePatch = mOpeningPatchList.get(i);
                if (possiblePatch.getAuthor().equals(author) && possiblePatch.getDate().equals(date)) {
                    possiblePatch.addError(Error.SEVERITY_LOW, "Opening and closing lines mismatch.", file, index);
                    Log.d(TAG, "Similar patch found: " + possiblePatch.toString(Patch.STRING_LEVEL_LITE));
                    return possiblePatch;
                }
            }
        }
        if (patch == null) {
            Log.d(TAG, "Still cannot find patch from closing patch line: " + line);
        }
        return patch;
    }

    private void fileEnd(String file) {
        for (SourceLine cachedSourceLine : mSourceLineCache) {
            if (mClosingPatchMap.isEmpty()) {
                return;
            }
            RelatedLine relatedLine = new RelatedLine(cachedSourceLine, false);
            for (Patch patch : mClosingPatchMap.keySet()) {
                patch.addRelatedLine(file, relatedLine);
            }
        }
        mClosingPatchMap.clear();
        if (!mOpeningPatchList.isEmpty()) {
            Log.e(TAG, "Dumping mOpeningPatchList because it is not empty (" + file + "):");
            for (Patch patch : mOpeningPatchList) {
                patch.addError(Error.SEVERITY_HIGH, "Cannot find the closing line.", file, -1);
                Log.e(TAG, "    " + patch.toString(Patch.STRING_LEVEL_LITE));
            }
        }
    }

    private static String[] getSubjectNames(String line) {
        Matcher matcher = PATTERN_SUBJECT.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        return removeTail(matcher.group(GROUP_SUBJECT_CONTENT)).split(" *[,;] *");
    }

    private static String getDescription(String line) {
        Matcher matcher = PATTERN_DESCRIPTION.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        return removeTail(matcher.group(GROUP_DESCRIPTION_CONTENT));
    }

    private static String removeTail(String target) {
        target = target.trim();
        String tail = "-->";
        if (target.endsWith(tail)) {
            target = target.substring(0, target.length() - tail.length());
        }
        return target.trim();
    }

    private static void logErrorLine(String file, int index, String line, String message) {
        Log.e(TAG, String.format("Error at %s#%s: %s", file, index, line));
        Log.e(TAG, "    " + message);
    }

}

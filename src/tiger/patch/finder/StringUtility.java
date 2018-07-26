package tiger.patch.finder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Read the string from the given source.
 */
public class StringUtility {

    private static final byte[] BUFFER = new byte[4096];

    /**
     * Read the string from the file.
     *
     * @param file The given file.
     * @param encoding The name of a supported {@linkplain java.nio.charset.Charset charset}
     * @return The string.
     * @throws IOException If there is anything wrong while reading the input stream.
     */
    public static String fromFile(File file, String encoding) throws IOException {
        return fromStream(new FileInputStream(file), encoding, true);
    }

    /**
     * Read the string from the input stream.
     *
     * @param inputStream The given input stream.
     * @param encoding The name of a supported {@linkplain java.nio.charset.Charset charset}
     * @param closeStream If the input stream needs to be closed on done reading.
     * @return The string.
     * @throws IOException If there is anything wrong while reading the input stream.
     */
    public static String fromStream(InputStream inputStream, String encoding, boolean closeStream) throws IOException {
        ByteArrayOutputStream oututputStream = new ByteArrayOutputStream();
        int reading = 0;
        while ((reading = inputStream.read(BUFFER)) != -1) {
            oututputStream.write(BUFFER, 0, reading);
        }
        if (closeStream) {
            inputStream.close();
        }
        String out = new String(oututputStream.toByteArray(), encoding);
        return out;
    }

    public static void toFile(File file, String text) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.print(text);
        printWriter.close();
    }

    public static <T> String join(Collection<T> names, String separator) {
        String s = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (T name : names) {
            stringBuilder.append(s);
            stringBuilder.append(name);
            if (s != separator) {
                s = separator;
            }
        }
        return stringBuilder.toString();
    }

}

package core.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * Scans and loads a given file.
 *
 * @author John Paul Quijano
 */
public final class Reader {
    private Reader() {}

    /**
     * Reads the file at the given path.
     *
     * @param path - file path
     * @return the loaded data as a string
     */
    public static String read(String path) {
        String source = "";

        try {
            try (Scanner scanner = new Scanner(new File(path)).useDelimiter("\\Z")) {
                source = scanner.next();
            }
        } catch (FileNotFoundException ex) {
            throw new EngineException("File not found: " + ex);
        }

        return source;
    }

    /**
     * Reads the file at the given url.
     *
     * @param url - file url
     * @return the loaded data as a string
     */
    public static String read(URL url) {
        String source = "";

        try {
            try (Scanner scanner = new Scanner(new File(url.toURI())).useDelimiter("\\Z")) {
                source = scanner.next();
            } catch (URISyntaxException ex) {
                throw new EngineException("File not found: " + ex);
            }
        } catch (FileNotFoundException ex) {
            throw new EngineException("File not found: " + ex);
        }

        return source;
    }
}

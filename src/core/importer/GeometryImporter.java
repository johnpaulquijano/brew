package core.importer;

import core.utility.EngineException;
import core.ShapeGeometry;

/**
 * Base class for all geometry importers.
 *
 * @author John Paul Quijano
 */
public abstract class GeometryImporter {
    private String extension;

    /**
     * @param extension - source file extension
     */
    public GeometryImporter(String extension) {
        this.extension = extension;
    }

    /**
     * Parses the given file then outputs the created geometry.
     *
     * @param path - path to the input file
     *
     * @return the created geometry from the parsed file
     */
    public ShapeGeometry importFile(String path) {
        validate(extension, path);
        return process(path);
    }

    /**
     * Checks if the given file has the given extension.
     *
     * @param ext  - extension to match the given file
     * @param path - path to the input file
     */
    private void validate(String ext, String path) {
        String[] splitPath = path.split("/");
        String[] splitName = splitPath[splitPath.length - 1].split("\\.");

        if (!splitName[splitName.length - 1].equals(ext)) {
            throw new EngineException("Invalid file extension.");
        }
    }

    /**
     * Importer implementation.
     *
     * @param path - path to the input file
     *
     * @return the created geometry from the parsed file
     */
    protected abstract ShapeGeometry process(String path);
}

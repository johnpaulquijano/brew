package core.importer;

import core.utility.EngineException;
import core.animation.Animation;

/**
 * Base class for all animation importers.
 *
 * @author John Paul Quijano
 */
public abstract class AnimationImporter {
    private String extension;

    /**
     * @param extension - source file extension
     */
    public AnimationImporter(String extension) {
        this.extension = extension;
    }

    /**
     * Parses the given file then outputs the created animation.
     *
     * @param path - path to the input file
     *
     * @return the created animation from the parsed file
     */
    public Animation importFile(String path) {
        validate(extension, path);
        return process(path);
    }

    /**
     * Checks if the given file has the given extension.
     *
     * @param ext  - extension to match the given file
     *
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
     * @param path path to the input file
     *
     * @return the created animation from the parsed file
     */
    protected abstract Animation process(String path);
}

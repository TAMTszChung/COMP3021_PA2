package castle.comp3021.assignment.gui.controllers;

import castle.comp3021.assignment.protocol.exception.ResourceNotFoundException;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for loading resources from the filesystem.
 */
public class ResourceLoader {
    /**
     * Path to the resources directory.
     */
    @NotNull
    private static final Path RES_PATH;

    static {
        // TODO: Initialize RES_PATH
        // replace null to the actual path
        String pathToDir = "src/main/resources";
        RES_PATH = Paths.get(pathToDir).toAbsolutePath();
    }

    /**
     * Retrieves a resource file from the resource directory.
     *
     * @param relativePath Path to the resource file, relative to the root of the resource directory.
     * @return Absolute path to the resource file.
     * @throws ResourceNotFoundException If the file cannot be found under the resource directory.
     */
    @NotNull
    public static String getResource(@NotNull final String relativePath) {
        // TODO
        Path resourcePath = RES_PATH.resolve(relativePath).toAbsolutePath();
        File resource = new File(resourcePath.toString());
        if (resource.exists()){
            return resourcePath.toUri().toASCIIString();
        }else{
            throw new ResourceNotFoundException("No file in the path specified " + relativePath);
        }
    }

    /**
     * Return an image {@link Image} object
     * @param typeChar a character represents the type of image needed.
     *                 - 'K': white knight (whiteK.png)
     *                 - 'A': white archer (whiteA.png)
     *                 - 'k': black knight (blackK.png)
     *                 - 'a': black archer (blackA.png)
     *                 - 'c': central x (center.png)
     *                 - 'l': light board (lightBoard.png)
     *                 - 'd': dark board (darkBoard.png)
     * @return an image
     */
    @NotNull
    public static Image getImage(char typeChar) {
        // TODO
        return switch (typeChar) {
            case 'K' -> new Image(ResourceLoader.getResource("assets/images/whiteK.png"));
            case 'A' -> new Image(ResourceLoader.getResource("assets/images/whiteA.png"));
            case 'k' -> new Image(ResourceLoader.getResource("assets/images/blackK.png"));
            case 'a' -> new Image(ResourceLoader.getResource("assets/images/blackA.png"));
            case 'c' -> new Image(ResourceLoader.getResource("assets/images/center.png"));
            case 'l' -> new Image(ResourceLoader.getResource("assets/images/lightBoard.png"));
            case 'd' -> new Image(ResourceLoader.getResource("assets/images/darkBoard.png"));
            default -> throw new ResourceNotFoundException("No image found for specified type: " + typeChar);
        };
    }


}
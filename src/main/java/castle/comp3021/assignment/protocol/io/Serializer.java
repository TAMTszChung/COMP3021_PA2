package castle.comp3021.assignment.protocol.io;


import castle.comp3021.assignment.gui.FXJesonMor;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class exports the entire game configuration and procedure to file
 * You need to overwrite .toString method for the class that will be serialized
 * Hint:
 *      - The output folder should be selected in a popup window {@link javafx.stage.FileChooser}
 *      - Read file with {@link java.io.BufferedWriter}
 */
public class Serializer {
    @NotNull
    private static final Serializer INSTANCE = new Serializer();

    /**
     * @return Singleton instance of this class.
     */
    @NotNull
    public static Serializer getInstance() {
        return INSTANCE;
    }


    /**
     * Save a {@link castle.comp3021.assignment.textversion.JesonMor} to file.
     * @param fxJesonMor a fxJesonMor instance under export
     * @throws IOException if an I/O exception has occurred.
     */
    public void saveToFile(FXJesonMor fxJesonMor) throws IOException {
        //TODO
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(null);

        String gameRecord = fxJesonMor.toString();

        if (file != null){
            if (file.exists()){
                file.delete();
            }

            try{
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try{
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.write(gameRecord);
                printWriter.close();
            } catch (IOException e) {
                throw new IOException(e.getMessage());
            }
        }

    }

}

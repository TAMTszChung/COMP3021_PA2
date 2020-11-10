package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import castle.comp3021.assignment.protocol.io.Deserializer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class ValidationPane extends BasePane{
    @NotNull
    private final VBox leftContainer = new BigVBox();
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Label explanation = new Label("Upload and validation the game history.");
    @NotNull
    private final Button loadButton = new BigButton("Load file");
    @NotNull
    private final Button validationButton = new BigButton("Validate");
    @NotNull
    private final Button replayButton = new BigButton("Replay");
    @NotNull
    private final Button returnButton = new BigButton("Return");

    private Canvas gamePlayCanvas = new Canvas();

    /**
     * store the loaded information
     */
    private Configuration loadedConfiguration;
    private Integer[] storedScores;
    private FXJesonMor loadedGame;
    private Place loadedcentralPlace;
    private ArrayList<MoveRecord> loadedMoveRecords = new ArrayList<>();

    private BooleanProperty isValid = new SimpleBooleanProperty(false);
    private String gameBasicError = null;
    private Thread replayThread = null;
    private boolean exit;

    public ValidationPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        // TODO
        this.leftContainer.getChildren().addAll(title, explanation, loadButton, validationButton, replayButton, returnButton);
        this.centerContainer.getChildren().addAll(gamePlayCanvas);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
        this.loadButton.setDisable(false);
        this.validationButton.setDisable(true);
        this.replayButton.setDisable(true);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * Add callbacks to each buttons.
     * Initially, replay button is disabled, gamePlayCanvas is empty
     * When validation passed, replay button is enabled.
     */
    @Override
    void setCallbacks() {
        //TODO
        this.loadButton.setOnAction(event -> {
            this.replayButton.setDisable(true);
            this.validationButton.setDisable(true);
            boolean loaded = this.loadFromFile();
            if (loaded) {
                this.validationButton.setDisable(false);
            }
        });

        this.validationButton.setOnAction(event -> this.onClickValidationButton());
        this.replayButton.setOnAction(event -> this.onClickReplayButton());
        this.returnButton.setOnAction(event -> this.returnToMainMenu());
    }

    /**
     * load From File and deserializer the game by two steps:
     *      - {@link ValidationPane#getTargetLoadFile}
     *      - {@link Deserializer}
     * Hint:
     *      - Get file from {@link ValidationPane#getTargetLoadFile}
     *      - Instantiate an instance of {@link Deserializer} using the file's path
     *      - Using {@link Deserializer#parseGame()}
     *      - Initialize {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedcentralPlace},
     *                   {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *                   {@link ValidationPane#storedScores}
     * @return whether the file and information have been loaded successfully.
     */
    private boolean loadFromFile() {
        //TODO
        this.resetPane();
        File file = this.getTargetLoadFile();
        if (file == null){
            return false;
        }
        try{
            Deserializer deserializer = new Deserializer(file.toPath());
            deserializer.parseGame();
            this.loadedConfiguration = deserializer.getLoadedConfiguration();
            this.storedScores = deserializer.getStoredScores();
            this.loadedMoveRecords = deserializer.getMoveRecords();
            this.loadedcentralPlace = deserializer.getCentralPlace();
            this.loadedGame = new FXJesonMor(loadedConfiguration);
            return true;
        } catch (FileNotFoundException e) {
            this.showErrorConfiguration(e.getMessage());
            return false;
        }catch (InvalidConfigurationError | InvalidGameException c){
            this.gameBasicError = c.getMessage();
            return true;
        }
    }

    /**
     * When click validation button, validate the loaded game configuration and move history
     * Hint:
     *      - if nothing loaded, call {@link ValidationPane#showErrorMsg}
     *      - if loaded, check loaded content by calling {@link ValidationPane#validateHistory}
     *      - When the loaded file has passed validation, the "replay" button is enabled.
     */
    private void onClickValidationButton(){
        //TODO
        if (gameBasicError != null){
            this.showErrorConfiguration(gameBasicError);
            return;
        }

        if (loadedConfiguration == null || storedScores == null
                || loadedGame == null || loadedcentralPlace == null
                || loadedMoveRecords == null){
            this.showErrorMsg();
            return;
        }

        boolean valid = this.validateHistory();
        if (valid){
            this.passValidationWindow();
            this.isValid.set(true);
            this.validationButton.setDisable(true);
            this.replayButton.setDisable(false);
        }
    }

    /**
     * Display the history of recorded move.
     * Hint:
     *      - You can add a "next" button to render each move, or
     *      - Or you can refer to {@link Task} for implementation.
     */
    private void onClickReplayButton(){
        //TODO
        if (replayThread != null){
            if (replayThread.isAlive()){
                return;
            }
        }

        if (!isValid.get()){
            return;
        }
        Configuration copyConfig = new Configuration(loadedConfiguration.getSize(),
                loadedConfiguration.getPlayers(), loadedConfiguration.getNumMovesProtection());
        this.loadedGame = new FXJesonMor(copyConfig);
        this.gamePlayCanvas.setHeight(loadedConfiguration.getSize() * ViewConfig.PIECE_SIZE);
        this.gamePlayCanvas.setWidth(loadedConfiguration.getSize() * ViewConfig.PIECE_SIZE);
        this.loadedGame.renderBoard(gamePlayCanvas);

        this.exit = false;

        Task<Void> replay = new Task<>() {
            @Override
            protected Void call() {
                if (loadedConfiguration == null || storedScores == null
                        || loadedGame == null || loadedcentralPlace == null
                        || loadedMoveRecords == null) {
                    showErrorMsg();
                    return null;
                }
                ArrayList<MoveRecord> localloadedMoveRecords = loadedMoveRecords;
                FXJesonMor localloadedGame = loadedGame;

                for (MoveRecord mr : localloadedMoveRecords) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (exit) {
                        return null;
                    }
                    Platform.runLater(() -> {
                        localloadedGame.movePiece(mr.getMove());
                        localloadedGame.renderBoard(gamePlayCanvas);
                        AudioManager.getInstance().playSound(AudioManager.SoundRes.PLACE);
                    });
                }
                return null;
            }
        };
        replayThread = new Thread(replay);
        replayThread.setDaemon(true);
        replayThread.start();
    }

    /**
     * Validate the {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedcentralPlace},
     *              {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *              {@link ValidationPane#storedScores}
     * Hint:
     *      - validate configuration of game
     *      - whether each move is valid
     *      - whether scores are correct
     */
    private boolean validateHistory(){
        //TODO
        Configuration tempConfig = null;
        try{
            tempConfig = new Configuration(loadedConfiguration.getSize(),
                    loadedConfiguration.getPlayers(), loadedConfiguration.getNumMovesProtection());
        } catch (InvalidConfigurationError e) {
            this.showErrorConfiguration(e.getMessage());
            return false;
        }

        if (!loadedcentralPlace.equals(loadedConfiguration.getCentralPlace())){
            this.showErrorConfiguration("Invalid Central Place, should be "
                    + loadedConfiguration.getCentralPlace().toString() + " but get " + loadedcentralPlace.toString());
            return false;
        }

        this.loadedGame = new FXJesonMor(tempConfig);
        String errorMessage = null;
        Player winner = null;
        for (int i=0; i < loadedMoveRecords.size(); i++){
            MoveRecord mr = loadedMoveRecords.get(i);
            Player currentPlayer = this.loadedGame.getCurrentPlayer();
            Move currentMove = mr.getMove();
            errorMessage = currentPlayer.validateMove(this.loadedGame, currentMove);

            if (errorMessage != null){
                break;
            }

            try{
                Piece movePiece = this.loadedGame.getPiece(currentMove.getSource());
                this.loadedGame.movePiece(currentMove);
                this.loadedGame.updateScore(currentPlayer, movePiece, currentMove);
                this.loadedGame.playerSwitch();
                winner = this.loadedGame.getWinner(currentPlayer, movePiece, currentMove);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                break;
            }

            if (winner != null && i < this.loadedMoveRecords.size() - 1){
                errorMessage = "Winner achieved before move record ends.";
                break;
            }
        }

        if (errorMessage != null){
            this.showErrorConfiguration(errorMessage);
            return false;
        }

        boolean scoreError = false;
        for (int i=0; i<this.loadedGame.getConfiguration().getPlayers().length; i++){
            if (this.loadedGame.getConfiguration().getPlayers()[i].getScore() != this.storedScores[i]){
                scoreError = true;
                String playerName = this.loadedGame.getConfiguration().getPlayers()[i].getName();
                int recordScore = this.storedScores[i];
                int correctScore = this.loadedGame.getConfiguration().getPlayers()[i].getScore();
                errorMessage = "Player " + playerName + "'s score was incorrect! Recorded: "+ recordScore + ", should be " + correctScore;
                showErrorConfiguration(errorMessage);
            }
        }

        if (scoreError){
            return false;
        }

        return true;
    }

    /**
     * Popup window show error message
     * Hint:
     *      - title: Invalid configuration or game process!
     *      - HeaderText: Due to following reason(s):
     *      - ContentText: errorMsg
     * @param errorMsg error message
     */
    private void showErrorConfiguration(String errorMsg){
        // TODO
        Alert errorConfig = new Alert(Alert.AlertType.ERROR);
        errorConfig.setTitle("Invalid configuration or game process!");
        errorConfig.setHeaderText("Due to following reason(s):");
        errorConfig.setContentText(errorMsg);
        errorConfig.showAndWait();
    }

    /**
     * Pop up window to warn no record has been uploaded.
     * Hint:
     *      - title: Error!
     *      - ContentText: You haven't loaded a record, Please load first.
     */
    private void showErrorMsg(){
        //TODO
        Alert errorWindow = new Alert(Alert.AlertType.ERROR);
        errorWindow.setTitle("Error!");
        errorWindow.setContentText("You haven't loaded a record, Please load first.");
        errorWindow.showAndWait();
    }

    /**
     * Pop up window to show pass the validation
     * Hint:
     *     - title: Confirm
     *     - HeaderText: Pass validation!
     */
    private void passValidationWindow(){
        //TODO
        Alert passValidation = new Alert(Alert.AlertType.CONFIRMATION);
        passValidation.setTitle("Confirm");
        passValidation.setHeaderText("Pass validation!");
        passValidation.getButtonTypes().setAll(ButtonType.OK);
        passValidation.showAndWait();
    }

    /**
     * Return to Main menu
     * Hint:
     *  - Before return, clear the rendered canvas, and clear stored information
     */
    private void returnToMainMenu(){
        // TODO
        resetPane();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }


    /**
     * Prompts the user for the file to load.
     * <p>
     * Hint:
     * Use {@link FileChooser} and {@link FileChooser#setSelectedExtensionFilter(FileChooser.ExtensionFilter)}.
     *
     * @return {@link File} to load, or {@code null} if the operation is canceled.
     */
    @Nullable
    private File getTargetLoadFile() {
        //TODO
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        return fileChooser.showOpenDialog(null);
    }

    private void resetPane(){
        this.exit = true;
        this.replayThread = null;

        this.loadedConfiguration = null;
        this.storedScores = null;
        this.loadedGame = null;
        this.loadedcentralPlace = null;
        this.loadedMoveRecords = null;
        this.gameBasicError = null;

        gamePlayCanvas.getGraphicsContext2D().clearRect(0,0,gamePlayCanvas.getWidth(),gamePlayCanvas.getHeight());
        this.gamePlayCanvas.setWidth(0.0);
        this.gamePlayCanvas.setHeight(0.0);

        this.loadButton.setDisable(false);
        this.validationButton.setDisable(true);
        this.replayButton.setDisable(true);

        this.isValid.set(false);
    }
}

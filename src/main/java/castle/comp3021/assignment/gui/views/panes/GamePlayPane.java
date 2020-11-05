package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.GameplayInfoPane;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.gui.controllers.Renderer;
import castle.comp3021.assignment.protocol.io.Serializer;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

/**
 * This class implements the main playing function of Jeson Mor
 * The necessary components have been already defined (e.g., topBar, title, buttons).
 * Basic functions:
 *      - Start game and play, update scores
 *      - Restart the game
 *      - Return to main menu
 *      - Elapsed Timer (ticking from 00:00 -> 00:01 -> 00:02 -> ...)
 *          - The format is defined in {@link GameplayInfoPane#formatTime(int)}
 * Requirement:
 *      - The game should be initialized by configuration passed from {@link GamePane}, instead of the default configuration
 *      - The information of the game (including scores, current player name, ect.) is implemented in {@link GameplayInfoPane}
 *      - The center canvas (defined as gamePlayCanvas) should be disabled when current player is computer
 * Bonus:
 *      - A countdown timer (if this is implemented, then elapsed timer can be either kept or removed)
 *      - The format of countdown timer is defined in {@link GameplayInfoPane#countdownFormat(int)}
 *      - If one player runs out of time of each round {@link DurationTimer#getDefaultEachRound()}, then the player loses the game.
 * Hint:
 *      - You may find it useful to synchronize javafx UI-thread using {@link javafx.application.Platform#runLater}
 */ 

public class GamePlayPane extends BasePane {
    @NotNull
    private final HBox topBar = new HBox(20);
    @NotNull
    private final SideMenuVBox leftContainer = new SideMenuVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Text parameterText = new Text();
    @NotNull
    private final BigButton returnButton = new BigButton("Return");
    @NotNull
    private final BigButton startButton = new BigButton("Start");
    @NotNull
    private final BigButton restartButton = new BigButton("Restart");
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label historyLabel = new Label("History");

    @NotNull
    private final Text historyFiled = new Text();
    @NotNull
    private final ScrollPane scrollPane = new ScrollPane();

    /**
     * time passed in seconds
     * Hint:
     *      - Bind it to time passed in {@link GameplayInfoPane}
     */
    private final IntegerProperty ticksElapsed = new SimpleIntegerProperty();

    @NotNull
    private final Canvas gamePlayCanvas = new Canvas();

    private GameplayInfoPane infoPane = null;

    /**
     * You can add more necessary variable here.
     * Hint:
     *      - the passed in {@link FXJesonMor}
     *      - other global variable you want to note down.
     */
    // TODO
    FXJesonMor currentGame = null;
    Place startPlace = null;
    Place endPlace = null;
    Player winner = null;


    public GamePlayPane() {
        connectComponents();
        styleComponents();
        setCallbacks();

    }

    /**
     * Components are added, adjust it by your own choice
     */
    @Override
    void connectComponents() {
        //TODO
        this.topBar.getChildren().addAll(title);
        this.topBar.setAlignment(Pos.CENTER);

        this.leftContainer.getChildren().addAll(parameterText, historyLabel,
                scrollPane, startButton, restartButton, returnButton);

        this.centerContainer.getChildren().addAll(gamePlayCanvas);

        this.setTop(topBar);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
    }

    /**
     * style of title and scrollPane have been set up, no need to add more
     */
    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(ViewConfig.WIDTH / 4.0, ViewConfig.HEIGHT / 3.0 );
        scrollPane.setContent(historyFiled);
    }

    /**
     * The listeners are added here.
     */
    @Override
    void setCallbacks() {
        //TODO
        this.gamePlayCanvas.setOnMousePressed(this::onCanvasPressed);
        this.gamePlayCanvas.setOnMouseDragged(this::onCanvasDragged);
        this.gamePlayCanvas.setOnMouseReleased(this::onCanvasReleased);

        this.startButton.setOnAction(event -> {
            this.startButton.setDisable(true);
            this.restartButton.setDisable(false);
            this.currentGame.startCountdown();
            this.startGame();
        });

        this.restartButton.setOnAction(event -> this.onRestartButtonClick());

        this.returnButton.setOnAction(event -> this.doQuitToMenuAction());
    }

    /**
     * Set up necessary initialization.
     * Hint:
     *      - Set buttons enable/disable
     *          - Start button: enable
     *          - restart button: disable
     *      - This function can be invoked before {@link GamePlayPane#startGame()} for setting up
     *
     * @param fxJesonMor pass in an instance of {@link FXJesonMor}
     */
    void initializeGame(@NotNull FXJesonMor fxJesonMor) {
        //TODO
        if (currentGame != null){
            this.endGame();
        }

        this.currentGame = fxJesonMor;
        currentGame.getConfiguration().setAllInitialPieces();

        this.startButton.setDisable(false);
        this.restartButton.setDisable(true);
        this.disnableCanvas();
        this.startPlace = null;
        this.endPlace = null;
        this.winner = null;

        StringBuilder parameters = new StringBuilder();
        parameters.append("Parameters:");
        parameters.append("\n\nSize of board: ");
        parameters.append(this.currentGame.getConfiguration().getSize());
        parameters.append("\nNum of protection moves: ");
        parameters.append(this.currentGame.getConfiguration().getNumMovesProtection());
        //player0 info
        parameters.append("\nPlayer ");
        Player player0 = this.currentGame.getConfiguration().getPlayers()[0];
        parameters.append(player0.getName());
        if (this.currentGame.getConfiguration().isFirstPlayerHuman()){
            parameters.append("(human)");
        }else{
            parameters.append("(computer)");
        }
        //player1 info
        Player player1 = this.currentGame.getConfiguration().getPlayers()[1];
        parameters.append("\nPlayer ");
        parameters.append(player1.getName());
        if (this.currentGame.getConfiguration().isSecondPlayerHuman()){
            parameters.append("(human)");
        }else{
            parameters.append("(computer)");
        }

        int boardsize = this.currentGame.getConfiguration().getSize();
        this.parameterText.setText(parameters.toString());

        this.infoPane = new GameplayInfoPane(currentGame.getPlayer1Score(), currentGame.getPlayer2Score(),
                currentGame.getCurPlayerName(), ticksElapsed);
        HBox.setHgrow(this.infoPane, Priority.ALWAYS);
        this.centerContainer.getChildren().add(infoPane);

        this.gamePlayCanvas.setWidth(boardsize*ViewConfig.PIECE_SIZE);
        this.gamePlayCanvas.setHeight(boardsize*ViewConfig.PIECE_SIZE);

        this.currentGame.addOnTickHandler(() -> Platform.runLater(() -> this.ticksElapsed.set(this.ticksElapsed.get()+1)));

        this.currentGame.addOnTimeupHandler(() -> {
            if (this.winner == null) {
                Platform.runLater(this::startGame);
            }
            if (this.ticksElapsed.get() >= DurationTimer.getDefaultEachRound() - 1) {
                AudioManager.getInstance().playSound(AudioManager.SoundRes.LOSE);
                showLoseAlert();
                Platform.runLater(() -> this.ticksElapsed.set(0));
                currentGame.stopCountdown();
            }
        });
        this.currentGame.renderBoard(this.gamePlayCanvas);
    }

    /**
     * enable canvas clickable
     */
    private void enableCanvas(){
        gamePlayCanvas.setDisable(false);
    }

    /**
     * disable canvas clickable
     */
    private void disnableCanvas(){
        gamePlayCanvas.setDisable(true);
    }

    /**
     * After click "start" button, everything will start from here
     * No explicit skeleton is given here.
     * Hint:
     *      - Give a carefully thought to how to activate next round of play
     *      - When a new {@link Move} is acquired, it needs to be check whether this move is valid.
     *          - If is valid, make the move, render the {@link GamePlayPane#gamePlayCanvas}
     *          - If is invalid, abort the move
     *          - Update score, add the move to {@link GamePlayPane#historyFiled}, also record the move
     *          - Move forward to next player
     *      - The player can be either computer or human, when the computer is playing, disable {@link GamePlayPane#gamePlayCanvas}
     *      - You can add a button to enable next move once current move finishes.
     *          - or you can add handler when mouse is released
     *          - or you can take advantage of timer to automatically change player. (Bonus)
     */
    public void startGame() {
        //TODO
    }

    /**
     * Restart the game
     * Hint: end the current game and start a new game
     */
    private void onRestartButtonClick(){
        //TODO
        Configuration currentConfig = currentGame.getConfiguration();
        this.endGame();
        FXJesonMor newGame = new FXJesonMor(currentConfig);
        this.initializeGame(newGame);
        this.disnableCanvas();
    }

    /**
     * Add mouse pressed handler here.
     * Play click.mp3
     * draw a rectangle at clicked board tile to show which tile is selected
     * Hint:
     *      - Highlight the selected board cell using {@link Renderer#drawRectangle(GraphicsContext, double, double)}
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse click
     */
    private void onCanvasPressed(MouseEvent event){
        // TODO
        double startX = toBoardCoordinate(event.getX());
        double startY = toBoardCoordinate(event.getY());
        this.startPlace = new Place((int)startX, (int)startY);

        GraphicsContext currentGC = gamePlayCanvas.getGraphicsContext2D();
        Renderer.drawRectangle(currentGC, startX*ViewConfig.PIECE_SIZE, startY*ViewConfig.PIECE_SIZE);
        AudioManager.getInstance().playSound(AudioManager.SoundRes.CLICK);
    }

    /**
     * When mouse dragging, draw a path
     * Hint:
     *      - When mouse dragging, you can use {@link Renderer#drawOval(GraphicsContext, double, double)} to show the path
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse position
     */
    private void onCanvasDragged(MouseEvent event){
        //TODO
        GraphicsContext currentGC = gamePlayCanvas.getGraphicsContext2D();
        Renderer.drawOval(currentGC, event.getX(), event.getY());
    }

    /**
     * Mouse release handler
     * Hint:
     *      - When mouse released, a {@link Move} is completed, you can either validate and make the move here, or somewhere else.
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     *      - If the piece has been successfully moved, play place.mp3 here (or somewhere else)
     * @param event mouse release
     */
    private void onCanvasReleased(MouseEvent event){
        // TODO
        double endX = toBoardCoordinate(event.getX());
        double endY = toBoardCoordinate(event.getY());
        this.endPlace = new Place((int)endX, (int)endY);
        this.currentGame.renderBoard(gamePlayCanvas);
    }

    /**
     * Creates a popup which tells the winner
     */
    private void createWinPopup(String winnerName){
        //TODO
        AudioManager.getInstance().playSound(AudioManager.SoundRes.WIN);
        this.endGame();
        Alert winAlert = new Alert(Alert.AlertType.CONFIRMATION);
        winAlert.setTitle("Congratulations!");
        winAlert.setContentText(winnerName + " wins!");
        winAlert.showAndWait();
    }


    /**
     * check winner, if winner comes out, then play the win.mp3 and popup window.
     * The window has three options:
     *      - Start New Game: the same function as clicking "restart" button
     *      - Export Move Records: Using {@link castle.comp3021.assignment.protocol.io.Serializer} to write game's configuration to file
     *      - Return to Main menu, using {@link GamePlayPane#doQuitToMenuAction()}
     */
    private void checkWinner(){
        //TODO
    }

    /**
     * Popup a window showing invalid move information
     * @param errorMsg error string stating why this move is invalid
     */
    private void showInvalidMoveMsg(String errorMsg){
        //TODO
        Alert invlidMoveAlert = new Alert(Alert.AlertType.ERROR);
        invlidMoveAlert.setHeaderText("Your movement is invalid due to following reason(s):");
        invlidMoveAlert.setContentText(errorMsg);
        invlidMoveAlert.showAndWait();
    }

    /**
     * Before actually quit to main menu, popup a alert window to double check
     * Hint:
     *      - title: Confirm
     *      - HeaderText: Return to menu?
     *      - ContentText: Game progress will be lost.
     *      - Buttons: CANCEL and OK
     *  If click OK, then refer to {@link GamePlayPane#doQuitToMenu()}
     *  If click Cancle, than do nothing.
     */
    private void doQuitToMenuAction() {
        // TODO
        Alert quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        quitAlert.setTitle("Confirm");
        quitAlert.setHeaderText("Return to menu?");
        quitAlert.setContentText("Game progress will be lost.");
        quitAlert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        Optional<ButtonType> result = quitAlert.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals(ButtonType.OK)) {
                doQuitToMenu();
            }
        }
    }

    /**
     * Update the move to the historyFiled
     * @param move the last move that has been made
     */
    private void updateHistoryField(Move move){
        //TODO
        String lastMove = "["
                + move.getSource().x()
                + ", " + move.getSource().y()
                + "] -> ["+ move.getDestination().x()
                + ", "+ move.getDestination().x()+ "]";
        String oldText = historyFiled.getText();
        historyFiled.setText(oldText + "\n" + lastMove);
    }

    /**
     * Go back to main menu
     * Hint: before quit, you need to end the game
     */
    private void doQuitToMenu() {
        // TODO
        this.endGame();
        this.currentGame = null;
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    /**
     * Converting a vertical or horizontal coordinate x to the coordinate in board
     * Hint:
     *      The pixel size of every piece is defined in {@link ViewConfig#PIECE_SIZE}
     * @param x coordinate of mouse click
     * @return the coordinate on board
     */
    private int toBoardCoordinate(double x){
        // TODO
        return (int) (Math.floor(x/ViewConfig.PIECE_SIZE));
    }

    /**
     * Handler of ending a game
     * Hint:
     *      - Clear the board, history text field
     *      - Reset buttons
     *      - Reset timer
     *
     */
    private void endGame() {
        //TODO
        //clear board, history field, infoPane
        this.gamePlayCanvas.getGraphicsContext2D().clearRect(0,0,gamePlayCanvas.getWidth(), gamePlayCanvas.getHeight());
        this.gamePlayCanvas.setHeight(0.0);
        this.gamePlayCanvas.setWidth(0.0);
        this.historyFiled.setText("");
        this.centerContainer.getChildren().remove(this.infoPane);

        //reset timer
        if (this.currentGame != null){
            this.currentGame.stopCountdown();
        }

        //rest Pane component
        this.ticksElapsed.set(0);

        this.infoPane = null;
        this.currentGame = null;

        //reset button
        this.startButton.setDisable(false);
        this.restartButton.setDisable(true);
        this.disnableCanvas();

        this.startPlace = null;
        this.endPlace = null;
        this.winner = null;
    }

    private void showWinAlert(){
        //alert
        ButtonType startnewGame = new ButtonType("Start New Game");
        ButtonType export = new ButtonType("Export Move Records");
        ButtonType returnMainMenu = new ButtonType("Return to Main Menu");

        Alert timesup = new Alert(Alert.AlertType.CONFIRMATION);
        timesup.setTitle("Congratulations!");
        timesup.setContentText(this.winner.getName() + " wins!");
        timesup.getButtonTypes().setAll(returnMainMenu, export, startnewGame);
        ButtonType result = timesup.showAndWait().orElseThrow();
        switch (result.getText()) {
            case "Start New Game" -> this.onRestartButtonClick();
            case "Export Move Records" -> {
                try {
                    Serializer.getInstance().saveToFile(this.currentGame);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.onRestartButtonClick();
            }
            case "Return to Main Menu" -> this.doQuitToMenu();
        }
    }

    private void showLoseAlert(){
        //alert
        ButtonType startnewGame = new ButtonType("Start New Game");
        ButtonType export = new ButtonType("Export Move Records");
        ButtonType returnMainMenu = new ButtonType("Return to Main Menu");

        Alert timesup = new Alert(Alert.AlertType.CONFIRMATION);
        timesup.setTitle("Sorry! Time's out!");
        timesup.setContentText(this.currentGame.getCurrentPlayer().getName() + " Lose!");
        timesup.getButtonTypes().setAll(returnMainMenu, export, startnewGame);
        ButtonType result = timesup.showAndWait().orElseThrow();
        switch (result.getText()) {
            case "Start New Game" -> this.onRestartButtonClick();
            case "Export Move Records" -> {
                try {
                    Serializer.getInstance().saveToFile(this.currentGame);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.onRestartButtonClick();
            }
            case "Return to Main Menu" -> this.doQuitToMenu();
        }
    }
}

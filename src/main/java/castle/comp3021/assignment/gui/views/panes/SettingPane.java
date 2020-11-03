package castle.comp3021.assignment.gui.views.panes;
import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.AudioManager;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.NumberTextField;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.protocol.Configuration;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SettingPane extends BasePane {
    @NotNull
    private final Label title = new Label("Jeson Mor <Game Setting>");
    @NotNull
    private final Button saveButton = new BigButton("Save");
    @NotNull
    private final Button returnButton = new BigButton("Return");
    @NotNull
    private final Button isHumanPlayer1Button = new BigButton("Player 1: ");
    @NotNull
    private final Button isHumanPlayer2Button = new BigButton("Player 2: ");
    @NotNull
    private final Button toggleSoundButton = new BigButton("Sound FX: Enabled");

    @NotNull
    private final VBox leftContainer = new SideMenuVBox();

    @NotNull
    private final NumberTextField sizeFiled = new NumberTextField(String.valueOf(globalConfiguration.getSize()));

    @NotNull
    private final BorderPane sizeBox = new BorderPane(null, null, sizeFiled, null, new Label("Board size"));

    @NotNull
    private final NumberTextField durationField = new NumberTextField(String.valueOf(DurationTimer.getDefaultEachRound()));
    @NotNull
    private final BorderPane durationBox = new BorderPane(null, null, durationField, null,
            new Label("Max Duration (s)"));

    @NotNull
    private final NumberTextField numMovesProtectionField =
            new NumberTextField(String.valueOf(globalConfiguration.getNumMovesProtection()));
    @NotNull
    private final BorderPane numMovesProtectionBox = new BorderPane(null, null,
            numMovesProtectionField, null, new Label("Steps of protection"));

    @NotNull
    private final VBox centerContainer = new BigVBox();
    @NotNull
    private final TextArea infoText = new TextArea(ViewConfig.getAboutText());


    public SettingPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Add components to corresponding containers
     */
    @Override
    void connectComponents() {
        //TODO
        this.leftContainer.getChildren().addAll(title, sizeBox, numMovesProtectionBox, durationBox,
                isHumanPlayer1Button, isHumanPlayer2Button, toggleSoundButton, saveButton, returnButton);
        this.centerContainer.getChildren().addAll(infoText);
        this.setLeft(leftContainer);
        this.setCenter(centerContainer);
        fillValues();
    }

    @Override
    void styleComponents() {
        infoText.getStyleClass().add("text-area");
        infoText.setEditable(false);
        infoText.setWrapText(true);
        infoText.setPrefHeight(ViewConfig.HEIGHT);
    }

    /**
     * Add handlers to buttons, textFields.
     * Hint:
     *  - Text of {@link SettingPane#isHumanPlayer1Button}, {@link SettingPane#isHumanPlayer2Button},
     *            {@link SettingPane#toggleSoundButton} should be changed accordingly
     *  - You may use:
     *      - {@link Configuration#isFirstPlayerHuman()},
     *      - {@link Configuration#isSecondPlayerHuman()},
     *      - {@link Configuration#setFirstPlayerHuman(boolean)}
     *      - {@link Configuration#isSecondPlayerHuman()},
     *      - {@link AudioManager#setEnabled(boolean)},
     *      - {@link AudioManager#isEnabled()},
     */
    @Override
    void setCallbacks() {
        //TODO
        this.isHumanPlayer1Button.setOnAction(event -> {
            globalConfiguration.setFirstPlayerHuman(!globalConfiguration.isFirstPlayerHuman());
            if (globalConfiguration.isFirstPlayerHuman()){
                this.isHumanPlayer1Button.setText("Player 1: Human");
            }else{
                this.isHumanPlayer1Button.setText("Player 1: Computer");
            }
        });

        this.isHumanPlayer2Button.setOnAction(event -> {
            globalConfiguration.setSecondPlayerHuman(!globalConfiguration.isSecondPlayerHuman());
            if (globalConfiguration.isSecondPlayerHuman()){
                this.isHumanPlayer2Button.setText("Player 2: Human");
            }else{
                this.isHumanPlayer2Button.setText("Player 2: Computer");
            }
        });

        this.toggleSoundButton.setOnAction(event -> {
            AudioManager audioMan = AudioManager.getInstance();
            boolean state = audioMan.isEnabled();
            audioMan.setEnabled(!state);
            if (audioMan.isEnabled()){
                this.toggleSoundButton.setText("Sound FX: Enabled");
            }else{
                this.toggleSoundButton.setText("Sound FX: Disabled");
            }
        });

        this.saveButton.setOnAction(event -> {
            int inputSize = this.sizeFiled.getValue();
            int inputnumProtection = this.numMovesProtectionField.getValue();
            int inputDuration = this.durationField.getValue();
            Optional<String> error = validate(inputSize, inputnumProtection, inputDuration);
            if (error.isPresent()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("Validation Failed");
                alert.setContentText(error.get());
                alert.showAndWait();
            }else{
                this.returnToMainMenu(true);
            }
        });

        this.returnButton.setOnAction(event -> this.returnToMainMenu(false));
    }

    /**
     * Fill in the default values for all editable fields.
     */
    private void fillValues() {
        // TODO
        this.sizeFiled.setText(String.valueOf(globalConfiguration.getSize()));
        this.numMovesProtectionField.setText(String.valueOf(globalConfiguration.getNumMovesProtection()));
        this.durationField.setText(String.valueOf(DurationTimer.getDefaultEachRound()));

        if (globalConfiguration.isFirstPlayerHuman()){
            this.isHumanPlayer1Button.setText("Player 1: Human");
        }else{
            this.isHumanPlayer1Button.setText("Player 1: Computer");
        }

        if (globalConfiguration.isSecondPlayerHuman()){
            this.isHumanPlayer2Button.setText("Player 2: Human");
        }else{
            this.isHumanPlayer2Button.setText("Player 2: Computer");
        }
    }

    /**
     * Switches back to the {@link MainMenuPane}.
     *
     * @param writeBack Whether to save the values present in the text fields to their respective classes.
     */
    private void returnToMainMenu(final boolean writeBack) {
        //TODO
        if (writeBack){
            globalConfiguration.setSize(this.sizeFiled.getValue());
            globalConfiguration.setNumMovesProtection(this.numMovesProtectionField.getValue());
            DurationTimer.setDefaultEachRound(this.durationField.getValue());
        }
        fillValues();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    /**
     * Validate the text fields
     * The useful msgs are predefined in {@link ViewConfig#MSG_BAD_SIZE_NUM}, etc.
     * @param size number in {@link SettingPane#sizeFiled}
     * @param numProtection number in {@link SettingPane#numMovesProtectionField}
     * @param duration number in {@link SettingPane#durationField}
     * @return If validation failed, {@link Optional} containing the reason message; An empty {@link Optional}
     *      * otherwise.
     */
    public static Optional<String> validate(int size, int numProtection, int duration) {
        //TODO
        if (size < 3) {
            return Optional.of("Size of game board must be at least 3");
        }
        if (size % 2 != 1) {
            return Optional.of("Size of game board must be an odd number");
        }
        if (size > 26) {
            return Optional.of("Size of game board is at most 26");
        }

        if (numProtection < 0) {
            return Optional.of("Steps of protection should be greater than or equal to 0.");
        }

        if (duration < 0){
            return Optional.of("Duration of each round should be greater than 0.");
        }

        return Optional.empty();
    }
}

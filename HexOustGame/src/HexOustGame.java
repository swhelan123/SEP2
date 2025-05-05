import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane; // Base class for root
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL; // Needed for CSS loading

/**
 * Main application class for the HexOust game.
 * Sets up the JavaFX stage and scene, manages screen transitions
 * (main menu, game, settings), loads CSS, initializes sounds,
 * and handles theme application.
 * @author [Group 34 WheMurPap] // <-- Update with your actual group name/members
 * @version 1.2
 * @since 2025-05-05 // <-- Adjust date if needed
 */
public class HexOustGame extends Application {

    /** The primary stage of the JavaFX application. */
    private Stage primaryStage;

    /** The user interface component for the main game view. */
    private HexUI gameUi;

    /** The main scene used to display different views (menu, game, settings). */
    private Scene mainScene;

    /** Service to open URLs in the default browser. */
    private HostServices hostServices;

    /** The filename for the application's CSS stylesheet. */
    private static final String CSS_FILE = "styles.css"; // CSS Filename

    /**
     * The main entry point for all JavaFX applications.
     * Initializes the primary stage, loads sounds, sets the default theme,
     * and displays the main menu.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.hostServices = getHostServices();

        // Initialize Managers
        SoundManager.loadSounds(); // Load sounds
        ThemeManager.setCurrentTheme(Theme.DARK); // Set default theme

        showMainMenu(); // Show the main menu first
    }

    /**
     * Applies the application's CSS stylesheet and the current theme's
     * specific style class to the given root node of a scene.
     * This ensures that UI elements are styled according to the selected theme.
     * It removes any previous theme classes before applying the new one.
     *
     * @param root The root node (usually a Pane or its subclass) of the scene
     * to which the styles should be applied.
     */
    private void applyThemeAndCSS(Pane root) {
        // Clear existing theme classes
        root.getStyleClass().removeIf(style -> style.startsWith("theme-"));
        // Add current theme class
        root.getStyleClass().add(ThemeManager.getThemeStyleClass());

        // Load CSS if not already loaded (or reload if necessary)
        URL cssUrl = getClass().getResource(CSS_FILE);
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!root.getStylesheets().contains(css)) {
                root.getStylesheets().add(css);
            }
            // Optional: Force reload if styles don't update dynamically
            // root.getStylesheets().remove(css);
            // root.getStylesheets().add(css);
        } else {
            System.err.println("Warning: CSS file not found: " + CSS_FILE);
        }
    }

    /**
     * Creates and displays the Main Menu scene.
     * Configures the layout, title label, and buttons (Play, How to Play, Settings, Exit)
     * with appropriate actions and CSS styling. Sets this menu as the root of the main scene.
     */
    public void showMainMenu() {
        VBox menuLayout = new VBox(30);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        // Assign ID for CSS styling
        menuLayout.setId("main-menu-layout");

        // Title
        Label titleLabel = new Label("HexOust");
        titleLabel.setId("main-title"); // ID for CSS

        // Buttons
        Button playButton = new Button("Play");
        playButton.getStyleClass().addAll("menu-button", "play-button"); // Style classes
        playButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            startGame();
        });

        Button howToButton = new Button("How to Play");
        howToButton.getStyleClass().addAll("menu-button", "info-button");
        howToButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            openHowToPlayLink();
        });

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().addAll("menu-button", "secondary-button");
        settingsButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            showSettingsMenu();
        });

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().addAll("menu-button", "exit-button");
        exitButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            Platform.exit();
        });

        // Add elements
        menuLayout.getChildren().addAll(titleLabel, playButton, howToButton, settingsButton, exitButton);

        // Apply CSS and Theme
        applyThemeAndCSS(menuLayout);

        // Create or update the main scene
        if (mainScene == null) {
            mainScene = new Scene(menuLayout, 900, 900);
        } else {
            mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, gameKeyListener);
            mainScene.setRoot(menuLayout);
        }

        primaryStage.setTitle("HexOust - Main Menu");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Creates and displays the Settings Menu scene.
     * Allows the user to change the visual theme of the application.
     * Includes a ChoiceBox for theme selection and a Back button.
     */
    private void showSettingsMenu() {
        VBox settingsLayout = new VBox(25);
        settingsLayout.setAlignment(Pos.CENTER);
        settingsLayout.setPadding(new Insets(50));
        settingsLayout.setId("settings-menu-layout"); // ID for CSS

        // Title
        Label titleLabel = new Label("Settings");
        titleLabel.setId("settings-title"); // ID for CSS

        // Theme Selection Label
        Label themeLabel = new Label("Select Theme:");
        themeLabel.getStyleClass().add("settings-label"); // Style class

        // Theme ChoiceBox
        ChoiceBox<Theme> themeSelector = new ChoiceBox<>();
        themeSelector.getItems().addAll(Theme.values());
        themeSelector.setValue(ThemeManager.getCurrentTheme());
        themeSelector.getStyleClass().add("theme-selector"); // Style class

        // Apply theme change on selection
        themeSelector.setOnAction(e -> {
            SoundManager.playSound("button-click");
            ThemeManager.setCurrentTheme(themeSelector.getValue());
            // Redraw the current settings menu to reflect the change immediately
            showSettingsMenu();
        });

        // Back Button
        Button backButton = new Button("Back to Menu");
        backButton.getStyleClass().addAll("menu-button", "secondary-button"); // Reuse menu button styles
        backButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            showMainMenu();
        });

        // Add elements
        settingsLayout.getChildren().addAll(titleLabel, themeLabel, themeSelector, backButton);
        VBox.setMargin(backButton, new Insets(40, 0, 0, 0));

        // Apply CSS and Theme
        applyThemeAndCSS(settingsLayout);

        // Set this layout as the root of the main scene
        mainScene.setRoot(settingsLayout);
        primaryStage.setTitle("HexOust - Settings");
    }


    /**
     * Opens the "How to Play" URL (linking to the HexOust rules on mindsports.nl)
     * in the system's default web browser. Uses HostServices if available,
     * otherwise falls back to java.awt.Desktop.
     */
    private void openHowToPlayLink() {
        final String url = "https://mindsports.nl/index.php/the-pit/614-hexoust";
        try {
            if (hostServices != null) {
                // Preferred way in JavaFX
                hostServices.showDocument(url);
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // Fallback for environments where HostServices might not be available
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.err.println("Cannot open link: Desktop browsing not supported and HostServices unavailable.");
            }
        } catch (IOException | URISyntaxException ex) { // *** FIXED: Added catch block ***
            System.err.println("Failed to open link: " + url);
            ex.printStackTrace();
        }
    } // *** FIXED: Added closing brace for method ***


    /**
     * Sets up and switches the scene to the actual game UI (HexUI).
     * Creates a new Board and Layout for the game instance.
     * Applies the current theme and adds a key listener for pausing.
     */
    public void startGame() {
        // Create new instances for a new game
        Board board = new Board(6); // Example radius
        // Centered layout for a 900x900 scene, considering top bar height
        Layout layout = new Layout(Layout.FLAT, 35, 450, 450 - (50 / 2)); // Adjust origin Y
        gameUi = new HexUI(board, layout, this); // Pass 'this' (HexOustGame instance)

        // Apply CSS and Theme to the game UI root (which is a StackPane)
        applyThemeAndCSS(gameUi);

        // Ensure the main scene exists before setting the root
        if (mainScene == null) {
            // If starting directly into game (e.g., for testing), create scene
            mainScene = new Scene(gameUi, 900, 900);
            primaryStage.setScene(mainScene);
            primaryStage.setResizable(false);
            primaryStage.show(); // Show stage if not shown before
        } else {
            mainScene.setRoot(gameUi);
        }

        // Add key listener for pausing the game
        mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, gameKeyListener); // Remove first just in case
        mainScene.addEventHandler(KeyEvent.KEY_PRESSED, gameKeyListener);

        primaryStage.setTitle("HexOust Game");

        // Initial drawing of the board
        gameUi.drawBoard();
    }

    /**
     * Key event handler specifically for the game screen.
     * Listens for the ESCAPE key to toggle the pause state via the HexUI.
     */
    private final javafx.event.EventHandler<KeyEvent> gameKeyListener = event -> {
        // Only toggle pause if the game UI is active and ESCAPE is pressed
        if (event.getCode() == KeyCode.ESCAPE && gameUi != null && mainScene.getRoot() == gameUi) {
            gameUi.togglePause(); // Call the togglePause method in HexUI
        }
    };

    /**
     * The main method, launching the JavaFX application.
     * This is the standard entry point for Java applications.
     *
     * @param args Command line arguments passed to the application. Not used by HexOust.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

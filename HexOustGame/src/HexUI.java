import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import java.util.List;

/**
 * Represents the user interface for the HexOust game, handling drawing and user input.
 * Extends {@link StackPane} to allow layering of overlays (like pause menu, victory screen)
 * over the main game area. Applies themes via CSS classes managed by {@link ThemeManager}.
 * Contains the main game view including the board canvas, status bar, and interactive elements.
 *
 * @see GameLogic for game rules implementation.
 * @see Board for game state representation.
 * @see Layout for coordinate conversions.
 * @see ThemeManager for visual styling.
 * @see SoundManager for audio feedback.
 * @author [Group 34 WheMurPap]
 */
public class HexUI extends StackPane {
    private final Board board;
    private final Layout layout;
    private final Canvas canvas;
    private Cell hoveredCell = null;
    private Stone currentStone = Stone.RED; // Red starts
    private final Label statusLabel;
    private final Label turnIndicator;
    private boolean gameOver = false;
    private boolean paused = false;
    private StackPane victoryScreen;
    private StackPane pauseMenu;
    private final HexOustGame game; // Reference back to main application for navigation
    private final HBox topBar;
    private final BorderPane gamePane; // BorderPane to hold game elements (top bar + canvas)
    private final double TOP_BAR_HEIGHT = 50.0; // Height of the top status bar

    /**
     * Constructs the main game UI.
     * Initializes the board, layout, canvas, status bar, and overlays.
     * Sets up mouse event handlers for interaction.
     *
     * @param board The game {@link Board} instance containing the cell data. Must not be null.
     * @param layout The {@link Layout} instance for coordinate conversions. Must not be null.
     * @param game The main {@link HexOustGame} application instance, used for navigation actions
     * (like returning to menu or restarting). Must not be null.
     * @throws NullPointerException if board, layout, or game is null.
     */
    public HexUI(Board board, Layout layout, HexOustGame game) {
        if (board == null || layout == null || game == null) {
            throw new NullPointerException("Board, Layout, and Game instances cannot be null.");
        }
        this.board = board;
        this.layout = layout;
        // Canvas size adjusted for the top bar
        this.canvas = new Canvas(900, 900 - TOP_BAR_HEIGHT);
        this.game = game;

        // Apply the base theme class for CSS styling
        this.getStyleClass().add(ThemeManager.getThemeStyleClass());

        // --- Create Top Status Bar ---
        statusLabel = new Label("Game in progress");
        statusLabel.getStyleClass().add("status-label");

        turnIndicator = new Label(); // Text set by updateTurnIndicator
        turnIndicator.getStyleClass().add("turn-indicator");

        Button pauseButton = new Button("⏸"); // Pause symbol
        pauseButton.getStyleClass().add("pause-button");
        pauseButton.setOnAction(e -> togglePause());

        HBox topLeftControls = new HBox(15, statusLabel, turnIndicator);
        topLeftControls.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region(); // Pushes pause button to the right
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar = new HBox(10, topLeftControls, spacer, pauseButton);
        topBar.getStyleClass().add("top-bar");
        topBar.setPrefHeight(TOP_BAR_HEIGHT);
        topBar.setMaxHeight(TOP_BAR_HEIGHT); // Prevent resizing
        topBar.setAlignment(Pos.CENTER); // Center items vertically
        topBar.setPadding(new Insets(5, 15, 5, 15)); // Padding around the bar

        // --- Setup Main Layout ---
        gamePane = new BorderPane();
        gamePane.setTop(topBar);
        gamePane.setCenter(canvas);
        // Prevent BorderPane from intercepting mouse events meant for overlays
        gamePane.setPickOnBounds(false);
        gamePane.getStyleClass().add("game-pane"); // For potential specific styling

        // Add the game pane (canvas + top bar) to the StackPane (this)
        this.getChildren().add(gamePane);

        // --- Setup Event Handlers ---
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseClicked(this::handleMouseClicked);

        // --- Create Overlays (initially hidden) ---
        createPauseMenu();
        createVictoryScreen();

        // --- Initial State ---
        updateTurnIndicator(); // Set initial turn text/color
        // Initial board drawing happens in HexOustGame after scene is set
    }

    /**
     * Draws the current state of the game board onto the canvas.
     * Clears the canvas, then iterates through each cell, drawing its outline
     * and any stone placed on it. Also handles highlighting for hovered cells
     * (legal moves, potential captures) and capturable stones based on the
     * current theme colors provided by {@link ThemeManager}.
     */
    public void drawBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear previous frame

        // Get potentially capturable cells for highlighting (only if game is active)
        List<Cell> capturableCells = (gameOver || paused) ? List.of() : GameLogic.getCapturableCells(board, currentStone);

        for (Cell cell : board.getCells()) {
            List<Point2D> corners = layout.polygonCorners(cell);
            int n = corners.size();
            double[] xs = new double[n], ys = new double[n];
            for (int i = 0; i < n; i++) { xs[i] = corners.get(i).x; ys[i] = corners.get(i).y; }

            // --- Hover Highlighting (only if game is active) ---
            if (!gameOver && !paused && cell == hoveredCell) {
                // Check if hovering over a valid empty cell for placement
                if (cell.stone == null && GameLogic.isMoveLegal(cell, board, currentStone)) {
                    gc.setFill(ThemeManager.getHighlightColor("legal"));
                    gc.fillPolygon(xs, ys, n);
                }
                // Check if hovering over an opponent stone that could be captured by placement (less common highlight)
                // Note: Direct capture highlighting is handled below near stone drawing
                // else if (cell.stone != null && cell.stone != currentStone && capturableCells.contains(cell)) {
                //     gc.setFill(ThemeManager.getHighlightColor("capture")); // Optional hover for capturable
                //     gc.fillPolygon(xs, ys, n);
                // }
            }

            // --- Draw Hexagon Outline ---
            gc.setStroke(ThemeManager.getHexOutlineColor());
            gc.setLineWidth(1.5);
            gc.strokePolygon(xs, ys, n);

            // --- Draw Stone and Capture Highlight ---
            if (cell.stone != null) {
                Point2D center = layout.hexToPixel(cell);

                // Highlight capturable opponent stones (draw highlight behind stone)
                if (capturableCells.contains(cell) && !paused && !gameOver && cell.stone != currentStone) {
                    gc.setFill(ThemeManager.getCaptureHighlightColor());
                    double highlightRadius = 16; // Slightly larger than stone
                    gc.fillOval(center.x - highlightRadius, center.y - highlightRadius, highlightRadius * 2, highlightRadius * 2);
                }

                // Draw the stone itself
                double stoneRadius = 12;
                gc.setFill(cell.stone.getColor());
                gc.fillOval(center.x - stoneRadius, center.y - stoneRadius, stoneRadius * 2, stoneRadius * 2);

                // Draw stone border for definition
                gc.setStroke(ThemeManager.getStoneBorderColor());
                gc.setLineWidth(1.0);
                gc.strokeOval(center.x - stoneRadius, center.y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
            }
        }
    }

    /**
     * Handles mouse movement over the canvas.
     * Finds the cell closest to the mouse cursor and updates the {@code hoveredCell} state.
     * Redraws the board if the hovered cell changes, to update highlighting.
     * Does nothing if the game is over or paused.
     * @param e The MouseEvent containing cursor coordinates.
     */
    private void handleMouseMoved(MouseEvent e) {
        if (gameOver || paused) {
            // If game is inactive, ensure no cell remains highlighted
            if (hoveredCell != null) {
                hoveredCell = null;
                drawBoard(); // Redraw once to remove highlight
            }
            return;
        }
        Cell newlyHovered = board.findCellClosest(e.getX(), e.getY(), layout);
        if (newlyHovered != hoveredCell) {
            hoveredCell = newlyHovered;
            drawBoard(); // Redraw to show/hide highlight
        }
    }

    /**
     * Handles mouse click events on the canvas to process player moves.
     * Determines the clicked cell and attempts either a stone placement or a direct capture.
     * Uses {@link GameLogic} to validate moves and process captures.
     * Updates game state (current player, board stones), plays sounds via {@link SoundManager},
     * checks for win conditions, and redraws the board.
     * Does nothing if the game is over or paused.
     *
     * @param e The MouseEvent containing click coordinates.
     */
    private void handleMouseClicked(MouseEvent e) {
        if (gameOver || paused) {
            return; // Ignore clicks when game is inactive
        }

        Cell clickedCell = board.findCellClosest(e.getX(), e.getY(), layout);
        if (clickedCell == null) {
            return; // Click was likely outside any cell boundary
        }

        // Check if the clicked cell is an opponent stone that is currently capturable
        List<Cell> capturableCells = GameLogic.getCapturableCells(board, currentStone);

        // --- Direct Capture Logic ---
        // If the clicked cell has an opponent's stone AND it's in the list of directly capturable cells
        if (clickedCell.stone != null && clickedCell.stone != currentStone && capturableCells.contains(clickedCell)) {
            SoundManager.playSound("capture-stone");
            clickedCell.stone = null; // Remove the captured stone

            // Check if this capture wins the game
            if (checkWinCondition()) {
                showVictory(currentStone); // Display victory screen
            }
            // Important: Turn does NOT change on a direct capture action
            drawBoard(); // Redraw to show the removed stone
            return; // Action complete
        }

        // --- Placement Logic ---
        // If the clicked cell is empty
        if (clickedCell.stone == null) {
            boolean legal = GameLogic.isMoveLegal(clickedCell, board, currentStone);

            if (legal) {
                // Process the move (places stone, handles captures resulting from placement)
                int moveResult = GameLogic.processMove(clickedCell, board, currentStone);

                if (moveResult > 0) { // Move was successful (1 = placement, 2 = placement + capture)
                    SoundManager.playSound("place-stone");

                    if (moveResult == 2) { // Placement caused a capture
                        SoundManager.playSound("capture-stone");
                        // Check if this capture wins the game
                        if (checkWinCondition()) {
                            showVictory(currentStone);
                        }
                        // Important: Turn does NOT change if placement causes capture
                    } else { // moveResult == 1 (Simple placement, no capture)
                        // Switch turn ONLY if placement did NOT cause a capture
                        currentStone = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
                        updateTurnIndicator();
                        // No win check here, as simple placement cannot win
                    }

                } else { // moveResult == 0 (Should not happen if isMoveLegal passed, but handle defensively)
                    System.err.println("Warning: processMove returned 0 after isMoveLegal was true for cell: " + clickedCell);
                    SoundManager.playSound("illegal-move"); // Play sound indicating issue
                }
            } else { // Move is illegal according to GameLogic
                SoundManager.playSound("illegal-move");
            }
        } else { // Clicked on an occupied cell (either own stone or non-capturable opponent stone)
            // Optionally play a sound for clicking on a non-actionable cell
            // SoundManager.playSound("illegal-move"); // Or a different "thud" sound
        }

        // Redraw the board after any action (or inaction if move was illegal)
        // unless the game just ended (victory screen handles final state)
        if (!gameOver) {
            drawBoard();
        } else {
            // If game just ended, ensure final board state is drawn before victory screen appears fully
            drawBoard();
        }
    }


    /**
     * Updates the turn indicator label's text and text color to reflect the current player.
     */
    private void updateTurnIndicator() {
        turnIndicator.setText("Turn: " + currentStone);
        turnIndicator.setTextFill(currentStone.getColor()); // Set text color to match player
    }

    /**
     * Checks if the game has reached a win condition.
     * A player wins if the opponent has zero stones left on the board.
     * Sets the {@code gameOver} flag and updates the status label if a win occurs.
     * Plays the win sound.
     *
     * @return {@code true} if a win condition is met, {@code false} otherwise.
     */
    private boolean checkWinCondition() {
        Stone opponent = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
        int opponentStones = board.countStones(opponent);
        if (opponentStones == 0) {
            gameOver = true;
            statusLabel.setText(currentStone + " WINS!"); // Update status bar
            SoundManager.playSound("win-game");
            return true;
        }
        return false;
    }

    /**
     * Creates the victory screen overlay (a {@link StackPane}) containing
     * the win message and buttons for replay or returning to the menu.
     * The screen is initially hidden.
     */
    private void createVictoryScreen() {
        victoryScreen = new StackPane();
        victoryScreen.setVisible(false); // Initially hidden
        // Allow clicks to pass through to the canvas when hidden
        victoryScreen.setPickOnBounds(false);
        // Add it to the main UI StackPane (this)
        this.getChildren().add(victoryScreen);
        // Ensure it's added after gamePane so it appears on top when visible
    }

    /**
     * Makes the victory screen visible and populates it with the winner's information
     * and action buttons (Play Again, Return to Menu).
     * If the game was paused, it unpauses it first.
     *
     * @param winner The {@link Stone} color of the winning player.
     */
    private void showVictory(Stone winner) {
        if (paused) { togglePause(); } // Ensure pause menu is hidden

        // Clear previous content if any (e.g., from a previous game)
        victoryScreen.getChildren().clear();

        // --- Create Overlay Background ---
        Rectangle overlay = new Rectangle();
        overlay.getStyleClass().add("overlay"); // Style from CSS for dimming effect
        // Bind size to the main UI size
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        // Make overlay transparent to mouse events so buttons underneath are clickable
        overlay.setMouseTransparent(true);

        // --- Create Content Box ---
        Label winLbl = new Label(winner + " WINS!");
        winLbl.setTextFill(winner.getColor()); // Use winner's color
        winLbl.getStyleClass().add("victory-title"); // Style from CSS

        Button replayButton = new Button("Play Again");
        replayButton.getStyleClass().addAll("popup-button", "replay-button"); // CSS classes
        replayButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            victoryScreen.setVisible(false); // Hide screen
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            board.reset(); // Reset the board state
            game.startGame(); // Tell main app to set up a new game UI
        });

        Button menuButton = new Button("Return to Menu");
        menuButton.getStyleClass().addAll("popup-button", "return-menu-button"); // CSS classes
        menuButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            victoryScreen.setVisible(false); // Hide screen
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            // No need to reset board here as main menu will create a new game later
            game.showMainMenu(); // Tell main app to show the main menu
        });

        HBox buttonBox = new HBox(30, replayButton, menuButton); // Horizontal layout for buttons
        buttonBox.setAlignment(Pos.CENTER);

        VBox contentVBox = new VBox(40, winLbl, buttonBox); // Vertical layout for title + buttons
        contentVBox.setAlignment(Pos.CENTER);
        contentVBox.setPadding(new Insets(40));

        // Wrap content in a styled pane
        StackPane contentPane = new StackPane(contentVBox);
        contentPane.getStyleClass().add("popup-content"); // Base style for popups
        // Make the content pane intercept mouse clicks so clicks outside don't go to canvas
        contentPane.setPickOnBounds(true);

        // --- Add Overlay and Content to Victory Screen ---
        victoryScreen.getChildren().addAll(overlay, contentPane);
        victoryScreen.setVisible(true); // Make it visible
        victoryScreen.toFront(); // Ensure it's on top of the game pane
    }

    /**
     * Toggles the pause state of the game.
     * Shows or hides the pause menu overlay. Plays a sound effect.
     * If pausing, clears the hovered cell highlight. If resuming, redraws the board.
     * Does nothing if the game is already over.
     */
    public void togglePause() {
        if (gameOver) return; // Cannot pause/unpause if game is finished

        paused = !paused;
        pauseMenu.setVisible(paused); // Show/hide the pause menu StackPane

        if (paused) {
            SoundManager.playSound("button-click");
            pauseMenu.toFront(); // Ensure pause menu is on top
            // Clear hover effect when pausing
            if (hoveredCell != null) {
                hoveredCell = null;
                drawBoard(); // Redraw to remove highlight
            }
        } else {
            // Resuming
            SoundManager.playSound("button-click");
            drawBoard(); // Redraw the board in its current state
        }
    }

    /**
     * Creates the pause menu overlay (a {@link StackPane}) containing the "PAUSED" title
     * and buttons for resuming, restarting, or returning to the main menu.
     * The menu is initially hidden.
     */
    private void createPauseMenu() {
        // --- Create Overlay Background ---
        Rectangle overlay = new Rectangle();
        overlay.getStyleClass().add("overlay"); // Style from CSS
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        overlay.setMouseTransparent(true); // Allow clicks on buttons below

        // --- Create Content Box ---
        Label pauseTitle = new Label("PAUSED");
        pauseTitle.getStyleClass().add("pause-title"); // Style from CSS

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().addAll("popup-button", "resume-button"); // CSS classes
        resumeButton.setOnAction(e -> togglePause()); // Resume simply toggles pause off

        Button restartButton = new Button("Restart Game");
        restartButton.getStyleClass().addAll("popup-button", "restart-button"); // CSS classes
        restartButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            if (paused) { togglePause(); } // Unpause visually before restarting
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            board.reset(); // Reset board state
            game.startGame(); // Tell main app to set up new game UI
        });

        Button menuButton = new Button("Return to Menu");
        menuButton.getStyleClass().addAll("popup-button", "return-menu-button"); // CSS classes
        menuButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            if (paused) { togglePause(); } // Unpause visually before going to menu
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            // No board reset needed here
            game.showMainMenu(); // Tell main app to show menu
        });

        VBox buttonsVBox = new VBox(25, resumeButton, restartButton, menuButton); // Vertical layout for buttons
        buttonsVBox.setAlignment(Pos.CENTER);

        VBox contentVBox = new VBox(45, pauseTitle, buttonsVBox); // Layout for title + buttons
        contentVBox.setAlignment(Pos.CENTER);
        contentVBox.setPadding(new Insets(40));

        // Wrap content in a styled pane
        StackPane contentPane = new StackPane(contentVBox);
        contentPane.getStyleClass().add("popup-content"); // Base style
        contentPane.getStyleClass().add("pause-popup"); // Specific style if needed
        contentPane.setPickOnBounds(true); // Intercept clicks

        // --- Create Pause Menu StackPane ---
        pauseMenu = new StackPane(overlay, contentPane);
        pauseMenu.setVisible(false); // Initially hidden
        // Allow clicks to pass through to canvas when hidden
        pauseMenu.setPickOnBounds(false);

        // Add pause menu to the main UI StackPane (this)
        this.getChildren().add(pauseMenu);
        // Ensure it's added after gamePane so it appears on top when visible
    }
}

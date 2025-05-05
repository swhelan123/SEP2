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
 * Extends StackPane for layering overlays. Applies Themes via CSS classes.
 * Contains the main game view including the board canvas, status bar, and overlays.
 *
 * @see GameLogic for game rules.
 * @see Board for game state representation.
 * @author [Group 34 WheMurPap] // <-- Add your group name/members
 * @version 1.5 // Version incremented
 * @since 2025-05-05 // <-- Adjust date if needed
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
    private final HexOustGame game; // Reference back to main application
    private final HBox topBar;
    private final BorderPane gamePane; // BorderPane to hold game elements
    private final double TOP_BAR_HEIGHT = 50.0; // Height of the top status bar

    // Constructor remains the same...
    public HexUI(Board board, Layout layout, HexOustGame game) {
        if (board == null || layout == null || game == null) {
            throw new NullPointerException("Board, Layout, and Game instances cannot be null.");
        }
        this.board = board;
        this.layout = layout;
        this.canvas = new Canvas(900, 900 - TOP_BAR_HEIGHT);
        this.game = game;

        this.getStyleClass().add(ThemeManager.getThemeStyleClass());

        statusLabel = new Label("Game in progress");
        statusLabel.getStyleClass().add("status-label");

        turnIndicator = new Label();
        turnIndicator.getStyleClass().add("turn-indicator");

        Button pauseButton = new Button("⏸");
        pauseButton.getStyleClass().add("pause-button");
        pauseButton.setOnAction(e -> togglePause());

        HBox topControls = new HBox(15, statusLabel, turnIndicator);
        topControls.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar = new HBox(10, topControls, spacer, pauseButton);
        topBar.getStyleClass().add("top-bar");
        topBar.setPrefHeight(TOP_BAR_HEIGHT);
        topBar.setMaxHeight(TOP_BAR_HEIGHT);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(5, 15, 5, 15));

        gamePane = new BorderPane();
        gamePane.setTop(topBar);
        gamePane.setCenter(canvas);
        gamePane.setPickOnBounds(false);
        gamePane.getStyleClass().add("game-pane");

        this.getChildren().add(gamePane);

        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseClicked(this::handleMouseClicked);

        createPauseMenu();
        createVictoryScreen();

        updateTurnIndicator();
    }

    // drawBoard method remains the same...
    public void drawBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        List<Cell> capturableCells = GameLogic.getCapturableCells(board, currentStone);

        for (Cell cell : board.getCells()) {
            List<Point2D> corners = layout.polygonCorners(cell);
            int n = corners.size();
            double[] xs = new double[n], ys = new double[n];
            for (int i = 0; i < n; i++) { xs[i] = corners.get(i).x; ys[i] = corners.get(i).y; }

            if (!gameOver && !paused) {
                if (cell == hoveredCell) {
                    if (cell.stone == null && GameLogic.isMoveLegal(cell, board, currentStone)) {
                        gc.setFill(ThemeManager.getHighlightColor("legal"));
                        gc.fillPolygon(xs, ys, n);
                    } else if (cell.stone != null && cell.stone != currentStone && capturableCells.contains(cell)) {
                        gc.setFill(ThemeManager.getHighlightColor("capture"));
                        gc.fillPolygon(xs, ys, n);
                    }
                }
            }

            gc.setStroke(ThemeManager.getHexOutlineColor());
            gc.setLineWidth(1.5);
            gc.strokePolygon(xs, ys, n);

            if (cell.stone != null) {
                Point2D center = layout.hexToPixel(cell);
                if (capturableCells.contains(cell) && !paused && !gameOver) {
                    gc.setFill(ThemeManager.getCaptureHighlightColor());
                    double highlightRadius = 16;
                    gc.fillOval(center.x - highlightRadius, center.y - highlightRadius, highlightRadius * 2, highlightRadius * 2);
                }
                double stoneRadius = 12;
                gc.setFill(cell.stone.getColor());
                gc.fillOval(center.x - stoneRadius, center.y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                gc.setStroke(ThemeManager.getStoneBorderColor());
                gc.setLineWidth(1.0);
                gc.strokeOval(center.x - stoneRadius, center.y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
            }
        }
    }

    // handleMouseMoved method remains the same...
    private void handleMouseMoved(MouseEvent e) {
        if (gameOver || paused) {
            if (hoveredCell != null) {
                hoveredCell = null;
                drawBoard();
            }
            return;
        }
        Cell newlyHovered = board.findCellClosest(e.getX(), e.getY(), layout);
        if (newlyHovered != hoveredCell) {
            hoveredCell = newlyHovered;
            drawBoard();
        }
    }

    /**
     * Handles mouse click events on the canvas to process player moves (placement or capture).
     * @param e The MouseEvent.
     */
    private void handleMouseClicked(MouseEvent e) {
        if (gameOver || paused) {
            return;
        }

        Cell clickedCell = board.findCellClosest(e.getX(), e.getY(), layout);
        if (clickedCell == null) {
            return;
        }

        List<Cell> capturableCells = GameLogic.getCapturableCells(board, currentStone);

        // --- Capture Logic ---
        if (clickedCell.stone != null && clickedCell.stone != currentStone && capturableCells.contains(clickedCell)) {
            SoundManager.playSound("capture-stone");
            clickedCell.stone = null;

            if (checkWinCondition()) {
                showVictory(currentStone);
            }
            // No turn change on direct capture
            drawBoard();
            return;
        }

        // --- Placement Logic ---
        if (clickedCell.stone == null) { // Check if empty first
            boolean legal = GameLogic.isMoveLegal(clickedCell, board, currentStone);

            if (legal) {
                int moveResult = GameLogic.processMove(clickedCell, board, currentStone);

                if (moveResult > 0) {
                    SoundManager.playSound("place-stone");

                    if (moveResult == 2) { // Placement caused capture
                        SoundManager.playSound("capture-stone");
                        if (checkWinCondition()) {
                            showVictory(currentStone);
                        }
                        // Turn does NOT change
                    } else { // moveResult == 1 (Simple placement)
                        // Switch turn ONLY if no capture occurred
                        currentStone = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
                        updateTurnIndicator();
                        // No win check here
                    }

                } else { // moveResult == 0 (Error)
                    SoundManager.playSound("illegal-move");
                }
            } else { // Move is illegal
                SoundManager.playSound("illegal-move");
            }
        } else { // Clicked on occupied/non-capturable cell
            // Optionally play a sound here too?
            // SoundManager.playSound("illegal-move");
        }

        // Redraw logic at the end
        if (!gameOver) {
            drawBoard();
        } else {
            // If game just ended, do one final draw before showing victory screen fully
            drawBoard();
        }
    }


    /**
     * Updates the turn indicator label text and color.
     */
    private void updateTurnIndicator() {
        turnIndicator.setText("Turn: " + currentStone);
        turnIndicator.setTextFill(currentStone.getColor());
    }

    /**
     * Checks if the game has reached a win condition (opponent has no stones).
     * @return true if a win condition is met, false otherwise.
     */
    private boolean checkWinCondition() {
        Stone opponent = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
        int opponentStones = board.countStones(opponent);
        if (opponentStones == 0) {
            gameOver = true;
            statusLabel.setText(currentStone + " WINS!");
            SoundManager.playSound("win-game");
            return true;
        }
        return false;
    }

    // createVictoryScreen method remains the same...
    private void createVictoryScreen() {
        victoryScreen = new StackPane();
        victoryScreen.setVisible(false);
        victoryScreen.setPickOnBounds(false);
        this.getChildren().add(victoryScreen);
    }

    // showVictory method remains the same...
    private void showVictory(Stone winner) {
        if (paused) { togglePause(); }

        victoryScreen.getChildren().clear();

        Rectangle overlay = new Rectangle();
        overlay.getStyleClass().add("overlay");
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        overlay.setMouseTransparent(true);

        Label winLbl = new Label(winner + " WINS!");
        winLbl.setTextFill(winner.getColor());
        winLbl.getStyleClass().add("victory-title");

        Button replayButton = new Button("Play Again");
        replayButton.getStyleClass().addAll("popup-button", "replay-button");
        replayButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            victoryScreen.setVisible(false);
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            game.startGame(); // Start a new game
        });

        Button menuButton = new Button("Return to Menu");
        menuButton.getStyleClass().addAll("popup-button", "return-menu-button");
        menuButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            victoryScreen.setVisible(false);
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            game.showMainMenu(); // Go back to main menu
        });

        HBox buttonBox = new HBox(30, replayButton, menuButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox contentVBox = new VBox(40, winLbl, buttonBox);
        contentVBox.setAlignment(Pos.CENTER);
        contentVBox.setPadding(new Insets(40));

        StackPane contentPane = new StackPane(contentVBox);
        contentPane.getStyleClass().add("popup-content");
        contentPane.setPickOnBounds(true);

        victoryScreen.getChildren().addAll(overlay, contentPane);
        victoryScreen.setVisible(true);
        victoryScreen.toFront();
    }

    // togglePause method remains the same...
    public void togglePause() {
        if (gameOver) return;

        paused = !paused;
        pauseMenu.setVisible(paused);

        if (paused) {
            SoundManager.playSound("button-click");
            pauseMenu.toFront();
            if (hoveredCell != null) {
                hoveredCell = null;
                drawBoard();
            }
        } else {
            SoundManager.playSound("button-click");
            drawBoard();
        }
    }

    // createPauseMenu method remains the same...
    private void createPauseMenu() {
        Rectangle overlay = new Rectangle();
        overlay.getStyleClass().add("overlay");
        overlay.widthProperty().bind(this.widthProperty());
        overlay.heightProperty().bind(this.heightProperty());
        overlay.setMouseTransparent(true);

        Label pauseTitle = new Label("PAUSED");
        pauseTitle.getStyleClass().add("pause-title");

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().addAll("popup-button", "resume-button");
        resumeButton.setOnAction(e -> togglePause());

        Button restartButton = new Button("Restart Game");
        restartButton.getStyleClass().addAll("popup-button", "restart-button");
        restartButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            if (paused) { togglePause(); }
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            game.startGame();
        });

        Button menuButton = new Button("Return to Menu");
        menuButton.getStyleClass().addAll("popup-button", "return-menu-button");
        menuButton.setOnAction(e -> {
            SoundManager.playSound("button-click");
            if (paused) { togglePause(); }
            gameOver = false; // Reset game over flag
            currentStone = Stone.RED; // Reset starting player
            game.showMainMenu();
        });

        VBox buttonsVBox = new VBox(25, resumeButton, restartButton, menuButton);
        buttonsVBox.setAlignment(Pos.CENTER);

        VBox contentVBox = new VBox(45, pauseTitle, buttonsVBox);
        contentVBox.setAlignment(Pos.CENTER);
        contentVBox.setPadding(new Insets(40));

        StackPane contentPane = new StackPane(contentVBox);
        contentPane.getStyleClass().add("popup-content");
        contentPane.getStyleClass().add("pause-popup");
        contentPane.setPickOnBounds(true);

        pauseMenu = new StackPane(overlay, contentPane);
        pauseMenu.setVisible(false);
        pauseMenu.setPickOnBounds(false);

        this.getChildren().add(pauseMenu);
    }
}

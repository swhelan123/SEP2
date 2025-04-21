import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HexOustGame extends Application {

    @Override
    public void start(Stage primaryStage) {
        Board board = new Board(6);
        Layout layout = new Layout(Layout.FLAT, 35, 450, 450);
        HexUI ui = new HexUI(board, layout);

        BorderPane root = new BorderPane();
        root.setCenter(ui);

        Scene scene = new Scene(root, 900, 900);
        primaryStage.setTitle("HexOust Game");
        primaryStage.setScene(scene);
        primaryStage.show();
        ui.drawBoard();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

enum Stone {
    RED,
    BLUE;

    public Color getColor() {
        return (this == RED) ? Color.RED : Color.BLUE;
    }

    @Override
    public String toString() {
        return (this == RED) ? "Red" : "Blue";
    }
}

class Cell {

    public final int q, r, s;
    public Stone stone = null;

    public Cell(int q, int r, int s) {
        if (q + r + s != 0) throw new IllegalArgumentException("q + r + s must be 0");
        this.q = q;
        this.r = r;
        this.s = s;
    }
}

class Board {

    private final List<Cell> cells = new ArrayList<>();

    public Board(int radius) {
        for (int q = -radius; q <= radius; q++) {
            for (int r = -radius; r <= radius; r++) {
                int s = -q - r;
                if (Math.abs(s) <= radius) {
                    cells.add(new Cell(q, r, s));
                }
            }
        }
    }

    public List<Cell> getCells() {
        return cells;
    }

    public Cell findCellClosest(double x, double y, Layout layout) {
        Cell closest = null;
        double minDist = Double.MAX_VALUE;
        for (Cell cell : cells) {
            Point2D c = layout.hexToPixel(cell.q, cell.r, cell.s);
            double dx = c.x - x, dy = c.y - y;
            double dist = dx * dx + dy * dy;
            if (dist < minDist) {
                minDist = dist;
                closest = cell;
            }
        }
        return closest;
    }

    public int countStones(Stone color) {
        int count = 0;
        for (Cell cell : cells) {
            if (cell.stone == color) {
                count++;
            }
        }
        return count;
    }
}

class Orientation {

    public final double f0, f1, f2, f3;
    public final double b0, b1, b2, b3;
    public final double startAngle;

    public Orientation(double f0, double f1, double f2, double f3, double b0, double b1, double b2, double b3, double startAngle) {
        this.f0 = f0;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.startAngle = startAngle;
    }
}

class Layout {

    public static final Orientation FLAT = new Orientation(3.0 / 2.0, 0.0, Math.sqrt(3.0) / 2.0, Math.sqrt(3.0), 2.0 / 3.0, 0.0, -1.0 / 3.0, Math.sqrt(3.0) / 3.0, 0.0);

    public final Orientation orientation;
    public final double size, originX, originY;

    public Layout(Orientation orientation, double size, double originX, double originY) {
        this.orientation = orientation;
        this.size = size;
        this.originX = originX;
        this.originY = originY;
    }

    public Point2D hexToPixel(int q, int r, int s) {
        double x = (orientation.f0 * q + orientation.f1 * r) * size + originX;
        double y = (orientation.f2 * q + orientation.f3 * r) * size + originY;
        return new Point2D(x, y);
    }

    public List<Point2D> polygonCorners(Cell cell) {
        List<Point2D> corners = new ArrayList<>();
        Point2D center = hexToPixel(cell.q, cell.r, cell.s);
        for (int i = 0; i < 6; i++) {
            Point2D offset = hexCornerOffset(i);
            corners.add(new Point2D(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }

    private Point2D hexCornerOffset(int corner) {
        double angle = (2.0 * Math.PI * (orientation.startAngle - corner)) / 6.0;
        double ox = size * Math.cos(angle);
        double oy = size * Math.sin(angle);
        return new Point2D(ox, oy);
    }
}

class HexUI extends BorderPane {

    private final Board board;
    private final Layout layout;
    private final Canvas canvas;
    private Cell hoveredCell = null;
    private Stone currentStone = Stone.RED;
    private final Label statusLabel;
    private final Label turnIndicator;
    private boolean gameOver = false;
    private int moveCount = 0;

    public HexUI(Board board, Layout layout) {
        this.board = board;
        this.layout = layout;
        this.canvas = new Canvas(900, 900);

        // Create top panel with status label and turn indicator
        statusLabel = new Label("Game in progress");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        turnIndicator = new Label("Current Turn: RED");
        turnIndicator.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        turnIndicator.setTextFill(Color.RED);

        VBox topPanel = new VBox(10);
        topPanel.getChildren().addAll(statusLabel, turnIndicator);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPrefHeight(50);

        setTop(topPanel);

        StackPane canvasHolder = new StackPane(canvas);
        setCenter(canvasHolder);

        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseClicked(this::handleMouseClicked);

        updateTurnIndicator();
    }

    public void drawBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Cell cell : board.getCells()) {
            List<Point2D> corners = layout.polygonCorners(cell);
            int n = corners.size();
            double[] xPoints = new double[n];
            double[] yPoints = new double[n];
            for (int i = 0; i < n; i++) {
                xPoints[i] = corners.get(i).x;
                yPoints[i] = corners.get(i).y;
            }
            if (cell == hoveredCell && !gameOver && GameLogic.isMoveLegal(cell, board, currentStone)) {
                gc.setFill(Color.LIGHTGREEN);
                gc.fillPolygon(xPoints, yPoints, n);
            }
            gc.setStroke(Color.BLACK);
            gc.strokePolygon(xPoints, yPoints, n);
            if (cell.stone != null) {
                Point2D c = layout.hexToPixel(cell.q, cell.r, cell.s);
                gc.setFill(cell.stone.getColor());
                gc.fillOval(c.x - 12, c.y - 12, 24, 24);
            }
        }
    }

    private void handleMouseMoved(MouseEvent e) {
        if (gameOver) return;

        double mx = e.getX();
        double my = e.getY();
        hoveredCell = board.findCellClosest(mx, my, layout);
        if (hoveredCell != null && !GameLogic.isMoveLegal(hoveredCell, board, currentStone)) {
            hoveredCell = null;
        }
        drawBoard();
    }

    private void handleMouseClicked(MouseEvent e) {
        if (gameOver) return;

        double mx = e.getX();
        double my = e.getY();
        Cell c = board.findCellClosest(mx, my, layout);
        if (c != null && GameLogic.isMoveLegal(c, board, currentStone)) {
            int result = GameLogic.processMove(c, board, currentStone);
            if (result > 0) { // Valid move
                moveCount++;

                // Check win condition only if capturing occurred (result == 2)
                // AND if both players have had a chance to place stones (moveCount > 2)
                if (result == 2 && moveCount > 2) {
                    Stone opponent = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
                    int opponentStoneCount = board.countStones(opponent);

                    if (opponentStoneCount == 0) {
                        gameOver = true;
                        statusLabel.setText(currentStone + " player wins!");
                    }
                }

                // Turn handling:
                if (result == 1) {
                    // non-capturing: alternate turn
                    currentStone = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
                    updateTurnIndicator();
                } else {
                    // capturing: same player goes again
                    updateTurnIndicator();
                }
            }
        }
        drawBoard();
    }

    private void updateTurnIndicator() {
        turnIndicator.setText("Current Turn: " + currentStone);
        turnIndicator.setTextFill(currentStone.getColor());
    }
}

class Point2D {

    public final double x, y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class GameLogic {

    private static final int[][] directions = { { 1, -1, 0 }, { 1, 0, -1 }, { 0, 1, -1 }, { -1, 1, 0 }, { -1, 0, 1 }, { 0, -1, 1 } };

    public static List<Cell> getNeighbors(Cell cell, Board board) {
        List<Cell> neighbors = new ArrayList<>();
        for (int[] d : directions) {
            int nq = cell.q + d[0], nr = cell.r + d[1], ns = cell.s + d[2];
            for (Cell candidate : board.getCells()) {
                if (candidate.q == nq && candidate.r == nr && candidate.s == ns) {
                    neighbors.add(candidate);
                    break;
                }
            }
        }
        return neighbors;
    }

    public static List<Cell> getConnectedGroup(Cell startCell, Board board, Stone color) {
        List<Cell> group = new ArrayList<>();
        List<Cell> frontier = new ArrayList<>();
        frontier.add(startCell);

        while (!frontier.isEmpty()) {
            Cell current = frontier.remove(0);
            if (!group.contains(current)) {
                group.add(current);
                for (Cell neighbor : getNeighbors(current, board)) {
                    if (neighbor.stone == color && !group.contains(neighbor)) {
                        frontier.add(neighbor);
                    }
                }
            }
        }
        return group;
    }

    public static List<List<Cell>> getOpponentGroups(List<Cell> sameGroup, Board board, Stone currentColor) {
        List<List<Cell>> opponentGroups = new ArrayList<>();
        Stone opponentColor = (currentColor == Stone.RED) ? Stone.BLUE : Stone.RED;
        for (Cell groupCell : sameGroup) {
            for (Cell neighbor : getNeighbors(groupCell, board)) {
                if (neighbor.stone == opponentColor && !containsCellInAnyGroup(neighbor, opponentGroups)) {
                    List<Cell> group = getConnectedGroup(neighbor, board, opponentColor);
                    opponentGroups.add(group);
                }
            }
        }
        return opponentGroups;
    }

    private static boolean containsCellInAnyGroup(Cell cell, List<List<Cell>> groups) {
        for (List<Cell> group : groups) {
            if (group.contains(cell)) {
                return true;
            }
        }
        return false;
    }

    // Check if a move is legal (non-destructive)
    public static boolean isMoveLegal(Cell c, Board board, Stone currentColor) {
        if (c.stone != null) return false;

        // First stone for each player can be placed anywhere
        boolean anyStoneExists = false;
        for (Cell cell : board.getCells()) {
            if (cell.stone != null) {
                anyStoneExists = true;
                break;
            }
        }
        if (!anyStoneExists) return true;

        // Check for second stone - must not be adjacent to own color
        boolean secondStoneMove = true;
        Stone opponentColor = (currentColor == Stone.RED) ? Stone.BLUE : Stone.RED;
        for (Cell cell : board.getCells()) {
            if (cell.stone == currentColor) {
                secondStoneMove = false;
                break;
            }
        }

        if (secondStoneMove) {
            for (Cell neighbor : getNeighbors(c, board)) {
                if (neighbor.stone == currentColor) {
                    return false; // Second stone can't be adjacent to first
                }
            }
            return true; // Second stone placement is ok if not adjacent
        }

        // Check if adjacent to any own stone
        boolean adjacentOwn = false;
        for (Cell neighbor : getNeighbors(c, board)) {
            if (neighbor.stone == currentColor) {
                adjacentOwn = true;
                break;
            }
        }

        // If not adjacent to own stone, move is legal
        if (!adjacentOwn) return true;

        // If adjacent to own stone, need to check if capture is possible
        // Temporarily place stone
        c.stone = currentColor;

        // Get the group that would be formed with the new stone
        List<Cell> newGroup = getConnectedGroup(c, board, currentColor);

        // Get all adjacent opponent groups from the new connected group
        List<List<Cell>> opponentGroups = getOpponentGroups(newGroup, board, currentColor);

        // Check if any capture would occur (our group size > opponent group size)
        boolean captureOccurs = false;
        for (List<Cell> opponentGroup : opponentGroups) {
            if (newGroup.size() > opponentGroup.size()) {
                captureOccurs = true;
                break;
            }
        }

        // Reset the cell
        c.stone = null;

        // Move is legal if it would cause a capture
        return captureOccurs;
    }

    // Process the move with actual game effects
    public static int processMove(Cell c, Board board, Stone currentColor) {
        if (c.stone != null) return 0;

        // First stone for each player can be placed anywhere
        boolean anyStoneExists = false;
        for (Cell cell : board.getCells()) {
            if (cell.stone != null) {
                anyStoneExists = true;
                break;
            }
        }
        if (!anyStoneExists) {
            c.stone = currentColor;
            return 1;
        }

        // Check for second stone - must not be adjacent to own color
        boolean secondStoneMove = true;
        Stone opponentColor = (currentColor == Stone.RED) ? Stone.BLUE : Stone.RED;
        for (Cell cell : board.getCells()) {
            if (cell.stone == currentColor) {
                secondStoneMove = false;
                break;
            }
        }

        if (secondStoneMove) {
            for (Cell neighbor : getNeighbors(c, board)) {
                if (neighbor.stone == currentColor) {
                    return 0; // Second stone can't be adjacent to first
                }
            }
            c.stone = currentColor;
            return 1; // Second stone placement is ok if not adjacent
        }

        // Check if adjacent to any own stone
        boolean adjacentOwn = false;
        for (Cell neighbor : getNeighbors(c, board)) {
            if (neighbor.stone == currentColor) {
                adjacentOwn = true;
                break;
            }
        }

        // If not adjacent to own stone, move is legal
        if (!adjacentOwn) {
            c.stone = currentColor;
            return 1;
        }

        // Place the stone
        c.stone = currentColor;

        // Get the group that's formed with the new stone
        List<Cell> newGroup = getConnectedGroup(c, board, currentColor);

        // Get all adjacent opponent groups from the new group
        List<List<Cell>> opponentGroups = getOpponentGroups(newGroup, board, currentColor);

        // Check if any capture would occur and process it
        boolean captureOccurred = false;
        for (List<Cell> opponentGroup : opponentGroups) {
            if (newGroup.size() > opponentGroup.size()) {
                // Remove all stones in the captured group
                for (Cell oppCell : opponentGroup) {
                    oppCell.stone = null;
                }
                captureOccurred = true;
            }
        }

        // If adjacent to own stone but no capture occurred, the move is illegal
        if (adjacentOwn && !captureOccurred) {
            c.stone = null;
            return 0;
        }

        return captureOccurred ? 2 : 1;
    }
}

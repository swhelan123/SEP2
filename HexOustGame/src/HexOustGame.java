import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class HexOustGame extends Application {

    @Override
    public void start(Stage primaryStage) {
        Board board = new Board(6);
        Layout layout = new Layout(Layout.FLAT, 35, 450, 450);

        HexUI ui = new HexUI(board, layout);

        Scene scene = new Scene(ui, 900, 900);
        primaryStage.setTitle("Sprint 2 - Stone Placing");
        primaryStage.setScene(scene);
        primaryStage.show();

        ui.drawBoard();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

enum Stone {
    RED, BLUE;
    public Color getColor() {
        return (this == RED) ? Color.RED : Color.BLUE;
    }
}

class Cell {
    public final int q, r, s;
    public Stone stone = null;

    public Cell(int q, int r, int s) {
        if (q + r + s != 0) {
            throw new IllegalArgumentException("q + r + s must be 0");
        }
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
            double dx = c.x - x;
            double dy = c.y - y;
            double dist = dx*dx + dy*dy;
            if (dist < minDist) {
                minDist = dist;
                closest = cell;
            }
        }
        return closest;
    }
}

class Orientation {
    public final double f0, f1, f2, f3;
    public final double b0, b1, b2, b3;
    public final double startAngle;

    public Orientation(double f0, double f1, double f2, double f3,
                       double b0, double b1, double b2, double b3,
                       double startAngle) {
        this.f0 = f0; this.f1 = f1;
        this.f2 = f2; this.f3 = f3;
        this.b0 = b0; this.b1 = b1;
        this.b2 = b2; this.b3 = b3;
        this.startAngle = startAngle;
    }
}

class Layout {
    public static final Orientation FLAT = new Orientation(
            3.0 / 2.0, 0.0,
            Math.sqrt(3.0) / 2.0, Math.sqrt(3.0),
            2.0 / 3.0, 0.0,
            -1.0 / 3.0, Math.sqrt(3.0) / 3.0,
            0.0
    );

    public final Orientation orientation;
    public final double size;
    public final double originX;
    public final double originY;

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
        double angle = 2.0 * Math.PI * (orientation.startAngle - corner) / 6.0;
        double ox = size * Math.cos(angle);
        double oy = size * Math.sin(angle);
        return new Point2D(ox, oy);
    }
}

class HexUI extends StackPane {
    private final Board board;
    private final Layout layout;
    private final Canvas canvas;
    private Cell hoveredCell = null;
    private Stone currentStone = Stone.RED;

    public HexUI(Board board, Layout layout) {
        this.board = board;
        this.layout = layout;
        this.canvas = new Canvas(900, 900);
        getChildren().add(canvas);

        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseClicked(this::handleMouseClicked);
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

            if (cell == hoveredCell) {
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
        double mx = e.getX();
        double my = e.getY();
        hoveredCell = board.findCellClosest(mx, my, layout);
        drawBoard();
    }

    private void handleMouseClicked(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        Cell c = board.findCellClosest(mx, my, layout);
        if (c != null && c.stone == null) {
            c.stone = currentStone;
            currentStone = (currentStone == Stone.RED) ? Stone.BLUE : Stone.RED;
        }
        drawBoard();
    }
}

class Point2D {
    public final double x, y;
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

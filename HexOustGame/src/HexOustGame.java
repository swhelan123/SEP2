import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class HexOustGame {

    public static void main(String[] args) {
        Board board = new Board(6);
    }
}

// enumerates cubic cells in a radius
class Board {
    private final int radius;
    private final List<Cell> cells;

    public Board(int radius) {
        this.radius = radius;
        this.cells = new ArrayList<>();
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
}

// represents a single cell with cubic coords
class Cell {
    public final int q, r, s;

    public Cell(int q, int r, int s) {
        if (q + r + s != 0) {
            throw new IllegalArgumentException("q + r + s must be 0");
        }
        this.q = q;
        this.r = r;
        this.s = s;
    }
}

// defines orientation factors for flat-topped hex
class Orientation {
    public final double f0, f1, f2, f3;
    public final double b0, b1, b2, b3;
    public final double startAngle;

    public Orientation(double f0, double f1, double f2, double f3,
                       double b0, double b1, double b2, double b3,
                       double startAngle) {
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

// stores layout info for hex grids
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
}
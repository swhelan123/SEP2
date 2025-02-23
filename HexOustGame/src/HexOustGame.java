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

// enumerates cubic coords for a given radius
class Board {
    private final java.util.List<Cell> cells = new java.util.ArrayList<>();

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

    public java.util.List<Cell> getCells() {
        return cells;
    }
}
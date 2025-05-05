import java.util.ArrayList;
import java.util.List;
import java.util.Collections; // For unmodifiable list if desired

/**
 * Represents the game board, containing a collection of hexagonal cells.
 * Provides methods for accessing cells and calculating game state information.
 * @author [Group 34 WheMurPap] // <-- Add your group name/members
 * @version 1.2
 * @since 2025-05-05 // <-- Adjust date if needed
 */
public class Board { // Changed to public
    private final List<Cell> cells = new ArrayList<>();

    /**
     * Constructs a hexagonal board with a given radius.
     * Populates the board with Cell objects within the specified radius
     * using axial coordinates. The number of cells generated follows the formula 3*R*(R+1) + 1.
     *
     * @param radius The radius of the hexagonal grid (number of steps from center to edge).
     * Must be non-negative.
     * @throws IllegalArgumentException if radius is negative.
     */
    public Board(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Board radius cannot be negative.");
        }
        // Generate cells using axial coordinates within the specified radius
        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);
            for (int r = r1; r <= r2; r++) {
                int s = -q - r;
                // Double check bounds (redundant with loop logic but safe)
                if (Math.abs(q) <= radius && Math.abs(r) <= radius && Math.abs(s) <= radius) {
                    cells.add(new Cell(q, r, s));
                }
            }
        }
    }

    /**
     * Returns an unmodifiable list of all cells on the board.
     * Returning an unmodifiable list prevents external code from accidentally
     * altering the board's internal cell list.
     * @return An unmodifiable List of Cell objects.
     */
    public List<Cell> getCells() {
        // Return an unmodifiable view to protect the internal list
        return Collections.unmodifiableList(cells);
    }

    /**
     * Finds the cell on the board whose center is closest to the given pixel coordinates.
     * Uses Euclidean distance (squared) for efficiency.
     * Used for mapping mouse clicks to board cells.
     *
     * @param x The x pixel coordinate.
     * @param y The y pixel coordinate.
     * @param layout The layout object used for coordinate conversion. Must not be null.
     * @return The Cell closest to the given coordinates, or null if the board is empty.
     * @throws NullPointerException if layout is null.
     */
    public Cell findCellClosest(double x, double y, Layout layout) {
        if (layout == null) {
            throw new NullPointerException("Layout cannot be null for findCellClosest.");
        }
        Cell closest = null;
        double minDistSq = Double.MAX_VALUE; // Use squared distance
        // Iterate over the internal list directly
        for (Cell cell : this.cells) {
            Point2D pt = layout.hexToPixel(cell); // Use the cell directly
            double dx = pt.x - x;
            double dy = pt.y - y;
            double distSq = dx*dx + dy*dy;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = cell;
            }
        }
        return closest;
    }

    /**
     * Finds a specific cell by its axial coordinates.
     *
     * @param q The q coordinate.
     * @param r The r coordinate.
     * @param s The s coordinate (must satisfy q + r + s = 0).
     * @return The Cell object if found, otherwise null.
     */
    public Cell getCell(int q, int r, int s) {
        if (q + r + s != 0) {
            // Optionally throw an exception or return null for invalid coordinates
            // System.err.println("Warning: Invalid coordinates requested: q=" + q + ", r=" + r + ", s=" + s);
            return null;
        }
        // Consider using a Map<Coords, Cell> for faster lookups if board is large or lookups are frequent
        for (Cell cell : this.cells) {
            if (cell.q == q && cell.r == r && cell.s == s) {
                return cell;
            }
        }
        return null; // Cell not found
    }


    /**
     * Counts the number of stones of a specific color currently on the board.
     *
     * @param color The Stone color to count (RED or BLUE). Must not be null.
     * @return The number of stones of the given color (non-negative).
     * @throws NullPointerException if color is null.
     */
    public int countStones(Stone color) {
        if (color == null) {
            throw new NullPointerException("Color cannot be null for countStones.");
        }
        int cnt = 0;
        for (Cell c : this.cells) {
            if (c.stone == color) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Resets the board by removing all stones from all cells.
     */
    public void reset() {
        for (Cell cell : this.cells) {
            cell.stone = null;
        }
    }
}

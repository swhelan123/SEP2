/**
 * Represents a single cell (hexagon) on the game board using axial coordinates.
 * Stores the q, r, s coordinates and the stone currently placed on it (if any).
 */
public class Cell { // Changed to public
    /** The q coordinate (axial system). */
    public final int q;
    /** The r coordinate (axial system). */
    public final int r;
    /** The s coordinate (axial system, s = -q -r). */
    public final int s;
    /** The stone placed on this cell, or null if empty. */
    public Stone stone = null;

    /**
     * Constructs a Cell with given axial coordinates.
     * Enforces the constraint q + r + s = 0.
     * @param q The q coordinate.
     * @param r The r coordinate.
     * @param s The s coordinate.
     * @throws IllegalArgumentException if q + r + s is not equal to 0.
     */
    public Cell(int q, int r, int s) {
        if (q + r + s != 0) throw new IllegalArgumentException("q + r + s must be 0");
        this.q = q; this.r = r; this.s = s;
    }

    // Optional: Consider adding equals() and hashCode() if Cells are used in Sets or as Map keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return q == cell.q && r == cell.r && s == cell.s;
    }

    @Override
    public int hashCode() {
        // Simple hash code based on coordinates
        return 31 * (31 * q + r) + s;
    }

    @Override
    public String toString() {
        return "Cell{" + "q=" + q + ", r=" + r + ", s=" + s + ", stone=" + stone + '}';
    }
}

import java.util.Objects;

/**
 * Represents a single cell (hexagon) on the game board using axial coordinates.
 * Stores the q, r, s coordinates and the stone currently placed on it (if any).
 * Axial coordinates always satisfy the constraint q + r + s = 0.
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

    /**
     * Compares this cell to another object for equality.
     * Two cells are considered equal if they have the same q, r, and s coordinates.
     * The stone placed on the cell is not considered in the equality check.
     * @param o The object to compare with.
     * @return true if the other object is a Cell with the same coordinates, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return q == cell.q && r == cell.r && s == cell.s;
    }

    /**
     * Generates a hash code for this cell based on its coordinates.
     * Consistent with the {@link #equals(Object)} method.
     * @return A hash code value for this cell.
     */
    @Override
    public int hashCode() {
        // Simple hash code based on coordinates
        // Uses Objects.hash for potentially better distribution and handling of coordinate values
        return Objects.hash(q, r, s);
        // Original: return 31 * (31 * q + r) + s;
    }

    /**
     * Returns a string representation of the cell, including coordinates and stone state.
     * @return A string in the format "Cell{q=X, r=Y, s=Z, stone=STONE_COLOR/null}".
     */
    @Override
    public String toString() {
        return "Cell{" + "q=" + q + ", r=" + r + ", s=" + s + ", stone=" + stone + '}';
    }
}

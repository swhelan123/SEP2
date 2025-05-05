import java.util.Objects;

/**
 * A simple helper class to represent an immutable 2D point using double-precision floating-point coordinates.
 * Primarily used for representing pixel coordinates in the {@link Layout} class and for UI event handling.
 */
public class Point2D { // Changed to public
    /** The x-coordinate of the point. */
    public final double x;
    /** The y-coordinate of the point. */
    public final double y;

    /**
     * Constructs a Point2D with the specified coordinates.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Optional: Add utility methods like distance, add, subtract if needed for geometry calculations.
    /**
     * Calculates the squared Euclidean distance between this point and another point.
     * Using squared distance avoids a square root calculation and is often sufficient
     * for comparisons (e.g., finding the closest point).
     *
     * @param other The other Point2D. Must not be null.
     * @return The squared distance between the points.
     * @throws NullPointerException if other is null.
     */
    public double distanceSq(Point2D other) {
        Objects.requireNonNull(other, "Other point cannot be null for distanceSq calculation.");
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    /**
     * Calculates the Euclidean distance between this point and another point.
     *
     * @param other The other Point2D. Must not be null.
     * @return The distance between the points.
     * @throws NullPointerException if other is null.
     */
    public double distance(Point2D other) {
        return Math.sqrt(distanceSq(other));
    }


    /**
     * Returns a string representation of the point.
     * @return A string in the format "Point2D{x=X.X, y=Y.Y}".
     */
    @Override
    public String toString() {
        return "Point2D{" + "x=" + x + ", y=" + y + '}';
    }

    /**
     * Compares this point to another object for equality.
     * Two Point2D objects are considered equal if their x and y coordinates are exactly equal
     * using {@link Double#compare(double, double)}.
     *
     * @param o The object to compare with.
     * @return true if the other object is a Point2D with the same coordinates, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point2D point2D = (Point2D) o;
        // Use Double.compare for precise floating-point equality check
        return Double.compare(point2D.x, x) == 0 && Double.compare(point2D.y, y) == 0;
    }

    /**
     * Generates a hash code for this point based on its coordinates.
     * Consistent with the {@link #equals(Object)} method. Uses {@link Objects#hash(Object...)}
     * for combining the hash codes of the double coordinates.
     *
     * @return A hash code value for this point.
     */
    @Override
    public int hashCode() {
        // Use Objects.hash for standard and robust hash code generation for multiple fields
        return Objects.hash(x, y);
    }
}

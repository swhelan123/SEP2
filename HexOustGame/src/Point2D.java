/**
 * Simple helper class to represent a 2D point with double precision coordinates.
 * Used for pixel coordinate calculations in the Layout class.
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

    // Optional: Add methods like distance, add, subtract if needed
    // Example:
    // public double distanceSq(Point2D other) {
    //     double dx = this.x - other.x;
    //     double dy = this.y - other.y;
    //     return dx * dx + dy * dy;
    // }

    @Override
    public String toString() {
        return "Point2D{" + "x=" + x + ", y=" + y + '}';
    }

    // Optional: equals() and hashCode() if used in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point2D point2D = (Point2D) o;
        return Double.compare(point2D.x, x) == 0 && Double.compare(point2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

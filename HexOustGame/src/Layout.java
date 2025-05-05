import java.util.ArrayList;
import java.util.List;

/**
 * Manages the layout properties of the hexagonal grid, including orientation (flat-top or pointy-top),
 * hexagon size, and the grid's origin point in pixel coordinates.
 * Provides essential methods for converting between hexagonal coordinates (used by {@link Cell})
 * and pixel coordinates (used for drawing and mouse interaction), and for calculating
 * the pixel coordinates of a hexagon's corners.
 * <p>
 * This implementation is based on the concepts and algorithms described at:
 * <a href="https://www.redblobgames.com/grids/hexagons/">Red Blob Games: Hexagonal Grids</a>
 * </p>
 *
 * @see Orientation
 * @see Cell
 * @see Point2D
 * @see HexUI
 */
public class Layout { // Changed to public
    /** Standard orientation definition for flat-topped hexagons. */
    public static final Orientation FLAT = new Orientation(
            3.0/2.0, 0.0, Math.sqrt(3.0)/2.0, Math.sqrt(3.0), // f0, f1, f2, f3 (Forward matrix components)
            2.0/3.0, 0.0, -1.0/3.0, Math.sqrt(3.0)/3.0,      // b0, b1, b2, b3 (Backward matrix components)
            0.0                                              // start_angle (0 degrees)
    );
    /** Standard orientation definition for pointy-topped hexagons. */
    public static final Orientation POINTY = new Orientation(
            Math.sqrt(3.0), Math.sqrt(3.0)/2.0, 0.0, 3.0/2.0, // f0, f1, f2, f3
            Math.sqrt(3.0)/3.0, -1.0/3.0, 0.0, 2.0/3.0,      // b0, b1, b2, b3
            0.5                                              // start_angle (0.5 * 60 = 30 degrees)
    );

    /** The orientation definition (e.g., {@link #FLAT} or {@link #POINTY}). */
    public final Orientation orientation;
    /** The size of the hexagon (distance from center to a corner) in pixels. */
    public final double size;
    /** The pixel coordinates of the grid's origin (typically the center of the hexagon at q=0, r=0, s=0). */
    public final Point2D origin;

    /**
     * Constructs a Layout object defining the geometry of the hexagonal grid.
     *
     * @param orientation The {@link Orientation} object (e.g., {@code Layout.FLAT} or {@code Layout.POINTY}). Must not be null.
     * @param size The size (radius) of hexagons in pixels (distance from center to corner). Must be positive.
     * @param originX The pixel x-coordinate of the grid origin (center of hex 0,0,0).
     * @param originY The pixel y-coordinate of the grid origin (center of hex 0,0,0).
     * @throws NullPointerException if orientation is null.
     * @throws IllegalArgumentException if size is not positive.
     */
    public Layout(Orientation orientation, double size, double originX, double originY) {
        if (orientation == null) throw new NullPointerException("Orientation cannot be null.");
        if (size <= 0) throw new IllegalArgumentException("Hexagon size must be positive.");
        this.orientation = orientation;
        this.size = size;
        this.origin = new Point2D(originX, originY);
    }

    /**
     * Converts hexagonal axial coordinates (from a {@link Cell} object) to pixel coordinates,
     * representing the center point of the hexagon on the screen.
     *
     * @param cell The {@link Cell} object containing the q, r, s coordinates. Must not be null.
     * @return A {@link Point2D} representing the pixel coordinates of the hexagon's center.
     * @throws NullPointerException if cell is null.
     */
    public Point2D hexToPixel(Cell cell) {
        if (cell == null) throw new NullPointerException("Cell cannot be null for hexToPixel.");
        // Alias for shorter code
        Orientation M = orientation;
        // Apply the forward matrix transformation
        double x = (M.f0 * cell.q + M.f1 * cell.r) * size;
        double y = (M.f2 * cell.q + M.f3 * cell.r) * size;
        // Add the origin offset
        return new Point2D(x + origin.x, y + origin.y);
    }

    /**
     * Calculates the pixel coordinates of the 6 corners for a given hexagon cell.
     * The corners are calculated relative to the cell's center pixel coordinate.
     *
     * @param cell The {@link Cell} for which to calculate corners. Must not be null.
     * @return A List of {@link Point2D} objects representing the corner coordinates,
     * typically in clockwise or counter-clockwise order depending on the implementation of {@link #hexCornerOffset(int)}.
     * @throws NullPointerException if cell is null.
     */
    public List<Point2D> polygonCorners(Cell cell) {
        if (cell == null) throw new NullPointerException("Cell cannot be null for polygonCorners.");
        List<Point2D> corners = new ArrayList<>();
        Point2D center = hexToPixel(cell); // Get the center pixel coordinate of the hex
        // Calculate each corner by adding an offset to the center
        for (int i = 0; i < 6; i++) {
            Point2D offset = hexCornerOffset(i);
            corners.add(new Point2D(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }

    /**
     * Calculates the pixel offset vector from a hexagon's center to one of its corners.
     * The angle depends on the layout's orientation ({@code startAngle}) and the corner index.
     *
     * @param corner The corner index (0 to 5). Corner 0 is typically to the right for flat-top,
     * or top-right for pointy-top, depending on the start angle.
     * @return A {@link Point2D} representing the offset vector (dx, dy) from the center.
     */
    private Point2D hexCornerOffset(int corner) {
        // Calculate the angle for the corner in radians.
        // The angle is determined by the orientation's start angle plus the corner index (scaled by 60 degrees or PI/3 radians).
        double angle = 2.0 * Math.PI * (orientation.startAngle + corner) / 6.0;
        // Calculate the offset using trigonometry (cosine for x, sine for y) scaled by the hex size.
        return new Point2D(size * Math.cos(angle), size * Math.sin(angle));
    }
}

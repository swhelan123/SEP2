import java.util.ArrayList;
import java.util.List;

/**
 * Manages the layout properties of the hexagonal grid (orientation, size, origin).
 * Provides methods for converting between hex and pixel coordinates and calculating polygon corners.
 * Based on the implementation from https://www.redblobgames.com/grids/hexagons/
 */
public class Layout { // Changed to public
    /** Standard orientation for flat-topped hexagons. */
    public static final Orientation FLAT = new Orientation(
            3.0/2.0, 0.0, Math.sqrt(3.0)/2.0, Math.sqrt(3.0), // f0, f1, f2, f3
            2.0/3.0, 0.0, -1.0/3.0, Math.sqrt(3.0)/3.0,      // b0, b1, b2, b3
            0.0                                              // start_angle
    );
    /** Standard orientation for pointy-topped hexagons. */
    public static final Orientation POINTY = new Orientation(
            Math.sqrt(3.0), Math.sqrt(3.0)/2.0, 0.0, 3.0/2.0, // f0, f1, f2, f3
            Math.sqrt(3.0)/3.0, -1.0/3.0, 0.0, 2.0/3.0,      // b0, b1, b2, b3
            0.5                                              // start_angle (0.5 * 60 = 30 degrees)
    );

    /** The orientation definition (e.g., FLAT or POINTY). */
    public final Orientation orientation;
    /** The size of the hexagon (center to corner distance in pixels). */
    public final double size; // Use Point or Vec2D for size if preferred
    /** The pixel coordinates of the grid's origin (usually center of cell 0,0,0). */
    public final Point2D origin; // Use Point or Vec2D for origin

    /**
     * Constructs a Layout.
     * @param orientation The Orientation object (e.g., Layout.FLAT).
     * @param size The size (radius) of hexagons in pixels. Must be positive.
     * @param originX The pixel x-coordinate of the grid origin (center of hex 0,0,0).
     * @param originY The pixel y-coordinate of the grid origin (center of hex 0,0,0).
     */
    public Layout(Orientation orientation, double size, double originX, double originY) {
        if (orientation == null) throw new NullPointerException("Orientation cannot be null.");
        if (size <= 0) throw new IllegalArgumentException("Hexagon size must be positive.");
        this.orientation = orientation;
        this.size = size; // Store size directly
        this.origin = new Point2D(originX, originY); // Store origin as Point2D
    }

    /**
     * Converts hexagonal axial coordinates (from a Cell object) to pixel coordinates (center of the hex).
     * @param cell The Cell object containing q, r, s coordinates. Must not be null.
     * @return A Point2D representing the pixel coordinates of the hexagon's center.
     * @throws NullPointerException if cell is null.
     */
    public Point2D hexToPixel(Cell cell) {
        if (cell == null) throw new NullPointerException("Cell cannot be null for hexToPixel.");
        Orientation M = orientation;
        double x = (M.f0 * cell.q + M.f1 * cell.r) * size;
        double y = (M.f2 * cell.q + M.f3 * cell.r) * size;
        return new Point2D(x + origin.x, y + origin.y);
    }

    // --- Methods below might be needed if converting pixels back to hex ---
    /*
    public FractionalHex pixelToHex(Point2D p) {
        Orientation M = orientation;
        Point2D pt = new Point2D((p.x - origin.x) / size, (p.y - origin.y) / size);
        double q = M.b0 * pt.x + M.b1 * pt.y;
        double r = M.b2 * pt.x + M.b3 * pt.y;
        return new FractionalHex(q, r, -q - r);
    }

    public Cell hexRound(FractionalHex h) {
        int q = (int)(Math.round(h.q));
        int r = (int)(Math.round(h.r));
        int s = (int)(Math.round(h.s));
        double q_diff = Math.abs(q - h.q);
        double r_diff = Math.abs(r - h.r);
        double s_diff = Math.abs(s - h.s);
        if (q_diff > r_diff && q_diff > s_diff) {
            q = -r - s;
        } else if (r_diff > s_diff) {
            r = -q - s;
        } else {
            s = -q - r;
        }
        return new Cell(q, r, s);
    }
    */

    /**
     * Calculates the pixel coordinates of the 6 corners for a given hexagon cell.
     * @param cell The Cell for which to calculate corners. Must not be null.
     * @return A List of Point2D objects representing the corner coordinates in clockwise or counter-clockwise order.
     * @throws NullPointerException if cell is null.
     */
    public List<Point2D> polygonCorners(Cell cell) {
        if (cell == null) throw new NullPointerException("Cell cannot be null for polygonCorners.");
        List<Point2D> corners = new ArrayList<>();
        Point2D center = hexToPixel(cell); // Get the center pixel
        for (int i = 0; i < 6; i++) {
            Point2D offset = hexCornerOffset(i);
            corners.add(new Point2D(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }

    /**
     * Calculates the pixel offset from a hexagon's center to one of its corners.
     * @param corner The corner index (0 to 5).
     * @return A Point2D representing the offset vector.
     */
    private Point2D hexCornerOffset(int corner) {
        // Calculate the angle for the corner based on orientation and index
        double angle = 2.0 * Math.PI * (orientation.startAngle + corner) / 6.0; // Adjusted angle calculation
        return new Point2D(size * Math.cos(angle), size * Math.sin(angle));
    }

    // Optional: Inner class for fractional hex coordinates if needed for pixelToHex
    /*
    private static class FractionalHex {
        public final double q, r, s;
        public FractionalHex(double q, double r, double s) {
            if (Math.round(q + r + s) != 0) throw new IllegalArgumentException("q + r + s must be 0");
            this.q = q; this.r = r; this.s = s;
        }
    }
    */
}

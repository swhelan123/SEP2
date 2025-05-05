/**
 * Defines the orientation constants (forward/backward transformation matrices, start angle)
 * required for converting between hexagonal coordinates and pixel coordinates within a {@link Layout}.
 * This class holds the mathematical constants specific to either flat-top or pointy-top hexagon orientations.
 * <p>
 * Based on the matrix and angle definitions from:
 * <a href="https://www.redblobgames.com/grids/hexagons/">Red Blob Games: Hexagonal Grids</a>
 * </p>
 *
 * @see Layout
 */
public class Orientation { // Changed to public
    /** Forward matrix component f0 (Hex -> Pixel transformation). */
    public final double f0;
    /** Forward matrix component f1 (Hex -> Pixel transformation). */
    public final double f1;
    /** Forward matrix component f2 (Hex -> Pixel transformation). */
    public final double f2;
    /** Forward matrix component f3 (Hex -> Pixel transformation). */
    public final double f3;

    /** Backward matrix component b0 (Pixel -> Hex transformation). */
    public final double b0;
    /** Backward matrix component b1 (Pixel -> Hex transformation). */
    public final double b1;
    /** Backward matrix component b2 (Pixel -> Hex transformation). */
    public final double b2;
    /** Backward matrix component b3 (Pixel -> Hex transformation). */
    public final double b3;

    /**
     * The starting angle for calculating corner positions, in multiples of 60 degrees.
     * For example, 0.0 for flat-top (first corner at 0 degrees/East) and
     * 0.5 for pointy-top (first corner at 30 degrees/North-East).
     * Used in {@link Layout#hexCornerOffset(int)}.
     */
    public final double startAngle;

    /**
     * Constructs an Orientation object holding the transformation constants.
     *
     * @param f0 Forward matrix element [0,0]
     * @param f1 Forward matrix element [0,1]
     * @param f2 Forward matrix element [1,0]
     * @param f3 Forward matrix element [1,1]
     * @param b0 Backward matrix element [0,0]
     * @param b1 Backward matrix element [0,1]
     * @param b2 Backward matrix element [1,0]
     * @param b3 Backward matrix element [1,1]
     * @param startAngle Start angle for corners (in units of 60 degrees, e.g., 0.0 for flat top, 0.5 for pointy top).
     */
    public Orientation(double f0, double f1, double f2, double f3,
                       double b0, double b1, double b2, double b3,
                       double startAngle) {
        this.f0 = f0; this.f1 = f1; this.f2 = f2; this.f3 = f3;
        this.b0 = b0; this.b1 = b1; this.b2 = b2; this.b3 = b3;
        this.startAngle = startAngle;
    }
}

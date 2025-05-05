/**
 * Defines the orientation constants (forward/backward matrices, start angle)
 * for converting between hexagonal and pixel coordinates.
 * Used by the {@link Layout} class.
 * Based on the implementation from https://www.redblobgames.com/grids/hexagons/
 */
public class Orientation { // Changed to public
    public final double f0, f1, f2, f3; // Forward matrix (Hex -> Pixel): f0, f1, f2, f3
    public final double b0, b1, b2, b3; // Backward matrix (Pixel -> Hex): b0, b1, b2, b3
    public final double startAngle;     // Angle offset for corners in multiples of 60 degrees (0.0 for flat-top, 0.5 for pointy-top)

    /**
     * Constructs an Orientation.
     * @param f0 Forward matrix element 0
     * @param f1 Forward matrix element 1
     * @param f2 Forward matrix element 2
     * @param f3 Forward matrix element 3
     * @param b0 Backward matrix element 0
     * @param b1 Backward matrix element 1
     * @param b2 Backward matrix element 2
     * @param b3 Backward matrix element 3
     * @param startAngle Start angle for corners (in units of 60 degrees, e.g., 0.0 for flat top)
     */
    public Orientation(double f0,double f1,double f2,double f3, double b0,double b1,double b2,double b3, double startAngle) {
        this.f0=f0; this.f1=f1; this.f2=f2; this.f3=f3;
        this.b0=b0; this.b1=b1; this.b2=b2; this.b3=b3;
        this.startAngle=startAngle;
    }
}

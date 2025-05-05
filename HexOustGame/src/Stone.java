import javafx.scene.paint.Color; // Added import

/**
 * Represents the two possible player stones (colors).
 */
public enum Stone { // Changed to public
    RED, BLUE;
    /** Gets the JavaFX Color associated with the stone. */
    public Color getColor() { return (this == RED) ? Color.web("#E53935") : Color.web("#1E88E5"); }
    /** Returns a string representation ("Red" or "Blue"). */
    @Override public String toString() { return (this == RED) ? "Red" : "Blue"; }
}

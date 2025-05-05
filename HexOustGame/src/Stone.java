import javafx.scene.paint.Color;

/**
 * Represents the two possible player stones (colors) in the HexOust game: RED and BLUE.
 * Provides methods to get the corresponding JavaFX {@link Color} and a user-friendly string representation.
 *
 * @author [Group 34 WheMurPap]
 */
public enum Stone {
    /** Represents the Red player's stone. */
    RED,
    /** Represents the Blue player's stone. */
    BLUE;

    /**
     * Gets the JavaFX {@link Color} associated with this stone type.
     * Used primarily for drawing the stones on the game board canvas.
     *
     * @return {@code Color.web("#E53935")} for RED, {@code Color.web("#1E88E5")} for BLUE.
     */
    public Color getColor() {
        // Uses specific web color hex codes for consistent appearance
        return (this == RED) ? Color.web("#E53935") : Color.web("#1E88E5");
    }

    /**
     * Returns a user-friendly string representation of the stone color ("Red" or "Blue").
     * Useful for display in UI elements like status labels or debugging.
     *
     * @return "Red" if the stone is RED, "Blue" if the stone is BLUE.
     */
    @Override
    public String toString() {
        // Capitalizes the first letter for better readability
        return (this == RED) ? "Red" : "Blue";
    }
}

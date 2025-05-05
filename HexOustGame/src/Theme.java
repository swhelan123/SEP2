import javafx.scene.paint.Color; // Added import

/**
 * Represents the available visual themes for the game.
 * Used by ThemeManager and the Settings UI.
 */
public enum Theme { // Changed to public
    LIGHT("Light"),
    DARK("Dark"),
    CONTRAST("High Contrast"); // Example additional theme

    private final String displayName;

    Theme(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of the theme, suitable for UI elements like ChoiceBox.
     * @return The user-friendly theme name.
     */
    @Override
    public String toString() {
        return displayName;
    }
}

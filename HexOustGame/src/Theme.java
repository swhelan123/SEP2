import javafx.scene.paint.Color; // Although not directly used here, often relevant in theme context

/**
 * Represents the available visual themes for the HexOust game application.
 * Each theme has a display name used in UI elements like settings menus.
 * The selected theme is managed by the {@link ThemeManager}.
 *
 * @author [Group 34 WheMurPap]
 */
public enum Theme {
    /** A standard light theme with dark text on light backgrounds. */
    LIGHT("Light"),
    /** A standard dark theme with light text on dark backgrounds. */
    DARK("Dark"),
    /** An example high-contrast theme, potentially for accessibility. */
    CONTRAST("High Contrast");

    /** The user-friendly name for display purposes. */
    private final String displayName;

    /**
     * Constructor for the enum constants.
     * @param displayName The name to be displayed in the UI for this theme.
     */
    Theme(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name of the theme.
     * This is intended for use in UI elements like {@code ChoiceBox} or {@code Label}.
     *
     * @return The user-friendly theme name (e.g., "Light", "Dark", "High Contrast").
     */
    @Override
    public String toString() {
        return displayName;
    }
}

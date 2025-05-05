import javafx.scene.paint.Color;

/**
 * Manages the currently selected theme and provides theme-specific
 * style classes and colors (primarily for canvas drawing).
 * This is a static utility class.
 */
public class ThemeManager { // Changed to public
    private static Theme currentTheme = Theme.DARK; // Default theme set

    /** Private constructor to prevent instantiation. */
    private ThemeManager() {}

    /**
     * Gets the currently active theme.
     * @return The current Theme enum value.
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Sets the active theme for the application.
     * @param theme The Theme enum value to set as current. Must not be null.
     */
    public static void setCurrentTheme(Theme theme) {
        if (theme != null) {
            currentTheme = theme;
        } else {
            System.err.println("Warning: Attempted to set null theme.");
        }
    }

    /**
     * Gets the CSS style class corresponding to the current theme.
     * This class is typically applied to the root node of a scene to enable
     * theme-specific styling defined in styles.css.
     * @return The CSS theme class name (e.g., "theme-dark").
     */
    public static String getThemeStyleClass() {
        switch (currentTheme) {
            case DARK: return "theme-dark";
            case CONTRAST: return "theme-contrast";
            case LIGHT: default: return "theme-light";
        }
    }

    // --- Color Methods for Canvas Drawing ---

    /**
     * Gets a theme-specific text color, typically used where CSS cannot easily reach
     * or for dynamic color setting outside of CSS variables.
     * @param type The type of text (e.g., "primary", "secondary").
     * @return The corresponding Color object based on the current theme.
     */
    public static Color getTextColor(String type) {
        switch (currentTheme) {
            case DARK: return type.equals("primary") ? Color.WHITESMOKE : Color.LIGHTGRAY;
            case CONTRAST: return Color.WHITE;
            case LIGHT: default: return type.equals("primary") ? Color.DARKSLATEGRAY : Color.rgb(50, 50, 50);
        }
    }

    /**
     * Gets the theme-specific color for drawing hexagon outlines on the canvas.
     * @return The Color for hexagon borders.
     */
    public static Color getHexOutlineColor() {
        switch (currentTheme) {
            case DARK: return Color.web("#90A4AE");
            case CONTRAST: return Color.YELLOW;
            case LIGHT: default: return Color.web("#757575");
        }
    }

    /**
     * Gets the theme-specific color for highlighting cells on the canvas.
     * @param type The type of highlight ("legal" for valid moves, "capture" for potential captures).
     * @return The corresponding Color object.
     */
    public static Color getHighlightColor(String type) {
        switch (currentTheme) {
            case DARK: return type.equals("legal") ? Color.web("#66BB6A", 0.7) : Color.web("#EF5350", 0.7);
            case CONTRAST: return type.equals("legal") ? Color.LIME : Color.FUCHSIA;
            case LIGHT: default: return type.equals("legal") ? Color.web("#A5D6A7") : Color.web("#EF9A9A");
        }
    }

    /**
     * Gets the theme-specific color for highlighting capturable opponent stones on the canvas.
     * @return The Color for the capture highlight circle.
     */
    public static Color getCaptureHighlightColor() {
        switch (currentTheme) {
            case DARK: return Color.web("#FFEE58", 0.8);
            case CONTRAST: return Color.CYAN;
            case LIGHT: default: return Color.web("#FFF176");
        }
    }

    /**
     * Gets the theme-specific color for the thin border drawn around placed stones on the canvas.
     * @return The Color for the stone border.
     */
    public static Color getStoneBorderColor() {
        return (currentTheme == Theme.DARK || currentTheme == Theme.CONTRAST)
                ? Color.WHITE.deriveColor(0, 1, 1, 0.3) // Lighter border for dark themes
                : Color.BLACK.deriveColor(0, 1, 1, 0.3); // Darker border for light theme
    }
}

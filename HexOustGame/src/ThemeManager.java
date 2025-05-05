import javafx.scene.paint.Color;

/**
 * Manages the currently selected application {@link Theme} and provides access to
 * theme-specific properties, such as CSS style classes and {@link Color} values
 * primarily intended for direct canvas drawing where CSS styling is not applicable
 * or convenient.
 * <p>
 * This is a static utility class responsible for holding the current theme state
 * and providing methods to retrieve theme-dependent values.
 * </p>
 *
 * @author [Group 34 WheMurPap]
 * @see Theme
 * @see HexOustGame#applyThemeAndCSS(javafx.scene.layout.Pane)
 * @see HexUI#drawBoard()
 */
public final class ThemeManager { // Changed to public

    /** The currently active theme. Defaults to DARK. */
    private static Theme currentTheme = Theme.DARK;

    /** Private constructor to prevent instantiation of this utility class. */
    private ThemeManager() {
        throw new UnsupportedOperationException("ThemeManager is a utility class and cannot be instantiated.");
    }

    /**
     * Gets the currently active theme for the application.
     *
     * @return The current {@link Theme} enum value (e.g., {@code Theme.DARK}).
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Sets the active theme for the application.
     * Updates the internal state used by other methods in this class.
     * Does nothing if the provided theme is null, logging a warning instead.
     *
     * @param theme The {@link Theme} enum value to set as current. Must not be null.
     */
    public static void setCurrentTheme(Theme theme) {
        if (theme != null) {
            if (currentTheme != theme) {
                System.out.println("ThemeManager: Setting current theme to " + theme);
                currentTheme = theme;
            }
        } else {
            System.err.println("ThemeManager Warning: Attempted to set null theme. Current theme remains " + currentTheme);
        }
    }

    /**
     * Gets the CSS style class name corresponding to the current theme.
     * This class name (e.g., "theme-dark") should be applied to the root node
     * (or relevant container) of a JavaFX scene to enable theme-specific styling
     * defined in an external CSS file (e.g., "styles.css").
     *
     * @return The CSS theme class name (e.g., "theme-light", "theme-dark", "theme-contrast").
     */
    public static String getThemeStyleClass() {
        switch (currentTheme) {
            case DARK: return "theme-dark";
            case CONTRAST: return "theme-contrast";
            case LIGHT: // Fallthrough intended, LIGHT is the default case
            default: return "theme-light";
        }
    }

    // --- Color Methods specifically for Canvas Drawing ---
    // These provide colors directly when CSS variables might not be easily accessible,
    // such as within the GraphicsContext drawing operations in HexUI.

    /**
     * Gets a theme-specific text color. Primarily intended for scenarios where
     * text is drawn directly onto a Canvas, not for general UI elements styled by CSS.
     *
     * @param type A string indicating the type or context of the text (e.g., "primary", "secondary").
     * Currently, only distinguishes between "primary" and others.
     * @return The corresponding {@link Color} object based on the current theme and type.
     */
    public static Color getTextColor(String type) {
        // Example implementation - adjust based on actual needs
        switch (currentTheme) {
            case DARK:
                return type.equals("primary") ? Color.WHITESMOKE : Color.LIGHTGRAY;
            case CONTRAST:
                // High contrast might use stark white/black regardless of type
                return Color.WHITE;
            case LIGHT: // Fallthrough intended
            default:
                return type.equals("primary") ? Color.DARKSLATEGRAY : Color.rgb(50, 50, 50); // Darker text for light theme
        }
    }

    /**
     * Gets the theme-specific color for drawing hexagon outlines on the game board canvas.
     *
     * @return The {@link Color} to be used for {@code gc.strokePolygon(...)}.
     */
    public static Color getHexOutlineColor() {
        switch (currentTheme) {
            case DARK: return Color.web("#90A4AE"); // Bluish-grey
            case CONTRAST: return Color.YELLOW;     // High visibility yellow
            case LIGHT: // Fallthrough intended
            default: return Color.web("#757575"); // Medium grey
        }
    }

    /**
     * Gets the theme-specific color for highlighting cells on the canvas, typically indicating
     * either a legal move location or a potential capture target when hovering.
     *
     * @param type The type of highlight required: "legal" for valid empty cells,
     * "capture" for opponent cells that might be capturable by placement.
     * @return The corresponding semi-transparent {@link Color} object for filling the hexagon background.
     */
    public static Color getHighlightColor(String type) {
        switch (currentTheme) {
            case DARK:
                // Use deriveColor or web colors with alpha for transparency
                return type.equals("legal") ? Color.web("#66BB6A", 0.7) : Color.web("#EF5350", 0.7); // Green / Red
            case CONTRAST:
                return type.equals("legal") ? Color.LIME : Color.FUCHSIA; // Bright, distinct colors
            case LIGHT: // Fallthrough intended
            default:
                // Lighter, less saturated versions for light theme
                return type.equals("legal") ? Color.web("#A5D6A7") : Color.web("#EF9A9A"); // Light Green / Light Red
        }
    }

    /**
     * Gets the theme-specific color for the circular highlight drawn *behind* capturable
     * opponent stones on the canvas, indicating they can be captured directly by clicking.
     *
     * @return The {@link Color} for the capture highlight circle (often slightly transparent).
     */
    public static Color getCaptureHighlightColor() {
        switch (currentTheme) {
            case DARK: return Color.web("#FFEE58", 0.8); // Yellowish, slightly transparent
            case CONTRAST: return Color.CYAN;            // Bright cyan
            case LIGHT: // Fallthrough intended
            default: return Color.web("#FFF176");        // Light yellow
        }
    }

    /**
     * Gets the theme-specific color for the thin border drawn around placed stones
     * on the game board canvas. This helps stones stand out from the background
     * and potentially from highlighted cells.
     *
     * @return The {@link Color} for the stone border (often slightly transparent white or black).
     */
    public static Color getStoneBorderColor() {
        // Use a light border on dark themes, dark border on light theme for contrast
        return (currentTheme == Theme.DARK || currentTheme == Theme.CONTRAST)
                ? Color.WHITE.deriveColor(0, 1, 1, 0.3) // White with low opacity
                : Color.BLACK.deriveColor(0, 1, 1, 0.3); // Black with low opacity
    }
}

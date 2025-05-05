import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.AudioClip;

/**
 * Manages loading and playback of sound effects using JavaFX {@link AudioClip}.
 * Sounds are loaded once at startup from the classpath (specifically, the /resources/audio directory)
 * and can then be played by their logical name (e.g., "button-click").
 * <p>
 * This is a static utility class and cannot be instantiated.
 * It handles potential errors during sound loading gracefully by logging them.
 * </p>
 *
 * @author [Group 34 WheMurPap]
 */
public final class SoundManager { // Changed to public

    /** Maps logical sound names to their file paths relative to the classpath root. */
    private static final Map<String, String> soundFilePaths = new HashMap<>() {{
        put("button-click", "/resources/audio/click.wav");
        put("place-stone", "/resources/audio/stone.wav");
        put("capture-stone", "/resources/audio/capture.wav");
        put("win-game", "/resources/audio/win.wav");
        put("illegal-move", "/resources/audio/click.wav"); // Optional: Reuse click or add specific sound
    }};

    /** Stores the loaded AudioClip objects, keyed by their logical sound name. */
    private static final Map<String, AudioClip> loadedClips = new HashMap<>();

    /** Flag indicating whether sounds have been successfully loaded. */
    private static boolean soundsLoaded = false;

    /** Private constructor to prevent instantiation of this utility class. */
    private SoundManager() {
        throw new UnsupportedOperationException("SoundManager is a utility class and cannot be instantiated.");
    }

    /**
     * Loads all defined sound files into {@link AudioClip} objects.
     * This method should be called once at application startup (e.g., in {@code Application.start()}).
     * It iterates through the {@code soundFilePaths} map, attempts to locate each sound file
     * as a classpath resource, and creates an AudioClip if found.
     * <p>
     * Logs errors to standard error if files are not found or cannot be loaded but continues
     * loading other sounds. Sets the {@code soundsLoaded} flag to true upon completion.
     * </p>
     */
    public static void loadSounds() {
        if (soundsLoaded) {
            System.out.println("SoundManager: Sounds already loaded.");
            return; // Prevent multiple loading attempts
        }
        System.out.println("SoundManager: Loading sounds...");
        for (Map.Entry<String, String> entry : soundFilePaths.entrySet()) {
            String soundName = entry.getKey();
            String filePath = entry.getValue();
            try {
                // Get the URL of the resource relative to the classpath root
                // Using SoundManager.class ensures it looks relative to this class's location
                URL resourceUrl = SoundManager.class.getResource(filePath);
                if (resourceUrl == null) {
                    System.err.println("SoundManager Error: Resource not found - " + filePath);
                    continue; // Skip this sound if not found
                }
                // Create AudioClip from the resource URL's external form
                AudioClip clip = new AudioClip(resourceUrl.toExternalForm());
                loadedClips.put(soundName, clip);
                System.out.println("  - Loaded: " + soundName + " from " + filePath);
            } catch (Exception e) {
                // Catch potential errors during AudioClip creation
                // (e.g., unsupported format, access issues, malformed URL)
                System.err.println("SoundManager Error: Failed to load sound '" + soundName + "' from " + filePath);
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
        soundsLoaded = true;
        System.out.println("SoundManager: Sound loading complete. " + loadedClips.size() + " clips loaded.");
    }

    /**
     * Plays the sound associated with the given logical name, if it was successfully loaded.
     * If sounds have not been loaded yet ({@code loadSounds()} was not called or failed),
     * or if the specified sound name is not found in the loaded clips, a warning is logged
     * to standard error, and no sound is played.
     * <p>
     * Note: {@link AudioClip#play()} is asynchronous; this method returns immediately
     * while the sound plays in the background.
     * </p>
     *
     * @param soundName The logical name of the sound to play (e.g., "button-click", "place-stone").
     * Must match a key in the {@code soundFilePaths} map used during loading.
     */
    public static void playSound(String soundName) {
        if (!soundsLoaded) {
            System.err.println("SoundManager Warning: Sounds not loaded via loadSounds(), cannot play '" + soundName + "'.");
            return;
        }
        AudioClip clip = loadedClips.get(soundName);
        if (clip != null) {
            // Play the sound.
            clip.play();
        } else {
            System.err.println("SoundManager Warning: Sound not found in loaded clips: '" + soundName + "'. Was it defined in soundFilePaths and loaded correctly?");
        }
    }
}

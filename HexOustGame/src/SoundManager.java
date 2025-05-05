import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.AudioClip;

/**
 * Manages loading and playback of sound effects using JavaFX AudioClip.
 * Sounds are loaded once at startup from the classpath and played by name.
 * This is a static utility class.
 */
public class SoundManager { // Changed to public
    // Map sound names to their file paths within the resources folder (relative to classpath root)
    private static final Map<String, String> soundFilePaths = new HashMap<>() {{
        put("button-click", "/resources/audio/click.wav");
        put("place-stone", "/resources/audio/stone.wav");
        put("capture-stone", "/resources/audio/capture.wav");
        put("win-game", "/resources/audio/win.wav");
        put("illegal-move", "/resources/audio/click.wav"); // Optional: Reuse click or add specific sound
    }};

    // Map to store loaded AudioClip objects, keyed by sound name
    private static final Map<String, AudioClip> loadedClips = new HashMap<>();
    private static boolean soundsLoaded = false;

    /** Private constructor to prevent instantiation. */
    private SoundManager() {}

    /**
     * Loads all defined sound files into AudioClip objects.
     * Should be called once at application startup (e.g., in Application.start()).
     * Looks for files in the classpath using the paths defined in soundFilePaths.
     * Logs errors if files are not found or cannot be loaded.
     */
    public static void loadSounds() {
        if (soundsLoaded) return; // Prevent multiple loading attempts
        System.out.println("SoundManager: Loading sounds...");
        for (Map.Entry<String, String> entry : soundFilePaths.entrySet()) {
            String soundName = entry.getKey();
            String filePath = entry.getValue();
            try {
                // Get the URL of the resource relative to the classpath root
                URL resourceUrl = SoundManager.class.getResource(filePath);
                if (resourceUrl == null) {
                    System.err.println("SoundManager Error: Resource not found - " + filePath);
                    continue; // Skip this sound if not found
                }
                // Create AudioClip from the resource URL
                AudioClip clip = new AudioClip(resourceUrl.toExternalForm());
                loadedClips.put(soundName, clip);
                System.out.println("  - Loaded: " + soundName + " from " + filePath);
            } catch (Exception e) {
                // Catch potential errors during AudioClip creation (e.g., unsupported format, access issues)
                System.err.println("SoundManager Error: Failed to load sound '" + soundName + "' from " + filePath);
                e.printStackTrace();
            }
        }
        soundsLoaded = true;
        System.out.println("SoundManager: Sound loading complete.");
    }

    /**
     * Plays the sound associated with the given name, if it was successfully loaded.
     * Logs a warning if the sound name is unknown or if sounds haven't been loaded.
     * @param soundName The logical name of the sound to play (e.g., "button-click").
     */
    public static void playSound(String soundName) {
        if (!soundsLoaded) {
            System.err.println("SoundManager Warning: Sounds not loaded, cannot play " + soundName);
            return;
        }
        AudioClip clip = loadedClips.get(soundName);
        if (clip != null) {
            // Play the sound. Note: AudioClip playback is asynchronous.
            clip.play();
        } else {
            System.err.println("SoundManager Warning: Sound not found in loaded clips: " + soundName);
        }
    }
}

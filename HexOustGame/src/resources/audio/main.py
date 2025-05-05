import os
from pydub import AudioSegment
from pydub.exceptions import CouldntDecodeError

# --- Configuration ---
# Directory to scan (current directory where the script is run)
AUDIO_DIRECTORY = "."
# Output format
OUTPUT_FORMAT = "wav"

# --- Conversion Logic ---
def convert_mp3_to_wav(directory):
    """
    Scans the specified directory for .mp3 files and converts them to .wav.

    Args:
        directory (str): The path to the directory containing audio files.
    """
    print(f"Scanning directory: {os.path.abspath(directory)}")
    found_mp3 = False

    # Iterate through all files in the specified directory
    for filename in os.listdir(directory):
        # Check if the file ends with .mp3 (case-insensitive)
        if filename.lower().endswith(".mp3"):
            found_mp3 = True
            # Construct the full path to the mp3 file
            mp3_path = os.path.join(directory, filename)
            # Create the output wav filename by replacing the extension
            wav_filename = os.path.splitext(filename)[0] + "." + OUTPUT_FORMAT
            wav_path = os.path.join(directory, wav_filename)

            print(f"Found MP3: {filename}")

            try:
                # Load the MP3 file using pydub
                print(f"  Converting '{filename}' to '{wav_filename}'...")
                audio = AudioSegment.from_mp3(mp3_path)

                # Export the audio to WAV format
                # You can specify parameters like bitrate if needed,
                # but defaults are usually fine for WAV.
                audio.export(wav_path, format=OUTPUT_FORMAT)
                print(f"  Successfully converted to: {wav_filename}")

            except CouldntDecodeError:
                print(f"  ERROR: Could not decode '{filename}'. Is ffmpeg/libav installed and in PATH?")
            except FileNotFoundError:
                 print(f"  ERROR: File not found '{mp3_path}'.")
            except Exception as e:
                # Catch any other unexpected errors during conversion
                print(f"  ERROR: An unexpected error occurred converting '{filename}': {e}")

    if not found_mp3:
        print("No .mp3 files found in this directory.")

# --- Main Execution ---
if __name__ == "__main__":
    # Ensure the target directory exists (should be current dir in this case)
    if not os.path.isdir(AUDIO_DIRECTORY):
        print(f"Error: Directory '{AUDIO_DIRECTORY}' not found.")
    else:
        convert_mp3_to_wav(AUDIO_DIRECTORY)
    print("Conversion process finished.")

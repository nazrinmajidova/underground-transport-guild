package team3.persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import team3.model.Game;

/**
 * Stores five Java-serialized game slots under the current user's home directory.
 * Save files are version-dependent and must only be loaded from trusted local sources.
 */
public class SaveManager {
    private static final DateTimeFormatter SAVE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Path saveDirectory =
        Paths.get(System.getProperty("user.home"), ".underground-guild-saves");

    /**
     * Writes the game, replacing any existing save in the slot.
     * @param game non-null game state
     * @param slot slot number from 1 through 5
     * @throws IllegalArgumentException if the slot is invalid or the game is null
     * @throws IllegalStateException if the directory or file cannot be written
     */
    public void save(Game game, int slot) {
        validateSlot(slot);
        if (game == null) {
            throw new IllegalArgumentException("Cannot save a null game.");
        }

        Path file = resolveSaveFile(slot);
        try {
            Files.createDirectories(saveDirectory);
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(file))) {
                output.writeObject(game);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save game to slot " + slot + ".", ex);
        }
    }

    /**
     * Reads a trusted local save; there is no cross-version migration or input filter.
     * @param slot slot number from 1 through 5
     * @return the deserialized game state
     * @throws IllegalArgumentException if the slot is invalid
     * @throws IllegalStateException if the save is absent, incompatible, or unreadable
     */
    public Game load(int slot) {
        validateSlot(slot);

        Path file = resolveSaveFile(slot);
        if (!Files.exists(file)) {
            throw new IllegalStateException("Save slot " + slot + " is empty.");
        }

        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            Object loaded = input.readObject();
            if (loaded instanceof Game game) {
                return game;
            }
            throw new IllegalStateException("Invalid save data in slot " + slot + ".");
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to load save slot " + slot + ".", ex);
        }
    }

    public boolean hasSave(int slot) {
        validateSlot(slot);
        return Files.exists(resolveSaveFile(slot));
    }

    public String getSaveMetadata(int slot) {
        validateSlot(slot);
        Path file = resolveSaveFile(slot);
        if (!Files.exists(file)) {
            return "— empty —";
        }

        try {
            FileTime modified = Files.getLastModifiedTime(file);
            String formatted = modified.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(SAVE_TIME_FORMAT);
            return "updated " + formatted;
        } catch (IOException ex) {
            return "saved";
        }
    }

    private Path resolveSaveFile(int slot) {
        return saveDirectory.resolve("slot-" + slot + ".ser");
    }

    private void validateSlot(int slot) {
        if (slot < 1 || slot > 5) {
            throw new IllegalArgumentException("Save slot must be between 1 and 5.");
        }
    }
}

package framework;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Write a complete UTF-8 file next to the destination before replacing it. */
final class AtomicTextFile {
    private AtomicTextFile() { }

    static void write(Path destination, String text) throws IOException {
        Path temporary = Files.createTempFile(destination.getParent(), ".budget-", ".tmp");
        try {
            Files.writeString(temporary, text, StandardCharsets.UTF_8);
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}

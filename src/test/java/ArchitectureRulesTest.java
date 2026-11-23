import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Enforces the Clean Architecture dependency rule: source code may only depend
 * inward (framework -> adapter -> usecase -> entity). If someone imports the
 * wrong layer, this test fails and says which file.
 */
class ArchitectureRulesTest {

    private static final Path MAIN = Path.of("src/main/java");

    /** layer -> import prefixes that layer is NOT allowed to use */
    private static void assertNoImports(String layer, List<String> forbiddenPrefixes) throws IOException {
        Path dir = MAIN.resolve(layer);
        List<String> violations;
        try (Stream<Path> files = Files.walk(dir)) {
            violations = files.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> imports(p).stream()
                            .filter(i -> forbiddenPrefixes.stream().anyMatch(i::startsWith))
                            .map(i -> p + " imports " + i))
                    .collect(Collectors.toList());
        }
        assertTrue(violations.isEmpty(), "Dependency rule broken in " + layer + ": " + violations);
    }

    private static List<String> imports(Path file) {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.map(String::trim)
                    .filter(l -> l.startsWith("import "))
                    .map(l -> l.substring("import ".length()).replace("static ", "").replace(";", "").trim())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void entitiesDependOnNothingOutward() throws IOException {
        assertNoImports("entity", List.of("usecase", "adapter", "framework", "app", "javafx", "java.sql", "java.net"));
    }

    @Test
    void useCasesDoNotKnowAdaptersFrameworkOrJavaFx() throws IOException {
        assertNoImports("usecase", List.of("adapter", "framework", "app", "javafx", "java.sql", "java.net", "java.io"));
    }

    @Test
    void adaptersDoNotKnowFrameworkOrJavaFx() throws IOException {
        assertNoImports("adapter", List.of("framework", "app", "javafx", "java.sql", "java.net", "java.io"));
    }
}

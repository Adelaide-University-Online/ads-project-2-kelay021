import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for {@link Runner}.
 */
@DisplayName("Runner")
class RunnerTest {

    @Nested
    @DisplayName("buildFromFile()")
    class BuildFromFileTests {
        @TempDir
        Path tempDir;

        /** Writes {@code content} to a temporary file and returns its path. */
        private Path createFile(String content) throws IOException {
            Path file = tempDir.resolve("courses.txt");
            Files.writeString(file, content);
            return file;
        }

        @Test
        @DisplayName("Returns an empty graph for an empty file")
        void emptyFile_returnsEmptyGraph() throws IOException {
            Path file = createFile("");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getCourses().isEmpty());
        }

        @Test
        @DisplayName("Registers all courses listed on line 1")
        void coursesOnlyFile_allCoursesRegistered() throws IOException {
            Path file = createFile("A, B, C");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getCourses().containsAll(List.of("A", "B", "C")));
        }

        @Test
        @DisplayName("Registers the correct number of courses from line 1")
        void coursesOnlyFile_correctCourseCount() throws IOException {
            Path file = createFile("A, B, C");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertEquals(3, graph.getCourses().size());
        }

        @Test
        @DisplayName("Initialises empty prerequisite list when no prerequisite lines exist")
        void courseOnlyFile_prerequisiteListsAreEmpty() throws IOException {
            Path file = createFile("A, B, C");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            for (String course : List.of("A", "B", "C")) {
                assertTrue(graph.getPrerequisites().get(course).isEmpty(), "Expected empty prerequisites for course: " + course);
            }
        }

        @Test
        @DisplayName("Adds prerequisites correctly from subsequent lines")
        void fileWithPrerequisites_prerequisitesAreAdded() throws IOException {
            Path file = createFile("A, B, C\nA, B\nB, C");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getPrerequisites().get("A").contains("B"));
            assertTrue(graph.getPrerequisites().get("B").contains("C"));
        }

        @Test
        @DisplayName("A course with no prerequisite line has an empty prerequisite list")
        void courseWithNoPrerequisiteLine_emptyPrerequisiteList() throws IOException {
            Path file = createFile("A, B, C\nA, B\nB, C");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getPrerequisites().get("C").isEmpty());
        }

        @Test
        @DisplayName("Records all prerequisites when a course has multiple")
        void courseWithMultiplePrerequisites_allPrerequisitesAdded() throws IOException {
            Path file = createFile("A, B, C, D\nA, B, C, D");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getPrerequisites().get("A").containsAll(List.of("B", "C", "D")));
        }

        @Test
        @DisplayName("Populates the dependents map as the reverse of the prerequisite map")
        void fileWithPrerequisites_dependentsArePopulated() throws IOException {
            Path file = createFile("A, B\nA, B");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getDependents().get("B").contains("A"));
        }

        @Test
        @DisplayName("Trims surrounding whitespace from course codes")
        void courseCodesWithWhitespace_areCorrectlyTrimmed() throws IOException {
            Path file = createFile("  A , B  , C  ");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getCourses().containsAll(List.of("A", "B", "C")));
        }

        @Test
        @DisplayName("Skips blank lines in the prerequisite section")
        void fileWithBlankLines_blankLinesAreSkipped() throws IOException {
            Path file = createFile("A, B, C\n\nA, B\n\n\nB, C");
            CourseGraph graph = Runner.buildFromFile(file.toString());
            assertTrue(graph.getPrerequisites().get("A").contains("B"));
            assertTrue(graph.getPrerequisites().get("B").contains("C"));
        }

        @Test
        @DisplayName("Throws IOException when the file does not exist")
        void nonExistentFile_throwsIOException() {
            assertThrows(IOException.class, () -> Runner.buildFromFile("this_file_does_not_exist_lol.txt"));
        }
    }
}
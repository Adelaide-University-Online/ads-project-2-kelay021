import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for {@link CourseGraph}.
 *
 * <p>Tests are organised by method under test using {@link Nested} classes. A fresh {@link CourseGraph} instance is
 * created before each test to ensure complete isolation between test cases.</p>
 */
class CourseGraphTest {

    private CourseGraph graph;

    @BeforeEach
    void setUp() {
        graph = new CourseGraph();
    }

    @Nested
    @DisplayName("addCourse()")
    class addCourseTests {

        @Test
        @DisplayName("Registers a single course in the courses set")
        void singleCourse_isRegistered() {
            graph.addCourse("A");
            assertTrue(graph.getCourses().contains("A"));
        }

        @Test
        @DisplayName("Initialises an empty prerequisites list for the new course")
        void singleCourse_prerequisiteListIsEmpty() {
            graph.addCourse("A");
            assertTrue(graph.getPrerequisites().get("A").isEmpty());
        }

        @Test
        @DisplayName("Registers all courses when multiple are added")
        void multipleCourses_allAreRegistered() {
            graph.addCourse("A");
            graph.addCourse("B");
            graph.addCourse("C");
            assertTrue(graph.getCourses().containsAll(List.of("A", "B", "C")));
        }

        @Test
        @DisplayName("Does not duplicate a course added more than once")
        void duplicateCourse_appearsOnlyOnce() {
            graph.addCourse("A");
            graph.addCourse("A");
            assertEquals(1, graph.getCourses().size());
        }

        @Test
        @DisplayName("Preserves insertion order across multiple courses")
        void multipleCourses_insertionOrderIsPreserved() {
            graph.addCourse("A");
            graph.addCourse("B");
            graph.addCourse("C");
            assertEquals(List.of("A", "B", "C"), new ArrayList<>(graph.getCourses()));
        }
    }
}
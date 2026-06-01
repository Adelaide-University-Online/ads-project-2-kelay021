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

    @Nested
    @DisplayName("addPrerequisite()")
    class AddPrerequisiteTests {

        @BeforeEach
        void addCourses() {
            graph.addCourse("A");
            graph.addCourse("B");
            graph.addCourse("C");
        }

        @Test
        @DisplayName("Adds the prerequisite to the course's prerequisite list")
        void singlePrerequisite_appearsInPrerequisitesMap() {
            graph.addPrerequisite("A", "B");
            assertTrue(graph.getPrerequisites().get("A").contains("B"));
        }

        @Test
        @DisplayName("Adds the reverse edge to the prerequisite's dependents list")
        void singlePrerequisite_reverseEdgeAppearsInDependentMap() {
            graph.addPrerequisite("A", "B");
            assertTrue(graph.getDependents().get("B").contains("A"));
        }

        @Test
        @DisplayName("Records all prerequisites when a course has multiple")
        void multiplePrerequisites_allAppearInPrerequisitesList() {
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("A", "C");
            assertTrue(graph.getPrerequisites().get("A").containsAll(List.of("B", "C")));
        }

        @Test
        @DisplayName("Records all dependents when a prerequisite is shared by multiple")
        void sharedPrerequisite_allDependentsAppearInDependentsList() {
            graph.addPrerequisite("A", "C");
            graph.addPrerequisite("B", "C");
            assertTrue(graph.getDependents().get("C").containsAll(List.of("A", "B")));
        }

        @Test
        @DisplayName("Does not affect the prerequisite list of unrelated courses")
        void prerequisiteAdded_doesNotAffectOtherCourses() {
            graph.addPrerequisite("A", "B");
            assertTrue(graph.getPrerequisites().get("C").isEmpty());
        }
    }

    @Nested
    @DisplayName("getCourses()")
    class GetCoursesTests {

        @Test
        @DisplayName("Returns an empty set for a newly created graph")
        void emptyGraph_returnsEmptySet() {
            assertTrue(graph.getCourses().isEmpty());
        }

        @Test
        @DisplayName("Returns the correct number of registered courses")
        void afterAddingCourses_correctCountIsReturned() {
            graph.addCourse("A");
            graph.addCourse("B");
            assertEquals(2, graph.getCourses().size());
        }

        @Test
        @DisplayName("Returns all registered courses")
        void afterAddingCourses_allCoursesAreReturned() {
            graph.addCourse("A");
            graph.addCourse("B");
            assertTrue(graph.getCourses().containsAll(List.of("A", "B")));
        }
    }

    @Nested
    @DisplayName("getPrerequisites()")
    class GetPrerequisiteTests {

        @Test
        @DisplayName("Returns an empty map for a newly created graph")
        void emptyGraph_returnsEmptyMap() {
            assertTrue(graph.getPrerequisites().isEmpty());
        }

        @Test
        @DisplayName("Returns an empty list for a course with no prerequisites")
        void courseWithNoPrerequisites_returnsEmptyList() {
            graph.addCourse("A");
            assertTrue(graph.getPrerequisites().get("A").isEmpty());
        }

        @Test
        @DisplayName("Returns the correct prerequisites for a course")
        void courseWithPrerequisites_returnsCorrectList() {
            graph.addCourse("A");
            graph.addCourse("B");
            graph.addCourse("C");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("A", "C");
            assertEquals(List.of("A", "B"), graph.getPrerequisites().get("A"));
        }
    }
}
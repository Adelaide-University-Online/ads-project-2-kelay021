import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for {@link Runner}.
 *
 * <p>Tests are organised into two {@link Nested} classes, one per public static method.</p>
 * <p>{@code buildFromFile} tests use JUnit's {@link TempDir} to create a real temporary files which keeps tests
 * self-contained with no dependency on files on disk.</p>
 * <p>{@code computeSchedule} tests build {@link CourseGraph} instances directly to keep each case focused on
 * scheduling logic alone.</p>
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

    @Nested
    @DisplayName("computeSchedule()")
    class ComputeScheduleTests {
        private CourseGraph graph;

        @BeforeEach
        void setUp() {
            graph = new CourseGraph();
        }

        /** Registers one or more courses in the graph */
        private void addCourses(String... courses) {
            for (String c : courses) graph.addCourse(c);
        }

        /** Flattens the schedule into a single list of all scheduled courses. Used to verify completeness without
         * worrying about period grouping
         */
        private List<String> allScheduledCourses(List<List<String>> schedule) {
            List<String> all = new ArrayList<>();
            for (List<String> period : schedule) all.addAll(period);
            return all;
        }

        //region Empty / single course
        @Test
        @DisplayName("Returns an empty schedule for an empty graph")
        void emptyGraph_returnsEmptySchedule() {
            assertTrue(Runner.computeSchedule(graph, 2).isEmpty());
        }

        @Test
        @DisplayName("Schedules a single course with no prerequisite in one period")
        void singleCourse_scheduledInOnePeriod() {
            addCourses("A");
            List<List<String>> schedule = Runner.computeSchedule(graph, 2);
            assertEquals(1, schedule.size());
            assertTrue(schedule.get(0).contains("A"));
        }
        //endregion

        //region Linear chains
        @Test
        @DisplayName("Produces the correct number of periods for a linear chain")
        void linearChain_correctPeriodCount() {
            addCourses("A", "B", "C");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("B", "C");
            // C -> B -> A: only one course can be taken per period therefore must take 3 periods
            assertEquals(3, Runner.computeSchedule(graph, 2).size());
        }

        @Test
        @DisplayName("Schedules a linear chain in prerequisite first order")
        void linearChain_correctOrder() {
            addCourses("A", "B", "C");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("B", "C");
            List<List<String>> schedule = Runner.computeSchedule(graph, 2);
            assertTrue(schedule.get(0).contains("C"), "C should be first (no prerequisites)");
            assertTrue(schedule.get(1).contains("B"), "B should be second (requires C)");
            assertTrue(schedule.get(2).contains("A"), "A should be last (requires B)");
        }
        //endregion

        //region Concurrency
        @Test
        @DisplayName("Groups all independent courses into one period when the limit allows")
        void allIndependentCourse_groupedInOnePeriod() {
            addCourses("A", "B", "C");
            List<List<String>> schedule = Runner.computeSchedule(graph, 3);
            assertEquals(1, schedule.size());
            assertTrue(schedule.get(0).containsAll(List.of("A", "B", "C")));
        }

        @Test
        @DisplayName("Spreads independent courses across period when the limit is 1")
        void independentCourses_spreadAcrossPeriodsWithLimitOne() {
            addCourses("A", "B", "C");
            List<List<String>> schedule = Runner.computeSchedule(graph, 1);
            assertEquals(3, schedule.size());
            for (List<String> period : schedule) {
                assertEquals(1, period.size(), "Each period should contain exactly one course");
            }
        }

        @Test
        @DisplayName("No single period exceeds the maximum concurrent course limit")
        void schedule_noPeriodExceedsLimit() {
            addCourses("A", "B", "C", "D", "E");
            int limit = 2;
            List<List<String>> schedule = Runner.computeSchedule(graph, limit);
            for (List<String> period : schedule) {
                assertTrue(period.size() <= limit, "Period exceeded the limit: " + period);
            }
        }

        @Test
        @DisplayName("A prerequisite is always scheduled in an earlier period than its dependent")
        void prerequisite_alwaysScheduledBeforeDependent() {
            addCourses("A", "B");
            graph.addPrerequisite("A", "B");
            List<List<String>> schedule = Runner.computeSchedule(graph, 10);
            int periodOfA = -1, periodOfB = -1;
            for (int i = 0; i < schedule.size(); i++) {
                if (schedule.get(i).contains("A")) periodOfA = i;
                if (schedule.get(i).contains("B")) periodOfB = i;
            }
            assertTrue(periodOfB < periodOfA, "B (prerequisite) must appear before A dependent");
        }

        @Test
        @DisplayName("A limit larger than the total course count still produces a valid schedule")
        void limitExceedsCourseCount_validScheduleReturned() {
            addCourses("A", "B");
            List<List<String>> schedule = Runner.computeSchedule(graph, 100);
            assertTrue(allScheduledCourses(schedule).containsAll(List.of("A", "B")));
        }
        //endregion

        //region Completeness
        @Test
        @DisplayName("The schedule contains every course in the graph")
        void schedule_containsAllCourses() {
            addCourses("A", "B", "C", "D");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("B", "C");
            List<List<String>> schedule = Runner.computeSchedule(graph, 2);
            assertTrue(allScheduledCourses(schedule).containsAll(List.of("A", "B", "C", "D")));
        }

        @Test
        @DisplayName("No course appears more than once across all periods")
        void schedule_noDuplicateCourses() {
            addCourses("A", "B", "C");
            graph.addPrerequisite("A", "B");
            List<List<String>> schedule = Runner.computeSchedule(graph, 2);
            List<String> all = allScheduledCourses(schedule);
            assertEquals(all.size(), new HashSet<>(all).size(), "Duplicate course found in schedule");
        }
        //endregion

        //region Mixed Dependencies (parallelism)
        @Test
        @DisplayName("Parallelizes independent branches and serialises shared prerequisites")
        void independentBranches_scheduledConcurrently() {
            // B and C are independent; A requires both
            addCourses("A", "B", "C");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("A", "C");
            List<List<String>> schedule = Runner.computeSchedule(graph, 2);
            assertEquals(2, schedule.size());
            assertTrue(schedule.get(0).containsAll(List.of("B", "C")));
            assertTrue(schedule.get(1).contains("A"));
        }

        @Test
        @DisplayName("Schedules a shared prerequisite only once even when multiple courses depend on it")
        void sharedPrerequisite_scheduledOnlyOnce() {
            // Both A and B depend on C
            addCourses("A", "B", "C");
            graph.addPrerequisite("A", "C");
            graph.addPrerequisite("B", "C");
            List<List<String>> schedule = Runner.computeSchedule(graph, 2);
            long occurrences = allScheduledCourses(schedule).stream()
                    .filter("C"::equals).count();
            assertEquals(1, occurrences, "C should appear exactly once in the schedule");
        }
        //endregion

        //region Cycle detection
        @Test
        @DisplayName("Throws IllegalStateException when a direct two-course cycle exists")
        void directCycle_throwsIllegalStateException() {
            // A requires B and B requires A therefore it is impossible
            addCourses("A", "B");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("B", "A");
            assertThrows(IllegalStateException.class, () -> Runner.computeSchedule(graph, 2));
        }

        @Test
        @DisplayName("Throws IllegalStateException when an indirect multi-course cycle exists")
        void indirectCycle_throwsIllegalStateException() {
            // A -> B -> C -> A
            addCourses("A", "B", "C");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("B", "C");
            graph.addPrerequisite("C", "A");
            assertThrows(IllegalStateException.class, () -> Runner.computeSchedule(graph, 2));
        }

        @Test
        @DisplayName("Throws IllegalStateException when a cycle involves only a subset of courses")
        void partialCycle_throwsIllegalStateException() {
            // D is standalone but A -> B -> C -> A is a cycle
            addCourses("A", "B", "C", "D");
            graph.addPrerequisite("A", "B");
            graph.addPrerequisite("B", "C");
            graph.addPrerequisite("C", "A");
            assertThrows(IllegalStateException.class, () -> Runner.computeSchedule(graph, 2));
        }
        //endregion
    }
}
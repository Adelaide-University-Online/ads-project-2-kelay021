/**
 * File: Runner.java
 * Description: A brief description of this Java module.
 * Author: Aidan Kelly-English
 * Student ID: 3159116
 * Email ID: aidan.kelly-english
 * AI Tool Used: Y/N (This includes all AI Tools e.g. ChatGPT, Microsoft or GitHub Copilot etc... Please leave blank if you do not wish to share this information)
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 **/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Runner {

    /**
     * Builds a {@link CourseGraph} by parsing a structured course prerequisite file.
     *
     * <p>The file must follow this format:
     * <ul>
     *     <li>Line 1: a comma-seperated list of all course codes in the degree</li>
     *     <li>Each subsequent line: a course code followed by its prerequisites, comma-separated</li>
     * </ul>
     * </p>
     * <p>Example file contents:</p>
     * <pre>
     *     A, B, C
     *     A, B
     *     B, C
     * </pre>
     *
     * <p>This file tells us that we need to complete courses A, B and C in order to complete the degree. We have to
     * complete course B before we can complete course A, and we need to complete course C before we can complete course
     * B.</p>
     *
     * @param filepath The path to the course prerequisite text file
     * @return a {@link CourseGraph} containing all courses as nodes and prerequisites as directed edges
     * @throws IOException If the file cannot be found or read
     */
    public static CourseGraph buildFromFile(String filepath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filepath));
        CourseGraph graph = new CourseGraph();

        if (lines.isEmpty()) {
            return graph;
        }

        // Process Line 1
        // Add all courses as nodes
        String[] courses = lines.getFirst().split(",");
        for (String course : courses) {
            graph.addCourse(course.trim());
        }

        // Process subsequent lines
        // Add prerequisites as edges
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(","); // Split into words around ","
            String course = parts[0].trim(); // first entry is the course

            for (int j = 1; j < parts.length; j++) {
                String prereq = parts[j].trim(); // remaining entries are the prerequisites
                graph.addPrerequisite(course, prereq);
            }
        }

        return graph;
    }

    /**
     * Computes the most time-efficient study schedule for the given {@link CourseGraph}.
     *
     * <p>Uses a modified version of Kahn's algorithm, a BFS-based approach to topological sorting. Rather than
     * processing one course at a time, courses are batched into study periods of up to {@code maxConcurrentCourses}
     * each.</p>
     *
     * <p>The schedule is optimal in the sense that every available course is started at the earliest possible period.
     * Delaying any available course could only push back courses that depend on it, increasing the total number of
     * study periods.</p>
     *
     * <p>A BFS-based topological sort was used instead of a DFS-based topological sort because a BFS-based approach
     * makes it easier to group courses into study periods because it processes the graph level by level, instead of
     * trying to figure out which courses sit at the same depth and can be studied at the same time, DFS is more
     * complex for no practical benefit.</p>
     *
     * @param graph The course dependency graph to schedule
     * @param maxConcurrentCourses The maximum number of courses a student may study at the same time
     * @return An ordered list of study periods, where each study period is a list of course codes to study concurrently
     * @throws IllegalStateException If a cycle is detected in the prerequisites, making it impossible to complete the
     * degree
     */
    public static List<List<String>> computeSchedule(CourseGraph graph, int maxConcurrentCourses) {

        // Retrieve both adjacency lists from the graph.
        // 'prerequisites' is what each course needs before it can be started
        // 'dependents' is the reverse, which courses to re-evaluate once a given course is completed.
        Map<String, List<String>> prerequisites = graph.getPrerequisites();
        Map<String, List<String>> dependents = graph.getDependents();

        // ### Step 1: Calculate the initial in-degree for each course ###
        //
        // A course's unsatisfiedPrerequisites is the number of prerequisites it still needs before it can be studied.
        // Initially this equals its total prerequisite count.
        //
        // As courses are completed, their dependents unsatisfiedPrerequisites are decremented. When a course reaches
        // unsatisfiedPrerequisites of 0, all of its prerequisites are satisfied, and it can be added to the available
        // queue.
        //
        // LinkedHashMap is used here to preserve the insertion order, to keep the final schedule deterministic and
        // easier to debug.
        Map<String, Integer> unsatisfiedPrerequisites = new LinkedHashMap<>();
        for (String course : graph.getCourses()) {
            unsatisfiedPrerequisites.put(course, prerequisites.get(course).size());
        }

        // ### Step 2: Seed the queue with immediately available courses ####
        //
        // Any course with an unsatisfiedPrerequisites of 0 has no prerequisites and can be studied immediately. These
        // will form the starting point of the schedule.
        //
        // LinkedList is used because it implements the Queue and supports efficient O(1) insertion at the tail and
        // removal at the head, which is exactly the access pattern required.
        Queue<String> available = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : unsatisfiedPrerequisites.entrySet()) {
            if (entry.getValue() == 0) available.add(entry.getKey());
        }

        List<List<String>> schedule = new ArrayList<>();

        // Tracks the total number of courses successfully scheduled.
        // Used after the main loop to detect whether a cycle prevented full scheduling.
        int scheduleCount = 0;

        // ### Step 3: Build the schedule study period by study period ###
        //
        // Each iteration of the loop represents one study period.
        // The loop continues until there are no more courses available, which happens either when all courses have been
        // scheduled, or when a cycle has left some courses permanently blocked.
        while (!available.isEmpty()) {

            // Fill this period up to the concurrent course limit.
            List<String> period = new ArrayList<>();
            // Math.min() ensures that isn't an attempt to poll more courses than are currently available (preventing
            // an IndexOutOfBoundsException if the queue has fewer courses than the concurrency limit allows)
            int slots = Math.min(maxConcurrentCourses, available.size());
            for (int i = 0; i < slots; i++) {
                period.add(available.poll());
            }
            schedule.add(period);
            scheduleCount += period.size();

            // ### Step 4: Unlock courses whose prerequisites are now satisfied ###
            //
            // For each course just completed, examine every course that depends on it. Decrement that dependent's
            // unsatisfiedPrerequisites to reflect the one fewer unsatisfied prerequisites. If the
            // unsatisfiedPrerequisites reaches 0, all prerequisites for that course are now met, and it can be queued
            // for a future period.
            for (String completed : period) {
                for (String dependent : dependents.get(completed)) {
                    int remaining = unsatisfiedPrerequisites.get(dependent) -1;
                    unsatisfiedPrerequisites.put(dependent, remaining);
                    if (remaining == 0) available.add(dependent);
                }
            }
        }

        // ### Step 5: Cycle detection ###
        //
        // In a valid acyclic graph, every course will eventually reach unsatisfiedPrerequisites 0 and be scheduled. If
        // the total scheduled falls short of the total course count, it means one or more courses were never reachable;
        // caused by a cycle in the prerequisite (e.g. A requires B, B requires A).
        //
        // Cycles make it impossible to ever satisfy all prerequisites, so the degree cannot be completed. This
        // edge-case is presented to the user as an exception.
        //
        // The provided course prerequisites graph is assumed to not have any cycles.
        if (scheduleCount < graph.getCourses().size()) {
            throw new IllegalStateException(
                    "A cycle was detected, this degree cannot be completed."
            );
        }

        return schedule;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter filename (including .txt): ");
        String filename = scanner.nextLine().trim();
        System.out.print("Enter number of courses able to be studied in one study period: ");
        int numConcurrentCourses = Integer.parseInt(scanner.nextLine().trim());
        try {
            CourseGraph graph = buildFromFile(filename);
            System.out.println("\nCourse   -> [prerequisite1, prerequisite2, ...]");
            System.out.println("-----------------------------------------------");
            System.out.println(graph);

            List<List<String>> schedule = computeSchedule(graph, numConcurrentCourses);
            System.out.print("\n====== Optimal Study Schedule ======");
            int yearCount = 1;
            int studyPeriodCount = 1;
            for (int i = 0; i < schedule.size(); i++) {
                if (i % 4 == 0) {
                    System.out.printf("%n\t\t\tYear %d%n", yearCount);
                    yearCount += 1;
                    studyPeriodCount = 1;
                }
                System.out.printf("Study Period %d: %s%n", studyPeriodCount, schedule.get(i));
                studyPeriodCount += 1;
            }
            System.out.printf("%nTotal study periods: %d%n", schedule.size());
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Scheduling error: " + e.getMessage());
        }
    }
}

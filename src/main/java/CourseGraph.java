/**
 * File: CourseGraph.java
 * Description: Defines a CourseGraph class which represents a directed graph of university courses and their
 * prerequisites implemented using adjacency lists.
 * Author: Aidan Kelly-English
 * Student ID: 3159116
 * Email ID: aidan.kelly-english
 * AI Tool Used: N
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 **/

import java.util.*;

/**
 * Represents a directed graph of university courses and their prerequisites.
 *
 * <p>Each course is a node in the graph. A directed edge from course {@code A} to course {@code B} indicates that
 * {@code B} must be completed before {@code A}.</p>
 *
 * <p>Two adjacency lists are maintained internally:</p>
 * <ul>
 *     <li>{@code prerequisites} - maps each course to the courses it directly requires</li>
 *     <li>{@code dependents} - the reverse mapping; maps each course to the courses that become available once it is
 *     completed</li>
 * </ul>
 */
public class CourseGraph {

    /** All course codes in the degree, preserving insertion order. */
    private final Set<String> courses = new LinkedHashSet<>();

    /** Adjacency list mapping each course to its direct prerequisite. */
    private final Map<String, List<String>> prerequisites = new LinkedHashMap<>();

    /** Reverse adjacency list mapping each course to the courses that depend on it. */
    private final Map<String, List<String>> dependents = new LinkedHashMap<>();

    /**
     * Registers a course as a node in the graph.
     *
     * <p>If the course has already been added, this method has no effect.</p>
     *
     * @param course the course code to add
     */
    public void addCourse(String course) {
        courses.add(course);
        prerequisites.putIfAbsent(course, new ArrayList<>());
        dependents.putIfAbsent(course, new ArrayList<>());
    }

    /**
     * Records that {@code prereq} must be completed before {@code course}.
     *
     * <p>This adds a directed edge from {@code course} to {@code prereq} in the prerequisites list, and the
     * corresponding reverse edge in the dependents list.</p>
     *
     * @param course the course that has the requirement
     * @param prereq the course that must be completed first
     */
    public void addPrerequisite(String course, String prereq) {
        prerequisites.get(course).add(prereq);
        dependents.get(prereq).add(course);
    }

    /** Returns all course registered in this graph, in insertion order.
     *
     * @return an ordered {@link Set} of course nodes
     */
    public Set<String> getCourses() {return courses;}

    /**
     * Returns the prerequisite adjacency list.
     * @return a {@link Map} from each course to its list of direct prerequisites
     */
    public Map<String, List<String>> getPrerequisites() {return prerequisites;}

    /**
     * Returns the dependents adjacency list.
     * @return a {@link Map} from each course to the courses that depend on it
     */
    public Map<String, List<String>> getDependents() {return dependents;}

    /**
     * Returns a string representation of the graph
     *
     * <p>Each line follows the format {@code COURSE -> [prereq1, prereq2, ...]}, listing every course alongside its
     * direct prerequisites.</p>
     *
     * @return a formatted multi-line string describing the graph edges
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : prerequisites.entrySet()) {
            sb.append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append("\n");
        }
        return sb.toString();
    }
}

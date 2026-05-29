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
import java.util.List;

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

    public static void main(String[] args) {
        try {
            CourseGraph graph = buildFromFile("XBDA.txt");
            System.out.println(graph);
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
        }
    }
}

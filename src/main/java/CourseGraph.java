/**
 * File: Runner.java
 * Description: A brief description of this Java module.
 * Author: Aidan Kelly-English
 * Student ID: 3159116
 * Email ID: aidan.kelly-english
 * AI Tool Used: Y/N (This includes all AI Tools e.g. ChatGPT, Microsoft or Github Copilot etc... Please leave blank if you do not wish to share this information)
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 **/

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseGraph {
    // Adjacency list: course -> list of prerequisites
    private final Map<String, List<String>> adjList = new LinkedHashMap<>();

    public void addCourse(String course) {
        adjList.putIfAbsent(course.trim(), new ArrayList<>());
    }

    public void addPrerequisite(String course, String prerequisite) {
        adjList.get(course.trim()).add(prerequisite.trim());
    }

    public Map<String, List<String>> getAdjList() {return adjList;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : adjList.entrySet()) {
            sb.append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append("\n");
        }
        return sb.toString();
    }
}

package com.example;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.time.Instant;
import java.nio.file.*;

@RestController
public class Reports {

    private static final String REPORTS_FILE = "Reports.txt";

    @PostMapping("/report")
    public Map<String, String> submitReport(
            @RequestParam String reported,
            @RequestParam String reason,
            @RequestParam String details,
            @RequestParam(required = false, defaultValue = "Anonymous") String reporter,
            @RequestParam(required = false, defaultValue = "None") String evidence
    ) {
        Map<String, String> result = new HashMap<>();
        try {
            FileWriter fw = new FileWriter(REPORTS_FILE, true);
            fw.write("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            fw.write("📋 PLAYER REPORT\n");
            fw.write("Reported Player: " + reported + "\n");
            fw.write("Reason: " + reason + "\n");
            fw.write("Details: " + details + "\n");
            fw.write("Reported By: " + reporter + "\n");
            fw.write("Evidence: " + evidence + "\n");
            fw.write("Time: " + Instant.now().toString() + "\n");
            fw.write("━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            fw.close();
            result.put("success", "true");
        } catch (IOException e) {
            result.put("success", "false");
        }
        return result;
    }

    @GetMapping("/reports")
    public List<String> getReports() {
        try {
            List<String> lines = Files.readAllLines(Path.of(REPORTS_FILE));
            List<String> reports = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith("━━━") && current.length() > 0) {
                    reports.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(line).append("\n");
                }
            }
            if (current.length() > 0 && !current.toString().isBlank()) {
                reports.add(current.toString().trim());
            }
            return reports;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @DeleteMapping("/reports")
    public Map<String, String> deleteReport(@RequestParam int index) {
        Map<String, String> result = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(REPORTS_FILE));
            List<String> reports = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith("━━━") && current.length() > 0) {
                    reports.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(line).append("\n");
                }
            }
            if (current.length() > 0 && !current.toString().isBlank()) {
                reports.add(current.toString().trim());
            }
            if (index >= 0 && index < reports.size()) {
                reports.remove(index);
            }
            FileWriter fw = new FileWriter(REPORTS_FILE, false);
            for (String report : reports) {
                fw.write("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                fw.write(report + "\n");
                fw.write("━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            }
            fw.close();
            result.put("success", "true");
        } catch (IOException e) {
            result.put("success", "false");
        }
        return result;
    }
}
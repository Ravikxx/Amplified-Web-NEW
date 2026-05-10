package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class Login {

    private static final String DISCORD_LINK = "https://discord.gg/E9QKXJ4QM5";
    private static final String SIGNUPS_FILE = "staff-signups.txt";
    private static final String PENDING_FILE = "staff-pending.txt";
    private static final Object FILE_LOCK = new Object();

    public static void main(String[] args) {
        SpringApplication.run(Login.class, args);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String role, @RequestParam String user, @RequestParam String pass) {
        Map<String, String> result = new HashMap<>();
        try {
            InputStream is = Login.class.getResourceAsStream("/staff.txt");
            if (is != null) {
                List<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.toList());
                is.close();
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3
                        && parts[0].trim().equals(role)
                        && parts[1].trim().equals(user)
                        && parts[2].trim().equals(pass)) {
                        result.put("success", "true");
                        result.put("discord", DISCORD_LINK);
                        return result;
                    }
                }
            }
            synchronized (FILE_LOCK) {
                File sf = new File(SIGNUPS_FILE);
                if (sf.exists()) {
                    for (String line : Files.readAllLines(sf.toPath(), StandardCharsets.UTF_8)) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3
                            && parts[0].trim().equals(role)
                            && parts[1].trim().equals(user)
                            && parts[2].trim().equals(pass)) {
                            result.put("success", "true");
                            result.put("discord", DISCORD_LINK);
                            return result;
                        }
                    }
                }
            }
            result.put("success", "false");
        } catch (IOException e) {
            result.put("success", "false");
        }
        return result;
    }

    @PostMapping("/staff/apply")
    public Map<String, String> apply(@RequestParam String role, @RequestParam String user, @RequestParam String pass) {
        Map<String, String> result = new HashMap<>();
        if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            result.put("success", "false");
            result.put("error", "Username and password cannot be empty.");
            return result;
        }
        String cleanRole = role.trim();
        String cleanUser = user.trim();
        String cleanPass = pass.trim();
        try {
            InputStream is = Login.class.getResourceAsStream("/staff.txt");
            if (is != null) {
                List<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.toList());
                is.close();
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2 && parts[1].trim().equalsIgnoreCase(cleanUser)) {
                        result.put("success", "false");
                        result.put("error", "Username already exists.");
                        return result;
                    }
                }
            }
        } catch (IOException e) { /* ignore */ }
        synchronized (FILE_LOCK) {
            try {
                File sf = new File(SIGNUPS_FILE);
                if (sf.exists()) {
                    for (String line : Files.readAllLines(sf.toPath(), StandardCharsets.UTF_8)) {
                        String[] parts = line.split(",");
                        if (parts.length >= 2 && parts[1].trim().equalsIgnoreCase(cleanUser)) {
                            result.put("success", "false");
                            result.put("error", "Username already exists.");
                            return result;
                        }
                    }
                }
            } catch (IOException e) { /* ignore */ }
            try {
                File pf = new File(PENDING_FILE);
                if (pf.exists()) {
                    for (String line : Files.readAllLines(pf.toPath(), StandardCharsets.UTF_8)) {
                        String[] parts = line.split(",");
                        if (parts.length >= 2 && parts[1].trim().equalsIgnoreCase(cleanUser)) {
                            result.put("success", "false");
                            result.put("error", "An application with this username is already pending.");
                            return result;
                        }
                    }
                }
            } catch (IOException e) { /* ignore */ }
            try (FileWriter fw = new FileWriter(PENDING_FILE, true); PrintWriter pw = new PrintWriter(fw)) {
                pw.println(cleanRole + "," + cleanUser + "," + cleanPass);
                result.put("success", "true");
            } catch (IOException e) {
                result.put("success", "false");
                result.put("error", "Server error. Please try again.");
            }
        }
        return result;
    }

    @GetMapping("/staff/pending")
    public List<Map<String, String>> getPending() {
        List<Map<String, String>> list = new ArrayList<>();
        synchronized (FILE_LOCK) {
            File pf = new File(PENDING_FILE);
            if (!pf.exists()) return list;
            try {
                for (String line : Files.readAllLines(pf.toPath(), StandardCharsets.UTF_8)) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        Map<String, String> entry = new HashMap<>();
                        entry.put("role", parts[0].trim());
                        entry.put("user", parts[1].trim());
                        list.add(entry);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading pending: " + e.getMessage());
            }
        }
        return list;
    }

    @PostMapping("/staff/approve")
    public Map<String, String> approve(@RequestParam int index) {
        Map<String, String> result = new HashMap<>();
        synchronized (FILE_LOCK) {
            File pf = new File(PENDING_FILE);
            if (!pf.exists()) { result.put("success", "false"); result.put("error", "No pending applications."); return result; }
            try {
                List<String> lines = new ArrayList<>(Files.readAllLines(pf.toPath(), StandardCharsets.UTF_8));
                if (index < 0 || index >= lines.size()) { result.put("success", "false"); result.put("error", "Invalid index."); return result; }
                String approved = lines.remove(index);
                Files.write(pf.toPath(), lines, StandardCharsets.UTF_8);
                try (FileWriter fw = new FileWriter(SIGNUPS_FILE, true); PrintWriter pw = new PrintWriter(fw)) {
                    pw.println(approved);
                }
                result.put("success", "true");
            } catch (IOException e) {
                result.put("success", "false");
                result.put("error", "Server error.");
            }
        }
        return result;
    }

    @PostMapping("/staff/decline")
    public Map<String, String> decline(@RequestParam int index) {
        Map<String, String> result = new HashMap<>();
        synchronized (FILE_LOCK) {
            File pf = new File(PENDING_FILE);
            if (!pf.exists()) { result.put("success", "false"); result.put("error", "No pending applications."); return result; }
            try {
                List<String> lines = new ArrayList<>(Files.readAllLines(pf.toPath(), StandardCharsets.UTF_8));
                if (index < 0 || index >= lines.size()) { result.put("success", "false"); result.put("error", "Invalid index."); return result; }
                lines.remove(index);
                Files.write(pf.toPath(), lines, StandardCharsets.UTF_8);
                result.put("success", "true");
            } catch (IOException e) {
                result.put("success", "false");
                result.put("error", "Server error.");
            }
        }
        return result;
    }
}

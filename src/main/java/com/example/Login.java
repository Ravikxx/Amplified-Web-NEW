package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class Login {

    private static final String DISCORD_LINK = "https://discord.gg/E9QKXJ4QM5";
    private static final String SIGNUPS_FILE = "staff-signups.txt";

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
            File signupsFile = new File(SIGNUPS_FILE);
            if (signupsFile.exists()) {
                List<String> signupLines = Files.readAllLines(signupsFile.toPath(), StandardCharsets.UTF_8);
                for (String line : signupLines) {
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
            result.put("success", "false");
        } catch (IOException e) {
            System.out.println("Error during login: " + e.getMessage());
            result.put("success", "false");
        }
        return result;
    }

    @PostMapping("/staff/signup")
    public Map<String, String> signup(@RequestParam String role, @RequestParam String user, @RequestParam String pass) {
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

        File signupsFile = new File(SIGNUPS_FILE);
        if (signupsFile.exists()) {
            try {
                List<String> signupLines = Files.readAllLines(signupsFile.toPath(), StandardCharsets.UTF_8);
                for (String line : signupLines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2 && parts[1].trim().equalsIgnoreCase(cleanUser)) {
                        result.put("success", "false");
                        result.put("error", "Username already exists.");
                        return result;
                    }
                }
            } catch (IOException e) { /* ignore */ }
        }

        try (FileWriter fw = new FileWriter(SIGNUPS_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(cleanRole + "," + cleanUser + "," + cleanPass);
            result.put("success", "true");
        } catch (IOException e) {
            System.out.println("Could not write to staff-signups.txt: " + e.getMessage());
            result.put("success", "false");
            result.put("error", "Server error. Please try again.");
        }
        return result;
    }
}

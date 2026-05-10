package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
    @RestController
    public class Login {

    private static final String DISCORD_LINK = "https://discord.gg/E9QKXJ4QM5";

    public static void main(String[] args) {
                SpringApplication.run(Login.class, args);
    }

    @PostMapping("/login")
            public Map<String, String> login(@RequestParam String role, @RequestParam String user, @RequestParam String pass) {
                        Map<String, String> result = new HashMap<>();
                        try {
                                        InputStream is = Login.class.getResourceAsStream("/staff.txt");
                                        if (is == null) {
                                                            System.out.println("staff.txt not found in classpath!");
                                                            result.put("success", "false");
                                                            return result;
                                        }
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
                                        result.put("success", "false");
                        } catch (IOException e) {
                                        System.out.println("Error reading staff.txt: " + e.getMessage());
                                        result.put("success", "false");
                        }
                        return result;
            }
    }

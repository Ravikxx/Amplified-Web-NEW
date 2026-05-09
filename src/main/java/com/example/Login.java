package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.file.*;
import java.util.List;

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
            System.out.println("Looking for staff.txt at: " + new File("staff.txt").getAbsolutePath());
            List<String> lines = Files.readAllLines(Path.of("staff.txt"));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts[0].equals(role) && parts[1].equals(user) && parts[2].equals(pass)) {
                    result.put("success", "true");
                    result.put("discord", DISCORD_LINK);
                    return result;
                }
            }
            result.put("success", "false");
        } catch (IOException e) {
            result.put("success", "false");
        }
        return result; // test
    }
}
package com.example.testprogram.controller;

import com.example.testprogram.service.TestRunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestRunnerService testRunnerService;

    @PostMapping("/start")
    public String startTests() {
        testRunnerService.runTests();
        return "Started";
    }

    @PostMapping("/stop")
    public String stopTests() {
        testRunnerService.stopTests();
        return "Stopped";
    }

    @GetMapping("/logs")
    public List<String> getLogs() {
        return testRunnerService.getLogs();
    }

    @PostMapping("/save")
    public String saveResult(@RequestBody Map<String, String> payload) {
        String filename = payload.get("filename");
        try {
            testRunnerService.saveResults(filename);
            return "Saved to " + filename + ".txt";
        } catch (IOException e) {
            return "Error saving file: " + e.getMessage();
        }
    }
}
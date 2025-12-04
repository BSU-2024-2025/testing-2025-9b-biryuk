package com.example.testprogram.controller;

import com.example.testprogram.service.TestRunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestRunnerService testRunnerService;

    @PostMapping("/start")
    public void start() { testRunnerService.runTests(); }

    @PostMapping("/stop")
    public void stop() { testRunnerService.stopTests(); }

    @PostMapping("/pause")
    public void pause() { testRunnerService.togglePause(); }

    @GetMapping("/logs")
    public List<String> logs() { return testRunnerService.getLogs(); }

    // Новый метод для ручного ввода
    @PostMapping("/calculate")
    public Map<String, String> calculate(@RequestBody Map<String, String> payload) {
        String result = testRunnerService.calculateManually(payload.get("expression"));
        return Map.of("result", result);
    }

    @PostMapping("/save")
    public String save(@RequestBody Map<String, String> payload) {
        try {
            testRunnerService.saveResults(payload.get("filename"));
            return "Saved";
        } catch (Exception e) { return "Error"; }
    }
}
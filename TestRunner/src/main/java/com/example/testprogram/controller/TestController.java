package com.example.testprogram.controller;

import com.example.testprogram.service.ScriptManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private ScriptManagerService scriptManagerService;

    @GetMapping("/stream-script")
    public SseEmitter streamScript(@RequestParam String processId, @RequestParam String code) {
        return scriptManagerService.executeScript(processId, code);
    }

    @PostMapping("/script/stop")
    public void stopScript(@RequestBody Map<String, String> payload) {
        scriptManagerService.stopScript(payload.get("processId"));
    }

    @PostMapping("/script/pause")
    public void pauseScript(@RequestBody Map<String, String> payload) {
        scriptManagerService.pauseScript(payload.get("processId"));
    }
}
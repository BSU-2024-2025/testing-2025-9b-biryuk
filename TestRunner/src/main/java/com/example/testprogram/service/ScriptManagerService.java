package com.example.testprogram.service;

import com.example.testprogram.logic.ScriptExecutionContext;
import com.example.testprogram.logic.ScriptInterpreter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScriptManagerService {
    private final Map<String, ScriptExecutionContext> activeScripts = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public SseEmitter executeScript(String processId, String code) {
        SseEmitter emitter = new SseEmitter(300_000L);
        ScriptExecutionContext context = new ScriptExecutionContext(processId, emitter);

        activeScripts.put(processId, context);

        executor.submit(() -> {
            try {
                new ScriptInterpreter(context).run(code);
            } finally {
                activeScripts.remove(processId);
            }
        });

        return emitter;
    }

    public void stopScript(String processId) {
        ScriptExecutionContext ctx = activeScripts.get(processId);
        if (ctx != null) ctx.stop();
    }

    public void pauseScript(String processId) {
        ScriptExecutionContext ctx = activeScripts.get(processId);
        if (ctx != null) {
            if (ctx.isPaused()) ctx.resume();
            else ctx.pause();
        }
    }
}
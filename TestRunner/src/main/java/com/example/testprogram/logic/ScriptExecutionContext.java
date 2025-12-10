package com.example.testprogram.logic;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScriptExecutionContext {
    private final String processId;
    private final SseEmitter emitter;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    public ScriptExecutionContext(String processId, SseEmitter emitter) {
        this.processId = processId;
        this.emitter = emitter;
    }

    public boolean isPaused() { return isPaused.get(); }
    public boolean isStopped() { return isStopped.get(); }

    public void pause() { isPaused.set(true); sendLog("SYSTEM", "Script paused."); }
    public void resume() { isPaused.set(false); sendLog("SYSTEM", "Script resumed."); }
    public void stop() { isStopped.set(true); sendLog("SYSTEM", "Script stopped."); }

    public void sendLog(String msg) {
        try {
            emitter.send(msg);
        } catch (IOException e) {
            stop();
        }
    }

    public void sendLog(String type, String msg) {
        sendLog("[" + type + "]:" + msg);
    }

    public void complete() {
        try {
            emitter.complete();
        } catch (Exception ignored) {}
    }
}
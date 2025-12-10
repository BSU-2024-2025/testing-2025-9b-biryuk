package com.example.testprogram.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ScriptInterpreter {
    private final Map<String, Double> scalars = new HashMap<>();
    private final Map<String, double[]> arrays = new HashMap<>();

    private final Calculator calculator = new Calculator(scalars, arrays);
    private final ScriptExecutionContext context;

    public ScriptInterpreter(ScriptExecutionContext context) {
        this.context = context;
    }

    public void run(String code) {
        scalars.clear();
        arrays.clear();
        broadcastVariables();
        try {
            String codeWithoutComments = code.replaceAll("//.*", "");
            String cleanCode = codeWithoutComments.replaceAll("[\n\r]", " ").trim();

            executeBlock(cleanCode);

            context.sendLog("SYSTEM", "Execution finished.");
        } catch (InterruptedException e) {
            context.sendLog("SYSTEM", "Script execution interrupted.");
        } catch (Exception e) {
            context.sendLog("ERROR", "Runtime Error: " + e.getMessage());
        } finally {
            context.complete();
        }
    }

    private void executeBlock(String code) throws InterruptedException {
        int i = 0;
        while (i < code.length()) {
            checkState();

            char c = code.charAt(i);
            if (Character.isWhitespace(c) || c == ';') { i++; continue; }

            if (code.startsWith("while", i)) {
                i += 5;
                int conditionStart = code.indexOf("(", i);
                int conditionEnd = findClosingParen(code, conditionStart);
                String conditionExpr = code.substring(conditionStart + 1, conditionEnd);

                i = conditionEnd + 1;
                while (i < code.length() && Character.isWhitespace(code.charAt(i))) i++;

                int blockStart = i;
                if (code.charAt(blockStart) != '{') throw new IllegalArgumentException("Expected '{' after while");

                int blockEnd = findClosingBrace(code, blockStart);
                String loopBody = code.substring(blockStart + 1, blockEnd);

                int safety = 0;
                while (calculator.calculate(conditionExpr) > 0.0) {
                    checkState();
                    if (safety++ > 1000) throw new RuntimeException("Infinite loop detected");
                    executeBlock(loopBody);
                }
                i = blockEnd + 1;
            }
            else if (code.startsWith("delay", i)) {
                i += 5;
                int start = code.indexOf("(", i);
                int end = code.indexOf(")", start);
                String arg = code.substring(start + 1, end);

                long seconds = (long) calculator.calculate(arg);
                context.sendLog("SYSTEM", "Sleeping for " + seconds + "s...");
                Thread.sleep(seconds * 1000);
                i = end + 1;
            }
            else if (code.startsWith("print", i)) {
                i += 5;
                int start = code.indexOf("(", i);
                int end = code.indexOf(")", start);
                String arg = code.substring(start + 1, end);

                double val = calculator.calculate(arg);
                context.sendLog("Output: " + val);
                i = end + 1;
            }
            else {
                int nextSemi = code.indexOf(";", i);
                if (nextSemi == -1) nextSemi = code.length();
                String statement = code.substring(i, nextSemi).trim();

                if (statement.contains("[") && statement.contains("]") &&
                        (!statement.contains("=") || statement.contains("{"))) {

                    String[] parts = statement.split("=");
                    String decl = parts[0].trim(); // a[5]

                    int bOpen = decl.indexOf('[');
                    int bClose = decl.lastIndexOf(']');

                    String arrName = decl.substring(0, bOpen).trim();
                    String sizeExpr = decl.substring(bOpen + 1, bClose);

                    if (arrays.containsKey(arrName)) {
                    } else {
                        int size = (int) calculator.calculate(sizeExpr);
                        if (size < 0) throw new RuntimeException("Array size cannot be negative: " + size);

                        double[] newArr = new double[size];

                        if (parts.length > 1) {
                            String initRaw = parts[1].trim();
                            if (!initRaw.startsWith("{") || !initRaw.endsWith("}")) {
                                throw new IllegalArgumentException("Array initialization requires {...}");
                            }
                            String initStr = initRaw.substring(1, initRaw.length() - 1).trim();
                            String[] initVals = initStr.split(",");

                            for(int k=0; k<Math.min(size, initVals.length); k++) {
                                if(!initVals[k].trim().isEmpty())
                                    newArr[k] = calculator.calculate(initVals[k]);
                            }
                        }
                        arrays.put(arrName, newArr);
                        broadcastVariables();
                    }
                }

                if (statement.matches(".*?\\[.+?].*?=.*") && !statement.contains("{")) {
                    String[] parts = statement.split("=", 2);
                    String left = parts[0].trim();
                    String right = parts[1].trim();

                    int bracketOpen = left.indexOf('[');
                    int bracketClose = left.lastIndexOf(']');

                    String arrName = left.substring(0, bracketOpen).trim();
                    String indexExpr = left.substring(bracketOpen + 1, bracketClose);

                    if (arrays.containsKey(arrName)) {
                        int idx = (int) calculator.calculate(indexExpr);
                        double val = calculator.calculate(right);
                        double[] arr = arrays.get(arrName);
                        if (idx >= 0 && idx < arr.length) {
                            arr[idx] = val;
                            broadcastVariables();
                        } else {
                            throw new RuntimeException("Array index out of bounds: " + arrName + "[" + idx + "]");
                        }
                    } else if (!statement.contains("{")) {
                        throw new RuntimeException("Unknown array: " + arrName);
                    }
                }
                else if (statement.contains("=")) {
                    String[] parts = statement.split("=");
                    String varName = parts[0].trim();
                    if (varName.isEmpty()) throw new IllegalArgumentException("Variable name cannot be empty");

                    double val = calculator.calculate(parts[1]);
                    scalars.put(varName, val);
                    broadcastVariables();
                }

                i = nextSemi + 1;
            }
        }
    }

    private void broadcastVariables() {
        StringBuilder json = new StringBuilder("{\"scalars\":{");

        json.append(scalars.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":" + e.getValue())
                .collect(Collectors.joining(",")));

        json.append("}, \"arrays\":{");

        json.append(arrays.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":" + Arrays.toString(e.getValue()))
                .collect(Collectors.joining(",")));

        json.append("}}");

        context.sendLog("VARS", json.toString());
    }

    private void checkState() throws InterruptedException {
        if (context.isStopped()) throw new InterruptedException("Script stopped by user");
        while (context.isPaused()) { Thread.sleep(200); if (context.isStopped()) throw new InterruptedException("Script stopped by user"); }
    }
    private int findClosingParen(String text, int openPos) {
        int balance = 1;
        for (int i = openPos + 1; i < text.length(); i++) {
            if (text.charAt(i) == '(') balance++;
            if (text.charAt(i) == ')') balance--;
            if (balance == 0) return i;
        }
        return -1;
    }
    private int findClosingBrace(String text, int openPos) {
        int balance = 1;
        for (int i = openPos + 1; i < text.length(); i++) {
            if (text.charAt(i) == '{') balance++;
            if (text.charAt(i) == '}') balance--;
            if (balance == 0) return i;
        }
        return -1;
    }
}
package com.example.testprogram.logic;

import java.util.Stack;
import java.util.Map;

public class Calculator {
    private final Map<String, Double> scalars;
    private final Map<String, double[]> arrays;

    public Calculator(Map<String, Double> scalars, Map<String, double[]> arrays) {
        this.scalars = scalars;
        this.arrays = arrays;
    }

    public double calculate(String expression) {
        if (expression == null || expression.trim().isEmpty()) return 0;
        String cleaned = expression.replaceAll("\\s+", "");
        return evaluateMathExpression(cleaned);
    }

    private double evaluateMathExpression(String expr) {
        if (expr.contains("<")) {
            String[] parts = expr.split("<", 2);
            double left = evaluateMathExpression(parts[0]);
            double right = evaluateMathExpression(parts[1]);
            return (left < right) ? 1.0 : 0.0;
        }
        if (expr.contains(">")) { // Добавляем >
            String[] parts = expr.split(">", 2);
            double left = evaluateMathExpression(parts[0]);
            double right = evaluateMathExpression(parts[1]);
            return (left > right) ? 1.0 : 0.0;
        }

        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i++));
                }
                i--;
                numbers.push(Double.parseDouble(num.toString()));
            } else if (Character.isLetter(c)) {
                StringBuilder nameBuilder = new StringBuilder();
                while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                    nameBuilder.append(expr.charAt(i++));
                }
                String name = nameBuilder.toString();

                if (i < expr.length() && expr.charAt(i) == '[') {
                    i++;
                    int braceBalance = 1;
                    StringBuilder indexExpr = new StringBuilder();
                    while (i < expr.length() && braceBalance > 0) {
                        char ic = expr.charAt(i++);
                        if (ic == '[') braceBalance++;
                        if (ic == ']') braceBalance--;
                        if (braceBalance > 0) indexExpr.append(ic);
                    }
                    if (braceBalance != 0) throw new IllegalArgumentException("Mismatched brackets in array access");
                    i--;

                    int idx = (int) evaluateMathExpression(indexExpr.toString());
                    if (arrays.containsKey(name) && idx >= 0 && idx < arrays.get(name).length) {
                        numbers.push(arrays.get(name)[idx]);
                    } else {
                        numbers.push(0.0); // 0, если массив не существует или индекс невалиден
                    }
                } else if (i < expr.length() && expr.charAt(i) == '(') {
                    i++;
                    int parenBalance = 1;
                    StringBuilder argsRaw = new StringBuilder();
                    while (i < expr.length() && parenBalance > 0) {
                        char ic = expr.charAt(i++);
                        if (ic == '(') parenBalance++;
                        if (ic == ')') parenBalance--;
                        if (parenBalance > 0) argsRaw.append(ic);
                    }
                    if (parenBalance != 0) throw new IllegalArgumentException("Mismatched parenthesis in function call");
                    i--;

                    numbers.push(executeFunction(name, argsRaw.toString()));
                } else {
                    i--;
                    numbers.push(scalars.getOrDefault(name, 0.0));
                }
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    applyOperation(numbers, operators.pop());
                }
                if (operators.isEmpty() || operators.pop() != '(') {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            } else if (isOperator(c)) {
                if (c == '-' && (i == 0 || expr.charAt(i-1) == '(' || isOperator(expr.charAt(i-1)))) {
                    numbers.push(0.0);
                }

                while (!operators.isEmpty() && getPriority(operators.peek()) >= getPriority(c)) {
                    applyOperation(numbers, operators.pop());
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            if (operators.peek() == '(') throw new IllegalArgumentException("Mismatched parentheses left over");
            applyOperation(numbers, operators.pop());
        }
        return numbers.isEmpty() ? 0 : numbers.pop();
    }

    private double executeFunction(String funcName, String argsStr) {
        String[] args = argsStr.split(",");
        funcName = funcName.toLowerCase();

        if (args.length == 1 && arrays.containsKey(args[0])) {
            double[] arr = arrays.get(args[0]);
            if (arr.length == 0) return 0;
            double val = arr[0];
            for (double v : arr) {
                if (funcName.equals("min")) val = Math.min(val, v);
                if (funcName.equals("max")) val = Math.max(val, v);
            }
            return val;
        }

        if (args.length >= 1 && (funcName.equals("min") || funcName.equals("max") )) {
            double result = evaluateMathExpression(args[0]);
            for (int k = 1; k < args.length; k++) {
                double nextVal = evaluateMathExpression(args[k]);
                if (funcName.equals("min")) result = Math.min(result, nextVal);
                if (funcName.equals("max")) result = Math.max(result, nextVal);
            }
            return result;
        }

        return 0.0;
    }

    private boolean isOperator(char c) { return c == '+' || c == '-' || c == '*' || c == '/'; }
    private int getPriority(char op) {
        if (op == '*' || op == '/') return 2;
        if (op == '+' || op == '-') return 1;
        return 0;
    }
    private void applyOperation(Stack<Double> numbers, char operation) {
        if (numbers.size() < 2) throw new IllegalArgumentException("Invalid expression: missing operand");
        double b = numbers.pop();
        double a = numbers.pop();
        switch (operation) {
            case '+': numbers.push(a + b); break;
            case '-': numbers.push(a - b); break;
            case '*': numbers.push(a * b); break;
            case '/': numbers.push(a / b); break;
        }
    }
}
package com.example.testprogram.logic;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

public class Calculator {
    private Map<String, Double> variables = new HashMap<>();
    public class SimpleParser {
        private String expression;
        private int position;
        private int length;

        public SimpleParser(String expression) {
            this.expression = expression != null ? expression : "";
            this.position = 0;
            this.length = this.expression.length();
        }

        public boolean parseExpression() {
            return parseOperand();
        }

        public boolean parseOperand() {
            return parseNum();
        }

        public boolean parseNum() {

            return false;
        }
    }
    public double calculate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }

        String cleanedExpr = expression.replaceAll("\\s+", "");

        if (cleanedExpr.contains(";")) {
            String[] expressions = cleanedExpr.split(";");
            double lastResult = 0;
            for (String expr : expressions) {
                lastResult = evaluateSingleExpression(expr);
            }
            return lastResult;
        } else {
            return evaluateSingleExpression(cleanedExpr);
        }
    }

    private double evaluateSingleExpression(String expr) {
        if (expr.contains("=")) {
            return parseAssignment(expr);
        } else {
            return evaluateMathExpression(expr);
        }
    }

    private double parseAssignment(String expr) {
        String[] parts = expr.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid assignment: " + expr);
        }

        String varName = parts[0];
        if (!varName.matches("[a-zA-Z_][a-zA-Z_0-9]*")) {
            throw new IllegalArgumentException("Invalid variable name: " + varName);
        }

        double value = evaluateMathExpression(parts[1]);
        variables.put(varName, value);
        return value;
    }

    private double evaluateMathExpression(String expr) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        boolean expectOperand = true;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isDigit(c) || c == '.' || (expectOperand && c == '-')) {
                StringBuilder num = new StringBuilder();
                boolean isNegative = false;

                if (c == '-' && expectOperand) {
                    isNegative = true;
                    i++;
                    if (i >= expr.length()) break;
                    c = expr.charAt(i);
                }

                while (i < expr.length() &&
                        (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i++));
                }
                i--;

                double value = num.length() > 0 ? Double.parseDouble(num.toString()) : 0;
                if (isNegative) {
                    value = -value;
                }
                numbers.push(value);
                expectOperand = false;
            }
            else if (Character.isLetter(c)) {
                StringBuilder varName = new StringBuilder();
                while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                    varName.append(expr.charAt(i++));
                }
                i--;

                String var = varName.toString();
                if (!variables.containsKey(var)) {
                    throw new IllegalArgumentException("Unknown variable: " + var);
                }
                numbers.push(variables.get(var));
                expectOperand = false;
            }
            else if (c == '(') {
                operators.push(c);
                expectOperand = true;
            }
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    applyOperation(numbers, operators.pop());
                }
                operators.pop();
                expectOperand = false;
            }
            else if (isOperator(c)) {
                while (!operators.isEmpty() &&
                        getPriority(operators.peek()) >= getPriority(c)) {
                    applyOperation(numbers, operators.pop());
                }
                operators.push(c);
                expectOperand = true;
            }
        }

        while (!operators.isEmpty()) {
            applyOperation(numbers, operators.pop());
        }

        return numbers.pop();
    }

    private void applyOperation(Stack<Double> numbers, char operation) {
        double b = numbers.pop();
        double a = numbers.pop();

        switch (operation) {
            case '+': numbers.push(a + b); break;
            case '-': numbers.push(a - b); break;
            case '*': numbers.push(a * b); break;
            case '/':
                if (b == 0) {
                    numbers.push(a / b);
                } else {
                    numbers.push(a / b);
                }
                break;
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int getPriority(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return 0;
        }
    }

    public void clearVariables() {
        variables.clear();
    }
}
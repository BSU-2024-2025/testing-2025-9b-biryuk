package com.example.testprogram.logic;

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
        skipWhitespace();

        if (position >= length) {
            return true;
        }

        if (isCommentStart()) {
            parseComment();
            skipWhitespace();
            return position == length;
        }

        if (!parseNum()) {
            return false;
        }

        skipWhitespace();

        while (position < length && expression.charAt(position) == '+') {
            position++;
            skipWhitespace();

            if (!parseNum()) {
                return false;
            }

            skipWhitespace();
        }

        parseComment();

        skipWhitespace();
        return position == length;
    }

    public boolean parseNum() {
        if (position >= length) {
            return false;
        }

        char currentChar = expression.charAt(position);

        if (Character.isDigit(currentChar)) {
            position++;

            while (position < length && Character.isDigit(expression.charAt(position))) {
                position++;
            }
            return true;
        }

        return false;
    }

    private boolean isCommentStart() {
        return position < length - 1 &&
                expression.charAt(position) == '/' &&
                expression.charAt(position + 1) == '/';
    }

    private void parseComment() {
        if (isCommentStart()) {
            position += 2;

            while (position < length && expression.charAt(position) != '\n') {
                position++;
            }
        }
    }

    private void skipWhitespace() {
        while (position < length && Character.isWhitespace(expression.charAt(position))) {
            position++;
        }
    }
}

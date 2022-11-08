package io.textor.codec;

import io.textor.DecodingState;

public class CodecUtils {
    static int moveAfter(String expr, int offset, char token) {
        int n = expr.indexOf(token, offset);
        if (n == -1 || n == expr.length() - 1) {
            return -1;
        }
        else {
            return n + 1;
        }
    }

    static int moveAfter(String expr, int offset, CharacterChecker skipped) {
        validateOffset(expr, offset);
        int cursor = offset;
        for (; cursor < expr.length(); ++cursor) {
            char c = expr.charAt(cursor);
            if (!skipped.check(c)) {
                break;
            }
        }
        return cursor;
    }

    static void validateOffset(String expr, int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Illegal offset: " + offset + ".");
        }
        if (offset > expr.length()) {
            throw new IllegalArgumentException("Offset overflow.");
        }
    }

    static String consumeSkip(String expr, char token, DecodingState state, CharacterChecker checker) {
        validateOffset(expr, state.getCursor());
        int begin = state.getCursor();
        int cur = begin;
        for (; cur < expr.length(); ++cur) {
            char c = expr.charAt(cur);
            if (c == token) {
                break;
            }
            else if (!checker.check(c)) {
                throw new IllegalArgumentException("Illegal character '" + c + "'.");
            }
        }
        state.setCursor(CodecUtils.moveAfter(expr, cur, token));
        return expr.substring(begin, cur);
    }

    static boolean isNumber(char c) {
        return '0' <= c && c <= '9';
    }

    static boolean isUpperLetter(char c) {
        return 'A' <= c && c <= 'Z';
    }

    static boolean isLowerLetter(char c) {
        return 'a' <= c && c <= 'z';
    }
}

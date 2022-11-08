package io.textor.codec;

import io.textor.DecodingState;

public class DecodeUtils {
    static int moveAfter(String expr, int offset, char token) {
        int n = expr.indexOf(token, offset);
        if (n == -1 || n == expr.length() - 1) {
            return -1;
        }
        else {
            return n + 1;
        }
    }

    static String consumeSkip(String expr, char token, DecodingState state, CharacterChecker checker) {
        int begin = state.getCursor();
        if (begin < 0) {
            throw new IllegalArgumentException("Illegal offset: " + begin + ".");
        }
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
        state.setCursor(DecodeUtils.moveAfter(expr, cur, token));
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

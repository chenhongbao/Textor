package io.textor.utils;

import io.textor.ColumnDescriptor;
import io.textor.DecodingState;
import io.textor.ValueDescriptor;
import io.textor.ValueType;

public class KeyUtils {
    public static ColumnDescriptor decodeKey(String expr, DecodingState state) {
        String name = decodeColumnName(expr, state);
        ValueDescriptor descriptor = decodeValueDescriptor(expr, state);
        return new ColumnDescriptor(name, descriptor);
    }

    private static ValueDescriptor decodeValueDescriptor(String expr, DecodingState state) {
        ValueType type = decodeType(expr, state);
        int binarySize = 0, precision = 0, fraction = 0;
        switch (type) {
            case ASCII, INTEGER, TIMESTAMP -> state.setCursor(DecodeUtils.moveAfter(expr, state.getCursor(), ')'));
            case BINARY ->
                    binarySize = Integer.parseInt(DecodeUtils.consumeSkip(expr, ')', state, DecodeUtils::isNumber));
            case DECIMAL -> {
                String params = DecodeUtils.consumeSkip(expr, ')', state, c -> DecodeUtils.isNumber(c) || c == ',');
                String[] splits = params.split(",");
                if (splits.length == 0 || splits.length > 2) {
                    throw new IllegalArgumentException("Illegal type parameters '" + params + "'.");
                }
                precision = Integer.parseInt(splits[0]);
                if (splits.length == 2) {
                    fraction = Integer.parseInt(splits[1]);
                }
            }
        }
        return new ValueDescriptor(type, precision, fraction, binarySize);
    }

    private static String decodeColumnName(String expr, DecodingState state) {
        return DecodeUtils.consumeSkip(expr, '(', state, KeyUtils::isValidKeyCharacter);
    }

    private static ValueType decodeType(String expr, DecodingState state) {
        int begin = state.getCursor();
        if (begin < 0) {
            throw new IllegalArgumentException("Illegal offset: " + begin + ".");
        }
        if (begin >= expr.length()) {
            throw new IllegalArgumentException("Offset overflow.");
        }
        if (begin == expr.length() - 1) {
            throw new IllegalArgumentException("Incomplete type.");
        }
        char c = expr.charAt(begin);
        char n = expr.charAt(begin + 1);
        switch (c) {
            case 'A', 'L', 'T' -> {
                if (n != ')') {
                    throw new IllegalArgumentException("Unexpected character '" + n + "' after type name.");
                }
                state.setCursor(DecodeUtils.moveAfter(expr, begin, ')'));
                return switch (c) {
                    case 'A' -> ValueType.ASCII;
                    case 'L' -> ValueType.INTEGER;
                    case 'T' -> ValueType.TIMESTAMP;
                    default -> throw new IllegalStateException("Type name is expected to be 'A', 'L' or 'T' but find '" + c + "'.");
                };
            }
            case 'B', 'D' -> {
                if (n != ':') {
                    throw new IllegalArgumentException("Unexpected character '" + n + "' after type name.");
                }
                state.setCursor(DecodeUtils.moveAfter(expr, begin, ':'));
                return switch (c) {
                    case 'B' -> ValueType.BINARY;
                    case 'D' -> ValueType.DECIMAL;
                    default -> throw new IllegalStateException("Type name is expected to be 'B' or 'D' but find '" + c + "'.");
                };
            }
            default -> throw new IllegalArgumentException("Unsupported type '" + c + "'.");
        }
    }

    private static boolean isValidKeyCharacter(char c) {
        return DecodeUtils.isUpperLetter(c) || DecodeUtils.isLowerLetter(c) || DecodeUtils.isNumber(c) || c == '_';
    }
}

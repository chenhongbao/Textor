package io.textor.codec;

import io.textor.*;

public class KeyUtils {

    public static String encodeKey(ColumnDescriptor columnDescriptor) {
        KeyDescriptor key = columnDescriptor.getKeyDescriptor();
        ValueDescriptor value = columnDescriptor.getValueDescriptor();
        return key.getType().getToken()  + key.getName() + "(" + encodeType(value) + ")";
    }

    private static String encodeType(ValueDescriptor descriptor) {
        return switch (descriptor.getType()) {
            case ASCII, INTEGER, TIMESTAMP -> descriptor.getType().getToken();
            case BINARY -> descriptor.getType().getToken() + ":" + descriptor.getBinarySize();
            case DECIMAL -> descriptor.getType().getToken() + ":" + descriptor.getDecimalWidth() + "," + descriptor.getDecimalPrecision();
        };
    }

    public static ColumnDescriptor decodeKey(String expr, DecodingState state) {
        KeyDescriptor key = decodeKeyDescriptor(expr, state);
        ValueDescriptor value = decodeValueDescriptor(expr, state);
        return new ColumnDescriptor(key, value);
    }

    private static ValueDescriptor decodeValueDescriptor(String expr, DecodingState state) {
        ValueType type = decodeType(expr, state);
        int binarySize = 0, precision = 0, fraction = 0;
        switch (type) {
            case ASCII, INTEGER, TIMESTAMP -> state.setCursor(CodecUtils.moveAfter(expr, state.getCursor(), ')'));
            case BINARY ->
                    binarySize = Integer.parseInt(CodecUtils.consumeSkip(expr, ')', state, CodecUtils::isNumber));
            case DECIMAL -> {
                String params = CodecUtils.consumeSkip(expr, ')', state, c -> CodecUtils.isNumber(c) || c == ',');
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

    private static KeyDescriptor decodeKeyDescriptor(String expr, DecodingState state) {
        CodecUtils.validateOffset(expr, state.getCursor());
        KeyType type;
        int first = state.getCursor();
        if (expr.charAt(first) == '@') {
            type = KeyType.ATTRIBUTE;
            state.setCursor(first + 1);
        }
        else {
            type = KeyType.COLUMN;
        }
        return new KeyDescriptor(decodeColumnName(expr, state), type);
    }

    private static String decodeColumnName(String expr, DecodingState state) {
        return CodecUtils.consumeSkip(expr, '(', state, KeyUtils::isValidKeyCharacter);
    }

    private static ValueType decodeType(String expr, DecodingState state) {
        CodecUtils.validateOffset(expr, state.getCursor());
        int begin = state.getCursor();
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
                state.setCursor(CodecUtils.moveAfter(expr, begin, ')'));
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
                state.setCursor(CodecUtils.moveAfter(expr, begin, ':'));
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
        return CodecUtils.isUpperLetter(c) || CodecUtils.isLowerLetter(c) || CodecUtils.isNumber(c) || c == '_';
    }
}

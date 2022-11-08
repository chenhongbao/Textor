package io.textor.codec;

import io.textor.DecodingState;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ValueUtils {
    private final static char[] codes = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', '`', '^'
    };

    private final static byte[] decodes = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
            0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
            0x21, 0x22, 0x23, 0x00, 0x00, 0x00, 0x3F, 0x00,
            0x3E, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A,
            0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32,
            0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A,
            0x3B, 0x3C, 0x3D,
    };

    public class Binary {
        public static String encode(byte[] binary, int offset, int length)  {
            // 3 bytes to 4 letters.
            if (offset < 0 || length < 0) {
                throw new IllegalArgumentException("Illegal offset and length.");
            }
            if (binary == null || length == 0) {
                return "";
            }
            if (binary.length < offset + length) {
                throw new IllegalArgumentException("Offset and length overflow.");
            }
            StringBuilder encoded = new StringBuilder();
            int cur = 0;
            for (; cur + 3 <= binary.length; cur += 3) {
                encoded.append(encode3Bytes(binary[cur], binary[cur + 1], binary[cur + 2]));
            }
            if (binary.length - cur == 1) {
                encoded.append(encode3Bytes(binary[cur], (byte)0x00, (byte)0x00));
            }
            else if (binary.length - cur == 2) {
                encoded.append(encode3Bytes(binary[cur], binary[cur + 1], (byte)0x00));
            }
            return encoded.toString();
        }

        public static byte[] decode(String expr, int binarySize, DecodingState state) {
            // 4 letters to 3 bytes.
            CodecUtils.validateOffset(expr, state.getCursor());
            if (binarySize < 0) {
                throw new IllegalArgumentException("Illegal binary size: " + binarySize +".");
            }
            int remainStrSize = expr.length() - state.getCursor();
            if (remainStrSize < 0) {
                throw new IllegalArgumentException("Illegal offset: " + state.getCursor() + ".");
            }
            int totalSize = (binarySize / 3 + (binarySize % 3 != 0 ? 1 : 0)) * 4;
            if (totalSize > remainStrSize) {
                throw new IllegalArgumentException("Insufficient codes to parse binary of the given size.");
            }
            int end = state.getCursor() + totalSize;
            if (end < expr.length()) {
                char token = expr.charAt(end);
                if (token != ',') {
                    throw new IllegalArgumentException("Expect ',' at index " + (state.getCursor() + totalSize) + " but find '" + token + "'.");
                }
            }
            if (remainStrSize == 0 || binarySize == 0) {
                state.setCursor(CodecUtils.moveAfter(expr, state.getCursor(), ','));
                return null;
            }

            byte[] decoded = new byte[totalSize];
            int decodeCur = 0;
            int cur = state.getCursor();
            for (; cur + 4 <= end; cur += 4, decodeCur += 3) {
                byte[] decodeBytes;
                char[] chars = new char[] {expr.charAt(cur), expr.charAt(cur + 1), expr.charAt(cur + 2), expr.charAt(cur + 3)};
                try {
                     decodeBytes = decode4Letters(chars[0], chars[1], chars[2], chars[3]);
                }
                catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Illegal binary code at index " + cur + ", [" + String.copyValueOf(chars) + "].");
                }

                decoded[decodeCur] = decodeBytes[0];
                decoded[decodeCur + 1] = decodeBytes[1];
                decoded[decodeCur + 2] = decodeBytes[2];
            }

            state.setCursor(CodecUtils.moveAfter(expr, cur, ','));
            return Arrays.copyOf(decoded, binarySize);
        }

        private static String encode3Bytes(byte b0, byte b1, byte b2) {
            char[] c = new char[4];
            c[0] = codes[  b0 & 0b00111111];
            c[1] = codes[((b0 & 0b11000000) >> 6) | ((b1 & 0b00001111) << 2)];
            c[2] = codes[((b1 & 0b11110000) >> 4) | ((b2 & 0b00000011) << 4)];
            c[3] = codes[ (b2 & 0b11111100) >> 2];
            return String.copyValueOf(c);
        }

        private static byte[] decode4Letters(char c0, char c1, char c2, char c3) {
            if (!areValidBinaryCodes(c0, c1, c2, c3)) {
                throw new IllegalArgumentException("Invalid character is found in binary encodes.");
            }
            byte[] dc = new byte[3];
            dc[0] = (byte)(((decodes[c1] & 0b00000011) << 6) | (decodes[c0] & 0b00111111));
            dc[1] = (byte)(((decodes[c2] & 0b00001111) << 4) | ((decodes[c1] & 0b00111100) >> 2));
            dc[2] = (byte)(((decodes[c3] & 0b00111111) << 2) | ((decodes[c2] & 0b00110000) >> 4));
            return dc;
        }

        private static boolean areValidBinaryCodes(char... chars) {
            for(char c : chars) {
                if (!isValidBinaryCode(c)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isValidBinaryCode(char c) {
            return CodecUtils.isNumber(c) || CodecUtils.isLowerLetter(c) || CodecUtils.isUpperLetter(c) || c == '-' || c == '_';
        }
    }

    public class Decimal {
        public static String encode(Double decimal, int precision, int fraction) {
            if (precision < fraction) {
                throw new IllegalArgumentException("Invalid decimal parameters (" + precision + "," + fraction + ").");
            }
            return String.format("%" + (precision - fraction) + "." + fraction + "f", decimal);
        }

        public static Double decode(String expr, int precision, int fraction, DecodingState state) {
            CodecUtils.validateOffset(expr, state.getCursor());
            if (precision < 0 || fraction < 0) {
                throw new IllegalArgumentException("Illegal decimal parameters: " + precision + ", " + fraction + ".");
            }
            if (precision < fraction) {
                throw new IllegalArgumentException("Invalid decimal parameters (" + precision + "," + fraction + ").");
            }

            int intEnd = -1;
            int fracEnd = -1;
            int begin = state.getCursor();
            int cur = begin;
            for (; cur < expr.length(); ++cur) {
                char c = expr.charAt(cur);
                if (intEnd == -1)
                {
                    if (c == '.') {
                        intEnd = cur;
                    }
                    else if (!CodecUtils.isNumber(c)) {
                        throw new IllegalArgumentException("Illegal character '" + c + "' at index " + cur + ".");
                    }
                }
                else {
                    if (c == ',') {
                        fracEnd = cur;
                        break;
                    }
                    else if (!CodecUtils.isNumber(c)) {
                        throw new IllegalArgumentException("Illegal character '" + c + "' at index " + cur + ".");
                    }
                }

            }

            if (fracEnd - begin - 1 > precision) {
                throw new IllegalArgumentException("Precision overflow.");
            }
            if (fracEnd - intEnd - 1 > fraction) {
                throw new IllegalArgumentException("Fraction overflow.");
            }

            state.setCursor(CodecUtils.moveAfter(expr, cur, ','));
            return Double.parseDouble(expr.substring(begin, intEnd) + "." + expr.substring(intEnd + 1, fracEnd));
        }
    }

    public class Integer {
        public static String encode(Long integer) {
            return Long.toString(integer);
        }

        public static Long decode(String expr, DecodingState state) {
            CodecUtils.validateOffset(expr, state.getCursor());
            return Long.parseLong(CodecUtils.consumeSkip(expr,',', state, CodecUtils::isNumber));
        }
    }

    public class ASCII {
        public static String encode(String ascii) {
            StringBuilder encoded = new StringBuilder();
            int cur = 0;
            for (; cur < ascii.length(); ++cur) {
                char c = ascii.charAt(cur);
                if (c == '"') {
                    encoded.append("\\\"");
                }
                else if (c == '\\') {
                    encoded.append("\\\\");
                }
                else {
                    encoded.append(c);
                }
            }
            return "\"" + encoded.toString() + "\"";
        }


        public static String decode(String expr, DecodingState state) {
            CodecUtils.validateOffset(expr, state.getCursor());
            StringBuilder decoded = new StringBuilder();
            boolean isBegun = false;
            boolean isFinished = false;
            int cur = state.getCursor();
            for (; cur < expr.length(); ++cur) {
                char c = expr.charAt(cur);
                if (!isBegun) {
                    if (c == '"') {
                        isBegun = true;
                    }
                    else{
                        throw new IllegalArgumentException("Expect '\"' but find '" + c + "'.");
                    }
                }
                else {
                    if (c == '\\') {
                        if (cur == expr.length() - 1) {
                            throw new IllegalArgumentException("Incomplete replacement expression before end of string.");
                        }
                        char next = expr.charAt(cur + 1);
                        if (next == '"' || next == '\\') {
                            decoded.append(next);
                            cur += 1;
                        }
                        else {
                            throw new IllegalArgumentException("Unknown replacement '\\" + next + "'.");
                        }
                    }
                    else if (c == '"') {
                        isFinished = true;
                        break;
                    }
                    else {
                        decoded.append(c);
                    }
                }
            }

            if (!isFinished) {
                throw new IllegalArgumentException("Incomplete ASCII string.");
            } else if(cur < expr.length() - 1) {
                char c = expr.charAt(cur + 1);
                if (c != ',') {
                    throw new IllegalArgumentException("Unexpected character '" + c + "' after end of ASCII string.");
                }
            }

            state.setCursor(CodecUtils.moveAfter(expr, cur, ','));
            return decoded.toString();
        }
    }

    public class Timestamp {
        public static String encode(ZonedDateTime timestamp) {
            return timestamp.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }

        public static ZonedDateTime decode(String expr, DecodingState state) {
            CodecUtils.validateOffset(expr, state.getCursor());
            return ZonedDateTime.parse(CodecUtils.consumeSkip(expr, ',', state, Timestamp::isValidTimestampCharacter), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }

        private static boolean isValidTimestampCharacter(char c) {
            return CodecUtils.isNumber(c) || CodecUtils.isUpperLetter(c) || CodecUtils.isLowerLetter(c) || c == '-' || c == ':' || c == '+' || c == '[' || c == ']' || c == '/';
        }
    }
}

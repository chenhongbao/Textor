package io.textor.codec;

import io.textor.DecodingState;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValueUtilsTest {

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public static class BinaryTest {
        private final byte[] binary = new byte[4096];
        private final DecodingState state = new DecodingState(0);

        @BeforeEach
        public void before() {
            byte mask = (byte)0b10110010;
            byte value = 0;
            for (int index = 0; index < binary.length; ++index) {
                binary[index] = (byte)((value++) ^ mask);
            }

            state.setCursor(0);
        }

        @Test
        @DisplayName("Encode empty binary data.")
        public void encodeEmpty() {
            assertEquals("", ValueUtils.Binary.encode(null, 0, 0));
            assertEquals("", ValueUtils.Binary.encode(new byte[0], 0, 0));
            assertEquals("", ValueUtils.Binary.encode(new byte[1], 0, 0));
        }

        @Test
        @DisplayName("Encode with padding zero bytes.")
        public void encodePadding() {
            String encoded = ValueUtils.Binary.encode(binary, 0, binary.length);
            byte[] decoded = ValueUtils.Binary.decode(encoded, binary.length, state);

            assertEquals(Arrays.hashCode(binary), Arrays.hashCode(decoded));
            assertEquals(-1, state.getCursor());
        }

        @Test
        @DisplayName("Encoding with no padding.")
        public void encodeNoPadding() {
            int length = binary.length - binary.length % 3;

            String encoded = ValueUtils.Binary.encode(binary, 0, length);
            byte[] decoded = ValueUtils.Binary.decode(encoded, length, state);

            assertEquals(Arrays.hashCode(Arrays.copyOf(binary, length)), Arrays.hashCode(decoded));
            assertEquals(-1, state.getCursor());
        }

        @Test
        @DisplayName("Decode and see ',' at the end.")
        public void decodeBeforeComma() {
            String encoded = ValueUtils.Binary.encode(binary, 0, binary.length);
            encoded += ',';
            byte[] decoded = ValueUtils.Binary.decode(encoded, binary.length, state);

            assertEquals(Arrays.hashCode(Arrays.copyOf(binary, binary.length)), Arrays.hashCode(decoded));
            // Find ',' at the end of string and move cursor after ',' which is length of string.
            assertEquals(encoded.length(), state.getCursor());
        }

        @Test
        @DisplayName("Decoding string doesn't end with a comma.")
        public void decodeNoCommaEnding() {
            String encoded = ValueUtils.Binary.encode(binary, 0, binary.length);
            int decodeLength = binary.length - 1;
            // The character after binary codes is not comma ','.
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Binary.decode(encoded, decodeLength, state));
        }

        @Test
        @DisplayName("Illegal character appear in decoding string.")
        public void decodeIllegalChars() {
            String encoded = ValueUtils.Binary.encode(binary, 0, binary.length);
            String replaced = encoded.replace('0', '@').replace('1', ',');
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Binary.decode(replaced, binary.length, state));
        }

        @Test
        @DisplayName("Decode with length 0.")
        public void decodeLengthZero() {
            assertNull(ValueUtils.Binary.decode("ABCD", 0, state));
        }

        @Test
        @DisplayName("Decode empty string.")
        public void decodeEmpty() {
            assertThrowsExactly(NullPointerException.class, () -> ValueUtils.Binary.decode(null, 1, state));
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Binary.decode("", 1, state));
        }

        @Test
        @DisplayName("Decode with null state.")
        public void decodeNullState() {
            assertThrowsExactly(NullPointerException.class, () -> ValueUtils.Binary.decode("ABCD", 1, null));
        }

        @Test
        @DisplayName("Decode too many bytes.")
        public void decodeTooMany() {
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Binary.decode("ABCD", 10, state));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public static class ASCII {
        private final DecodingState state = new DecodingState(0);

        @BeforeEach
        public void before() {
            state.setCursor(0);
        }

        @Test
        @DisplayName("Encode empty string.")
        public void encodeEmpty() {
            assertEquals("\"\"", ValueUtils.ASCII.encode(""));
            assertEquals("\"\"", ValueUtils.ASCII.encode(null));
        }

        @Test
        @DisplayName("Encoding special chars.")
        public void encodeSpecialChars() {
            assertEquals("\"\\\"\\\\\"", ValueUtils.ASCII.encode("\"\\"));
        }

        @Test
        @DisplayName("Encode white chars.")
        public void encodeWhiteChars() {
            assertEquals("\"\n\r\u0020\"", ValueUtils.ASCII.encode("\n\r\u0020"));
        }

        @Test
        @DisplayName("Encode illegal non-ASCII chars.")
        public void encodeNonAsciiChars() {
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.ASCII.encode("你好"));
        }

        @Test
        @DisplayName("Decode special chars.")
        public void decodeSpecialChars() {
            assertEquals("\"\\", ValueUtils.ASCII.decode("\"\\\"\\\\\"", state));
        }

        @Test
        @DisplayName("Decode unknown replacement chars.")
        public void decodeUnknownReplacement() {
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.ASCII.decode("\"\\n\"", state));
        }

        @Test
        @DisplayName("Decode incomplete replacement chars.")
        public void decodeIncompleteReplacement() {
            // The last '\' combined with the ending quote '"' and form a replacement of '"'.
            // Then cursor goes to the end and no more ending quote found.
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.ASCII.decode("\"abcd\\\"", state));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public static class Integer {
        private final DecodingState state = new DecodingState(0);

        @BeforeEach
        public void before() {
            state.setCursor(0);
        }

        @Test
        @DisplayName("Encode null.")
        public void encodeNull() {
            assertEquals("", ValueUtils.Integer.encode(null));
        }

        @Test
        @DisplayName("Encode positive integer.")
        public void encodePositive() {
            assertEquals("12345", ValueUtils.Integer.encode(12345L));
        }

        @Test
        @DisplayName("Encode negative integer.")
        public void encodeNegative() {
            assertEquals("-12345", ValueUtils.Integer.encode(-12345L));
        }

        @Test
        @DisplayName("Encode negative zero.")
        public void encodeNegativeZero() {
            assertEquals("0", ValueUtils.Integer.encode(-0L));
        }

        @Test
        @DisplayName("Decode positive integer.")
        public void decodePositive() {
            assertEquals(12345, ValueUtils.Integer.decode("12345", state));
        }

        @Test
        @DisplayName("Decode negative number.")
        public void decodeNegative() {
            assertEquals(-12345, ValueUtils.Integer.decode("-12345", state));
        }

        @Test
        @DisplayName("Decode zeros.")
        public void decodeZeros() {
            assertEquals(-0L, ValueUtils.Integer.decode("-0", state));
            state.setCursor(0);
            assertEquals(0L, ValueUtils.Integer.decode("0", state));
        }

        @Test
        @DisplayName("Decode overflow integer.")
        public void decodeOverflow() {
            assertThrowsExactly(NumberFormatException.class, () -> ValueUtils.Integer.decode("123456789012345678901234567890", state));
        }
    }

    public static class Decimal {
        private final DecodingState state = new DecodingState(0);

        @BeforeEach
        public void before() {
            state.setCursor(0);
        }

        @Test
        @DisplayName("Encode null decimal.")
        public void encodeNull() {
            assertEquals("", ValueUtils.Decimal.encode(null, 0, 0));
        }

        @Test
        @DisplayName("Encode with wrong parameters.")
        public void encodeWrongParams() {
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Decimal.encode(0D, 0, 2));
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Decimal.encode(0D, -1, 0));
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Decimal.encode(0D, 2, 3));
            assertThrowsExactly(IllegalArgumentException.class, () -> ValueUtils.Decimal.encode(0D, 1, -1));
        }

        @Test
        @DisplayName("Encode with different params.")
        public void encodeDifferentParams() {
            assertEquals("1.1", ValueUtils.Decimal.encode(1.123D, 2, 1));
        }

        //TODO Keep on unit tests.
    }
}
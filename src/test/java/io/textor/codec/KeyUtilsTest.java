package io.textor.codec;

import io.textor.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyUtilsTest {

    @Test
    @DisplayName("Encode integer column.")
    void encodeIntegerColumn() {
        assertEquals("_key_(L)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("_key_", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.INTEGER, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode decimal column.")
    void encodeDecimalColumn() {
        assertEquals("KEY(D:10,5)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("KEY", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.DECIMAL, 10, 5, 0))));
    }

    @Test
    @DisplayName("Encode ASCII column.")
    void encodeAsciiColumn() {
        assertEquals("Key(A)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("Key", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.ASCII, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode binary column.")
    void encodeBinaryColumn() {
        assertEquals("Key123(B:128)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("Key123", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.BINARY, 0, 0, 128))));
    }

    @Test
    @DisplayName("Encode timestamp column.")
    void encodeTimestampColumn() {
        assertEquals("_123Key(T)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("_123Key", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode integer attribute.")
    void encodeIntegerAttr() {
        assertEquals("@_key_(L)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("_key_", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.INTEGER, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode decimal attribute.")
    void encodeDecimalAttr() {
        assertEquals("@KEY(D:10,5)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("KEY", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.DECIMAL, 10, 5, 0))));
    }

    @Test
    @DisplayName("Encode ASCII attribute.")
    void encodeAsciiAttr() {
        assertEquals("@Key(A)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("Key", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.ASCII, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode binary attribute.")
    void encodeBinaryAttr() {
        assertEquals("@Key123(B:128)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("Key123", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.BINARY, 0, 0, 128))));
    }

    @Test
    @DisplayName("Encode timestamp attribute.")
    void encodeTimestampAttr() {
        assertEquals("@_123Key(T)", KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("_123Key", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode empty key name.")
    public void encodeEmpty() {
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode null key name.")
    public void encodeNullKeyName() {
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor(null, KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode null key type.")
    public void encodeNullKeyType() {
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("key", null),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode null value type.")
    public void encodeNullValueType() {
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("key", KeyType.COLUMN),
                        new ValueDescriptor(null, 0, 0, 0))));
    }

    @Test
    @DisplayName("Encode wrong value type.")
    public void encodeWrongValueType() {
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("key", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.BINARY, 0, 0, -1))));
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("key", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.DECIMAL, 0, -1, 0))));
    }

    @Test
    @DisplayName("Encode wrong key name.")
    public void encodeWrongKeyName() {
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor(",", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("@", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
        assertThrowsExactly(IllegalArgumentException.class, ()-> KeyUtils.encodeKey(
                new ColumnDescriptor(
                        new KeyDescriptor("]", KeyType.ATTRIBUTE),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0))));
    }

    @Test
    @DisplayName("Decode integer key.")
    public void decodeIntegerKey() {
        ColumnDescriptor desc = KeyUtils.decodeKey("_key_(L)", new DecodingState(0));

        assertEquals("_key_", desc.getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, desc.getKeyDescriptor().getType());
        assertEquals(ValueType.INTEGER, desc.getValueDescriptor().getType());
        assertEquals(0, desc.getValueDescriptor().getBinarySize());
        assertEquals(0, desc.getValueDescriptor().getDecimalWidth());
        assertEquals(0, desc.getValueDescriptor().getDecimalPrecision());
    }

    @Test
    @DisplayName("Decode ACII key.")
    public void decodeAsciiKey() {
        ColumnDescriptor desc = KeyUtils.decodeKey("_key123(A)", new DecodingState(0));

        assertEquals("_key123", desc.getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, desc.getKeyDescriptor().getType());
        assertEquals(ValueType.ASCII, desc.getValueDescriptor().getType());
        assertEquals(0, desc.getValueDescriptor().getBinarySize());
        assertEquals(0, desc.getValueDescriptor().getDecimalWidth());
        assertEquals(0, desc.getValueDescriptor().getDecimalPrecision());
    }

    @Test
    @DisplayName("Decode decimal key.")
    public void decodeDecimalKey() {
        ColumnDescriptor desc = KeyUtils.decodeKey("_123(D:12,3)", new DecodingState(0));

        assertEquals("_123", desc.getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, desc.getKeyDescriptor().getType());
        assertEquals(ValueType.DECIMAL, desc.getValueDescriptor().getType());
        assertEquals(0, desc.getValueDescriptor().getBinarySize());
        assertEquals(12, desc.getValueDescriptor().getDecimalWidth());
        assertEquals(3, desc.getValueDescriptor().getDecimalPrecision());

        desc = KeyUtils.decodeKey("_123(D:12)", new DecodingState(0));

        assertEquals("_123", desc.getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, desc.getKeyDescriptor().getType());
        assertEquals(ValueType.DECIMAL, desc.getValueDescriptor().getType());
        assertEquals(0, desc.getValueDescriptor().getBinarySize());
        assertEquals(12, desc.getValueDescriptor().getDecimalWidth());
        assertEquals(0, desc.getValueDescriptor().getDecimalPrecision());
    }

    @Test
    @DisplayName("Decode binary key.")
    public void decodeBinaryKey() {
        ColumnDescriptor desc = KeyUtils.decodeKey("_123Key(B:128)", new DecodingState(0));

        assertEquals("_123Key", desc.getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, desc.getKeyDescriptor().getType());
        assertEquals(ValueType.BINARY, desc.getValueDescriptor().getType());
        assertEquals(128, desc.getValueDescriptor().getBinarySize());
        assertEquals(0, desc.getValueDescriptor().getDecimalWidth());
        assertEquals(0, desc.getValueDescriptor().getDecimalPrecision());
    }

    @Test
    @DisplayName("Decode timestamp key.")
    public void decodeTimestampKey() {
        ColumnDescriptor desc = KeyUtils.decodeKey("_KEY(T)", new DecodingState(0));

        assertEquals("_KEY", desc.getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, desc.getKeyDescriptor().getType());
        assertEquals(ValueType.TIMESTAMP, desc.getValueDescriptor().getType());
        assertEquals(0, desc.getValueDescriptor().getBinarySize());
        assertEquals(0, desc.getValueDescriptor().getDecimalWidth());
        assertEquals(0, desc.getValueDescriptor().getDecimalPrecision());
    }

    @Test
    @DisplayName("Decode wrong key name.")
    public void decodeWrongKeyName() {
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("(T)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("123(T)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey(",123(T)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey(".KEY(T)", new DecodingState(0)));
    }

    @Test
    @DisplayName("Decode wrong value type.")
    public void decodeWrongValueType() {
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(X)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B:128,0)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B:a)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B:,128)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B:)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:1,0,0)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:,1)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:a,2)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(A,B)", new DecodingState(0)));
    }

    @Test
    @DisplayName("Decode incomplete type.")
    public void decodeIncompleteType() {
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:1", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:1,", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(D:1,0", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(A", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B:", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key(B:1", new DecodingState(0)));
    }

    @Test
    @DisplayName("Decode incomplete key name.")
    public void decodeIncompleteKeyName() {
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("key", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyD", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyD:)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyD:1)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyD:1,)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyD:1,0)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyA)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyB)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyB:)", new DecodingState(0)));
        assertThrowsExactly(IllegalArgumentException.class, () -> KeyUtils.decodeKey("keyB:1)", new DecodingState(0)));
    }
}
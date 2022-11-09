package io.textor.codec;

import io.textor.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CodecTest {

    private final static ZonedDateTime now = ZonedDateTime.now();
    private final static byte[] binary = new byte[] { 0, 1, 2, 3 };
    private final static String timestampStr = ValueUtils.Timestamp.encode(now);
    private final static String binaryStr = ValueUtils.Binary.encode(binary, 0, 4);

    private Cell[] columns = new Cell[0];
    private Cell[] attrs = new Cell[0];
    private String columnStr = "";
    private String attrStr = "";

    @BeforeAll
    public void beforeAll() {
        columns = new Cell[] {
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Key1", KeyType.COLUMN),
                                new ValueDescriptor(ValueType.INTEGER, 0, 0, 0)),
                        1L),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Key2", KeyType.COLUMN),
                                new ValueDescriptor(ValueType.ASCII, 0, 0, 0)),
                        "ASCII string!"),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Key3", KeyType.COLUMN),
                                new ValueDescriptor(ValueType.DECIMAL, 3, 1, 0)),
                        12.3D),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Key4", KeyType.COLUMN),
                                new ValueDescriptor(ValueType.BINARY, 0, 0, 4)),
                        binary),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Key5", KeyType.COLUMN),
                                new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0)),
                        now),
        };
        attrs = new Cell[] {
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Attr1", KeyType.ATTRIBUTE),
                                new ValueDescriptor(ValueType.INTEGER, 0, 0, 0)),
                        1L),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Attr2", KeyType.ATTRIBUTE),
                                new ValueDescriptor(ValueType.ASCII, 0, 0, 0)),
                        "ASCII string!"),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Attr3", KeyType.ATTRIBUTE),
                                new ValueDescriptor(ValueType.DECIMAL, 3, 1, 0)),
                        12.3D),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Attr4", KeyType.ATTRIBUTE),
                                new ValueDescriptor(ValueType.BINARY, 0, 0, 4)),
                        binary),
                new Cell(1,
                        new ColumnDescriptor(
                                new KeyDescriptor("Attr5", KeyType.ATTRIBUTE),
                                new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0)),
                        now),
        };

        columnStr = "Key1(L)1,Key2(A)\"ASCII string!\",Key3(D:3,1)12.3,Key4(B:4)" + binaryStr + ",Key5(T)" + timestampStr;
        attrStr = "@Attr1(L)1,@Attr2(A)\"ASCII string!\",@Attr3(D:3,1)12.3,@Attr4(B:4)" + binaryStr + ",@Attr5(T)" + timestampStr;
    }

    @Test
    @DisplayName("Encode empty.")
    public void encodeEmpty() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Codec.encode(new Row(new Cell[0], null)));
        assertThrowsExactly(IllegalArgumentException.class, () -> Codec.encode(new Row(new Cell[0], new Cell[0])));
    }

    @Test
    @DisplayName("Encode null row.")
    public void encodeNull() {
        assertThrowsExactly(IllegalArgumentException.class, () -> new Row(null, null));
        assertEquals("", Codec.encode(null));
    }

    @Test
    @DisplayName("Encode columns only.")
    public void encodeColumnsOnly() {
        assertEquals(columnStr, Codec.encode(new Row(columns, null)));
    }

    @Test
    @DisplayName("Encode attributes and columns.")
    public void encodeAttributesColumns() {
        assertEquals(attrStr + "," + columnStr, Codec.encode(new Row(columns, attrs)));
    }

    @Test
    @DisplayName("Decode empty row.")
    public void decodeEmpty() {
        assertThrowsExactly(IllegalArgumentException.class, () -> Codec.decode("", 1));
    }

    @Test
    @DisplayName("Decode columns only.")
    public void decodeColumnsOnly() {
        Row row = Codec.decode(columnStr, 1);

        assertEquals(1, row.getIndex());
        assertEquals(0, row.getAttributes().length);
        assertEquals(5, row.getColumns().length);

        Cell[] columns = row.getColumns();

        assertEquals("Key1", columns[0].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, columns[0].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.INTEGER, columns[0].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, columns[0].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, columns[0].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, columns[0].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(1L, columns[0].getInteger());

        assertEquals("Key2", columns[1].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, columns[1].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.ASCII, columns[1].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, columns[1].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, columns[1].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, columns[1].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals("ASCII string!", columns[1].getAsciiString());

        assertEquals("Key3", columns[2].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, columns[2].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.DECIMAL, columns[2].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, columns[2].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(1, columns[2].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(3, columns[2].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(12.3D, columns[2].getDecimal());

        assertEquals("Key4", columns[3].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, columns[3].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.BINARY, columns[3].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(4, columns[3].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, columns[3].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, columns[3].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(Arrays.hashCode(binary), Arrays.hashCode(columns[3].getBinary()));

        assertEquals("Key5", columns[4].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.COLUMN, columns[4].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.TIMESTAMP, columns[4].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, columns[4].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, columns[4].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, columns[4].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(now, columns[4].getTimestamp());
    }

    @Test
    @DisplayName("Decode attributes and columns.")
    public void decodeAttributesColumns() {
        Row row = Codec.decode(attrStr + "," + columnStr, 1);

        assertEquals(1, row.getIndex());
        assertEquals(5, row.getAttributes().length);
        assertEquals(5, row.getColumns().length);

        Cell[] attrs = row.getAttributes();

        assertEquals("Attr1", attrs[0].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.ATTRIBUTE, attrs[0].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.INTEGER, attrs[0].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, attrs[0].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, attrs[0].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, attrs[0].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(1L, attrs[0].getInteger());

        assertEquals("Attr2", attrs[1].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.ATTRIBUTE, attrs[1].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.ASCII, attrs[1].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, attrs[1].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, attrs[1].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, attrs[1].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals("ASCII string!", attrs[1].getAsciiString());

        assertEquals("Attr3", attrs[2].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.ATTRIBUTE, attrs[2].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.DECIMAL, attrs[2].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, attrs[2].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(1, attrs[2].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(3, attrs[2].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(12.3D, attrs[2].getDecimal());

        assertEquals("Attr4", attrs[3].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.ATTRIBUTE, attrs[3].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.BINARY, attrs[3].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(4, attrs[3].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, attrs[3].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, attrs[3].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(Arrays.hashCode(binary), Arrays.hashCode(attrs[3].getBinary()));

        assertEquals("Attr5", attrs[4].getColumnDescriptor().getKeyDescriptor().getName());
        assertEquals(KeyType.ATTRIBUTE, attrs[4].getColumnDescriptor().getKeyDescriptor().getType());
        assertEquals(ValueType.TIMESTAMP, attrs[4].getColumnDescriptor().getValueDescriptor().getType());
        assertEquals(0, attrs[4].getColumnDescriptor().getValueDescriptor().getBinarySize());
        assertEquals(0, attrs[4].getColumnDescriptor().getValueDescriptor().getDecimalPrecision());
        assertEquals(0, attrs[4].getColumnDescriptor().getValueDescriptor().getDecimalWidth());
        assertEquals(now, attrs[4].getTimestamp());
    }
}
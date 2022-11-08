package io.textor;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Cell {
    private final int index;
    private final ColumnDescriptor descriptor;
    private final Object value;

    public Cell(int cellIndex, ColumnDescriptor valueDescriptor, Object nullableValue) {
        Objects.requireNonNull(valueDescriptor);
        index = cellIndex;
        descriptor = valueDescriptor;
        value = nullableValue;
    }

    public int getIndex() {
        return index;
    }

    public ColumnDescriptor getColumnDescriptor() {
        return descriptor;
    }

    public Object getValue() {
        return value;
    }

    public Long getInteger() {
        return value == null ? null : (Long)value;
    }

    public Double getDecimal() {
        return value == null ? null : (Double)value;
    }

    public String getAsciiString() {
        return value == null ? null : (String)value;
    }

    public byte[] getBinary() {
        return value == null ? null : (byte[])value;
    }

    public ZonedDateTime getTimestamp() {
        return value == null ? null : (ZonedDateTime)value;
    }
}

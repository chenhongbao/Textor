package io.textor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {
    @Test
    @DisplayName("Create cell with wrong value type.")
    public void constructWrongValueType() {
        assertThrowsExactly(IllegalArgumentException.class, () -> new Cell(1,
                new ColumnDescriptor(
                        new KeyDescriptor("Key5", KeyType.COLUMN),
                        new ValueDescriptor(ValueType.TIMESTAMP, 0, 0, 0)),
                1L));
    }
}
package io.textor;

import java.util.HashMap;
import java.util.Map;

public class Inserta {
    private final Map<String, Cell> columns = new HashMap<>();
    private final Map<String, Cell> attributes = new HashMap<>();
    private final int index;

    public Inserta(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Cell setColumn(Cell cell) {
        return columns.put(cell.getColumnDescriptor().getName(), cell);
    }

    public Cell getColumn(String columnName) {
        return columns.get(columnName);
    }

    public Cell setAttribute(Cell attribute) {
        return attributes.put(attribute.getColumnDescriptor().getName(), attribute);
    }

    public Cell getAttribute(String attributeName) {
        return columns.get(attributeName);
    }
}

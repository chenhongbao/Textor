package io.textor;

import java.util.*;

public class Row {
    private final static Cell[] zeros = new Cell[0];
    private final Map<String, Cell> cols = new HashMap<>();
    private final Map<String, Cell> attrs = new HashMap<>();
    private final int index;

    public Row(Cell[] columns, Cell[] attributes) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("Cannot construct row from no cell.");
        }
        index = columns[0].getIndex();
        for (Cell c : columns) {
            if (c.getIndex() != index) {
                throw new IllegalArgumentException("Unmatched index between row and cells.");
            }
            cols.put(c.getColumnDescriptor().getKeyDescriptor().getName(), c);
        }
        if (attributes != null) {
            for (Cell a : attributes) {
                attrs.put(a.getColumnDescriptor().getKeyDescriptor().getName(), a);
            }
        }
    }

    public int getIndex() {
        return index;
    }

    public Cell getColumn(String columnName) {
        return cols.get(columnName);
    }

    public Cell[] getColumns() {
        return getSortedCells(cols.values());
    }

    public Cell getAttribute(String attributeName) {
        return attrs.get(attributeName);
    }

    public Cell[] getAttributes() {
        return getSortedCells(attrs.values());
    }

    private Cell[] getSortedCells(Collection<Cell> cells) {
        List<Cell> sorted = new LinkedList<>(cells);
        sorted.sort(Comparator.comparing(cell -> cell.getColumnDescriptor().getKeyDescriptor().getName()));
        return sorted.toArray(zeros);
    }
}

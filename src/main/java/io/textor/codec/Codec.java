package io.textor.codec;

import io.textor.*;

import java.util.ArrayList;
import java.util.List;

public class Codec {
    private final static Cell[] zeros = new Cell[0];

    public static String encode(Row row) {
        // @TABLE(A)"system_admin",@_user_defined(D:3,1)10.9,_last_visit(T)2019-09-15T08:58:18.788860-04:00,_age(L)35
        StringBuilder encoded = new StringBuilder();
        encoded.append(encodeCells(row.getAttributes(), encode -> "@" + encode));
        if (encoded.length() > 0 && row.getColumns().length > 0) {
            encoded.append(',');
        }
        encoded.append(encodeCells(row.getColumns(), encode -> encode));
        return encoded.toString();
    }

    private static String encodeCells(Cell[] cells, EncodeManipulator manipulator) {
        if (cells == null || cells.length == 0) {
            return "";
        }
        StringBuilder encoded = new StringBuilder();
        encoded.append(manipulator.manipulate(encodeCell(cells[0])));
        if (cells.length > 1) {
            int cur = 1;
            for (; cur < cells.length; ++cur) {
                encoded.append(',');
                encoded.append(manipulator.manipulate(encodeCell(cells[0])));
            }
        }
        return encoded.toString();
    }

    private static String encodeCell(Cell cell) {
        return KeyUtils.encodeKey(cell.getColumnDescriptor()) + encodeAnyValue(cell);
    }

    private static String encodeAnyValue(Cell cell) {
        ValueDescriptor value = cell.getColumnDescriptor().getValueDescriptor();
        return switch (value.getType()) {
            case INTEGER -> ValueUtils.Integer.encode(cell.getInteger());
            case DECIMAL -> ValueUtils.Decimal.encode(cell.getDecimal(), value.getDecimalWidth(), value.getDecimalPrecision());
            case ASCII -> ValueUtils.ASCII.encode(cell.getAsciiString());
            case BINARY -> ValueUtils.Binary.encode(cell.getBinary(), 0, value.getBinarySize());
            case TIMESTAMP -> ValueUtils.Timestamp.encode(cell.getTimestamp());
        };
    }

    public static Row decode(String expr, int index) {
        DecodingState state = new DecodingState(CodecUtils.moveAfter(expr, 0, c -> Character.isWhitespace(c) || c == '\0'));
        List<Cell> columns = new ArrayList<>();
        List<Cell> attrs = new ArrayList<>();
        while (state.getCursor() != -1) {
            Cell cell =  decodeCell(index, expr, state);
            if (cell.getColumnDescriptor().getKeyDescriptor().getType() == KeyType.ATTRIBUTE) {
                attrs.add(cell);
            }
            else {
                columns.add(cell);
            }
        }
        return new Row(columns.toArray(zeros), attrs.toArray(zeros));
    }

    private static Cell decodeCell(int index, String expr, DecodingState state) {
        ColumnDescriptor columnDesc = KeyUtils.decodeKey(expr, state);
        return switch (columnDesc.getValueDescriptor().getType()) {
            case ASCII -> new Cell(index, columnDesc, ValueUtils.ASCII.decode(expr, state));
            case BINARY -> new Cell(index, columnDesc, ValueUtils.Binary.decode(expr, columnDesc.getValueDescriptor().getBinarySize(), state));
            case DECIMAL -> new Cell(index, columnDesc, ValueUtils.Decimal.decode(expr, columnDesc.getValueDescriptor().getDecimalWidth(), columnDesc.getValueDescriptor().getDecimalPrecision(), state));
            case INTEGER -> new Cell(index, columnDesc, ValueUtils.Integer.decode(expr, state));
            case TIMESTAMP -> new Cell(index, columnDesc, ValueUtils.Timestamp.decode(expr, state));
        };
    }
}

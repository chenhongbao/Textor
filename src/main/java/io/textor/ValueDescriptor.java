package io.textor;

public class ValueDescriptor {
    private final ValueType type;
    private final Integer width;
    private final Integer precision;
    private final Integer size;

    public ValueDescriptor(ValueType valueType, Integer decimalWidth, Integer decimalPrecision, Integer binarySize) {
        type = valueType;
        width = decimalWidth;
        precision = decimalPrecision;
        size = binarySize;
    }

    public ValueType getType() {
        return type;
    }

    public int getDecimalWidth() {
        return width;
    }

    public int getDecimalPrecision() {
        return precision;
    }

    public int getBinarySize() {
        return size;
    }
}

package io.textor;

public class ValueDescriptor {
    private final ValueType type;
    private final Integer precision;
    private final Integer fraction;
    private final Integer size;

    public ValueDescriptor(ValueType valueType, Integer decimalPrecision, Integer decimalFraction, Integer binarySize) {
        type = valueType;
        precision = decimalPrecision;
        fraction = decimalFraction;
        size = binarySize;
    }

    public ValueType getType() {
        return type;
    }

    public int getDecimalPrecision() {
        return precision;
    }

    public int getDecimalFraction() {
        return fraction;
    }

    public int getBinarySize() {
        return size;
    }
}

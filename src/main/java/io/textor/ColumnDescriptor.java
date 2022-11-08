package io.textor;

public class ColumnDescriptor {
    private final KeyDescriptor keyDesc;
    private final ValueDescriptor valueDesc;

    public ColumnDescriptor(KeyDescriptor keyDescriptor, ValueDescriptor valueDescriptor) {
        keyDesc = keyDescriptor;
        valueDesc = valueDescriptor;
    }

    public ValueDescriptor getValueDescriptor() {
        return valueDesc;
    }

    public KeyDescriptor getKeyDescriptor() {
        return keyDesc;
    }
}

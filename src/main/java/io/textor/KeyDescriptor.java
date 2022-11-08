package io.textor;

public class KeyDescriptor {
    private final String name;
    private final KeyType type;

    public KeyDescriptor(String keyName, KeyType keyType) {
        name = keyName;
        type = keyType;
    }

    public String getName() {
        return name;
    }

    public KeyType getType() {
        return type;
    }
}

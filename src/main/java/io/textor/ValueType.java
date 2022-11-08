package io.textor;

public enum ValueType {
    INTEGER("L"),
    DECIMAL("D"),
    ASCII("A"),
    BINARY("B"),
    TIMESTAMP("T");

    private final String token;
    ValueType(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

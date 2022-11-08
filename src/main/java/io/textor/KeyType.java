package io.textor;

public enum KeyType {
    COLUMN(""),
    ATTRIBUTE("@");

    private final String token;

    KeyType(String token) {
       this.token = token;
    }

    public String getToken() {
        return token;
    }
}

package io.textor;

public class DecodingState {
    private int cursor;

    public DecodingState(int initCursor) {
        cursor = initCursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public int getCursor() {
        return cursor;
    }
}

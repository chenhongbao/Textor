package io.textor.codec;

@FunctionalInterface
public interface EncodeManipulator {
    String manipulate(String encode);
}

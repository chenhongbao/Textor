package io.textor.codec;

@FunctionalInterface
public interface CharacterChecker {
    boolean check(char c);
}

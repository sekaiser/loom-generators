package loom.generators.kelemen;

import java.util.function.Consumer;

public interface ForEachable<T> {
    void forEach(Consumer<? super T> action);
}
package loom.generators;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.function.Supplier;

public abstract class Generator<T> implements Supplier<T> {

    private static final ContinuationScope continuationScope = new ContinuationScope("Generator");
    private final Continuation continuation;
    private T next;

    public Generator() {
        continuation = new Continuation(continuationScope, this::run);
    }

    protected abstract void run();

    @Override
    public final T get() {
        continuation.run();
        return next;
    }

    protected final void yield(T value) {
        next = value;
        Continuation.yield(continuationScope);
    }

}

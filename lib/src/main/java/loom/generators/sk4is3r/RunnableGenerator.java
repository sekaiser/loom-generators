package loom.generators.sk4is3r;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.function.Supplier;

public class RunnableGenerator<T> implements Supplier<T> {

    private static final ContinuationScope continuationScope = new ContinuationScope("RunnableGenerator");

    private final InternalContinuation<T> continuation;

    public RunnableGenerator(Runnable target) {
        continuation = new InternalContinuation<>(target);
    }

    public static <T> void yield(T value) {
        InternalContinuation<T> internalContinuation = getCurrentContinuation();
        internalContinuation.next = value;
        Continuation.yield(continuationScope);
        internalContinuation.next = null;
    }

    private static <T> InternalContinuation<T> getCurrentContinuation() {
        if (Continuation.getCurrentContinuation(continuationScope) instanceof InternalContinuation<?> internal) {
            @SuppressWarnings("unchecked")
            InternalContinuation<T> internalContinuation = (InternalContinuation<T>) internal;
            return internalContinuation;
        }
        throw new IllegalStateException("Unexpected Continuation");
    }

    @Override
    public T get() {
        continuation.run();
        return continuation.next;
    }

    public boolean isDone() {
        return continuation.isDone();
    }

    private static class InternalContinuation<T> extends Continuation {
        private T next;

        InternalContinuation(Runnable target) {
            super(continuationScope, target);
        }
    }
}

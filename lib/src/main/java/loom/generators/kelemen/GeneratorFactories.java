package loom.generators.kelemen;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.jtrim2.cancel.Cancellation;
import org.jtrim2.collections.ReservablePollingQueues;
import org.jtrim2.collections.ReservedElementRef;
import org.jtrim2.concurrent.collections.TerminableQueue;
import org.jtrim2.concurrent.collections.TerminableQueues;
import org.jtrim2.concurrent.collections.TerminatedQueueException;
import org.jtrim2.event.ListenerRef;
import org.jtrim2.utils.ExceptionHelper;

public final class GeneratorFactories {
    public static <T> Iterator<T> toIterator(ForEachable<T> forEachable) {
        Objects.requireNonNull(forEachable, "forEachable");

        var queue = TerminableQueues
                .<ValueOrException<T>>withWrappedQueue(ReservablePollingQueues.createFifoQueue(1));

        var queueWrapper = new Object() {
            public void shutdown() {
                queue.shutdown();
            }

            public ReservedElementRef<ValueOrException<T>> get() {
                try {
                    return queue.takeButKeepReserved(Cancellation.UNCANCELABLE_TOKEN);
                } catch (TerminatedQueueException e) {
                    return null;
                }
            }
        };

        var cleanupRef = registerCleanupAction(queueWrapper, queue::shutdown);

        Object owner = new Object();
        Thread.startVirtualThread(() -> {
            try {
                forEachable.forEach(e -> {
                    Objects.requireNonNull(e, "e");
                    try {
                        queue.put(Cancellation.UNCANCELABLE_TOKEN, new ValueOrException<>(e, null));
                    } catch (TerminatedQueueException ex) {
                        throw new StoppedGeneratorException(owner);
                    }
                });
            } catch (StoppedGeneratorException e) {
                if (!owner.equals(e.owner())) {
                    putException(queue, e);
                }
                // else: consumer went away.
            } catch (Throwable e) {
                putException(queue, e);
            } finally {
                queue.shutdown();
                cleanupRef.unregister();
            }
        });

        return new Iterator<>() {
            private ReservedElementRef<ValueOrException<T>> nextRef = queueWrapper.get();

            @Override
            public boolean hasNext() {
                return nextRef != null;
            }

            @Override
            public T next() {
                var result = nextRef;
                if (result == null) {
                    throw new NoSuchElementException();
                }

                result.release();
                nextRef = queueWrapper.get();
                if (nextRef == null) {
                    cleanupRef.unregister();
                    queueWrapper.shutdown();
                }
                return result.element().getOrThrow();
            }
        };
    }

    private static ListenerRef registerCleanupAction(Object obj, Runnable cleanupAction) {
        Objects.requireNonNull(obj, "obj");
        Objects.requireNonNull(cleanupAction, "cleanupAction");

        var referenceQueue = new ReferenceQueue<>();
        var phantomRef = new PhantomReference<Object>(obj, referenceQueue) {
            public void clean() {
                cleanupAction.run();
            }
        };

        var cleanupThreadRef = new AtomicReference<>(Thread.startVirtualThread(() -> {
            boolean canceled = false;
            try {
                referenceQueue.remove();
            } catch (InterruptedException e) {
                canceled = true;
            } finally {
                if (!canceled) {
                    phantomRef.clean();
                }
            }
        }));

        return () -> {
            var cleanupThread = cleanupThreadRef.getAndSet(null);
            if (cleanupThread != null) {
                cleanupThread.interrupt();
            }
        };
    }

    private static <T> void putException(
            TerminableQueue<ValueOrException<T>> queue,
            Throwable exception
    ) {
        try {
            queue.put(Cancellation.UNCANCELABLE_TOKEN, new ValueOrException<>(null, exception));
        } catch (TerminatedQueueException ex) {
            // consumer went away.
        }
    }

    private record ValueOrException<T>(T value, Throwable exception) {
        T getOrThrow() {
            ExceptionHelper.rethrowIfNotNull(exception);
            return value;
        }
    }

    private GeneratorFactories() {
        throw new AssertionError();
    }
}
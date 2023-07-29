package loom.generators.mvysny;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs given {@link Runnable} as a series of continuations.
 */
public final class ContinuationInvoker {

    /**
     * The {@link #runnable} to run. Will be run in a virtual thread. The runnable will call {@link #suspend()}.
     * if it needs to suspend.
     */
    private final Runnable runnable;

    /**
     * If <code>true</code> the {@link #runnable} has finished its execution and there will be no more executions
     * to run.
     */
    private volatile boolean executionDone = false;

    private BlockingQueue<Object> continuationUnpark = null;

    /**
     * Used to assert that continuation.unpark() is invoked synchronously from {@link Thread#start()} and
     * continuationUnpark.take(), otherwise this class won't work properly.
     */
    private volatile AtomicInteger continuationsInvoked = new AtomicInteger();

    /**
     * Creates an invoker which runs given block as a series of continuations. Does not call the block just yet -
     * you need to call {@link #next()}.
     */
    public ContinuationInvoker(Runnable runnable) {
        this.runnable = Objects.requireNonNull(runnable);
    }

    public boolean isDone() {
        return executionDone;
    }

    /**
     * Runs next continuation.
     *
     * @return <code>true</code> if there are follow-up continuations, otherwise <code>false</code> to indicate that
     *         {@link #runnable} finished its execution.
     */
    public boolean next() {
        if (isDone()) {
            throw new IllegalStateException("Execution is done!");
        }

        if (continuationUnpark == null) {
            // First invocation of next() function. Let's create the virtual thread factory.
            // The factory runs the continuations directly instead of submitting them into a runner.
            final Executor synchronousExecutor = command -> {
                try {
                    command.run();
                } finally {
                    continuationsInvoked.incrementAndGet();
                }
            };

            final ThreadFactory virtualThreadFactory = Util.newVirtualBuilder(synchronousExecutor).factory();
            final Thread thread = virtualThreadFactory.newThread(() -> {
                try {
                    runnable.run();
                } finally {
                    executionDone = true;
                }
            });
            continuationUnpark = new LinkedBlockingQueue<>(1);

            // VirtualThread.start() runs the first continuation immediately via the synchronousExecutor,
            // which executes it right away. That causes VirtualThread.start() to block until its continuation
            // finishes or suspends via this.suspend().
            thread.start();

            if (continuationsInvoked.get() != 1) {
                throw new IllegalStateException("Expected to run the continuation in VirtualThread.start() but nothing was done");
            }

            return !isDone();
        } else {
            // This deque is populated only from this function, and then it's cleaned immediately.
            // Therefore, it must be empty.
            assert continuationUnpark.isEmpty();

            // The runnable is at the moment stuck in this.suspend(), waiting for the queue to become non-empty.
            // The virtual thread running the runnable is unmounted and laying dormant.
            final AtomicInteger invocationCount = new AtomicInteger(continuationsInvoked.intValue());

            // Similar trick as above: continuationUnpark.offer() unblocks the runnable which is stuck in this.suspend().
            // The virtual thread submits another continuation to the executor. Since we're using
            // synchronousExecutor, the execution runs right away.
            // The this.runnable consumes the item offered to the queue (since it was stuck in BlockingQueue.take()), making the queue empty.
            // The this.runnable execution continues until it either terminates, or calls this.suspend(), which blocks again on a now-empty queue.
            // Only then the call to offer() ends.
            var ignored = continuationUnpark.offer(""); // the type of the item doesn't really matter.
            // the continuation finished its execution, either by terminating or by calling this.suspend().

            // check that the trick above worked and the continuation finished executing.
            // To avoid the compiler optimizing this to false, let's mark continuationsInvoked volatile.
            if (continuationsInvoked.intValue() != invocationCount.intValue() + 1) {
                throw new IllegalStateException("Expected to run the continuation in unpark() but nothing was done");
            }

            // To avoid the compiler optimizing this to false, executionDone must be volatile.
            if (isDone()) {
                // runnable terminated. There will be no more continuations => return false.
                return false;
            }

            // The execution of this.runnable called this.suspend() which cleared up the queue. Therefore,
            // the continuationUnpark queue must be empty. Check.
            if (!continuationUnpark.isEmpty()) {
                throw new IllegalStateException("Runnable is only allowed to call this.suspend() but it blocked in another way");
            }
            return true;
        }
    }

    /**
     * Only {@link #runnable} is allowed to call this. Suspends the execution of the {@link #runnable} and causes
     * the ongoing call to {@link #next()} to return.
     */
    public void suspend() {
        if (!Thread.currentThread().isVirtual()) {
            // this.runnable runs in a virtual thread. If the current thread isn't virtual, it's most definitely not called from this.runnable.
            throw new IllegalStateException("Can only be called from this.runnable");
        }
        if (continuationUnpark == null) {
            // next() hasn't been called, therefore this.runnable isn't running and therefore can't be the
            // one calling this function.
            throw new IllegalStateException("Can only be called from this.runnable");
        }
        if (!continuationUnpark.isEmpty()) {
            // The execution flow is as follows:
            // 1. this.runnable is stuck in this.suspend(), which means that the queue is empty.
            // 2. next() gets called which offers an item to the queue.
            // 3. continuationUnpark.take() consumes the item (making the queue empty) and
            //    unblocks the runnable which executes synchronously until it calls suspend() again.
            // 4. YOU ARE HERE: suspend() gets called. The queue must be empty.
            throw new IllegalStateException("Can only be called from this.runnable");
        }
        try {
            // Since the queue is empty, this call blocks. That will park the virtual thread,
            // which will cause the current continuation to stop executing. Remember that the
            // this.next() function is blocked and busy running the continuation; therefore this call
            // unblocks the this.next() function.
            continuationUnpark.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

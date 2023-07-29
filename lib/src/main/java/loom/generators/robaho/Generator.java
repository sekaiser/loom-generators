package loom.generators.robaho;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Generator<T> implements Iterable<T> {
    private final Producer<T> producer;

    public Generator(Producer<T> producer) {
        this.producer = producer;
    }

    @Override
    public Iterator<T> iterator() {
        IteratorImpl<T> itr = new IteratorImpl<>();
        new ProducerImpl<T>(producer,itr);
        return itr;
    }

    private static class ProducerImpl<T> implements Callback<T> {
        private final WeakReference<IteratorImpl<T>> itrRef;
        private final Semaphore ready = new Semaphore(0);

        ProducerImpl(Producer<T> producer,IteratorImpl<T> itr) {
            itrRef = new WeakReference<>(itr);
            itr.setProducer(this);
            Thread.startVirtualThread(() -> {
                try {
                    ready.acquire();
                    if(itrRef.get()==null)
                        return;
                    producer.run(ProducerImpl.this);
                } catch (InterruptedException ignore) {
                    // finally will clean-up
                } finally {
                    IteratorImpl<T> itr1 = itrRef.get();
                    if(Objects.nonNull(itr1)) itr1.markDone();

                }
            });
        }

        @Override
        public boolean yield(T value) {
            pushValue(value);
            try {
                ready.acquire();
            } catch (InterruptedException e) {
                return false;
            }
            return Objects.nonNull(itrRef.get());
        }

        private boolean pushValue(T value) {
            IteratorImpl<T> itr = itrRef.get();
            if(itr==null) return false;
            return itr.newValue(value);
        }

        private void ready() {
            this.ready.release();
        }
    }

    private static class IteratorImpl<T> implements Iterator<T> {
        private final AtomicReference<T> next = new AtomicReference<>();
        private volatile boolean done;
        private ProducerImpl<T> producer;
        private volatile Thread reader;

        private void setProducer(ProducerImpl<T> producer) {
            this.producer = producer;
        }

        @Override
        public boolean hasNext() {
            reader = Thread.currentThread();
            boolean released=false;
            while(true) {
                if(next.get()!=null)
                    return true;
                if(!released) {
                    producer.ready.release();
                    released=true;
                }
                if(done) return false;
                LockSupport.park();
            }
        }

        @Override
        public T next() {
            reader = Thread.currentThread();
            if(!hasNext()) throw new NoSuchElementException();
            return next.getAndSet(null);
        }

        private boolean newValue(T value) {
            boolean result = next.compareAndSet(null,value);
            LockSupport.unpark(reader);
            return result;
        }
        private void markDone() {
            done=true;
            LockSupport.unpark(reader);
        }
        protected void finalize() throws Throwable {
            producer.ready();
        }
    }

    public interface Producer<T> {
        void run(Callback<T> callback);
    }

    public interface Callback<T> {
        /** return false if the producer should terminate */
        boolean yield(T value);
    }
}
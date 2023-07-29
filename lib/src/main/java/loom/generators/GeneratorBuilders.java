package loom.generators;

import loom.generators.kelemen.ForEachable;
import loom.generators.kelemen.GeneratorFactories;
import loom.generators.mvysny.ContinuationInvoker;
import loom.generators.mvysny.Coroutine;
import loom.generators.robaho.Generator;
import loom.generators.sk4is3r.RunnableGenerator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

class GeneratorBuilders {

    static <T> GeneratorService<T> newKelemenGenerator(ForEachable<T> forEachable) {
        return new KelemenGeneratorImpl<>(forEachable);
    }

    static <T> GeneratorService<T> newMySnyGenerator(Consumer<Coroutine.Yielder<T>> generator) {
        return new MySnyGeneratorImpl<>(generator);
    }

    static <T> GeneratorService<T> newRobahoGenerator(loom.generators.robaho.Generator.Producer<T> producer) {
        return new RobahoGeneratorImpl<>(producer);
    }

    static <T> GeneratorService<T> newRunnableGenerator(Runnable task) {
        return new RunnableGeneratorImpl<>(task);
    }

    private static class KelemenGeneratorImpl<T> implements GeneratorService<T> {
        private final ForEachable<T> forEachable;

        public KelemenGeneratorImpl(ForEachable<T> forEachable) {
            this.forEachable = forEachable;
        }

        @Override
        public Iterator<T> iterator() {
            return GeneratorFactories.toIterator(forEachable);
        }
    }

    private static class MySnyGeneratorImpl<T> implements GeneratorService<T> {
        final ContinuationInvoker invoker;
        final Coroutine.Yielder<T> yielder;

        MySnyGeneratorImpl(Consumer<Coroutine.Yielder<T>> generator) {
            this.yielder = new Coroutine.Yielder<>();
            this.invoker = new ContinuationInvoker(() -> generator.accept(yielder));
            yielder.setContinuationInvoker(invoker);
        }

        private static <E> Iterator<E> generate(Supplier<E> nextFunction) {
            return new Iterator<E>() {
                /**
                 * If true the iteration is done and there will be no more items.
                 */
                private boolean done = false;
                /**
                 * Next item returned by {@link #next()}. If null then the item hasn't been
                 * calculated yet.
                 */
                private E nextItem = null;

                /**
                 * Polls nextFunction and calculates {@link #nextItem}.
                 */
                private void peekNext() {
                    if (!done && nextItem == null) {
                        nextItem = nextFunction.get();
                        if (nextItem == null) {
                            done = true;
                        }
                    }
                }

                @Override
                public boolean hasNext() {
                    peekNext();
                    return !done;
                }

                @Override
                public E next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final E item = nextItem;
                    nextItem = null;
                    return item;
                }
            };
        }

        @Override
        public Iterator<T> iterator() {
            final Supplier<T> itemSupplier = () -> {
                if (Objects.nonNull(yielder.getItem())) {
                    T item = yielder.getItem();
                    yielder.resetItem();
                    return item;
                }

                final boolean hasMoreContinuations = invoker.next();
                if (!hasMoreContinuations) {
                    return null;
                }

                T item = yielder.getItem();
                yielder.resetItem();
                return item;
            };
            return generate(itemSupplier);
        }
    }

    private static class RobahoGeneratorImpl<T> implements GeneratorService<T> {
        private final Iterator<T> iterator;

        RobahoGeneratorImpl(loom.generators.robaho.Generator.Producer<T> producer) {
            this.iterator = new Generator<>(producer).iterator();
        }

        @Override
        public Iterator<T> iterator() {
            return iterator;
        }
    }

    private static class RunnableGeneratorImpl<T> implements GeneratorService<T> {
        private final RunnableGenerator<T> generator;

        RunnableGeneratorImpl(Runnable task) {
            this.generator = new RunnableGenerator<>(task);
        }

        private static <E> Iterator<E> generate(Supplier<E> nextFn) {
            return new Iterator<>() {
                /**
                 * If true the iteration is done and there will be no more items.
                 */
                private boolean done = false;
                /**
                 * Next item returned by {@link #next()}. If null then the item hasn't been
                 * calculated yet.
                 */
                private E nextItem = null;

                /**
                 * Polls nextFunction and calculates {@link #nextItem}.
                 */
                private void peekNext() {
                    if (!done && nextItem == null) {
                        nextItem = nextFn.get();
                        if (nextItem == null) {
                            done = true;
                        }
                    }
                }

                @Override
                public boolean hasNext() {
                    peekNext();
                    return !done;
                }

                @Override
                public E next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final E item = nextItem;
                    nextItem = null;
                    return item;
                }
            };
        }
//        @Override
//        public void close() throws Exception {
//            runnableAdapter.shutdown = true;
//            generator.exhaust(tRef.get());
//            System.out.println("foo");
//
//            boolean terminated = isDone();
//            if (!terminated) {
//                Thread t = tRef.get();
//                if (Objects.nonNull(t)) {
//                    boolean interrupted = false;
//                    while (!terminated) {
//                        if (t.isInterrupted()) {
//                            terminated = true;
//                        } else {
//                            t.interrupt();
//                            interrupted = true;
//                        }
//                    }
//
//                    if (interrupted) {
//                        Thread.currentThread().interrupt();
//                    }
//
//                }
//            }
//
//
//            Thread t = tRef.get();
//            System.out.println("Generator is done? " + isDone());
//
//            System.out.println("Thread name: " + t);
//            System.out.println("Thread isAlive: " + t.isAlive());
//        }

        @Override
        public Iterator<T> iterator() {
            return generate(generator::get);
        }
    }
}

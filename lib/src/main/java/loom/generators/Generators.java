package loom.generators;

import loom.generators.kelemen.ForEachable;
import loom.generators.mvysny.Coroutine;
import loom.generators.robaho.Generator;
import loom.generators.sk4is3r.RunnableGenerator;

import java.util.function.Consumer;

public class Generators {

    public static <T> GeneratorService<T> newMvySnyGenerator(Consumer<Coroutine.Yielder<T>> generator) {
        return GeneratorBuilders.newMySnyGenerator(generator);
    }

    public static <T> GeneratorService<T> newRobahoGenerator(Generator.Producer<T> producer) {
        return GeneratorBuilders.newRobahoGenerator(producer);
    }

    public static <T> GeneratorService<T> newRunnableGenerator(Consumer<Yieldable<T>> task) {
        return GeneratorBuilders.newRunnableGenerator(() -> task.accept(RunnableGenerator::yield));
    }

    public static <T> GeneratorService<T> newKelemenGenerator(ForEachable<T> forEachable) {
        return GeneratorBuilders.newKelemenGenerator(forEachable);
    }

//    public static <T> GeneratorService<T> newVirtualThreadGenerator(Consumer<Yieldable<T>> task) {
//        return createNewVirtualThreadGenerator(() -> task.accept(RunnableGenerator::yield));
//    }

    //    public static <T> GeneratorService<T> newVirtualThreadGenerator(Runnable task) {
//        return createNewVirtualThreadGenerator(task);
//    }
//
//    private static <T> GeneratorService<T> createNewVirtualThreadGenerator(Runnable task) {
//        ThreadFactory factory = newVirtualBuilder(Runnable::run).factory();
//        return newVirtualThreadGenerator(factory, task);
//    }
//
//    public static <T> GeneratorService<T> newVirtualThreadGenerator(
//            ThreadFactory threadFactory,
//            Runnable runnable
//    ) {
//        Thread t = threadFactory.newThread(runnable);
//        return new RunnableGeneratorImpl<>(t::start);
//    }
//
//    /**
//     * Creates a virtual thread builder which runs continuations on given executor.
//     *
//     * @param executor runs continuations.
//     * @return the virtual thread builder
//     */
//    private static Thread.Builder.OfVirtual newVirtualBuilder(Executor executor) {
//        Objects.requireNonNull(executor);
//        try {
//            final Class<?> vtbclass = Class.forName("java.lang.ThreadBuilders$VirtualThreadBuilder");
//            final Constructor<?> c = vtbclass.getDeclaredConstructor(Executor.class);
//            c.setAccessible(true);
//            return (Thread.Builder.OfVirtual) c.newInstance(executor);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
    @FunctionalInterface
    public interface Yieldable<T> {
        void yield(T t);
    }
}

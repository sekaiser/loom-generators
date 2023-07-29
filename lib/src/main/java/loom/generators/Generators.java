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

    @FunctionalInterface
    public interface Yieldable<T> {
        void yield(T t);
    }
}

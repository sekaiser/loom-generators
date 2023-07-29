package loom.generators.mvysny;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.concurrent.Executor;

public class Util {
    /**
     * Creates a virtual thread builder which runs continuations on given executor.
     *
     * @param executor runs continuations.
     * @return the virtual thread builder
     */
    public static Thread.Builder.OfVirtual newVirtualBuilder(Executor executor) {
        Objects.requireNonNull(executor);
        // Construct a specialized virtual thread builder which runs continuations on given executor.
        // We need to use reflection since the API is not public:
        // https://bugs.java.com/bugdatabase/view_bug?bug_id=JDK-8308541

        try {
            final Class<?> vtbclass = Class.forName("java.lang.ThreadBuilders$VirtualThreadBuilder");
            final Constructor<?> c = vtbclass.getDeclaredConstructor(Executor.class);
            c.setAccessible(true);
            return (Thread.Builder.OfVirtual) c.newInstance(executor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

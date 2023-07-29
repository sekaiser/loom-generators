package loom.generators;

import loom.generators.kelemen.GeneratorFactories;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class BinaryTreeComparisonTest {

    public static Object[] T(Object left, int value, Object right) {
        return new Object[]{left, value, right};
    }

    private static <E> void visit(Generators.Yieldable<E> y, Object[] t) {
        if (Objects.nonNull(t)) {
            visit(y, (Object[]) t[0]);
            y.yield((E) t[1]);
            visit(y, (Object[]) t[2]);
        }
    }

    private static Iterator<Integer> visit(Object[] t) {
        GeneratorService<Integer> g = newGenerator(y -> visit(y, t));
        return g.iterator();
    }

    public static boolean cmp(Object[] t1, Object[] t2) {
        var next1 = visit(t1);
        var next2 = visit(t2);

        while (true) {
            if (next1.hasNext() != next2.hasNext()) return false;
            if (!next1.hasNext()) return true;

            var v1 = next1.next();
            var v2 = next2.next();

            if (!v1.equals(v2)) return false;
        }
    }

    static <T> GeneratorService<T> newGenerator(Consumer<Generators.Yieldable<T>> y) {
        return Generators.newRunnableGenerator(y);
    }

    @Test
    void compareBinaryTrees() {
        Object e = null;
        Object[] t1 = T(T(T(e, 1, e), 2, T(e, 3, e)), 4, T(e, 5, e));
        Object[] t2 = T(e, 1, T(e, 2, T(e, 3, T(e, 4, T(e, 5, e)))));
        Object[] t3 = T(e, 1, T(e, 2, T(e, 3, T(e, 4, T(e, 6, e)))));
        Object[] t4 = T(e, 1, T(e, 2, e));

        assertThat(cmp(t1, t1)).isTrue();
        assertThat(cmp(t2, t2)).isTrue();
        assertThat(cmp(t3, t3)).isTrue();
        assertThat(cmp(t1, t2)).isTrue();
        assertThat(cmp(t1, t3)).isFalse();
        assertThat(cmp(t2, t3)).isFalse();
        assertThat(cmp(t1, t4)).isFalse();
    }
}

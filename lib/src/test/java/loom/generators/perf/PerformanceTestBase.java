package loom.generators.perf;

import loom.generators.GeneratorService;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public abstract class PerformanceTestBase {

    protected static final int COUNT = 1_000_000;
    private static final int RUNS = 10;

    abstract GeneratorService<Integer> generator();

    protected boolean useVirtualThread() {
        return true;
    }

    void measure(List<Long> measurements) {
        GeneratorService<Integer> g = generator();
        long start = System.currentTimeMillis();
        int count = 0;

        for (Integer i : g) {
            count++;
        }

        long diff = System.currentTimeMillis() - start;
        if (count != COUNT) {
            throw new IllegalStateException("incorrect number of entries, count " + count + ", expected " + COUNT);
        }
        measurements.add(diff);
    }

    @Test
    public void runPerformanceTest() throws InterruptedException {
        List<Long> measurements = new LinkedList<>();

        if (useVirtualThread()) {
            for (int i = 0; i < RUNS; i++) {
                Thread thread = Thread.startVirtualThread(() -> measure(measurements));
                thread.join();
            }
        } else {
            for (int i = 0; i < RUNS; i++) {
                measure(measurements);
            }
        }


        double avg = measurements.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.println("Result: " + avg + " ms");
    }

}

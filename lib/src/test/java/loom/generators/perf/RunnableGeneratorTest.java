package loom.generators.perf;

import loom.generators.GeneratorService;
import loom.generators.Generators;

public class RunnableGeneratorTest extends PerformanceTestBase {
    @Override
    GeneratorService<Integer> generator() {
        return Generators.newRunnableGenerator(g -> {
            for(int i=0;i<COUNT;i++) {
                g.yield(i);
            }
        });
    }
}

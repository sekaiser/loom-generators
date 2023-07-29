package loom.generators.perf;

import loom.generators.GeneratorService;
import loom.generators.Generators;

public class MvsnyGeneratorTest extends PerformanceTestBase {
    @Override
    protected boolean useVirtualThread() {
        return Boolean.FALSE;
    }

    @Override
    GeneratorService<Integer> generator() {
        return Generators.newMvySnyGenerator(g -> {
            for(int i=0;i<COUNT;i++) {
                g.yield(i);
            }
        });
    }
}

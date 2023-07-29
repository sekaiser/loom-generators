package loom.generators.perf;

import loom.generators.GeneratorService;
import loom.generators.Generators;

public class RobahoGeneratorTest extends PerformanceTestBase {
    @Override
    GeneratorService<Integer> generator() {
        return Generators.newRobahoGenerator(g -> {
            for(int i=0;i<COUNT;i++) {
                g.yield(i);
            }
        });
    }
}

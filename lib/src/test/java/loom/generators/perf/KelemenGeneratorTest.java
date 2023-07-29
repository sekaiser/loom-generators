package loom.generators.perf;

import loom.generators.GeneratorService;
import loom.generators.Generators;

public class KelemenGeneratorTest extends PerformanceTestBase {
    @Override
    GeneratorService<Integer> generator() {
        return Generators.newKelemenGenerator(g -> {
            for (int i = 0; i < COUNT; i++) {
                g.accept(i);
            }
        });
    }
}

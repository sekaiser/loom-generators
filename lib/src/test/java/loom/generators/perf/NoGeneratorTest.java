package loom.generators.perf;

import loom.generators.GeneratorService;

import java.util.ArrayList;
import java.util.List;

public class NoGeneratorTest extends PerformanceTestBase {
    @Override
    GeneratorService<Integer> generator() {
        List<Integer> nums = new ArrayList<>();
        for(int i=0;i<COUNT;i++) {
            nums.add(i);
        }
        return nums::iterator;
    }
}
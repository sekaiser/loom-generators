package loom.generators.kelemen;

public final class StoppedGeneratorException extends RuntimeException {
    private final Object owner;

    StoppedGeneratorException(Object owner) {
        super("stopped-processing-generator", null, true, false);
        this.owner = owner;
    }

    Object owner() {
        return owner;
    }
}
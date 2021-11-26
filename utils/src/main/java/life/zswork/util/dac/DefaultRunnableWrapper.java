package life.zswork.util.dac;

public class DefaultRunnableWrapper extends AbstractRunnableWrapper {
    @Override
    Runnable of(Runnable runnable) {
        return runnable;
    }
}

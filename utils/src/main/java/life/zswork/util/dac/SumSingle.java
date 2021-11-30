package life.zswork.util.dac;

import life.zswork.util.concurrent.ConcurrentCountDownLatch;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

public class SumSingle<E> {

    private List<E> list;
    /**
     * 执行的线程池
     */
    private ExecutorService executor;

    private AbstractRunnableWrapper runnableWrapper;

    /**
     * 超时时间
     */
    public Duration timeout;

    public SumSingle(List<E> list) {
        this.list = list;
    }

    public SumSingle runnableWrapper(AbstractRunnableWrapper runnableWrapper) {
        this.runnableWrapper = runnableWrapper;
        return this;
    }


    public SumSingle<E> pool(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public SumSingle<E> timeout(Duration duration) {
        this.timeout = duration;
        return this;
    }

    public <U> void reduce(U identity, BiFunction<U, ? super E, U> accumulator,
                           BinaryOperator<U> combiner) {
        for (E es : list) {
            combiner.apply(accumulator.apply(identity, es), identity);
        }
    }

    public void parallelForeach(Consumer<E> consumer) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        ExecutorService service = Optional.ofNullable(executor).orElse(SumFuture.pool);
        List<Future<?>> futureList = new CopyOnWriteArrayList<>();
        ConcurrentCountDownLatch countDownLatch = new ConcurrentCountDownLatch(list.size());
        AtomicBoolean wef = new AtomicBoolean(true);
        AbstractRunnableWrapper runner = Optional.ofNullable(runnableWrapper).orElse(SumFuture.runnableWrapper);
        list.forEach(es ->
                futureList.add(service.submit(runner.of(() -> {
                            try {
                                if (wef.get()) {
                                    consumer.accept(es);
                                }
                            } catch (Throwable e) {
                                futureList.forEach(future -> future.cancel(true));
                                wef.set(false);
                                countDownLatch.exception(e);
                            } finally {
                                countDownLatch.countDown();
                            }
                        })
                )));
        try {
            if (Objects.nonNull(timeout)) {
                countDownLatch.await(timeout);
            } else {
                countDownLatch.await();
            }
        } catch (Throwable e) {
            throw new DacException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <U> U parallelReduce(U identity, BiFunction<U, ? super E, U> accumulator,
                                BinaryOperator<U> combiner) {
        if (CollectionUtils.isEmpty(list)) {
            return identity;
        }
        List<Future<?>> futureList = new CopyOnWriteArrayList<>();
        ConcurrentCountDownLatch countDownLatch = new ConcurrentCountDownLatch(list.size());
        AtomicBoolean wef = new AtomicBoolean(true);
        ExecutorService service = Optional.ofNullable(executor).orElse(SumFuture.pool);
        AbstractRunnableWrapper runner = Optional.ofNullable(runnableWrapper).orElse(SumFuture.runnableWrapper);
        list.forEach(es ->
                futureList.add(service.submit(runner.of(() -> {
                            try {
                                if (wef.get()) {
                                    combiner.apply(accumulator.apply(identity, es), identity);
                                }
                            } catch (Throwable e) {
                                futureList.forEach(future -> future.cancel(true));
                                wef.set(false);
                                countDownLatch.exception(e);
                            } finally {
                                countDownLatch.countDown();
                            }
                        })
                )));
        try {
            if (Objects.nonNull(timeout)) {
                countDownLatch.await(timeout);
            } else {
                countDownLatch.await();
            }
        } catch (Throwable e) {
            throw new DacException(e);
        }
        return identity;
    }
}

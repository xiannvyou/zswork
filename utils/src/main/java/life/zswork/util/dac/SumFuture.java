package life.zswork.util.dac;


import life.zswork.util.concurrent.ConcurrentCountDownLatch;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SumFuture {

    protected final static ForkJoinPool pool;
    protected final static AbstractRunnableWrapper runnableWrapper;
    private AbstractRunnableWrapper wrapper;

    static {
        pool = new ForkJoinPool(Optional.ofNullable(System.getProperty("dac.pool.parallelism"))
                .map(Integer::getInteger)
                .orElse(Runtime.getRuntime().availableProcessors() * 2));
        runnableWrapper = Optional.ofNullable(System.getProperty("dac.runnableWrapper"))
                .map(path -> {
                    try {
                        return (AbstractRunnableWrapper) ClassLoader.getSystemClassLoader().loadClass(path).newInstance();
                    } catch (Exception e) {
                        throw new DacException(e);
                    }
                })
                .orElse(new DefaultRunnableWrapper());
    }

    /**
     * 执行内容
     */
    private final List<Runnable> runnableList;

    /**
     * 计数器,记录有多少条执行链
     */
    private final AtomicInteger index;

    /**
     * 预计的线程数量
     */
    private final AtomicInteger threadNum;

    /**
     * 线程池
     */
    private ExecutorService executor;

    /**
     * 返回值容器,存放任务执行返回值
     */
    private final SumResultMap sumResultMap;

    protected SumFuture() {
        this.sumResultMap = SumResultMap.build();
        this.index = new AtomicInteger(-1);
        this.runnableList = new LinkedList<>();
        this.threadNum = new AtomicInteger(0);
    }

    public <T, U> SumFuture addChain(List<T> list,
                                     int partition,
                                     U identity,
                                     BiFunction<U, ? super List<T>, U> accumulator,
                                     BinaryOperator<U> combiner) {
        List<List<T>> subList = new ArrayList<>(10);
        subList(list, subList, partition);
        AbstractRunnableWrapper runner = Optional.ofNullable(wrapper).orElse(runnableWrapper);
        subList.forEach(es -> runnableList.add(runner.of(() ->
                combiner.apply(identity, accumulator.apply(identity, es))
        )));
        sumResultMap.put(index.addAndGet(1), identity);
        return this;
    }

    public <T> SumFuture listChain(List<T> list, int partition, Function<List<T>, List<?>> function) {
        List<List<T>> subList = new ArrayList<>(10);
        subList(list, subList, partition);
        List<Object> l = Collections.synchronizedList(new ArrayList<>());
        AbstractRunnableWrapper runner = Optional.ofNullable(wrapper).orElse(runnableWrapper);
        subList.forEach(es -> runnableList.add(runner.of(() ->
                l.addAll(function.apply(es))
        )));
        sumResultMap.put(index.addAndGet(1), l);
        return this;
    }

    public <T> SumFuture mapChain(List<T> list, int partition, Function<List<T>, Map<T, ?>> function) {
        List<List<T>> subList = new ArrayList<>(10);
        subList(list, subList, partition);
        Map<T, Object> m = new ConcurrentHashMap<>();
        AbstractRunnableWrapper runner = Optional.ofNullable(wrapper).orElse(runnableWrapper);
        subList.forEach(es -> runnableList.add(runner.of(() ->
                m.putAll(function.apply(es))
        )));
        sumResultMap.put(index.addAndGet(1), m);
        return this;
    }

    /**
     * 线程池
     */
    public SumFuture pool(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public SumFuture runnableWrapper(AbstractRunnableWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }


    /**
     * 获取一个执行链
     */
    public static SumFuture createSumChain() {
        return new SumFuture();
    }

    /**
     * 获取一个执行链
     */
    public static SumFuture createSumChain(ExecutorService executor) {
        SumFuture sumFuture = new SumFuture();
        sumFuture.executor = executor;
        return sumFuture;
    }

    /**
     * 获取一个执行链
     */
    public static SumFuture createSumChain(ExecutorService executor, AbstractRunnableWrapper wrapper) {
        SumFuture sumFuture = new SumFuture();
        sumFuture.executor = executor;
        sumFuture.wrapper = wrapper;
        return sumFuture;
    }

    public static <T> SumList<T> partition(List<T> list, int size) {
        List<List<T>> sub = new ArrayList<>();
        subList(list, sub, size);
        return new SumList<>(sub);
    }

    public static <T> SumSingle<T> single(List<T> list) {
        return new SumSingle<>(list);
    }

    private static <T> void subList(List<T> list, List<List<T>> subList, int partition) {
        int size = list.size();
        int group;
        if (0 == size % partition) {
            group = size / partition;
        } else {
            group = size / partition + 1;
        }
        for (int i = 0; i < group; i++) {
            subList.add(list.subList(i * partition, i == group - 1 ? size : (i + 1) * partition));
        }
    }

    public SumResultMap start() {
        return start(null);
    }

    public SumResultMap start(Duration duration) {
        if (0 == runnableList.size()) {
            throw new DacException("runnableChain is Empty");
        }
        ConcurrentCountDownLatch countDownLatch = new ConcurrentCountDownLatch(runnableList.size());
        AtomicBoolean wef = new AtomicBoolean(true);
        List<Future<?>> futureList = new LinkedList<>();
        for (Runnable runnable : runnableList) {
            ExecutorService service = Optional.ofNullable(executor).orElse(pool);
            futureList.add(service.submit(() -> {
                try {
                    if (wef.get()) {
                        runnable.run();
                    }
                } catch (Throwable e) {
                    wef.set(false);
                    futureList.forEach(future -> future.cancel(true));
                    countDownLatch.exception(e);
                } finally {
                    countDownLatch.countDown();
                }
            }));
        }
        try {
            if (Objects.nonNull(duration)) {
                countDownLatch.await(duration);
            } else {
                countDownLatch.await();
            }
        } catch (Throwable e) {
            throw new DacException(e);
        }
        return sumResultMap;
    }

    public static void main(String[] args) {
        List<Integer> list = Stream.iterate(1, o -> o + 1).limit(12345).collect(Collectors.toList());
        //1
        List<Integer> r = SumFuture.partition(list, 50).parallelReduce(new CopyOnWriteArrayList<>(), (u, l) -> {
            System.out.println(Thread.currentThread().getName() + ":" + l.size());
            u.addAll(l);
            return u;
        }, (u, l) -> u);
        System.out.println("r:" + r.stream().distinct().collect(Collectors.toList()).size());
        //2
        SumFuture.single(list).parallelForeach(integer -> {
            System.out.println(Thread.currentThread().getName() + ":" + integer);
        });
        //3
        List<Integer> list1 = Stream.iterate(1, o -> o + 1).limit(1100).collect(Collectors.toList());
        List<Integer> r1 = new CopyOnWriteArrayList<>();
        List<Integer> list2 = Stream.iterate(1, o -> o + 1).limit(2200).collect(Collectors.toList());
        List<Integer> r2 = new CopyOnWriteArrayList<>();
        SumResultMap start = SumFuture.createSumChain()
                .pool(pool)
                .listChain(list1, 50, l -> {
                    System.out.println(Thread.currentThread().getName() + ":" + l.size());
                    r1.addAll(l);
                    return l;
                })
                .listChain(list2, 50, l -> {
                    System.out.println(Thread.currentThread().getName() + ":" + l.size());
                    r2.addAll(l);
                    return l;
                }).start(Duration.ofSeconds(3));
        List<Integer> listResult1 = start.getListResult(0, Integer.class);
        List<Integer> listResult2 = start.getListResult(1, Integer.class);
        System.out.println("r1:" + r1.stream().distinct().collect(Collectors.toList()).size());
        System.out.println("r2:" + r2.stream().distinct().collect(Collectors.toList()).size());
        System.out.println("listResult1:" + listResult1.stream().distinct().collect(Collectors.toList()).size());
        System.out.println("listResult2:" + listResult2.stream().distinct().collect(Collectors.toList()).size());
    }
}

package life.zswork.util.concurrent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 支持异常处理的线程计数器
 */
public class ConcurrentCountDownLatch {
    private final LongAdder adder;
    private final Condition condition;
    private final ReentrantLock lock;
    private Throwable e;

    public ConcurrentCountDownLatch(int size) {
        LongAdder longAdder = new LongAdder();
        longAdder.add(Integer.toUnsignedLong(size));
        this.adder = longAdder;
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
    }

    public  void countDown() {
        adder.decrement();
        synchronized (this){
            if (adder.sum() <= 0) {
                lock.lock();
                try {
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public synchronized void exception(Throwable e) {
        if (Objects.nonNull(this.e)) {
            return;
        }
        this.e = e;
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void await(Duration duration) throws Throwable {
        lock.lock();
        try {
            condition.await(duration.getSeconds(), TimeUnit.SECONDS);
        } finally {
            lock.unlock();
        }
        if (Objects.nonNull(e)) {
            throw e;
        }
    }

    public void await() throws Throwable {
        lock.lock();
        try {
            condition.await();
        } finally {
            lock.unlock();
        }
        if (Objects.nonNull(e)) {
            throw e;
        }
    }
}

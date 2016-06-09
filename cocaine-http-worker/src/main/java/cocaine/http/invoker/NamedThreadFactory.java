package cocaine.http.invoker;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author akirakozov
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger threadPoolNumber = new AtomicInteger(1);
    private static final String NAME_PATTERN = "%s-%d-thread";

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String threadNamePrefix;

    public NamedThreadFactory(String threadNamePrefix) {
        final SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.threadNamePrefix = String.format(NAME_PATTERN, threadNamePrefix, threadPoolNumber.getAndIncrement());
    }

    public Thread newThread(Runnable r) {
        return new Thread(group, r, threadNamePrefix + "-" + threadNumber.getAndIncrement(), 0);
    }

}

package styles.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadSafetyProvider {
    // Class used to secure thread safety along all code

    public static final ScheduledExecutorService DeathSystemScheduler = Executors.newScheduledThreadPool(1);

    public static void shutdownAllThreads() {
        DeathSystemScheduler.shutdownNow();
    }
}

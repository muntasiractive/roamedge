package com.roam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Centralized thread pool manager for background operations.
 * 
 * <p>
 * Provides managed ExecutorService instances to prevent resource leaks
 * from uncontrolled thread creation. This singleton class manages three
 * specialized thread pools optimized for different workload types.
 * </p>
 * 
 * <h2>Thread Pools</h2>
 * <ul>
 * <li><b>IO Pool</b> - Fixed pool (5 threads) for file I/O operations like
 * export and import.
 * Larger pool since I/O operations are blocking but not CPU-intensive.</li>
 * <li><b>Compute Pool</b> - Fixed pool (CPU cores) for CPU-intensive operations
 * like indexing
 * and search. Size based on available processors.</li>
 * <li><b>Scheduled Pool</b> - Scheduled pool (2 threads) for periodic or
 * delayed tasks.</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * This class is thread-safe. The singleton is implemented using double-checked
 * locking
 * with a volatile instance variable.
 * </p>
 * 
 * <h2>Shutdown</h2>
 * <p>
 * Call {@link #gracefulShutdown()} during application termination to properly
 * release thread pool resources.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * ThreadPoolManager manager = ThreadPoolManager.getInstance();
 * 
 * // Submit an I/O task
 * manager.submitIoTask(() -> exportData());
 * 
 * // Submit a compute task with result
 * Future<Result> future = manager.submitComputeTask(() -> processData());
 * 
 * // Schedule a delayed task
 * manager.schedule(() -> cleanup(), 5, TimeUnit.MINUTES);
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class ThreadPoolManager {

    // ========================================
    // CONSTANTS
    // ========================================

    /** Logger for thread pool operations. */
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    /** Timeout in seconds for graceful shutdown. */
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

    // ========================================
    // SINGLETON INSTANCE
    // ========================================

    /** Volatile singleton instance for thread-safe access. */
    private static volatile ThreadPoolManager instance;

    /** Lock object for double-checked locking. */
    private static final Object LOCK = new Object();

    // ========================================
    // THREAD POOLS
    // ========================================

    /** Thread pool for I/O-bound operations (file export/import). */
    private final ExecutorService ioPool;

    /** Thread pool for CPU-intensive operations (indexing, search). */
    private final ExecutorService computePool;

    /** Thread pool for scheduled and delayed tasks. */
    private final ScheduledExecutorService scheduledPool;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Private constructor for singleton pattern.
     * Initializes thread pools with appropriate sizes.
     */
    private ThreadPoolManager() {
        int processors = Runtime.getRuntime().availableProcessors();

        // IO Pool: Fixed size for file operations (export/import)
        // Larger pool since I/O operations are blocking but not CPU-intensive
        this.ioPool = Executors.newFixedThreadPool(
                5,
                createThreadFactory("io-pool"));

        // Compute Pool: Fixed size based on CPU cores for CPU-intensive tasks
        // Used for indexing, search, and other computational work
        this.computePool = Executors.newFixedThreadPool(
                Math.max(2, processors),
                createThreadFactory("compute-pool"));

        // Scheduled Pool: For periodic or delayed tasks
        this.scheduledPool = Executors.newScheduledThreadPool(
                2,
                createThreadFactory("scheduled-pool"));

        logger.info("✓ ThreadPoolManager initialized: IO={}, Compute={}, Scheduled={}",
                5, Math.max(2, processors), 2);
    }

    // ========================================
    // SINGLETON ACCESS
    // ========================================

    /**
     * Gets the singleton instance using double-checked locking.
     * 
     * <p>
     * This method provides thread-safe initialization without synchronization
     * overhead on subsequent calls.
     * </p>
     * 
     * @return the singleton ThreadPoolManager instance
     */
    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    // ========================================
    // POOL ACCESSORS
    // ========================================

    /**
     * Gets the ExecutorService for I/O operations (file export/import).
     * 
     * @return the ExecutorService for I/O tasks
     */
    public ExecutorService getIoPool() {
        return ioPool;
    }

    /**
     * Get ExecutorService for CPU-intensive operations (indexing, search).
     * 
     * @return ExecutorService for compute tasks
     */
    public ExecutorService getComputePool() {
        return computePool;
    }

    /**
     * Get ScheduledExecutorService for scheduled/delayed tasks.
     * 
     * @return ScheduledExecutorService for scheduled tasks
     */
    public ScheduledExecutorService getScheduledPool() {
        return scheduledPool;
    }

    // ========================================
    // TASK SUBMISSION
    // ========================================

    /**
     * Submits a task to the I/O pool and returns a Future.
     * 
     * @param task the Runnable task to execute
     * @return Future representing pending completion
     */
    public Future<?> submitIoTask(Runnable task) {
        logger.debug("Submitting I/O task: {}", task.getClass().getSimpleName());
        return ioPool.submit(task);
    }

    /**
     * Submit a task to the compute pool and return a Future.
     * 
     * @param task Runnable task to execute
     * @return Future representing pending completion
     */
    public Future<?> submitComputeTask(Runnable task) {
        logger.debug("Submitting compute task: {}", task.getClass().getSimpleName());
        return computePool.submit(task);
    }

    /**
     * Submit a callable task to the I/O pool and return a Future with result.
     * 
     * @param task Callable task to execute
     * @param <T>  Return type
     * @return Future containing the result
     */
    public <T> Future<T> submitIoTask(Callable<T> task) {
        logger.debug("Submitting I/O callable: {}", task.getClass().getSimpleName());
        return ioPool.submit(task);
    }

    /**
     * Submit a callable task to the compute pool and return a Future with result.
     * 
     * @param task Callable task to execute
     * @param <T>  Return type
     * @return Future containing the result
     */
    public <T> Future<T> submitComputeTask(Callable<T> task) {
        logger.debug("Submitting compute callable: {}", task.getClass().getSimpleName());
        return computePool.submit(task);
    }

    // ========================================
    // TASK SCHEDULING
    // ========================================

    /**
     * Schedules a task to run after a delay.
     * 
     * @param task  the task to execute
     * @param delay delay before execution
     * @param unit  time unit for delay
     * @return ScheduledFuture representing pending completion
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        logger.debug("Scheduling task with delay: {} {}", delay, unit);
        return scheduledPool.schedule(task, delay, unit);
    }

    /**
     * Schedule a task to run periodically.
     * 
     * @param task         Task to execute
     * @param initialDelay Initial delay before first execution
     * @param period       Period between successive executions
     * @param unit         Time unit for delays
     * @return ScheduledFuture representing pending completion
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        logger.debug("Scheduling periodic task: initial={} {}, period={} {}",
                initialDelay, unit, period, unit);
        return scheduledPool.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    // ========================================
    // LIFECYCLE MANAGEMENT
    // ========================================

    /**
     * Initiates an orderly shutdown of all thread pools.
     * 
     * <p>
     * Previously submitted tasks are executed, but no new tasks will be accepted.
     * This method does not wait for previously submitted tasks to complete
     * execution.
     * Use {@link #awaitTermination()} to wait for shutdown completion.
     * </p>
     */
    public void shutdown() {
        logger.info("🛑 Shutting down thread pools...");

        ioPool.shutdown();
        computePool.shutdown();
        scheduledPool.shutdown();

        logger.info("✓ Thread pool shutdown initiated");
    }

    /**
     * Attempts to stop all actively executing tasks and halts the processing
     * of waiting tasks. This method does not wait for actively executing tasks
     * to terminate. Use {@link #awaitTermination()} to wait for termination.
     */
    public void shutdownNow() {
        logger.warn("⚠️ Force shutting down thread pools...");

        ioPool.shutdownNow();
        computePool.shutdownNow();
        scheduledPool.shutdownNow();

        logger.info("✓ Thread pool force shutdown complete");
    }

    /**
     * Waits for all thread pools to terminate after shutdown.
     * Blocks until all tasks have completed execution, or the timeout occurs,
     * or the current thread is interrupted, whichever happens first.
     * 
     * @return true if all pools terminated, false if timeout elapsed
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination() throws InterruptedException {
        logger.info("⏳ Waiting for thread pools to terminate (timeout: {} seconds)...",
                SHUTDOWN_TIMEOUT_SECONDS);

        boolean ioTerminated = ioPool.awaitTermination(
                SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        boolean computeTerminated = computePool.awaitTermination(
                SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        boolean scheduledTerminated = scheduledPool.awaitTermination(
                SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        boolean allTerminated = ioTerminated && computeTerminated && scheduledTerminated;

        if (allTerminated) {
            logger.info("✓ All thread pools terminated successfully");
        } else {
            logger.warn("⚠️ Some thread pools did not terminate within timeout");
            if (!ioTerminated)
                logger.warn("  - I/O pool still active");
            if (!computeTerminated)
                logger.warn("  - Compute pool still active");
            if (!scheduledTerminated)
                logger.warn("  - Scheduled pool still active");
        }

        return allTerminated;
    }

    /**
     * Performs graceful shutdown: initiates shutdown and waits for termination.
     * If termination times out, performs forced shutdown.
     */
    public void gracefulShutdown() {
        try {
            shutdown();
            boolean terminated = awaitTermination();

            if (!terminated) {
                logger.warn("⚠️ Graceful shutdown timeout, forcing shutdown...");
                shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("✗ Shutdown interrupted, forcing shutdown...", e);
            shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ========================================
    // STATUS CHECKS
    // ========================================

    /**
     * Checks if all thread pools are shut down.
     * 
     * @return true if all pools are shut down
     */
    public boolean isShutdown() {
        return ioPool.isShutdown() && computePool.isShutdown() && scheduledPool.isShutdown();
    }

    /**
     * Check if all thread pools have terminated.
     * 
     * @return true if all pools have terminated
     */
    public boolean isTerminated() {
        return ioPool.isTerminated() && computePool.isTerminated() && scheduledPool.isTerminated();
    }

    // ========================================
    // INTERNAL UTILITIES
    // ========================================

    /**
     * Creates a ThreadFactory with custom thread naming and exception handling.
     * 
     * @param poolName name prefix for threads created by this factory
     * @return configured ThreadFactory instance
     */
    private ThreadFactory createThreadFactory(String poolName) {
        return new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, poolName + "-" + (++threadCount));
                thread.setDaemon(false); // Non-daemon to allow proper shutdown
                thread.setUncaughtExceptionHandler((t, e) -> logger.error("✗ Uncaught exception in thread {}: {}",
                        t.getName(), e.getMessage(), e));
                return thread;
            }
        };
    }
}

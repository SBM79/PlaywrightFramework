package utils;

import config.QAConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class RetryAnalyzer implements IRetryAnalyzer {

    /**
     * Maximum number of additional attempts beyond the first run.
     * Sourced from {@link QAConfig#MAX_RETRY_COUNT} so it never needs to be
     * changed here.
     */
    private static final int MAX_RETRY = QAConfig.MAX_RETRY_COUNT;

    /**
     * Per-test retry counter map.
     * Key: {@code "ClassName#methodName"} to uniquely identify each test case
     * across parallel threads.
     */
    private final ConcurrentHashMap<String, AtomicInteger> retryMap =
            new ConcurrentHashMap<>();

    /**
     * Determine whether a failed test should be retried.
     *
     * @param result the result of the test that just failed
     * @return {@code true}  — re-run the test (retry attempt consumed)
     *         {@code false} — mark the test as failed, do not retry again
     */
    @Override
    public boolean retry(ITestResult result) {
        // Build a unique key per test method
        String testKey = result.getTestClass().getName()
                + "#" + result.getName();

        // Initialise counter on first failure for this test
        retryMap.putIfAbsent(testKey, new AtomicInteger(0));
        AtomicInteger counter = retryMap.get(testKey);

        if (counter.get() < MAX_RETRY) {
            int attempt = counter.incrementAndGet();
            System.out.printf(
                "[RetryAnalyzer] Re-running %-60s  (attempt %d / %d)%n",
                result.getName(), attempt, MAX_RETRY
            );
            return true;
        }

        System.out.printf(
            "[RetryAnalyzer] Test FAILED after %d attempt(s): %s%n",
            MAX_RETRY + 1, result.getName()
        );
        return false;
    }
}

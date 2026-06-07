package utils;

import config.QAConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY = QAConfig.MAX_RETRY_COUNT;

    private final ConcurrentHashMap<String, AtomicInteger> retryMap =
            new ConcurrentHashMap<>();

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

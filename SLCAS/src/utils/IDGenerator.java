package utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe unique ID generator.
 * Format: PREFIX-YY-NNNN  e.g. ITEM-26-1001, USR-26-1002
 */
public class IDGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1000);

    public static String generateId(String prefix) {
        int year = java.time.Year.now().getValue() % 100;
        return String.format("%s-%02d-%04d", prefix, year, counter.getAndIncrement());
    }
}

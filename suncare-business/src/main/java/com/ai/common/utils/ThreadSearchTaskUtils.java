package com.ai.common.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class ThreadSearchTaskUtils {

    private static ExecutorService executor;

    public static void add(Runnable runnable) {
        executor.submit(runnable);
    }

    public static void create() {
        executor = Executors.newCachedThreadPool();
    }

    public static void shutDown() {
        executor.shutdown();
    }

    public static class FixPool {
        private ExecutorService executor;

        public void add(Runnable runnable) {
            executor.submit(runnable);
        }

        public void removeAll() {
            executor.shutdownNow();
        }

        public FixPool(int num) {
            executor = Executors.newFixedThreadPool(num);
        }

        public ExecutorService getExecutor() {
            return executor;
        }
    }
}

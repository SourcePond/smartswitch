package ch.sourcepond.commons.smartswitch.tests;

import java.util.concurrent.ExecutorService;

/**
 * Created by rolandhauser on 24.02.17.
 */
public class TestComponent {

    private volatile ExecutorService testExecutor;

    public void execute(final Runnable runnable) {
        testExecutor.execute(runnable);
    }
}

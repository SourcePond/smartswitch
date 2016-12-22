import ch.sourcepond.commons.failsafeservice.api.SmartSwitchFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rolandhauser on 22.12.16.
 */
public class APITest {
    public void test() {
        SmartSwitchFactory sw = null;

        // adapter.whenService(ExecutorService.class).withFilter("someFilter").isUnavailableThenUse(xxxx).untilServiceIsAvailable()

        // adapter.whenService(ExecutorService.class).isUnavailableThenUse(xxxx).andExecute(xxxx).whenServiceBecomesAvailable();

        ExecutorService e1 = sw.whenService(
                ExecutorService.class).isUnavailableThenUse(
                        () -> Executors.newCachedThreadPool()).instead();

        ExecutorService e2 = sw.whenService(
                ExecutorService.class).withFilter(
                        "SomeFilter").isUnavailableThenUse(
                            ()-> Executors.newCachedThreadPool()).instead();

        ExecutorService e3 = sw.whenService(
                ExecutorService.class).isUnavailableThenUse(
                () -> Executors.newCachedThreadPool()).insteadAndObserveAvailability(
                        e -> e.shutdown());

        ExecutorService e4 = sw.whenService(
                ExecutorService.class).withFilter("SomeFilter").isUnavailableThenUse(
                () -> Executors.newCachedThreadPool()).insteadAndObserveAvailability(
                e -> e.shutdown());
    }
}


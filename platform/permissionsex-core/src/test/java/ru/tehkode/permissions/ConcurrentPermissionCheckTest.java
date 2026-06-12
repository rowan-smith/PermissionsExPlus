package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentPermissionCheckTest extends PEXTestBase {

    @Test
    public void parallelHasChecksRemainConsistent() throws Exception {
        PermissionUser user = manager.getUser("concurrentUser");
        user.addPermission("test.node.*", null);

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tasks.add(() -> user.has("test.node.child", null));
        }

        List<Future<Boolean>> results = pool.invokeAll(tasks);
        pool.shutdown();

        for (Future<Boolean> result : results) {
            assertTrue(result.get());
        }
    }
}

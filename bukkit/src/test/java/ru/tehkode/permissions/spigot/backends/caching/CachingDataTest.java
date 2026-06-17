package ru.tehkode.permissions.spigot.backends.caching;

import dev.rono.permissions.core.backends.caching.CachingUserData;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.spigot.backends.memory.MemoryData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachingDataTest {
    @Test
    public void testCacheAndAsyncWrite() throws Exception {
        MemoryData backingData = new MemoryData("testUser");
        CachingUserData cachingData = new CachingUserData(backingData, Runnable::run, new Object());

        cachingData.setPermissions(Arrays.asList("perm1", "perm2"), "world");
        assertEquals(Arrays.asList("perm1", "perm2"), cachingData.getPermissions("world"));
        assertEquals(Arrays.asList("perm1", "perm2"), backingData.getPermissions("world"));

        var exec = Executors.newSingleThreadExecutor();
        try {
            CachingUserData asyncData = new CachingUserData(backingData, exec, new Object());
            asyncData.setPermissions(Collections.singletonList("async-perm"), "world");
            assertEquals(Collections.singletonList("async-perm"), asyncData.getPermissions("world"));
        } finally {
            exec.shutdown();
            exec.awaitTermination(5, TimeUnit.SECONDS);
        }

        // Once the executor has drained, backing data must reflect changes.
        assertEquals(Collections.singletonList("async-perm"), backingData.getPermissions("world"));
    }

    @Test
    public void testOptionsAndParents() {
        MemoryData backingData = new MemoryData("testUser");
        CachingUserData cachingData = new CachingUserData(backingData, Runnable::run, new Object());

        cachingData.setOption("opt1", "val1", "world");
        assertEquals("val1", cachingData.getOption("opt1", "world"));
        assertEquals("val1", backingData.getOption("opt1", "world"));

        List<String> parents = Arrays.asList("group1", "group2");
        cachingData.setParents(parents, "world");
        assertEquals(parents, cachingData.getParents("world"));
        assertEquals(parents, backingData.getParents("world"));
    }
}

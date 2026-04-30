package ru.tehkode.permissions.backends.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.backends.memory.MemoryData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CachingDataTest extends PEXTestBase {
    private MemoryData backingData;
    private CachingUserData cachingData;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        backingData = new MemoryData("testUser");
        // Use a direct executor for synchronous testing
        cachingData = new CachingUserData(backingData, Runnable::run, new Object());
    }

    @Test
    public void testCacheLoading() {
        backingData.setPermissions(Arrays.asList("perm1", "perm2"), "world");
        
        // Should load from backing data
        List<String> perms = cachingData.getPermissions("world");
        assertEquals(Arrays.asList("perm1", "perm2"), perms);
        
        // Modify backing data directly
        backingData.setPermissions(Collections.singletonList("perm3"), "world");
        
        // Should still return cached value
        assertEquals(Arrays.asList("perm1", "perm2"), cachingData.getPermissions("world"));
        
        // Call load() to clear cache and reload
        cachingData.load();
        assertEquals(Collections.singletonList("perm3"), cachingData.getPermissions("world"));
    }

    @Test
    public void testSetPermissions() {
        cachingData.setPermissions(Collections.singletonList("new-perm"), "world");
        
        // Should be in cache immediately
        assertEquals(Collections.singletonList("new-perm"), cachingData.getPermissions("world"));
        
        // Should be in backing data (because we used DirectExecutor)
        assertEquals(Collections.singletonList("new-perm"), backingData.getPermissions("world"));
    }

    @Test
    public void testOptions() {
        backingData.setOption("opt1", "val1", "world");
        
        assertEquals("val1", cachingData.getOption("opt1", "world"));
        
        cachingData.setOption("opt1", "val2", "world");
        assertEquals("val2", cachingData.getOption("opt1", "world"));
        assertEquals("val2", backingData.getOption("opt1", "world"));
        
        cachingData.setOption("opt1", null, "world");
        assertNull(cachingData.getOption("opt1", "world"));
        assertNull(backingData.getOption("opt1", "world"));
    }

    @Test
    public void testParents() {
        backingData.setParents(Collections.singletonList("group1"), "world");
        
        assertEquals(Collections.singletonList("group1"), cachingData.getParents("world"));
        
        cachingData.setParents(Arrays.asList("group1", "group2"), "world");
        assertEquals(Arrays.asList("group1", "group2"), cachingData.getParents("world"));
        assertEquals(Arrays.asList("group1", "group2"), backingData.getParents("world"));
    }

    @Test
    public void testAsyncExecutor() throws InterruptedException {
        // Test with real async executor
        CachingUserData asyncCachingData = new CachingUserData(backingData, manager.getExecutor(), new Object());
        
        asyncCachingData.setPermissions(Collections.singletonList("async-perm"), "world");
        
        // Cache should be updated immediately
        assertEquals(Collections.singletonList("async-perm"), asyncCachingData.getPermissions("world"));
        
        // Backing data might not be updated yet (if it were truly async)
        // In our case, the executor is a single thread. 
        // We wait for it.
        waitForExecutor();
        
        assertEquals(Collections.singletonList("async-perm"), backingData.getPermissions("world"));
    }
}

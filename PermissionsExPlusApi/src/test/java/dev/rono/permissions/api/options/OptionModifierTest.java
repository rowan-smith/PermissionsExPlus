package dev.rono.permissions.api.options;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionModifierTest {

    @Test
    void temporaryOptionConvenienceBuildsAnExpiringNode() {
        var contexts = ContextSet.builder().add("world", "nether").build();

        var modifier = new TestModifier();

        modifier.setTemporaryOption("chat:colour", "gold", contexts, Duration.ofHours(1));

        assertEquals("chat:colour", modifier.lastOption.key());

        assertEquals("gold", modifier.lastOption.value());

        assertEquals(contexts, modifier.lastOption.contexts());

        assertTrue(modifier.lastOption.temporary());
    }

    @Test
    void temporaryPrefixAndSuffixUseBuiltInKeys() {
        var modifier = new TestModifier();

        modifier.setTemporaryPrefix("[Admin]", Duration.ofHours(1));

        assertEquals(OptionKeys.PREFIX, modifier.lastOption.key());

        assertTrue(modifier.lastOption.temporary());

        modifier.setTemporarySuffix("[AFK]", Duration.ofHours(1));

        assertEquals(OptionKeys.SUFFIX, modifier.lastOption.key());

        assertTrue(modifier.lastOption.temporary());
    }

    private static final class TestModifier implements OptionModifier<TestModifier> {
        private OptionNode lastOption;

        @Override
        public TestModifier setOption(OptionNode option) {
            this.lastOption = option;

            return this;
        }

        @Override
        public TestModifier removeOption(String key, ContextSet contexts) {
            return this;
        }

        @Override
        public TestModifier removeOptions(String key) {
            return this;
        }

        @Override
        public TestModifier clearOptions() {
            return this;
        }

        @Override
        public TestModifier clearOptions(ContextSet contexts) {
            return this;
        }
    }
}

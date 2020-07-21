/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.menzani.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AbstractBuilderTest {
    private Builder builder;

    @BeforeEach
    void init() {
        builder = new Builder();
    }

    @Test
    void buildUnlocked() {
        Object object = builder.build();
        assertEquals(builder.object, object);
    }

    @Test
    void buildUnlockedAndPropertyMissing() {
        Builder.PropertyMissingValidation validation = new Builder.PropertyMissingValidation();
        builder.validation = validation;
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.build());
        assertEquals("These properties must be set: " + validation.property + '.', e.getMessage());
    }

    @Test
    void buildLocked() {
        builder.lock();
        Object object = builder.build();
        assertEquals(builder.object, object);
    }

    @Test
    void buildLockedAndPropertyMissing() {
        builder.lock();
        builder.validation = new Builder.PropertyMissingValidation();
        Object object = builder.build();
        assertEquals(builder.object, object);
    }

    @Test
    void lock() {
        builder.lock();
        assertTrue(builder.isLocked());
    }

    @Test
    void lockWithPropertyMissing() {
        Builder.PropertyMissingValidation validation = new Builder.PropertyMissingValidation();
        builder.validation = validation;
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.lock());
        assertEquals("These properties must be set: " + validation.property + '.', e.getMessage());
        assertFalse(builder.isLocked());
    }

    @Test
    void lockLocked() {
        builder.lock();
        builder.validation = new Builder.PropertyMissingValidation();
        builder.lock();
        assertTrue(builder.isLocked());
    }

    @Test
    void checkLockedOnUnlocked() {
        builder.checkLocked();
    }

    @Test
    void checkLockedOnLocked() {
        builder.lock();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.checkLocked());
        assertEquals("Builder is locked.", e.getMessage());
    }

    @Test
    void twoPropertiesMissing() {
        String property1 = UUID.randomUUID().toString();
        String property2 = UUID.randomUUID().toString();
        builder.validation = missingProperties -> {
            missingProperties.add(UUID.randomUUID().toString());
            missingProperties.poll();
            missingProperties.offer(UUID.randomUUID().toString());
            missingProperties.remove();
            missingProperties.add(property1);
            missingProperties.offer(property2);
        };
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.build());
        assertEquals("These properties must be set: " + property1 + ", " + property2 + '.', e.getMessage());
    }

    private static class Builder extends AbstractBuilder<Object> {
        private final Object object = new Object();
        private Consumer<Queue<String>> validation = missingProperties -> {};

        @Override
        protected Object doBuild() {
            return object;
        }

        @Override
        protected void validate(Queue<String> missingProperties) {
            validation.accept(missingProperties);
        }

        private static class PropertyMissingValidation implements Consumer<Queue<String>> {
            private final String property = UUID.randomUUID().toString();

            @Override
            public void accept(Queue<String> missingProperties) {
                missingProperties.add(property);
            }
        }
    }
}
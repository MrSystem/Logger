/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.menzani.logger.api;

import eu.menzani.logger.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractLoggerTest {
    private BufferConsumer consumer;
    private Logger logger;

    @BeforeEach
    void init() {
        consumer = new BufferConsumer();
        logger = newLogger(new Pipeline()
                .setProducer(new Producer()
                        .append('[')
                        .append(new TimestampFormatter())
                        .append(' ')
                        .append(new LevelFormatter())
                        .append("] ")
                        .append(new MessageFormatter()))
                .addConsumer(consumer));
    }

    protected abstract PipelineLogger newLogger(Pipeline pipeline);

    @ParameterizedTest
    @EnumSource(StandardLevel.class)
    void logMessage(StandardLevel level) throws InterruptedException {
        UUID message = UUID.randomUUID();
        logger.log(level, message);

        String entry = consumer.nextEntry();
        assertTrue(entry.contains(' ' + level.getMarker() + "] "));
        assertTrue(entry.contains("] " + message));
    }

    @ParameterizedTest
    @EnumSource(StandardLevel.class)
    void logLazyMessage(StandardLevel level) throws InterruptedException {
        UUID message = UUID.randomUUID();
        logger.log(level, () -> message);

        String entry = consumer.nextEntry();
        assertTrue(entry.contains(' ' + level.getMarker() + "] "));
        assertTrue(entry.contains("] " + message));
    }

    @ParameterizedTest
    @EnumSource(StandardLevel.class)
    void logLazyMessageThrowingException(StandardLevel level) throws InterruptedException {
        Exception e = new Exception();
        logger.log(level, () -> { throw e; });

        String entry = consumer.nextEntry();
        final String errorMarker = AbstractLogger.ReservedLevel.ERROR.getMarker();
        assertTrue(entry.contains(' ' + errorMarker + "] "));
        assertTrue(entry.contains("Could not evaluate lazy message at level: " + level.getMarker() + System.lineSeparator()));
        assertTrue(entry.contains(AbstractLogger.throwableToString(e)));
    }

    @ParameterizedTest
    @EnumSource(StandardLevel.class)
    void logThrowableWithMessage(StandardLevel level) throws InterruptedException {
        Exception e = new Exception();
        UUID message = UUID.randomUUID();
        logger.throwable(level, e, message);

        String entry = consumer.nextEntry();
        assertTrue(entry.contains(' ' + level.getMarker() + "] "));
        assertTrue(entry.contains("] " + message + System.lineSeparator()));
        assertTrue(entry.contains(AbstractLogger.throwableToString(e)));
    }

    @ParameterizedTest
    @EnumSource(StandardLevel.class)
    void logThrowableWithLazyMessage(StandardLevel level) throws InterruptedException {
        Exception e = new Exception();
        UUID message = UUID.randomUUID();
        logger.throwable(level, e, () -> message);

        String entry = consumer.nextEntry();
        assertTrue(entry.contains(' ' + level.getMarker() + "] "));
        assertTrue(entry.contains("] " + message + System.lineSeparator()));
        assertTrue(entry.contains(AbstractLogger.throwableToString(e)));
    }

    @ParameterizedTest
    @EnumSource(StandardLevel.class)
    void logThrowableWithParameterizedMessage(StandardLevel level) throws InterruptedException {
        Exception e = new Exception();
        UUID message = UUID.randomUUID();
        logger.throwable(level, e, "{}", message);

        String entry = consumer.nextEntry();
        assertTrue(entry.contains(' ' + level.getMarker() + "] "));
        assertTrue(entry.contains("] " + message + System.lineSeparator()));
        assertTrue(entry.contains(AbstractLogger.throwableToString(e)));
    }
}
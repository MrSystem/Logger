package it.menzani.logger.api;

import it.menzani.logger.impl.LogEntry;

import java.util.function.BiConsumer;

public interface Consumer extends BiConsumer<LogEntry, String> {
    void consume(LogEntry entry, String formattedEntry) throws Exception;

    default void accept(LogEntry entry, String formattedEntry) {
        try {
            consume(entry, formattedEntry);
        } catch (Exception e) {
            AbstractLogger.Error error = new PipelineLogger.PipelineError(Consumer.class, this);
            error.print(e);
        }
    }
}

package it.menzani.logger.api;

import it.menzani.logger.EvaluationException;
import it.menzani.logger.LogEntry;

import java.util.Optional;
import java.util.function.BiFunction;

public interface Formatter extends BiFunction<LogEntry, Logger, Optional<String>> {
    String format(LogEntry entry) throws Exception;

    default Optional<String> apply(LogEntry entry, Logger logger) {
        try {
            return Optional.ofNullable(format(entry));
        } catch (EvaluationException e) {
            Object message = "Could not evaluate lazy message at level: " + entry.getLevel().getMarker();
            logger.throwable(AbstractLogger.ReservedLevel.LOGGER, e.getCause(), message);
        } catch (Exception e) {
            AbstractLogger.Error error = new AbstractLogger.PipelineError(Formatter.class, this);
            error.print();
            e.printStackTrace();
        }
        return Optional.empty();
    }
}

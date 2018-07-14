package it.menzani.logger.impl;

import it.menzani.logger.AtomicLazy;
import it.menzani.logger.Lazy;
import it.menzani.logger.api.LazyMessage;
import it.menzani.logger.api.Level;

public final class LogEntry {
    private final Level level;
    private final Object message;
    private final Lazy<Object> lazyMessage;

    public LogEntry(Level level, Object message, LazyMessage lazyMessage) {
        this.level = level;
        this.message = message;
        this.lazyMessage = lazyMessage == null ? null : new AtomicLazy<>(lazyMessage::evaluate, 5);
    }

    public Level getLevel() {
        return level;
    }

    public Object getMessage() throws EvaluationException {
        if (message != null) {
            return message;
        }
        assert lazyMessage != null;
        try {
            return lazyMessage.get();
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }
}
import it.menzani.logger.api.Logger;
import it.menzani.logger.api.RotationPolicy;
import it.menzani.logger.impl.*;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class RotatingFileConsumerTest {
    public static void main(String[] args) {
        Logger logger = new SynchronousLogger()
                .addPipeline(new Pipeline()
                        .setProducer(new Producer()
                                .append(new TimestampFormatter())
                                .append(" -> ")
                                .append(new MessageFormatter()))
                        .addConsumer(new ConsoleConsumer())
                        .addConsumer(new RotatingFileConsumer(Runtime.folder, createRotationPolicy())));

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> logger.info(UUID.randomUUID()), 0, 500, TimeUnit.MILLISECONDS);
    }

    private static RotationPolicy createRotationPolicy() {
        switch (Runtime.Setting.ROTATION_POLICY.get()) {
            case "startup":
                return newStartupRotationPolicy();
            case "temporal":
                return newTemporalRotationPolicy();
            default:
                throw Runtime.Setting.ROTATION_POLICY.newInvalidValueException();
        }
    }

    private static RotationPolicy newStartupRotationPolicy() {
        return new StartupRotationPolicy("Startup {TIMESTAMP}-{ID}.log",
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    private static RotationPolicy newTemporalRotationPolicy() {
        Runtime.cleanOutput();
        return new TemporalRotationPolicy("Temporal {TIMESTAMP}.log",
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM), 2, ChronoField.SECOND_OF_MINUTE);
    }
}
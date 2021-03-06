/*
 * Copyright 2020 Francesco Menzani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.menzani.logger.impl;

import eu.menzani.logger.Objects;
import eu.menzani.logger.api.Consumer;
import eu.menzani.logger.api.RotationPolicy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.temporal.Temporal;
import java.util.Optional;

public final class RotatingFileConsumer implements Consumer {
    private final Path root;
    private RotationPolicy policy;
    private volatile LogFile currentFile;

    public RotatingFileConsumer(Path root) {
        if (Files.isRegularFile(Objects.objectNotNull(root, "root"))) {
            throw new IllegalArgumentException("root must not be an existing file.");
        }
        this.root = root;
    }

    public synchronized RotatingFileConsumer setPolicy(RotationPolicy policy) {
        this.policy = Objects.objectNotNull(policy, "policy");
        currentFile = null;
        return this;
    }

    @Override
    public void consume(LogEntry entry, String formattedEntry) throws Exception {
        synchronized (this) {
            shouldRotate(entry.getTimestamp()).ifPresent(newerFile -> currentFile = newerFile);
        }
        currentFile.writer.println(formattedEntry);
    }

    private Optional<LogFile> shouldRotate(Temporal timestamp) throws IOException {
        if (currentFile == null) {
            Files.createDirectories(root);
            policy.doInitialize(root, timestamp);
            return Optional.of(new LogFile(policy.getCurrentFile(root, timestamp)));
        }
        Path currentFilePath = policy.getCurrentFile(root, timestamp);
        if (currentFile.path.equals(currentFilePath)) {
            return Optional.empty();
        }
        currentFile.writer.close();
        return Optional.of(new LogFile(currentFilePath));
    }

    private static final class LogFile {
        private final Path path;
        private final PrintWriter writer;

        private LogFile(Path path) throws IOException {
            this.path = Objects.objectNotNull(path, "policy#currentFile()");
            OutputStream stream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            writer = new PrintWriter(stream, true);
        }
    }
}

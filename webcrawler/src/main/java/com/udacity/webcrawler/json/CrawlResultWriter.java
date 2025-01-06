package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
    private final CrawlResult result;
    
    /**
     * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
     */
    public CrawlResultWriter(CrawlResult result) {
        this.result = Objects.requireNonNull(result);
    }
    
    /**
     * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
     *
     * <p>If a file already exists at the path, the existing file should not be deleted; new data
     * should be appended to it.
     *
     * @param path the file path where the crawl result data should be written.
     */
    public void write(Path path) {
        // This is here to get rid of the unused variable warning.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        Writer writer = null;
        try {
            Objects.requireNonNull(path);
            String crawlResult = objectMapper.writeValueAsString(result);
            if (Files.exists(path)) {
                writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
                writer.write(crawlResult);
            } else {
                writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE);
                writer.write(crawlResult);
            }
        } catch (NullPointerException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
     *
     * @param writer the destination where the crawl result data should be written.
     */
    public void write(Writer writer) {
        // This is here to get rid of the unused variable warning.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        try {
            Objects.requireNonNull(writer);
            objectMapper.writeValue(writer, result);
        } catch (NullPointerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

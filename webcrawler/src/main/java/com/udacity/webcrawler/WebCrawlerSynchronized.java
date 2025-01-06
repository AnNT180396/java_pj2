package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebCrawlerSynchronized extends RecursiveAction {
    private final Clock clock;
    private final String url;
    private final ConcurrentSkipListSet<String> visitedUrls;
    private final Instant deadline;
    private final int maxDepth;
    private final ConcurrentHashMap<String, Integer> counts;
    private final List<Pattern> ignoredUrls;
    private final PageParserFactory parserFactory;
    public WebCrawlerSynchronized(Clock clock, String url, ConcurrentSkipListSet<String> visitedUrls,
                                  Instant deadline, int maxDepth, ConcurrentHashMap<String, Integer> counts,
                                  List<Pattern> ignoredUrls, PageParserFactory parserFactory) {
        this.clock = clock;
        this.url= url;
        this.visitedUrls = visitedUrls;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;

    }

    @Override
    protected void compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        boolean isIgnoredURL = ignoredUrls.stream().anyMatch(ignoredUrl -> ignoredUrl.matcher(url).matches());
        if (isIgnoredURL) {
            return;
        }

        synchronized (this) {
            if (visitedUrls.contains(url)) {
                return;
            } else {
                visitedUrls.add(url);
            }
        }

        PageParser.Result result = parserFactory.get(url).parse();
        synchronized (this) {
            result.getWordCounts()
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        counts.compute(entry.getKey(), (k, v) -> v == null ? entry.getValue() : v + entry.getValue());
                    });
        }

        List<WebCrawlerSynchronized> subtasks = result.getLinks().stream()
                .map(childUrl -> new WebCrawlerSynchronized(clock, childUrl, visitedUrls, deadline, maxDepth - 1, counts, ignoredUrls,  parserFactory))
                .collect(Collectors.toList());

        invokeAll(subtasks);
    }
}

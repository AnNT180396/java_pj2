package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class WebCrawlerMain {
    
    private final CrawlerConfiguration config;
    
    private WebCrawlerMain(CrawlerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }
    
    @Inject
    private WebCrawler crawler;
    
    @Inject
    private Profiler profiler;
    
    private void run() throws Exception {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);
        
        CrawlResult result = crawler.crawl(config.getStartPages());
        CrawlResultWriter resultWriter = new CrawlResultWriter(result);
        Writer writer = new OutputStreamWriter(System.out);
        // Write the crawl results to a JSON file (or System.out if the file name is empty)
        if (StringUtils.isNotBlank(config.getResultPath())) {
            Path path = Paths.get(config.getResultPath());
            resultWriter.write(path);
        } else {
            System.out.println("The Result Path is empty\n");
            resultWriter.write(writer);
            writer.flush();
            System.out.println(" \n ");
        }
        
        // Write the profile data to a text file (or System.out if the file name is empty)
        if (StringUtils.isNotBlank(config.getProfileOutputPath())) {
            Path path = Paths.get(config.getProfileOutputPath());
            profiler.writeData(path);
        } else {
            System.out.println("The Profile Output Path is empty\n");
            profiler.writeData(writer);
            writer.flush();
        }
        writer.close();
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: WebCrawlerMain [starting-url]");
            return;
        }
        
        CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
        new WebCrawlerMain(config).run();
    }
}

package steps;

import static utils.matcherUtils.textHas;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import main.BMProxy;
import main.Base;
import main.Log;
import crawler.Crawler;
import crawler.QueueHandler;
import crawler.Result;
import crawler.Config;
import java.util.Map;

public class CrawlerSteps implements Base {

    private final Crawler crawler = new Crawler(driver);
    private final QueueHandler queue = new QueueHandler(driver);
    private final Result result = new Result();
    private boolean runResultsInTeardown = true;

    @Before(value = "@crawler", order = 0)
    public void setUpCrawlerConfig() {
        Config.defaultCrawlerConfig();
    }

    @After(value = "@teardown-reset-crawler-maps", order = 1)
    public void teardownResetCrawlerMaps() {
        if (runResultsInTeardown) crawlerLogResultsAndReset();
        queue.resetMaps();
        BMProxy.resetProxyStats();
    }

    // Print full link log to report by setting 'logCrawlerLinksToReport'=true. If false, only link
    // count will appear in report. Full link logs are always logged in log file for reference.
    @Then("^output crawler log data$")
    public void crawlerLogResultsAndReset() {
        Log.info("output crawler log data");
        boolean logReportOn = (boolean) Config.crawlerConfig.get("report log");
        result.crawlerReport("crawled", QueueHandler.crawledLinks, logReportOn);
        result.crawlerReport("scraped", QueueHandler.scrapedLinks, logReportOn);
        result.crawlerReport("white listed", QueueHandler.whiteListedLinks, logReportOn);
        Log.addStepLog("", "crawler config");
        for (Map.Entry<String, Object> config : Config.crawlerConfig.entrySet()) {
            String key = config.getKey();
            String value = config.getValue().toString();
            if (!textHas(key, "local crawl only")) {
                if (textHas(value, "^false$") || (value).isEmpty()) continue;
            }
            Log.addStepLog(value, " - " + key);
        }
        runResultsInTeardown = false;
        QueueHandler.responseUrlsCheckedNoDuplicates.addAll(QueueHandler.responseUrlsChecked);
        Log.addStepLog(Integer.toString(QueueHandler.crawledLinks.size()), "Crawled Count");
        Log.addStepLog(Integer.toString(QueueHandler.scrapedLinks.size()), "Scraped Count");
        Log.addStepLog(Integer.toString(QueueHandler.whiteListedLinks.size()), "White Listed Count");
        Log.addStepLog(Integer.toString(QueueHandler.responseUrlsCheckedNoDuplicates.size()),
            "Total Unique Url Status Codes Checked");
        Log.addStepLog(Integer.toString(QueueHandler.responseUrlsChecked.size()),
            "Total Status Codes Checked");
        if (steps.fails.isEmpty()) Log.addStepLog("NO FAILS REPORTED", "final");
        steps.failHandler();
    }

    // For enabling console, you can pass in an optional search term to search within console log
    // text, instead of true/false. Example: | console warning level | Content Security Policy |
    // will search for string "Content Security Policy" in console warning logs, and report if found.
    @And("^I set crawler configs:$")
    public void setCrawlConfig(Map<String, String> configs) {
        Log.info("I set crawler configs:\n" + configs);
        for (Map.Entry<String, String> config : configs.entrySet()) {
            // if entered value is empty/null, we make it an empty string and use the default value.
            String value = config.getValue() == null ? "" : config.getValue();
            Config.updateCrawlerConfig(config.getKey(), value);
        }
    }

    // to get all sitemap.xml urls, enter "site map urls". To get only non-language set of urls,
    // enter "site map urls - quick" (saves time where the only difference is localization).
    @And("^I add \"([^\"]*)\" to crawl list$")
    public void addUrlToCrawlList(String url) {
        Log.info("I add " + url + " to crawl list");
        if (textHas(url, "site ?map url")) {
            queue.addSiteMapLinksToQueue(url);
        } else {
            queue.addUrlToQueue(url);
        }
    }

    @Then("^toggle proxy capture traffic (on|off)$")
    public void toggleProxyCapture(String onOrOff) {
        Log.info("toggle proxy capture traffic " + onOrOff);
        BMProxy.captureTraffic = textHas(onOrOff, "on");
    }

    @Then("^I run crawler from current page$")
    public void crawlPageAndValidate() {
        Log.info("I run crawler from current page");
        if (!BMProxy.captureTraffic) {
            toggleProxyCapture("on");
            page.pageRefresh();
        }
        Log.addStepLog(page.currentUrl());
        crawler.runCrawler(page.currentUrl(), (int) Config.crawlerConfig.get("total depth"));
        page.setStandardTimeoutAndResolution();
        toggleProxyCapture("off");
    }

    @And("^I run crawler from list$")
    public void crawlEnteredLinks() {
        Log.info("I run crawler from list");
        toggleProxyCapture("on");
        crawler.runCrawlerFromStoredQueue((int) Config.crawlerConfig.get("total depth"));
        page.setStandardTimeoutAndResolution();
        toggleProxyCapture("off");
    }
}
package crawler;

import static crawler.Config.crawlerConfig;
import static crawler.Config.whiteListCrawlerRegex;
import static crawler.Config.whiteListScraperRegex;
import static main.Base.coreEnv;

import crawler.validators.CoreValidator;
import main.Base;
import main.Log;
import main.Core;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;

public class Crawler extends QueueHandler {

  public Crawler(WebDriver driver) { super(driver); }

  private final CoreValidator val = new CoreValidator(driver);
  private final String siteUrl = Core.meta.get("url").replaceAll("/$", "");

  public void runCrawlerFromStoredQueue(int depthLevel) {
    crawlQueue.forEach((url, referrer) -> runCrawler(
        url, referrer, (String) crawlerConfig.get("scrape filter"), depthLevel));
    crawlQueue.clear();
  }

  public void runCrawler(String url, int depthLevel) {
    runCrawler(url, "", (String) crawlerConfig.get("scrape filter"), depthLevel);
  }

  /**
   * This is the core crawler method that combines crawler, validator and scraper together.
   * arg: String url: url to crawl, validate, and scrape (if contains siteUrl).
   * arg: String referrer: referrer url for "url" arg.
   * arg: String cssQuery: css String locator to use when scraping for further links.
   * arg: int depthRemaining: level of depth to crawl/scrape. depthRemaining=0 is no scraping.
   * Method design:
   * - Runs enabled validators via crawlerValidations method.
   *  - If depthRemaining = 0, method ends (allowing us to perform single page validations).
   *  - If depthRemaining > 0, decrement depthRemaining by 1 and enter linkScraper.
   *   - If logic true, scrapes links matching cssQuery, stores in localScrapedLinks map.
   *   - If scraped link already in crawledLinks map, will not enter new recursive crawl.
   *   - If scraped link not already in crawledLinks map, will check additional logic.
   *    - If logic true, enters recursive method call with url=newUrl and current depthRemaining.
   */
  private void runCrawler(String url, String referrer, String cssQuery, int depthRemaining) {
    setReducedTimeoutAndResolution();
    url = cleanupAndDecodeUrl(url);
    // collect white listed urls for logging output.
    if (textHas(url, whiteListCrawlerRegex)) {
      whiteListedLinks.put(url, referrer);
      return;
    }
    try {
      if (!url.equals(currentUrl())) coreEnv.open(url, 1, false);
    } catch (Exception ignored) {}
    scrollScreenToBottom();
    Log.info("Crawling url: " + url + ", remaining depth: " + depthRemaining);
    Log.info("\ttitle: " + currentTitle());
    val.crawlerValidations(url, referrer);
    crawledLinks.put(url, referrer);
    if (depthRemaining > 0) {
      depthRemaining -= 1;
      for (Map.Entry<String, String> newUrls : linkScraper(url, cssQuery).entrySet()) {
        String newUrl = newUrls.getKey();
        // if we already crawled the url, skip current newUrl and go to the next.
        if (!crawledLinks.containsKey(newUrl)) {
          // Only re-enter crawl if link contains siteUrl or if local crawl only is false.
          if (!(boolean) crawlerConfig.get("local crawl only") || textHas(newUrl, siteUrl)) {
            runCrawler(newUrl, newUrls.getValue(), cssQuery, depthRemaining);
          }
        }
      }
    }
  }

  private Map<String, String> linkScraper(String url, String cssQuery) {
    return linkScraper(url, cssQuery, true);
  }

  private Map<String, String> linkScraper(String url, String cssQuery, boolean retry) {
    if (!scrapedLinks.containsKey(url)) scrapedLinks.put(url, siteUrl);
    Map<String, String> localScrapedLinks = new LinkedHashMap<>();
    try {
      // only scrape links matching siteUrl (local) pages.
      if (textHas(url, siteUrl)) {
        for (WebElement element : getElements(By.cssSelector(cssQuery))) {
          localScrapedLinks.putAll(getHrefSrcLinks(element, url));
          localScrapedLinks.putAll(getSrcSetLinks(element, url));
        }
        localScrapedLinks.remove("");
        scrapedLinks.putAll(localScrapedLinks);
        Log.info("Newly added local scraped count: " + localScrapedLinks.size());
        Log.info("Global total scraped count: " + scrapedLinks.size());
      }
    } catch (Exception e) {
      Log.warn("Exception checking " + url + ":\n" + e);
      e.printStackTrace();
      if (retry) {
        linkScraper(url, cssQuery, false);
      } else {
        Base.steps.fails.add("****************************"
            + "\n<br />* Failed scraping content on " + url + ", " + e.toString());
      }
    }
    return localScrapedLinks;
  }

  // customized for links scraped that have certain redirect code in them. Without this cleanup,
  // the links are unusable in a crawler/ping format.
  public String cleanupAndDecodeUrl(String url) {
    if (url != null) {
      if (textHas(url, "/redirect?url=")) {
        url = url.contains("=http") ? url.replaceAll(".*?/redirect\\?(url|rsrc)=", "")
            : url.replaceAll("redirect\\?(url|rsrc)=", "");
      }  else if (textHas(url, "bat.bing.com/action/0?")) {
        url = url.replaceAll(".*?=http", "http").replaceAll("&[A-z0-9&<=]+$", "");
      } else if (textHas(url, "googleads.g.doubleclick.net/pagead/viewthroughconversion/763159290")) {
        url = url.replaceAll(".*?=http", "http").replaceAll("&tiba.*?$", "");
      }
      // Below regex removes in-page jump links to reduce duplicate crawls since
      // they use same base url. Exception for countly event links.
      if (!textHas(url, "\\?events=")) {
        url = url.replaceAll("([#?])[=#&0-9A-z\\-]*$", "");
      }
      url = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
    } else url = "";
    return url;
  }

  private Map<String, String> getHrefSrcLinks(WebElement element, String referrerUrl) {
    Map<String, String> hrefSrcMap =  new LinkedHashMap<>();
    String href = cleanupAndDecodeUrl(getMyAttribute(element, "href"));
    String src = cleanupAndDecodeUrl(getMyAttribute(element, "src"));
    if (!textHas(href, whiteListScraperRegex)) {
      if (!scrapedLinks.containsKey(href)) hrefSrcMap.put(href, referrerUrl);
      if (!scrapedLinks.containsKey(src)) hrefSrcMap.put(src, referrerUrl);
    }
    return hrefSrcMap;
  }

  private Map<String, String> getSrcSetLinks(WebElement element, String referrerUrl) {
    Map<String, String> srcSetMap =  new LinkedHashMap<>();
    // Parse pattern: link1 123w, link2 1234w
    String[] parseSetLinks =
        getMyAttribute(element,  "srcset").replaceAll(" [0-9]+w", "").split(", ?");
    for (String link : parseSetLinks) {
      if (!scrapedLinks.containsKey(link)) {
        srcSetMap.put(cleanupAndDecodeUrl(
            textHas(link, "http") ? link : siteUrl + link), referrerUrl);
      }
    }
    return srcSetMap;
  }
}

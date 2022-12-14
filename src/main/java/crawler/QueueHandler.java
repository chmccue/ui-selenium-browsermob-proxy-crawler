package crawler;

import static main.Base.coreEnv;
import static main.Core.siteUrl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import main.Base;
import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.matcherUtils;

public class QueueHandler extends matcherUtils {

  public QueueHandler(WebDriver driver) { super(driver); }

  // key=child url (unique), value=referrer url
  public static LinkedHashMap<String, String> scrapedLinks = new LinkedHashMap<>();
  public static LinkedHashMap<String, String> crawledLinks = new LinkedHashMap<>();
  public static LinkedHashMap<String, String> whiteListedLinks = new LinkedHashMap<>();
  static LinkedHashMap<String, String> crawlQueue = new LinkedHashMap<>();
  public static List<String> responseUrlsChecked = new ArrayList<>();
  public static HashSet<String> responseUrlsCheckedNoDuplicates = new HashSet<>();

  public void addUrlToQueue(String url) {
    if (!textHas(url, "http")) {
      url = combineSiteUrlAndRelativeUrl(url);
      if (url.replace(" ", "").equalsIgnoreCase("siteurl")) url = "";
      Log.info("Url Added to validator list: " + url);
    }
    crawlQueue.put(url, siteUrl);
  }

  public void resetMaps() {
    crawlQueue.clear();
    crawledLinks.clear();
    scrapedLinks.clear();
    responseUrlsChecked.clear();
    responseUrlsCheckedNoDuplicates.clear();
  }

  public void addSiteMapLinksToQueue(String url) {
    // To get only non-language set of urls, enter "site map urls - quick".
    String ignoreFilter = textHas(url, "quick") ? "/(en-|ja-|es-|zh-|fr-|tr-|ru-|pt-|it-)" : "";

    // Get count of crawlQueue before sitemap crawl, to subtract from after sitemap crawl.
    int queueBeforeSiteMap = crawlQueue.size();
    String siteMapUrl = siteUrl + "/sitemap.xml";
    try {
      coreEnv.open(siteMapUrl, 0, false);
    } catch (Exception ignore) {}

    String textItem;
    if (!waitForElement(By.cssSelector(".html-tag"), 5)) {
      Base.steps.fails.add("Issue loading the sitemap. Check manually: " + siteMapUrl);
    }
    for (WebElement element : getElements(By.cssSelector(".text"))) {
      try {
        textItem = getElementText(element).replace("https://www.company.com", siteUrl);
        // CM 3/30/2020: remove below logic for features/ not matched when BUG-1234 resolved
        if (textHas(textItem, siteUrl) && !textHas(textItem, "features/")
        && (!textHas(textItem, ignoreFilter) || ignoreFilter.isEmpty())) {
          addUrlToQueue(textItem);
        }
      } catch (NullPointerException ignored) {}
    }
    int queueAfterSiteMap = crawlQueue.size() - queueBeforeSiteMap;
    Log.addStepLog(Integer.toString(queueAfterSiteMap), "Items added to queue from sitemap.xml");
  }
}

package utils;

import main.Driver;
import main.Log;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static main.Core.siteUrl;
import static utils.matcherUtils.textHas;

public class helperUtils extends Driver {

  helperUtils(WebDriver driver) { super(driver); }

  WebDriverWait wait(double waitTime) {
    return new WebDriverWait(driver, Duration.ofSeconds((long) waitTime));
  }

  public void pageRefresh() {
    try {
      driver.navigate().refresh();
      waitForPageReload(5);
    } catch (WebDriverException ignored) {}
  }

  public String currentUrl() {
    try {
      return driver.getCurrentUrl().replaceAll("://.*?@", "://");
    } catch (Exception e) {
      Log.warn("Exception encountered during get currentUrl: " + e.toString());
      return e.toString();
    }
  }

  public String currentTitle() {
    try {
      return driver.getTitle();
    } catch (Exception e) {
      Log.warn("Exception encountered during get currentTitle: " + e.toString());
      return e.toString();
    }
  }

  public boolean waitForPageReload(double timeout) {
    try {
      timeout = Math.max(timeout, Driver.maxWaitSeconds);
      wait(timeout).until(webDriver -> ((JavascriptExecutor) webDriver)
          .executeScript("return document.readyState").equals("complete"));
    } catch (Exception e) {
      Log.warn("Error waiting for page to reload: " + e);
      return false;
    }
    return true;
  }

  public void scrollScreenToBottom() {
    Long pageSize = (Long) ((JavascriptExecutor) driver).executeScript(
        "return document.documentElement.scrollHeight");
    // if pageSize is equal to or smaller than current browser height, we exit.
    if (driver.manage().window().getSize().getHeight() > pageSize) return;
    long scrollTo = pageSize / 2;
    while (scrollTo < pageSize + 1) {
      ((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + scrollTo + ")");
      scrollTo = scrollTo * 2;
      hardWait(0.07);
    }
  }

  // Uses seconds arg, not milliseconds: 0.1 s = 100 ms, 1 s = 1000 ms, etc.
  public void hardWait(double seconds) {
    try { Thread.sleep(Math.round(seconds * 1000)); } catch (Exception ignored) {}
  }

  protected int getRandomNum(int rangeStart, int rangeEnd, int max) {
    if (rangeEnd > max) rangeEnd = max;
    if (rangeStart == rangeEnd) return rangeStart;
    return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd);
  }

  public String combineSiteUrlAndRelativeUrl(String relativeUrl) {
    // if we enter a url that starts with http, we simply return that.
    if (textHas(relativeUrl, "^http")) return relativeUrl;
    // we ensure there is not double "//" connecting siteUrl and relativeUrl
    if (textHas(siteUrl, "/$") && textHas(relativeUrl, "^/")) {
      relativeUrl = relativeUrl.replaceFirst("/", "");
    }
    return siteUrl + relativeUrl;
  }
}

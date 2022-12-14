package main;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import static main.BMProxy.proxy;
import static utils.matcherUtils.textHas;

public class Core extends Driver {
  public static Map<String, String> meta = new HashMap<>();
  public static boolean remoteTest = false;
  public static String siteUrl;
  private final static String prodEnvs = "www";

  public Core(WebDriver driver) { super(driver); }

  /**
   * The meta data structure
   */
  public static void loadMetaData() {
    if (meta.isEmpty()) {
      try {
        Log.info("Loading metaData");

        Properties properties = new Properties();
        Properties systemProperties = System.getProperties();
        Map<String, String> env = System.getenv();

        meta.put("url", env.get("SITE_URL"));

        // selenium configuration
        meta.put("driver.type", env.get("SELENIUM_DRIVER_TYPE"));
        meta.put("remote.url", env.get("SELENIUM_REMOTE_URL"));
        remoteTest = textHas(env.get("SELENIUM_DRIVER_TYPE"), "remote");
        meta.put("browser.type", env.get("SELENIUM_BROWSER_TYPE"));
        meta.put("browser.version", env.get("SELENIUM_BROWSER_VERSION"));

        // get properties file
        ClassLoader myCl = Core.class.getClassLoader();
        properties.load(myCl.getResourceAsStream("environment.properties"));

        // override properties file with system properties
        for (Map.Entry<Object, Object> e : systemProperties.entrySet())
          properties.setProperty(e.getKey().toString(), e.getValue().toString());
        meta.put("env", properties.getProperty("environment"));
        Log.info("Finished loading metaData");

      } catch (IOException e) {
        Log.error(e.getMessage());
      }
    }
  }

  public void open() throws Exception {
    setStandardTimeoutAndResolution();
    open(3);
  }

  public void open(int retry) throws Exception {
    // Note: we use meta.get("url") instead of siteUrl to ensure any site authentication is entered
    open(meta.get("url"), retry, true);
  }

  public void open(String openUrl, int retry, boolean logToReport) throws Exception {
    if (openUrl == null) {
      Log.info("URL not set in Environment. Open browser will fail!");
      throw new Exception("Url not set in Environment");
    }
    try {
      try {
        driver.get(openUrl);
      } catch (org.openqa.selenium.NoSuchSessionException e) {
        driver = Driver.startDriver();
        driver.get(openUrl);
      } catch (TimeoutException e) {
        Log.warn(e.toString());
      }
      // Below replaces any entered site authentication so we do not log username/password
      openUrl = openUrl.replaceAll("://.*?@", "://");
      String openMsg = "Open Browser to: " + openUrl;
      if (textHas(openUrl, "https://" + prodEnvs)) {
        driver.get(openUrl); // re-open the cleaned url to remove credentials in logging
      }
      if (logToReport) Log.addStepLog(openMsg); else Log.info(openMsg);
      // If proxy is on, there is an occasional issue loading page and needs to be refreshed
      if (driver.getPageSource().contains("An error occurred loading this page's template")) {
        driver.navigate().refresh();
      }
    } catch (Exception e) {
      Log.error("Caught error opening browser: " + e.toString());
      if (retry > 0) {
        Log.warn("Retrying open browser after driver error");
        retry -= 1;
        open(openUrl, retry, logToReport);
      }
    }
  }

  public void switchToPage(String name) {
    Log.info("Switch to page: " + name);
    try {
      for (String window : driver.getWindowHandles()) {
        driver.switchTo().window(window);
        Log.info("Current window URL: " + driver.getCurrentUrl().replaceAll("://.*?@", "://"));
        if ((driver.getCurrentUrl().contains(siteUrl) && name.equals("Home"))) {
          Log.info("Found page: " + name);
          return;
        }
      }
      Log.info("Page not found: " + name);
    } catch (Exception e) {
      Log.info("Error : " + e + " Switch to " + name + " failed");
    }
  }

  public void closeBrowser() {
    Log.info("Closing Browser.");
    if (proxy.isStarted()) proxy.stop();
    if (!driver.toString().contains("null")) try { driver.quit(); } catch (Exception ignored) {}
  }
}

package main;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import static main.Core.meta;
import static main.Core.remoteTest;

public class Driver {
  protected static WebDriver driver;
  protected static Integer maxWaitSeconds = 0;
  public static String parentWindow;

  public Driver(WebDriver driver) { Driver.driver = driver; }

  public WebDriver getDriver() { return driver; }

  @SuppressWarnings("unchecked")
  static WebDriver startDriver() {
    Core.loadMetaData();
    String os = System.getProperty("os.name");
    Log.info("Starting driver for " + os);

    Class driverClass = null;
    Map<String, Object> prefs = new HashMap<>();
    ChromeOptions chromeOptions = new ChromeOptions();

    DesiredCapabilities caps = new DesiredCapabilities();
    if (meta.get("browser.version") != null) {
      caps.setVersion(meta.get("browser.version"));
    }
    caps.setPlatform(Platform.ANY);
    caps.setAcceptInsecureCerts(true);

    BMProxy.startProxy();
    caps.setCapability(CapabilityType.PROXY, BMProxy.seleniumProxy);
    caps.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

    String browser = meta.get("browser.type");
    String driverType = meta.get("driver.type");
    Log.info("Running with browser " + browser);
    if (browser.toLowerCase().contains("chrome")) {
      caps.setBrowserName("Chrome");
      if (driverType.equalsIgnoreCase("local")) {
        WebDriverManager.chromedriver();
      }
      chromeOptions.addArguments("--no-sandbox", "--verbose", "--allow-running-insecure-content");
      if (browser.toLowerCase().contains("headless")) {
        chromeOptions.addArguments("--headless", "--disable-gpu", "--window-size=1210,1000");
      } else { // assumes non-headless mode
        chromeOptions.addArguments("--window-size=1600,1200");
      }
      prefs.put("credentials_enable_service", false);
      prefs.put("profile.password_manager_enabled", false);
      prefs.put("profile.default_content_setting_values.media_stream_camera", 1);
      prefs.put("download_restrictions", 3);
      chromeOptions.setExperimentalOption("prefs", prefs);
      caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
      driverClass = ChromeDriver.class;
    } else {
      Log.error("Invalid 'browser' value: '" + browser + "'");
      System.exit(1);
    }

    if (driverType.equalsIgnoreCase("remote")) {
      // instantiate a remote web driver
      try {
        Log.info("Creating RemoteWebDriver with hub URL " + meta.get("remote.url"));
        driver = new RemoteWebDriver(new URL(meta.get("remote.url")), caps);
      } catch (MalformedURLException e) {
        Log.error("The selenium remote URL is not valid: " + meta.get("remote.url"));
        System.exit(1);
      }
    } else {
      // instantiate a local browser driver
      try {
        Log.info("Creating local browser driver for " + browser);
        driver =
            (WebDriver) driverClass.getDeclaredConstructor(Capabilities.class).newInstance(caps);
      } catch (ReflectiveOperationException roe) {
        roe.printStackTrace();
        System.exit(1);
      }
    }

    setMaxWaitSeconds();
    Log.info("Driver successfully created. Browser:" + browser
        + " Driver type:" + meta.get("driver.type") + " Operating System: " + os
        + " Environment: " + meta.get("env") + " Max Wait (seconds): " + maxWaitSeconds);

    parentWindow = driver.getWindowHandle();
    return driver;
  }

  private static void setMaxWaitSeconds() {
    try {
      // maximum number of seconds that the selenium web driver should wait for DOM elements to meet conditions
      maxWaitSeconds = Integer.parseInt(meta.get("maxwait"));
      if (maxWaitSeconds < 0 || maxWaitSeconds > 600)
        throw new NumberFormatException("Invalid wait range");
    } catch (NumberFormatException nfe) {
      Log.info("The selenium max wait should be between [0, 600]. Defaulting to zero to disable it.");
      maxWaitSeconds = 0;
    }
  }

  public static void embedScreenshot(Scenario scenario) {
    try {
      byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      scenario.attach(screenshot, "image/png", "screenshot");
    } catch (WebDriverException wde) {
      System.err.println(wde.getMessage());
    } catch (ClassCastException cce) {
      cce.printStackTrace();
    }
  }

  public void setStandardTimeoutAndResolution() {
    if (remoteTest) {
      setResolution(1210, 1000);
    } else {
      setResolution(1600, 1200);
    }
    setPageTimeout(45);
  }

  protected void setReducedTimeoutAndResolution() {
    if (remoteTest) setResolution(800, 500);
    setPageTimeout(5);
  }

  private void setResolution(int x, int y) {
    int resWidth = browserWidth();
    int resHeight = driver.manage().window().getSize().getHeight();
    if (x != resWidth || y != resHeight) {
      driver.manage().window().setSize(new Dimension(x, y));
      logBrowserResolution();
    }
  }

  protected int browserWidth() {
    return driver.manage().window().getSize().getWidth();
  }

  private void logBrowserResolution() {
    Log.addStepLog("Browser Resolution: " + driver.manage().window().getSize());
  }

  private void setPageTimeout(int timeout) {
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
  }
}
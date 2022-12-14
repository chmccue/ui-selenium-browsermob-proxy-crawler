package utils;

import crawler.Config;
import main.Core;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class consoleUtils extends matcherUtils {

  public consoleUtils(WebDriver driver) { super(driver); }

  private final List<String> whiteListConsole = Arrays.asList("source list for Content "
          + "Security Policy directive 'frame-ancestors' contains an invalid source",
      "https://p.adsymptotic.com/d/px/");

  // Relies on updateCrawlerConfig method to set a console trigger var to true and console search
  // var. If console trigger vars are false, returns empty List.
  public Set<String> searchConsoleWithCrawlerValidators() {
    Set<String> localFails = new HashSet<>();
    String consoleSearch = (String) Config.crawlerConfig.get("console search");
    if ((boolean) Config.crawlerConfig.get("test all console output")) {
      localFails = searchConsoleOutput("(severe|warning)", consoleSearch);
    } else if ((boolean) Config.crawlerConfig.get("test severe console output")) {
      localFails = searchConsoleOutput("severe", consoleSearch);
    } else if ((boolean) Config.crawlerConfig.get("test warning console output")) {
      localFails = searchConsoleOutput("warning", consoleSearch);
    }
    return localFails;
  }

  // Chrome Console log retrieval. Search by console log level and message.
  // To only search by log level, enter consoleSearch="".
  private Set<String> searchConsoleOutput(String consoleLevel, String consoleSearch) {
    Set<String> localFails = new HashSet<>();
    String url;
    for (LogEntry consoleLog : driver.manage().logs().get("browser")) {
      if (textHas(consoleLog.getLevel().toString(), consoleLevel)
          && textHas(consoleLog.getMessage(), consoleSearch)) {
        url = currentUrl();
        if (textHas(url, Core.siteUrl)) {
          // if output is not found in whitelist, it gets added to localFails list to be reported.
          if (whiteListConsole.stream().noneMatch(consoleLog.getMessage()::contains)) {
            localFails.add("Console Output: " + consoleLog.getLevel() + ": "
                + consoleLog.getMessage() + "<b> => from: " + url + "</b>");
          }
        }
      }
    }
    return localFails;
  }
}

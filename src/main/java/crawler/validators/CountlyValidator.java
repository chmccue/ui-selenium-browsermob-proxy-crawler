package crawler.validators;

import crawler.Config;
import crawler.Crawler;
import main.BMProxy;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static main.Core.siteUrl;

public class CountlyValidator extends JsValidator {

  public CountlyValidator(WebDriver driver) { super(driver); }

  public List<String> countlyEventValidation(int retry) {
    List<String> localFails = new ArrayList<>();
    if ((boolean) Config.crawlerConfig.get("test countly")) {
      // We only want to check Countly on local pages that are customer facing.
      if (textHas(currentUrl(), siteUrl) && !textHas(currentUrl(), Config.whiteListCspRegex)) {
        if ((BMProxy.responseCountly == null || BMProxy.responseCountly.isEmpty()) && retry > 0) {
          hardWait(0.5);
          return countlyEventValidation(retry - 1);
        } else if ((BMProxy.responseCountly == null
            || BMProxy.responseCountly.isEmpty()) && retry == 0) {
          localFails.add("Countly Violation: No event found on page");
          return localFails;
        }
        String countlyUrl = new Crawler(driver).cleanupAndDecodeUrl(BMProxy.responseCountly)
            .replaceAll("\"", "");
        Map<String, String> countlyEvent = buildCountlyMap();
        boolean checkMatch;
        for (Map.Entry<String, String> item : countlyEvent.entrySet()) {
          String parsed = regexParse(countlyUrl, item.getKey() + ".*?([,&}])")
              .replaceAll(item.getKey() + "([:=])", "").replaceAll("([,&}])$", "");
          if (textHas(item.getKey(), "name")) {
            checkMatch = textHas(parsed, "^/$")
                ? textHas(currentUrl() + "$", siteUrl) : textHas(item.getValue(), parsed);
            if (!checkMatch && retry > 0) {
              hardWait(0.5);
              return countlyEventValidation(retry - 1);
            }
          } else {
            checkMatch = textHas(item.getValue(), "^not empty$")
                ? parsed.length() > 0 : textHas(parsed, item.getValue());
          }
          if (!checkMatch) {
            localFails.add("Countly Violation: " + item.getKey() + ": " + parsed
                + "\n<br />Countly url: " + countlyUrl);
          }
        }
      }
    }
    BMProxy.responseCountly = "";
    return localFails;
  }

  private Map<String, String> buildCountlyMap() {
    Map<String, String> countlyEvent = new LinkedHashMap<>();
    countlyEvent.put("name", currentUrl());
    countlyEvent.put("app_key", "not empty");
    countlyEvent.put("device_id", "not empty");
    countlyEvent.put("sdk_name", "javascript_native_web");
    countlyEvent.put("sdk_version", "20.04");
    countlyEvent.put("timestamp", "\\d{1,}");
    countlyEvent.put("hour", "\\d\\d?");
    countlyEvent.put("dow", "\\d");
    return countlyEvent;
  }
}
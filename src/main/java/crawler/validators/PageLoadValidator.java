package crawler.validators;

import static main.Core.siteUrl;

import crawler.Config;
import crawler.QueueHandler;
import main.BMProxy;
import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.consoleUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

class PageLoadValidator extends consoleUtils {

  PageLoadValidator(WebDriver driver) { super(driver); }

  String currentProxyUrl;

  // Testing main page document status code, which tests both local and non-local redirects.
  // This also sets variable "currentProxyUrl" that is used in crawlerValidations method for fails.
  private List<String> checkMainDocumentStatusCodes() {
    List<String> localFails = new ArrayList<>();
    StringBuilder redirectPath = new StringBuilder();
    StringBuilder spacing = new StringBuilder();
    for (Map.Entry<String, String> currentPage : BMProxy.currentPagePath.entrySet()) {
      // captcha logic handling to ensure it only gets validated if we hit captcha url directly
      if ((!textHas(currentUrl(), "captcha/") && !textHas(currentPage.getKey(), "captcha/"))
          || textHas(currentUrl(), "captcha/")) {
        redirectPath.append(spacing.append("--")).append("> ").append(currentPage.getValue())
            .append(": ").append(currentPage.getKey()).append("<br />\n");
      }
      if (!textHas(currentPage.getValue(), "(1|2|3)0")) {
        if (textHas(currentPage.getKey(), Config.whiteListStatusCodesRegex)) {
          QueueHandler.whiteListedLinks.put(currentPage.getKey(), currentUrl());
        } else {
          localFails.add("Reported status code error found loading main page document:<br />\n"
              + redirectPath);
          break;
        }
      }
    }
    Log.info("\nProxy path:\n" + redirectPath);
    currentProxyUrl = redirectPath.toString();
    return localFails;
  }

  // Checks status codes of page assets. Method also compiles check response url list for output
  // in end report. Manually, you can see these assets in Browser Developer Tools > Network tab.
  List<String> checkPageAssetStatusCodes() {
    List<String> localFails = new ArrayList<>();
    if (textHas(currentUrl(), siteUrl)) {
      QueueHandler.responseUrlsChecked.addAll(BMProxy.responseUrlsCaptured);
      if (!BMProxy.responseBadStatusCodes.isEmpty()) {
        for (String fail : BMProxy.responseBadStatusCodes) {
          if (textHas(fail, Config.whiteListStatusCodesRegex)) {
            QueueHandler.whiteListedLinks.put(fail, currentUrl());
          } else {
            localFails.add("Reported status code error found. Check network tab:<br />\n" + fail);
          }
        }
      }
    } else {
      QueueHandler.responseUrlsChecked.addAll(BMProxy.currentPagePath.keySet());
    }
    BMProxy.responseUrlsCaptured.clear();
    BMProxy.responseBadStatusCodes.clear();
    return localFails;
  }

  List<String> badPageLoadValidations() {
    List<String> localFails = new ArrayList<>();
    if ((boolean) Config.crawlerConfig.get("test page load")) {
      String nonEmptyBody = "(?s)body.*?>.+<\\/body>";
      if (!textHas(driver.getPageSource(), nonEmptyBody)) {
        pageRefresh();
        if (!textHas(driver.getPageSource(), nonEmptyBody)) {
          localFails.add("Blank page load error. Nothing found loaded within html body.");
        }
      }
      localFails.addAll(checkMainDocumentStatusCodes());
      if (elementHas(By.cssSelector("body"), "^Invalid Request")
          || elementHas(By.cssSelector("body"),
          "As a security precaution, you must manually navigate to all account pages")) {
        localFails.add("Invalid Request page encountered. This only occurs on select logged in"
            + " pages as a security precaution. Ensure a referrer is set.");
      }
      String templateErrorMsg = "^An error occurred loading this page's template. "
          + "More information is available in the console\\.$";
      if (elementHas(By.cssSelector("body"), templateErrorMsg)) {
        localFails.add("Template page load error: " + templateErrorMsg);
      }
    }
    return localFails;
  }
}

package crawler.validators;

import crawler.Config;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.ArrayList;

class JsValidator extends CspValidator {

  JsValidator(WebDriver driver) { super(driver); }

  List<String> jsScreener() {
    List<String> localFails = new ArrayList<>();
    if ((boolean) Config.crawlerConfig.get("test js screener")) {
      if (textHas(currentUrl(), "\\.js$")) {
        // Looks for possible user comments.
        if (textHas(driver.getPageSource(), "\\/Users\\/")) {
          localFails.add("Possible comment violation: Found '/Users/' in JS");
        }
      }
    }
    return localFails;
  }
}

package crawler.validators;

import static main.Core.siteUrl;

import crawler.Config;
import main.BMProxy;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class CspValidator extends PageLoadValidator {

  CspValidator(WebDriver driver) { super(driver); }

  List<String> responseCspHeadersValidation() {
    List<String> localFails = new ArrayList<>();
    if ((boolean) Config.crawlerConfig.get("test csp headers")) {
      Map<String, String> cspHeaders = new HashMap<>();
      final String cspExpected = "default-src '(none|self)'.*?;style-src 'self'.*?;font-src.*?;"
          + "script-src 'self'.*?;frame-ancestors.*?;";
      cspHeaders.put("Content-Security-Policy", cspExpected);
      cspHeaders.put("X-Content-Security-Policy", cspExpected);
      cspHeaders.put("X-Frame-Options", "sameorigin");
      cspHeaders.put("X-Content-Type-Options", "nosniff");
      cspHeaders.put("X-XSS-Protection", "1;mode=block");
      // We only want to check CSP on local pages that are customer facing.
      if (textHas(currentUrl(), siteUrl) && !textHas(currentUrl(), Config.whiteListCspRegex)) {
        String proxyUrl = BMProxy.localResponseHeaders.get("responseUrl");
        if (!textHas(currentUrl(), proxyUrl)) {
          localFails.add("CSP proxy url did not match current page url. This needs to match in "
              + "order to validate CSP on correct page.\n<br />Proxy Url: " + proxyUrl);
        } else {
          String responseHeader;
          String expected;
          for (Map.Entry<String, String> csp : cspHeaders.entrySet()) {
            responseHeader = getResponseHeaderSection(csp.getKey()).replaceAll("; ", ";");
            expected = csp.getKey() + "=" + csp.getValue();
            if (!regexMatch(responseHeader, expected)) {
              localFails.add("Header Violation: " + csp.getKey() + ".\n<br /><b>Expected:</b> "
                  + expected + "\n<br /><b>  Actual:</b> " + responseHeader);
            }
          }
        }
      }
    }
    return localFails;
  }

  private String getResponseHeaderSection(String headerName) {
    return regexParse(BMProxy.localResponseHeaders.get("responseHeaders"),
        "(?i)(\\[| )" + headerName + "=.*?,");
  }
}

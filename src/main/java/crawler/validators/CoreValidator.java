package crawler.validators;

import main.BMProxy;
import main.Base;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.ArrayList;

public class CoreValidator extends CountlyValidator {

  public CoreValidator(WebDriver driver) { super(driver); }

  public void crawlerValidations(String url, String referrer) {
    List<String> localFailList = new ArrayList<>();
    try {
      localFailList.addAll(badPageLoadValidations());
      localFailList.addAll(searchConsoleWithCrawlerValidators());
      localFailList.addAll(responseCspHeadersValidation());
      localFailList.addAll(jsScreener());
      localFailList.addAll(checkPageAssetStatusCodes());
      localFailList.addAll(countlyEventValidation(14));
    } catch (NoSuchSessionException e) {
      localFailList.add(e.toString());
    }
    BMProxy.currentPagePath.clear();
    // Compiles fail list and runs them through global failHandler method.
    if (localFailList.size() > 0) {
      String proxyUrl = "\n<br />* Proxy Url: &emsp;&emsp;&nbsp;\n<br />" + currentProxyUrl;
      StringBuilder failMsg = new StringBuilder("****************************"
          + "\n<br />* Referrer Url: &ensp;&nbsp;&nbsp;" + referrer
          + "\n<br />* <b>Request Url: &ensp;" + url + "</b>"
          + "\n<br />* <b>Response Url: " + (currentUrl()) + "</b>"
          + proxyUrl
          + "\n<br />* Response Title: " + currentTitle());
      for (String failDetail : localFailList) {
        failMsg.append(
            "\n<br />----------------------------\n<br /> ---->>>>> ").append(failDetail);
      }
      // Must use Base fails list to connect fails to Base failHandler method.
      Base.steps.fails.add(String.valueOf(failMsg));
    }
  }
}
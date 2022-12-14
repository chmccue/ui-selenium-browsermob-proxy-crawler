package steps;

import static main.Driver.embedScreenshot;

import io.cucumber.java.Scenario;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import main.Base;
import main.Log;
import main.Core;

public class Hooks implements Base {

  private static boolean shutDownHookActive = false;

  @Before(order = 0)
  public void setUp(Scenario scenario) throws Exception {
    Core.loadMetaData();
    Log.info("Starting Feature: Scenario: " + scenario.getId());
    Log.currentScenario = scenario;
    Log.addStepLog("Test Environment Details...");
    // Below replaces any entered site authentication so we do not log username/password
    Core.siteUrl = Core.meta.get("url").replaceAll("://.*?@", "://");
    Log.addStepLog("Base Url: " + Core.siteUrl);
    Log.addStepLog("Java Version: " + System.getProperty("java.version"));
    Log.addStepLog("Browser Type: " + Core.meta.get("browser.type"));
    coreEnv.open();
    Log.addStepLog("Browser User Agent: " + ((org.openqa.selenium.JavascriptExecutor) driver)
        .executeScript("return navigator.userAgent;"));
  }

  @After(order = 5)
  public void teardownReporting(Scenario scenario) {
    Log.info("Finishing Scenario with Status: " + scenario.getStatus());
    if (scenario.isFailed()) {
      embedScreenshot(scenario);
    }
  }

  @After(order = 0)
  public void teardownFinalStep(Scenario scenario) {
    if (scenario.getSourceTagNames().contains("@teardown-close-browser")) {
      coreEnv.closeBrowser();
    }
    Core.meta.clear();
    Runtime.getRuntime().gc();
    // after final test scenario, browser is closed
    if (!shutDownHookActive) {
      Runtime.getRuntime().addShutdownHook(new Thread(coreEnv::closeBrowser));
      shutDownHookActive = true;
    }
    Log.info("\n\n\n\n<----------------*****[SCENARIO END]*****---------------->\n\n\n");
  }

  @Given("^I switch tab control to \"([^\"]*)\" site$")
  public void switchToCompany(String site) {
    Log.info("I switch to " + site + " site");
    coreEnv.switchToPage("Home");
    page.pageRefresh();
  }
}

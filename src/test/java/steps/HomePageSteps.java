package steps;

import org.junit.Assert;
import io.cucumber.java.en.Given;
import main.Base;
import main.Log;

public class HomePageSteps implements Base {

  @Given("^I have an open browser on test site$")
  public void gotoTestSite() {
    Log.info("I have an open browser on test site");
    coreEnv.switchToPage("Home");
    Log.info("Home Title: " + page.currentTitle());
    Assert.assertTrue(page.regexMatch(page.currentTitle(), "Test"));
  }
}

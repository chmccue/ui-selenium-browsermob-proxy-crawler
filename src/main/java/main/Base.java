package main;

import org.openqa.selenium.WebDriver;
import utils.matcherUtils;
import utils.stepUtils;

public interface Base {
  WebDriver driver = Driver.startDriver();
  Core coreEnv = new Core(driver);
  matcherUtils page = new matcherUtils(driver);
  stepUtils steps = new stepUtils();
}

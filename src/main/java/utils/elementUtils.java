package utils;

import main.Driver;
import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class elementUtils extends helperUtils {

  protected elementUtils(WebDriver driver) {
    super(driver);
  }

  // Wait to be visible and clickable. Use when expected result is true.
  // If defined, this method will use max wait seconds variable to overwrite set wait time.
  public boolean waitForElement(By locator, double timeout) {
    return waitForElement(locator, Math.max(timeout, Driver.maxWaitSeconds), true, 0);
  }

  public boolean waitForElement(By locator, double timeout, boolean logger, int retry) {
    try {
      wait(timeout).until(ExpectedConditions.and(ExpectedConditions
          .visibilityOfElementLocated(locator), ExpectedConditions.elementToBeClickable(locator)));
    } catch (Exception e) {
      if (logger) {
        Log.warn("Locator '" + locator.toString() + "' not found in '" + timeout
            + "' second(s) on page " + currentUrl());
        Log.warn("Exception caught during wait: " + e.toString());
      }
      if (retry > 0) {
        retry -= 1;
        Log.warn("Reloading page and rechecking");
        pageRefresh();
        return waitForElement(locator, timeout, true, retry);
      }
      return false;
    }
    return true;
  }

  // gets attribute value from locator. Handles null pointer exception.
  protected String getMyAttribute(WebElement locator, String attribute) {
    String attr = "";
    try {
      if (locator.getAttribute(attribute) != null) {
        attr = locator.getAttribute(attribute);
      }
    } catch (NullPointerException ignored) {
    }
    return attr;
  }

  public String getElementText(WebElement element) {
    try {
      if (element.getText().isEmpty())
        return element.getAttribute("value");
      return element.getText();
    } catch (Exception e) {
      Log.warn("Exception: " + e + " getting text on : " + element.toString());
      return "";
    }
  }

  // Gets all the elements identified by "locator" and returns WebElement list.
  protected List<WebElement> getElements(By locator) {
    try {
      return driver.findElements(locator);
    } catch (WebDriverException e) {
      return null;
    }
  }
}

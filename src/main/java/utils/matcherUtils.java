package utils;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class matcherUtils extends elementUtils {

  public matcherUtils(WebDriver driver) { super(driver); }

  protected String regexParse(String textToSearch, String regexPattern) {
    Pattern pattern = Pattern.compile(regexPattern);
    Matcher matcher = pattern.matcher(textToSearch);
    return matcher.find() ? matcher.group() : "";
  }

  // use for passive or logic checking, when you don't need a mismatch to be logged.
  public static boolean textHas(String actualText, String expectedText) {
    return regexMatch(actualText, expectedText, false);
  }

  public boolean elementHas(By locator, String expectedText) {
    Map<String, String> matchers = new HashMap<>();
    matchers.put(expectedText, "text");
    return getElementMatch(locator, matchers, 1, false) != null;
  }

  // use for return or assert matching, when you need a mismatch to be reported in the log.
  public boolean regexMatch(String actualText, String expectedText) {
    return regexMatch(actualText, expectedText, true);
  }

  /**
   * locator: main element to search. Will traverse all matching page elements until match is found.
   * matchers: HashMap that takes 2 Strings. Key is unique text/attribute value to search for,
   * value is attribute type ("text" if searching for text. Other examples to use: "class", "id").
   * matchCount: Number of webelements to match before returning element.
   * matchCount=1 checks 1 match. matchCount="all" checks matchers are in all elements of locator.
   * matchCount=0 checks for no match. Expected return value is null since no elements match. If
   * match is found when matchCount=0, a fail message is logged and matching element is returned.
   */
  protected WebElement getElementMatch(
      By locator, Map<String,String> matchers, Object matchCount, boolean logger) {
    waitForElement(By.cssSelector("html body"), 5);
    Map<String, String> foundMatches = new HashMap<>();
    List<WebElement> elementList = getElements(locator);
    if (elementList == null || elementList.isEmpty()) {
      Log.addStepLog("Element not found: " + locator, "error");
      return null;
    }
    int elementCount = 0;
    for (WebElement element : elementList) {
      for (Map.Entry<String, String> entry : matchers.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key.isEmpty() && matchCount.hashCode() == 0) continue;
        if (textHas(value, "text")) {
          if (textHas(getElementText(element), key)) {
            foundMatches.put(key, value);
          }
        } else {
          try {
            if (textHas(element.getAttribute(value), key)) {
              foundMatches.put(key, value);
              continue;
            }
            // if match not found in main element, will traverse sub elements w/ entered attribute
            for (WebElement sub :
                element.findElements(By.cssSelector("[" + value + "]"))) {
              if (textHas(sub.getAttribute(value), key)) {
                foundMatches.put(key, value);
                break;
              }
            }
          } catch (NullPointerException e) {
            Log.warn(value + " attribute not found in element: " + e.getMessage());
          }
        }
      }
      if (foundMatches.size() == matchers.size()) {
        elementCount += 1;
        if ((matchCount.toString().equalsIgnoreCase("all")
            && (elementCount) == getElements(locator).size())
            || matchCount.hashCode() == (elementCount)) {
          Log.info("Matchers '" + matchers + "' found in element " + locator
              + ". --- Count Matched: " + elementCount);
          return element;
        } else if (matchCount.hashCode() == 0) {
          if (logger) {
            Log.addStepLog("matchers=" + foundMatches + ": " + locator + ": expected matchCount="
                + matchCount + " but found " + elementCount + ".", "warn");
          }
          return element;
        }
      }
      foundMatches.clear();
    }
    if (logger && (matchCount.hashCode() != 0)) {
      if (textHas(matchCount.toString(), "all")) {
        matchCount = getElements(locator).size() + " (all)";
      }
      Log.addStepLog("matchers=" + matchers + ": " + locator + ": expected matchCount="
          + matchCount + " but found " + elementCount + ".", "warn");
    }
    return null;
  }

  // Add special flag `{escaperegex}` at end of expectedText string to not use regex special chars.
  // Example: regexMatch(actualText, expectedText="search literal * char {escapeRegex}", true);
  private static boolean regexMatch(String actualText, String expectedText, boolean logger) {
    if (actualText == null) actualText = "^null$";
    boolean skipRegexRetry = expectedText.toLowerCase().contains("{escaperegex}");
    expectedText = expectedText.replaceAll("(?i) ?\\{escaperegex}", "");
    boolean checkMatch = actualText.matches("(?msi).*?\\Q" + expectedText + "\\E.*?");
    if (!checkMatch && !skipRegexRetry) {
      checkMatch = actualText.matches("(?msi).*?" + expectedText + ".*?");
    }
    if (!checkMatch && logger) {
      Log.error("Actual text does not contain expected text."
          + "\nexpected: " + expectedText + "\n  actual: " + actualText);
    }
    return checkMatch;
  }
}

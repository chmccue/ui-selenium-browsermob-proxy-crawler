package main;

import static utils.matcherUtils.textHas;

import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Log extends TestWatcher implements TestRule {

  // initialize
  private static String resultLog = "";
  private static final Logger Log = LogManager.getLogger(Log.class);
  public static Scenario currentScenario;

  @Override
  public Statement apply(Statement base, Description description) {
    return super.apply(base, description);
  }

  @Override
  protected void succeeded(Description description) {
    resultLog = description.getMethodName() + ": " + "PASSED \n";
    Log.info(resultLog);
  }

  @Override
  protected void failed(Throwable e, Description description) {
    resultLog = description.getMethodName() + ":  " + "FAILED " + "\n";
    Log.info(resultLog);
  }

  @Override
  protected void starting(Description description) {
    super.starting(description);
  }

  @Override
  protected void finished(Description description) {
    super.finished(description);
  }

  public static void info(String message) {
    Log.info(removeLoggingAuthCredentials(message));
  }

  public static void debug(String message) {
    Log.debug(removeLoggingAuthCredentials(message));
  }

  public static void warn(String message) {
    Log.warn(removeLoggingAuthCredentials(message));
  }

  public static void error(String message) {
    Log.error(removeLoggingAuthCredentials(message));
  }

  // method to print out multiple lines to report.
  public static void addStepLog(List<String> messages, String logType) {
    int messageCount = 0;
    for (String message : messages) {
      messageCount += 1;
      addStepLog(message, logType + " " + messageCount);
    }
  }

  public static void addStepLog(Set<String> messages, String logType) {
    addStepLog(new ArrayList<>(messages), logType);
  }

  public static void addStepLog(String message) {
    addStepLog(message, "info");
  }

  public static void addStepLog(String message, String logType) {
    if (textHas(logType, "^info")) Log.info(logType.replace("info", "") + " - " + message);
    else if (textHas(logType, "^warn")) Log.warn(logType.replace("warn", "") + " - " + message);
    else if (textHas(logType, "^error")) Log.error(logType.replace("error", "") + " - " + message);
    else Log.info(logType.toUpperCase() + " - " + message);
    try {
      currentScenario.log(logType.toUpperCase() + ": " + removeLoggingAuthCredentials(message));
    } catch (NullPointerException ignored) {}
  }

  private static String removeLoggingAuthCredentials(String message) {
    return message.replaceAll("https://.*?:.*?[^ ]@", "https://");
  }
}

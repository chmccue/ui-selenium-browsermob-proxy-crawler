package utils;

import main.Log;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.List;

public class stepUtils {

  public List<String> fails = new ArrayList<>();

  // Outputs any fails to report and throws assertion fail
  public void failHandler() {
    failHandler(true);
  }

  public void failHandler(boolean assertOnFail) {
    if (!fails.isEmpty()) {
      Log.addStepLog("FAIL COUNT: " + fails.size());
      Log.addStepLog("FAIL(S) REPORTED: ", "error");
      Log.addStepLog(fails, "error");
      fails.clear();
      if (assertOnFail) Assert.fail();
    }
  }
}

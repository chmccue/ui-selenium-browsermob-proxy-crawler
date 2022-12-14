package crawler;

import main.Log;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Result {

  // titleName="scraped" or titleName="crawled". map=key/value pairs of urls/referrer urls.
  // reportLog=true prints detailed log to uat report. If false, log is viewable in logs folder.
  public <K, V> void crawlerReport(String titleName, LinkedHashMap<K, V> map, boolean reportLog) {
    String startText = "<br />\n*****************" + "<br />\nSTART OF "
        + titleName.toUpperCase() + " PARENT CHILD URL LIST<br />\n*****************<br />\n"
        + "Total Url links " + titleName + ": " + map.size() + "<br />\n";
    if (reportLog) {
      Log.addStepLog(startText, "report");
    } else {
      Log.info(startText);
    }
    int reportCount = 0;
    String linkText;
    for (HashMap.Entry<K, V> entry : map.entrySet()) {
      reportCount += 1;
      linkText = "----------" + titleName + " Link " + reportCount + "----------<br />\n-> Parent:"
          + " [" + entry.getValue() + "] <br />\n--> Child: [" + entry.getKey() + "]";
      if (reportLog) {
        Log.addStepLog(linkText, "report");
      } else {
        Log.info(linkText);
      }
    }
    String endText = "<br />\n*****************<br />\nEND OF "
        + titleName.toUpperCase() + " PARENT CHILD URL LIST<br />\n*****************";
    if (reportLog) {
      Log.addStepLog(endText, "report");
    } else {
      Log.info(endText);
    }
  }
}

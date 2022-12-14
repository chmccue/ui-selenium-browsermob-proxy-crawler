package crawler;

import java.util.LinkedHashMap;
import java.util.Map;

public class Config {

  public static Map<String, Object> crawlerConfig = new LinkedHashMap<>();

  // White List filters
  static String whiteListCrawlerRegex = "(/(dist|bundles|initjs)|/img/favicon\\.ico|\\.svg|"
      + "\\.woff|about:blank|bid.g.doubleclick.net/xbbe/pixel|javascript: void(0);)";
  public static String whiteListCspRegex = "(/(launch-|analytics|bundles|_assets|"
      + "livechat|internal)|\\.(css|js|ico|png|jpe?g|svg|woff|webp|asc))";
  public static String whiteListStatusCodesRegex = "(alb\\.reddit\\.com/rp\\.gif)";
  static String whiteListScraperRegex = "(/logout|mailto)";

  // Default test is "test page load"=true with "total depth"=0
  public static void defaultCrawlerConfig() {
    crawlerConfig.put("report log", false);
    crawlerConfig.put("total depth", 0);
    crawlerConfig.put("local crawl only", true);
    crawlerConfig.put("test page load", true);
    crawlerConfig.put("test csp headers", false);
    crawlerConfig.put("test countly", false);
    crawlerConfig.put("test js screener", true);
    crawlerConfig.put("test all console output", false);
    crawlerConfig.put("test warning console output", false);
    crawlerConfig.put("test severe console output", false);
    crawlerConfig.put("console search", "");
    crawlerConfig.put("scrape filter", "[href], [src], [srcset]");
  }

  public static void updateCrawlerConfig(String key, String value) {
    key = key.toLowerCase();
    if (key.contains("console")) {
      if (!value.matches("^(true|false)$")) {
        crawlerConfig.put("console search", value);
        value = "true";
      }
    }
    Object newValue = value;
    if (value.matches("^(true|false)$")) newValue = value.contains("true");
    else if (value.matches("^[0-9]+$")) newValue = (Integer.parseInt(value));
    if (!value.isEmpty()) crawlerConfig.put(key, newValue);
  }
}

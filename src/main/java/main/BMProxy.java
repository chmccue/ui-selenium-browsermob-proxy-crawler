package main;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.net.NetworkUtils;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import static main.Core.siteUrl;
import static utils.matcherUtils.textHas;

public class BMProxy {

  static net.lightbody.bmp.BrowserMobProxy proxy = new BrowserMobProxyServer();
  static org.openqa.selenium.Proxy seleniumProxy;

  public static boolean captureTraffic = false;
  public static List<String> responseUrlsCaptured = new ArrayList<>();
  public static LinkedHashMap<String, String> currentPagePath = new LinkedHashMap<>();
  public static LinkedHashSet<String> responseBadStatusCodes = new LinkedHashSet<>();
  public static LinkedHashMap<String, String> localResponseHeaders = new LinkedHashMap<>();
  public static String responseCountly;

  static void startProxy() {
    if (!proxy.isStarted()) {
      proxy.setTrustAllServers(true);
      proxy.start();
      Log.info("Browsermob-proxy is running");
      // get Selenium proxy object
      seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
      String ipAddress = Core.remoteTest
          ? new NetworkUtils().getIp4NonLoopbackAddressOfThisMachine().getHostAddress()
          : new NetworkUtils().obtainLoopbackIp4Address();
      int port = proxy.getPort();
      seleniumProxy.setHttpProxy(ipAddress + ":" + port);
      seleniumProxy.setSslProxy(ipAddress + ":" + port);
      addRefererToRequiredPages();
      getCurrentPageResponseData();
    }
  }

  private static void getCurrentPageResponseData() {
    proxy.addResponseFilter((response, contents, messageInfo) -> {
      if (captureTraffic) {
        responseUrlsCaptured.add(messageInfo.getUrl());
        if (textHas(messageInfo.getUrl(), siteUrl) && textHas(messageInfo.getOriginalRequest()
            .headers().get("Sec-Fetch-Mode"), "^navigate$")) {
          localResponseHeaders.clear();
          localResponseHeaders.put("responseUrl", messageInfo.getUrl());
          localResponseHeaders.put("responseHeaders", response.headers().entries().toString());
        }
        if (textHas(
            messageInfo.getOriginalRequest().headers().get("Sec-Fetch-Mode"), "^navigate$")) {
          currentPagePath.put(messageInfo.getUrl(), response.getStatus().toString());
        }
        if (!textHas(response.getStatus().toString(), "(1|2|3)0")) {
          responseBadStatusCodes.add(
              response.getStatus().toString() + ": " + messageInfo.getUrl());
        }
        if (textHas(messageInfo.getUrl(), "\\?events=")) {
          responseCountly = messageInfo.getUrl();
        }
      }
    });
  }

  private static void addRefererToRequiredPages() {
    proxy.addRequestFilter((request, contents, messageInfo) -> {
      // Pages that get "Invalid Request" error have "/u/" in url, so we only add referer
      // to them if they don't already have a Referer set.
      if (textHas(messageInfo.getUrl(), "/u/")
          && !request.headers().contains("Referer")) {
        request.headers().add("Referer", siteUrl + "/");
      }
      return null;
    });
  }

  public static void resetProxyStats() {
    captureTraffic = false;
    responseUrlsCaptured.clear();
    currentPagePath.clear();
    responseBadStatusCodes.clear();
    localResponseHeaders.clear();
  }
}

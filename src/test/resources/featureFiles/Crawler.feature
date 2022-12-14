@crawler @crawler-external @teardown-reset-crawler-maps
Feature: Crawler for External Public facing pages

  Background:
    Given I have an open browser on test site

  Scenario: Crawler: External site: Create crawl list and validate with no depth
  # supports adding "site url" to run env var SITE_URL.
    Given I add "site url" to crawl list
  # supports adding relative urls (/sign-up, etc), which append env var SITE_URL in the backend.
    When I add "/sign-up" to crawl list
    And I add "/sign-in" to crawl list
  # supports external link validation when added with full url (including http portion).
    And I add "https://support.company.com/" to crawl list
    When I run crawler from list
    Then output crawler log data
  @crawler-console-output
  Scenario: Crawler: External site: Console Output: Validate no Warning Level errors
    Given I set crawler configs:
      | report log | true |
      | test warning console output | true |
    And I add "site url" to crawl list
    When I run crawler from list
    Then output crawler log data
  @crawler-csp-high-priority
  Scenario: Crawler: External site: Crawl high priority pages for CSP Headers
    Given I set crawler configs:
      | test csp headers | true |
      | test all console output | Content Security Policy |
    And I add "/" to crawl list
    And I add "/sign-in" to crawl list
    And I add "/sign-up" to crawl list
    And I add "/prices" to crawl list
    When I run crawler from list
    Then output crawler log data
  @crawler-external-src-set-images
  Scenario: Crawler: External site: Validate source set image links
    Given I set crawler configs:
      | report log    | true |
      | total depth   |  2   |
      | scrape filter | [srcset], [href] |
    And I add "site map urls - quick" to crawl list
    And I add "/sign-up" to crawl list
    And I add "/sign-in" to crawl list
    When I run crawler from list
    Then output crawler log data

  Scenario Outline: Crawler: External site: Local Crawl Only = <localCrawlOnly>, CSP = <csp>, Countly = <countly>: Site Map + non-Facade
    Given I set crawler configs:
      | total depth      | 4 |
      | report log       | false |
      | scrape filter    | <scrapeFilter> |
      | test csp headers | <csp> |
      | test countly     | <countly> |
      | local crawl only | <localCrawlOnly> |
      | test all console output | Content Security Policy |
    And I add "site map urls - quick" to crawl list
    And I add "/sign-up" to crawl list
    And I add "/sign-in" to crawl list
    When I run crawler from list
    Then output crawler log data
    @csp-true-countly-true
    Examples:
      | localCrawlOnly | csp  | countly | scrapeFilter |
      | true           | true | true    | a[href]      |
    @csp-true-countly-true-js-scrape-true
    Examples:
      | localCrawlOnly | csp  | countly | scrapeFilter |
      | true           | true | true    | a[href], [href*='.js'], [src*='.js'] |
    @countly-true
    Examples:
      | localCrawlOnly | csp   | countly | scrapeFilter |
      | true           | false | true    | a[href]      |
    @countly-true-js-scrape-true
    Examples:
      | localCrawlOnly | csp  | countly | scrapeFilter |
      | true           | false | true    | a[href], [href*='.js'], [src*='.js'] |
    @csp-true
    Examples:
      | localCrawlOnly | csp  | countly | scrapeFilter |
      | true           | true | false   | a[href]      |
    @csp-true-js-scrape-true
    Examples:
      | localCrawlOnly | csp  | countly | scrapeFilter |
      | true           | true | false   | a[href], [href*='.js'], [src*='.js'] |
    @csp-false-countly-false-js-scrape-false
    Examples:
      | localCrawlOnly | csp   | countly | scrapeFilter |
      | true           | false | false   | a[href]      |
    @csp-false-countly-false
    Examples:
      | localCrawlOnly | csp   | countly | scrapeFilter |
      | true           | false | false   | a[href], [href*='.js'], [src*='.js'] |
    @csp-true-crawl-all
    Examples:
      | localCrawlOnly | csp  | countly | scrapeFilter |
      | false          | true | false   |              |
    @csp-false-crawl-all
    Examples:
      | localCrawlOnly | csp   | countly | scrapeFilter |
      | false          | false | false   |              |

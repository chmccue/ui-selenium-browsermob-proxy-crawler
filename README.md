# UI Crawler with Additional Validations

> UI Crawler/Scraper Automation built in the following 
> (Current versions can be found in build.gradle file):
> * Java 
> * Selenium WebDriver
> * BrowserMob Proxy
> * Junit
> * Cucumber
> * Gradle

**Supports:** MacOSX, Linux, Windows.

## Installation

Install the following requirements with their respective versions:

| [Docker](https://www.docker.com/community-edition) | [Java](https://jdk.java.net/) | [Gradle](https://gradle.org/releases/) |
| --- | --- | --- |
| 19.03 | 11.0.7 LTS | 6.5 |

All other dependencies are defined in `build.gradle` and are automatically loaded.

## Configuration

Create `.env` file from provided `.env_template` and edit with your desired configuration / credentials:

```bash
cp .env-template .env
vim .env
```

Also, additional and more advanced settings can be configured in the `gradle.properties` file.

### How It Works

* This crawler is more than a typical link checker.
* Easily scalable with additional validations that can be added like plug-ins.
* Validations for checking console errors, page text, page header content, and more.
* Set up currently supports Chrome only.
* Negative: runs slower than typical crawlers, since it is utilizing a front end tool 
for the additional validations and is not running asynchronously, though you could adjust it
to run tests in parallel using Cucumber and run faster.

### Explanation of `.env` variables

* `SITE_URL`: HTTPS URL of website.

View `docker/start.sh` file to see the default values that get set when running in docker.

## Usage

### Running locally

First modify the `.env` file:
```bash
SITE_URL=<HTTPS URL of the QA website>
```

After the .env file is set up and you have set the desired cucumber tags in `src/test/java/suites/ChromeTestSuite`,
the easiest way to run the tests is by running the preconfigured bash script localscripts/runLocal.sh.

### Running with [docker-compose](https://docs.docker.com/compose/) and Selenium Standalone

#### For Chrome
Run:
```bash
docker-compose up --detach selenium-chrome
docker-compose up --build company-uat
```

### Running with [docker-compose](https://docs.docker.com/compose/) and Selenium Grid

#### For Chrome

Running a single node:

```bash
docker-compose up --detach selenium-hub node-chrome
```

Scale to more than 1 node:

```bash
docker-compose up --detach --scale node-chrome=<scale number> --no-recreate node-chrome
docker-compose up --build company-uat
```

### Reports

Test Report generated locally:
* `build/chrome/`

Test Log files: `build/logs`

# SauceDemo Playwright Automation Framework (Java)

End-to-end test automation framework for [saucedemo.com](https://www.saucedemo.com) built with **Playwright for Java** and **TestNG**.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Test Runner | TestNG 7.10.0 |
| Build Tool | Maven |
| Reporting | TestNG HTML Reporter |
| Browsers | Chromium, Firefox |

---



---


## Setup

### 1. Clone or download the project from following URL

```https://github.com/SBM79/PlaywrightFramework.git```

### 2. Install Playwright browsers

Run once after cloning to download Chromium and Firefox binaries:

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium firefox"
```

### 3. Verify the build

```bash
mvn compile
```

---

## Running Tests

### Run the full suite (Chromium + Firefox, parallel)

```bash
mvn test
```

### Run a specific browser only

Override the `browser` parameter from the command line:

```bash
mvn test -Dbrowser=chromium
mvn test -Dbrowser=firefox
```

### Run a specific test class

```bash
mvn test -Dtest=LoginTest
mvn test -Dtest=BuyProductTest
```

### Run with visible browser (disable headless)

Open `src/main/java/config/QAConfig.java` and set:

```java
public static final boolean HEADLESS = false;
```

Then run normally:

```bash
mvn test
```

---

## Test Reports

### HTML Report

After every run, TestNG generates a full HTML report:

```
test-output/index.html
```

Open it in any browser to see pass/fail status, duration, and retry history.

### Screenshots (on failure)

Full-page screenshots are automatically captured when a test fails:

```
Failed Screenshots are saved in screenshot folder - playwright-java-framework\screenshots
```

### Playwright Traces (on failure)

Playwright trace archives are saved as ZIP files on failure:

```
traces/<TestName>.zip
```
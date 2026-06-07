# SauceDemo Playwright Automation Framework (Java)

End-to-end test automation framework for [saucedemo.com](https://www.saucedemo.com) built with **Playwright for Java** and **TestNG**.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Browser Automation | Playwright for Java 1.44.0 |
| Test Runner | TestNG 7.10.0 |
| Build Tool | Maven |
| Reporting | TestNG HTML Reporter |
| Browsers | Chromium, Firefox |

---

## Project Structure

```
playwright-java-framework/
│
├── src/
│   ├── main/java/
│   │   ├── config/
│   │   │   └── QAConfig.java          # Centralized config: URLs, credentials, timeouts
│   │   └── pages/
│   │       ├── LoginPage.java         # Login page interactions
│   │       ├── InventoryPage.java     # Product browsing, cart access, logout
│   │       ├── CartPage.java          # Cart validation, checkout navigation
│   │       └── CheckoutPage.java      # Checkout form, order completion
│   │
│   └── test/java/
│       ├── base/
│       │   └── BaseTest.java          # Browser lifecycle, screenshots, traces
│       ├── utils/
│       │   └── RetryAnalyzer.java     # TestNG retry on failure
│       └── tests/
│           ├── LoginTest.java         # Auth test suite (3 scenarios)
│           └── BuyProductTest.java    # E2E purchase workflow
│
├── screenshots/                       # Auto-created; failure screenshots saved here
├── traces/                            # Auto-created; Playwright trace ZIPs saved here
├── test-output/                       # TestNG HTML report output
│
├── testng.xml                         # Suite: parallel Chromium + Firefox execution
├── pom.xml                            # Maven dependencies and build config
└── README.md
```

---

## Prerequisites

| Requirement | Minimum Version |
|---|---|
| Java JDK | 17+ |
| Apache Maven | 3.8+ |
| Internet Access | Required (tests run against live saucedemo.com) |

Playwright downloads browser binaries automatically on first use — no manual browser installation needed.

---

## Setup

### 1. Clone or download the project

```bash
git clone <repository-url>
cd playwright-java-framework
```

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
screenshots/<TestName>_yyyyMMdd_HHmmss.png
```

### Playwright Traces (on failure)

Playwright trace archives are saved as ZIP files on failure:

```
traces/<TestName>.zip
```

Open a trace with the Playwright CLI for a frame-by-frame browser recording:

```bash
npx playwright show-trace traces/testSuccessfulLogin.zip
```

> Requires Node.js installed; or use [trace.playwright.dev](https://trace.playwright.dev) to open the ZIP in browser without Node.js.

---

## Configuration Reference (`QAConfig.java`)

All test settings live in one place. Tests **must not** hardcode credentials or URLs.

```java
// Application URL
BASE_URL         = "https://www.saucedemo.com"

// Valid credentials — sourced by all login-success tests
VALID_USERNAME   = "standard_user"
VALID_PASSWORD   = "secret_sauce"

// Invalid credentials — sourced by failed-login tests
INVALID_USERNAME = "not_a_real_user"
INVALID_PASSWORD = "wrong_password_123"

// Browser behaviour
HEADLESS         = true          // false for visual debugging
SLOW_MO_MS       = 0             // ms delay between actions (debug: try 200)
VIEWPORT_WIDTH   = 1280
VIEWPORT_HEIGHT  = 720

// Timeouts
DEFAULT_TIMEOUT_MS = 30_000      // 30 seconds

// Retries
MAX_RETRY_COUNT  = 2             // max re-runs per failing test
```

---

## Locator Strategy

All selectors follow the three approved strategies (no fragile CSS class selectors):

| Strategy | Example | Used For |
|---|---|---|
| **ID-based** | `page.locator("#user-name")` | Stable HTML `id` attributes |
| **Role-based** | `page.getByRole(AriaRole.BUTTON, new ...().setName("Login"))` | Interactive controls |
| **Relative XPath with attribute** | `page.locator("//h3[@data-test='error']")` | Elements with `data-test` ids |
| **Text + Role filter** | `.filter(hasText(name)).getByRole(BUTTON, "Add to cart")` | Product-scoped buttons |

---

## Test Scenarios

### Authentication Suite (`LoginTest.java`)

| ID | Scenario | Assertions |
|---|---|---|
| TC-AUTH-001 | Successful login with valid credentials | URL contains `inventory`, product list visible |
| TC-AUTH-002 | Failed login with invalid credentials | Error message visible, URL stays on login page, login button visible |
| TC-AUTH-003 | Logout | URL returns to base URL, login button visible |

### End-to-End Purchase (`BuyProductTest.java`)

| ID | Scenario |
|---|---|
| TC-E2E-001 | Login → Browse → Add "Sauce Labs Backpack" → Cart verification → Checkout → Fill info → Finish → Confirm order |

---

## Parallel Execution Design

The `testng.xml` runs **4 parallel test groups** using `parallel="tests"` with `thread-count="4"`:

```
Thread 1: Login Tests     — Chromium
Thread 2: Login Tests     — Firefox
Thread 3: Buy Product     — Chromium
Thread 4: Buy Product     — Firefox
```

Thread-safety is achieved by storing all Playwright objects (`Playwright`, `Browser`, `BrowserContext`, `Page`) in `ThreadLocal` variables inside `BaseTest`. Each thread has its own isolated browser instance — no shared mutable state.

---

## Retry Mechanism

`RetryAnalyzer` retries a failing test up to `QAConfig.MAX_RETRY_COUNT` times (default: 2).  
Each `@Test` method is annotated with `retryAnalyzer = RetryAnalyzer.class`.

On each retry `BaseTest.tearDown()` saves the trace from the failed attempt, then `setUp()` spins up a fresh browser for the next attempt.

---

## Extending the Framework

### Add a new Page Object

1. Create `src/main/java/pages/MyPage.java` extending nothing (plain POJO).
2. Accept `Page page` in the constructor.
3. Declare locators as `private final Locator` fields using role / id / text strategy.
4. Add public action and query methods.

### Add a new test class

1. Create `src/test/java/tests/MyTest.java` extending `BaseTest`.
2. Add a `@BeforeMethod initPages()` that initialises your page objects via `getPage()`.
3. Annotate each test with `@Test(retryAnalyzer = RetryAnalyzer.class)`.
4. Register the class in `testng.xml` under each browser `<test>` block.

---

## Troubleshooting

| Issue | Solution |
|---|---|
| `Browser not found` error | Run `mvn exec:java ... install chromium firefox` (see Setup step 2) |
| Tests time out on slow CI | Increase `DEFAULT_TIMEOUT_MS` in `QAConfig.java` |
| Want to see the browser during debug | Set `HEADLESS = false` and `SLOW_MO_MS = 200` in `QAConfig.java` |
| Trace viewer not opening | Use [trace.playwright.dev](https://trace.playwright.dev) — drag & drop the ZIP |
| `@Parameters` warning in IDE | The `browser` value is injected at runtime by TestNG from `testng.xml` |

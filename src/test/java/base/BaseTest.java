package base;

import com.microsoft.playwright.*;
import config.QAConfig;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public abstract class BaseTest {

    // ─── Thread-local Playwright objects ─────────────────────────────────────

    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext>  TL_CONTEXT    = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();


    protected Page getPage() {
        return PAGE.get();
    }

    // ─── Setup ───────────────────────────────────────────────────────────────


    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chromium") String browserName) throws IOException {

        // Ensure output directories exist
        Files.createDirectories(Paths.get(QAConfig.SCREENSHOTS_DIR));
        Files.createDirectories(Paths.get(QAConfig.TRACES_DIR));

        // ── 1. Playwright instance ──────────────────────────────────────────
        Playwright playwright = Playwright.create();
        BaseTest.playwright.set(playwright);

        // ── 2. Browser launch options (shared by all browser types) ─────────
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(QAConfig.HEADLESS);

        // ── 3. Launch the requested browser ─────────────────────────────────
        Browser browser;
        switch (browserName.toLowerCase().trim()) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                System.out.printf("[BaseTest] Launched Firefox  (headless=%b)%n",
                        QAConfig.HEADLESS);
                break;
            case "chromium":
            default:
                browser = playwright.chromium().launch(launchOptions);
                System.out.printf("[BaseTest] Launched Chromium (headless=%b)%n",
                        QAConfig.HEADLESS);
                break;
        }
        BaseTest.browser.set(browser);

        // ── 4. Browser context —  baseURL + viewport ─────────────────────────
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setBaseURL(QAConfig.BASE_URL)
                .setViewportSize(QAConfig.VIEWPORT_WIDTH, QAConfig.VIEWPORT_HEIGHT);

        BrowserContext context = browser.newContext(contextOptions);


        // ── 5. Start tracing (screenshots + DOM snapshots + sources) ─────────
        //    Tracing is stopped in tearDown; the ZIP is saved only on failure.
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)   // captures screenshots inside the trace
                .setSnapshots(true)     // captures DOM snapshots for timeline view
                .setSources(true));     // embeds page sources

        TL_CONTEXT.set(context);

        // ── 6. Open a new page ───────────────────────────────────────────────
        PAGE.set(context.newPage());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws IOException {

        Page           page    = PAGE.get();
        BrowserContext context = TL_CONTEXT.get();
        Browser        browser = BaseTest.browser.get();
        Playwright     pw      = playwright.get();

        boolean failed = result.getStatus() == ITestResult.FAILURE;

        try {
            // ── Screenshot on failure ────────────────────────────────────────
            if (page != null && failed) {
                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path screenshotPath = Paths.get(
                        QAConfig.SCREENSHOTS_DIR,
                        result.getName() + "_" + timestamp + ".png"
                );
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(screenshotPath)
                        .setFullPage(true));
                System.out.println("[BaseTest] Screenshot  → " + screenshotPath);
            }

            // ── Trace on failure (or retry) ─────────────────────────────────
            if (context != null) {
                if (failed) {
                    Path tracePath = Paths.get(
                            QAConfig.TRACES_DIR,
                            result.getName() + ".zip"
                    );
                    context.tracing().stop(new Tracing.StopOptions()
                            .setPath(tracePath));
                    System.out.println("[BaseTest] Trace saved → " + tracePath);
                    System.out.println("           Open with:  npx playwright show-trace "
                            + tracePath);
                } else {
                    // Stop tracing without writing — no waste for green tests
                    context.tracing().stop();
                }
            }
        } finally {
            // ── Always close in reverse order ───────────────────────────────
            if (page    != null) page.close();
            if (context != null) context.close();
            if (browser != null) browser.close();
            if (pw      != null) pw.close();

            // Remove thread-local references to prevent memory leaks
            PAGE.remove();
            TL_CONTEXT.remove();
            BaseTest.browser.remove();
            playwright.remove();
        }
    }


}

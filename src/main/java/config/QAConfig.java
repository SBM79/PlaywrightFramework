package config;


public final class QAConfig {

    // ─── Application URL ───────────────────────────────────────────────────────

    public static final String BASE_URL = "https://www.saucedemo.com";

    // ─── Valid Credentials ─────────────────────────────────────────────────────
    public static final String VALID_USERNAME = "standard_user";
    public static final String VALID_PASSWORD = "secret_sauce";

    // ─── Invalid Credentials ──────────────────────────────────────────────────

    public static final String INVALID_USERNAME = "not_a_real_user";
    public static final String INVALID_PASSWORD = "wrong_password_123";

    // ─── Browser Configuration ────────────────────────────────────────────────


    public static final boolean HEADLESS = true;
    public static final int VIEWPORT_WIDTH = 1280;
    public static final int VIEWPORT_HEIGHT = 720;

    // ─── Retry Configuration ──────────────────────────────────────────────────

    public static final int MAX_RETRY_COUNT = 2;

    // ─── Output Paths ─────────────────────────────────────────────────────────

    public static final String SCREENSHOTS_DIR = "screenshots";

    /** Directory where Playwright traces (zip) of failures are saved. */
    public static final String TRACES_DIR = "traces";

    // ─── Prevent instantiation ────────────────────────────────────────────────

    private QAConfig() {
        throw new UnsupportedOperationException("QAConfig is a utility class");
    }
}

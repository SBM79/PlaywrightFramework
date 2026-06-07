package config;


public final class QAConfig {


    public static final String BASE_URL = "https://www.saucedemo.com";
    public static final String VALID_USERNAME = "standard_user";
    public static final String VALID_PASSWORD = "secret_sauce";
    public static final String INVALID_USERNAME = "not_a_real_user";
    public static final String INVALID_PASSWORD = "wrong_password_123";
    public static final boolean HEADLESS = true;
    public static final int VIEWPORT_WIDTH = 1280;
    public static final int VIEWPORT_HEIGHT = 720;
    public static final int MAX_RETRY_COUNT = 2;
    public static final String SCREENSHOTS_DIR = "screenshots";
    public static final String TRACES_DIR = "traces";
}

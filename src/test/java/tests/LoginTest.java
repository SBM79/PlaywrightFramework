package tests;

import base.BaseTest;
import config.QAConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.RetryAnalyzer;


public class LoginTest extends BaseTest {

    // ─── Page Objects ─────────────────────────────────────────────────────────

    private LoginPage    loginPage;
    private InventoryPage inventoryPage;

    // ─── Before Each Test ─────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        loginPage     = new LoginPage(getPage());
        inventoryPage = new InventoryPage(getPage());
    }

    @Test(
        description   = "T-001: Successful login with valid credentials",
        retryAnalyzer = RetryAnalyzer.class,
        priority      = 1
    )
    public void testSuccessfulLogin() {

        // ── Step 1: Open application ─────────────────────────────────────────
        loginPage.navigate();

        // ── Step 2: Login with valid credentials from QAConfig ───────────────
        loginPage.login(QAConfig.VALID_USERNAME, QAConfig.VALID_PASSWORD);

        // ── Assertion 1: URL should contain "inventory" ──────────────────────
        Assert.assertTrue(
            getPage().url().contains("inventory"),
            "FAIL — Expected URL to contain 'inventory' after successful login.\n"
            + "       Actual URL: " + getPage().url()
        );

        // ── Assertion 2: Product list must be visible ────────────────────────
        Assert.assertTrue(
            inventoryPage.isInventoryListVisible(),
            "FAIL — Product inventory list is not visible after login."
        );
    }

    // ─── Test 2: Failed Login ─────────────────────────────────────────────────

    @Test(
        description   = "T-002: Failed login with invalid credentials",
        retryAnalyzer = RetryAnalyzer.class,
        priority      = 2
    )
    public void testFailedLogin() {

        // ── Step 1: Open application ─────────────────────────────────────────
        loginPage.navigate();

        // ── Step 2: Login with invalid credentials from QAConfig ─────────────
        loginPage.login(QAConfig.INVALID_USERNAME, QAConfig.INVALID_PASSWORD);

        // ── Assertion 1: Error message must be displayed ─────────────────────
        String errorText = loginPage.getErrorMessage();
        Assert.assertTrue(errorText.toLowerCase().contains("username and password do not match"),
            "FAIL — Error message content unexpected.\n"
            + "       Actual: " + errorText);
        // ── Assertion 2: User remains on login page (URL check) ───────────────
        Assert.assertFalse(
            getPage().url().contains("inventory"),
            "FAIL — User was redirected to the inventory page with invalid credentials."
        );

        // ── Assertion 3: Login button still visible ───────────────────────────
        Assert.assertTrue(
            loginPage.isLoginPageDisplayed(),
            "FAIL — Login button is no longer visible; user may have been redirected."
        );
    }

    // ─── Test 3: Logout ───────────────────────────────────────────────────────

    @Test(
        description   = "T-003: Successful logout redirects to login page",
        retryAnalyzer = RetryAnalyzer.class,
        priority      = 3
    )
    public void testLogout() {

        // ── Step 1: Login with valid credentials ─────────────────────────────
        loginPage.navigate();
        loginPage.login(QAConfig.VALID_USERNAME, QAConfig.VALID_PASSWORD);

        // Pre-condition guard: verify we are on inventory page before logout
        Assert.assertTrue(
            getPage().url().contains("inventory"),
            "PRE-CONDITION FAIL — Could not reach inventory page; login may have failed."
        );

        inventoryPage.logout();
        // ── Assertion 1: Redirected to the login page ────────────────────────
        String currentUrl = getPage().url().replaceAll("/$", "");   // strip trailing slash
        String baseUrl    = QAConfig.BASE_URL.replaceAll("/$", "");

        Assert.assertEquals(currentUrl, baseUrl,
            "FAIL — After logout, URL should be the base URL.\n"
            + "       Expected : " + baseUrl + "\n"
            + "       Actual   : " + currentUrl
        );

        // ── Assertion 2: Login button is visible ─────────────────────────────
        Assert.assertTrue(
            loginPage.isLoginPageDisplayed(),
            "FAIL — Login button not visible after logout; login page may not have loaded."
        );
    }
}

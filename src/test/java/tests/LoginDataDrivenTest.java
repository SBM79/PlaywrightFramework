package tests;


import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.CheckoutPage;
import pages.InventoryPage;
import pages.LoginPage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



public class LoginDataDrivenTest extends BaseTest {

    // ─── CSV location (classpath-relative, under src/test/resources/) ─────────
    private static final String CSV_FILE  = "testdata/login_data.csv";
    private static final String DELIMITER = ",";

    // ─── Page Objects ─────────────────────────────────────────────────────────
    private LoginPage     loginPage;
    private InventoryPage inventoryPage;
    private CartPage      cartPage;
    private CheckoutPage  checkoutPage;

    // ─── DataProvider — CSV parsed here, no external class needed ─────────────

    @DataProvider(name = "loginData")
    public Object[][] loginData() {

        List<Object[]> rows = new ArrayList<>();

        InputStream stream = getClass()
                .getClassLoader()
                .getResourceAsStream(CSV_FILE);

        if (stream == null) {
            throw new RuntimeException(
                    "[loginData] CSV file not found on classpath: '" + CSV_FILE + "'.\n"
                            + "Ensure the file exists at src/test/resources/" + CSV_FILE
            );
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

            String  line;
            boolean isHeader   = true;
            int     lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;                           // skip blank lines
                }

                if (isHeader) {
                    isHeader = false;                   // skip header row
                    System.out.printf("[loginData] Header  (line %2d) skipped : %s%n",
                            lineNumber, line);
                    continue;
                }

                // -1 limit keeps empty trailing fields intact
                String[] cols     = line.split(DELIMITER, -1);
                String   username = cols.length > 0 ? cols[0].trim() : "";
                String   password = cols.length > 1 ? cols[1].trim() : "";

                rows.add(new Object[]{ username, password });

                System.out.printf("[loginData] Data row (line %2d) loaded  : username=%-24s password=%s%n",
                        lineNumber,
                        username.isEmpty() ? "<empty>" : username,
                        password.isEmpty() ? "<empty>" : "****");
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "[loginData] Failed to read CSV: '" + CSV_FILE + "'", e
            );
        }

        return rows.toArray(new Object[0][]);
    }

    // ─── Before Each CSV Row ──────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        loginPage     = new LoginPage(getPage());
        inventoryPage = new InventoryPage(getPage());
        cartPage      = new CartPage(getPage());
        checkoutPage  = new CheckoutPage(getPage());
    }
    // ─── Data-Driven Test ─────────────────────────────────────────────────────

    @Test(
            description  = "T-DDT-001: Data-driven login using credentials from login_data.csv",
            dataProvider = "loginData"
    )
    public void testLoginWithCsvData(String username, String password) {

        String displayUser = username.isEmpty() ? "<empty>" : username;
        String displayPass = password.isEmpty() ? "<empty>" : "****";

        // STEP 1 — Open the login page
        loginPage.navigate();
        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "STEP 1 FAIL — Login page did not load; login button not visible."
        );
        // STEP 2 — Attempt login with CSV credentials
        System.out.printf("── STEP 2: Login  → username='%s'%n", displayUser);
        loginPage.login(username, password);

        // STEP 3 — Branch on post-login URL
        boolean loginSucceeded = getPage().url().contains("inventory");

        if (loginSucceeded) {
            assertSuccessfulLogin(username);
        } else {
            assertFailedLogin(username);
        }
    }

    // ─── Private Assertion Helpers ────────────────────────────────────────────

    private void assertSuccessfulLogin(String username) {

        System.out.println("── STEP 3 [SUCCESS PATH]: Verify inventory page ──");

        Assert.assertTrue(
                getPage().url().contains("inventory"),
                "SUCCESS FAIL — URL should contain 'inventory'.\n"
                        + "               username='"  + username + "'\n"
                        + "               Actual URL: " + getPage().url()
        );
        System.out.println("✔  URL contains 'inventory'.");

        Assert.assertTrue(
                inventoryPage.isInventoryListVisible(),
                "SUCCESS FAIL — Product list is not visible.\n"
                        + "               username='" + username + "'"
        );
        System.out.println("✔  Product list visible on inventory page.");
        System.out.printf( "✔  LOGIN PASSED  → username='%s' reached inventory page.%n", username);
    }


    private void assertFailedLogin(String username) {

        System.out.println("── STEP 3 [FAILURE PATH]: Verify error state ─────");

        String errorText = loginPage.getErrorMessage();

        System.out.println("error message = "+errorText);
        Assert.assertNotNull(
                errorText,
                "FAILURE FAIL — Error message element not found.\n"
                        + "               username='" + username + "'"
        );
        Assert.assertFalse(
                errorText.isEmpty(),
                "FAILURE FAIL — Error message is empty.\n"
                        + "               username='" + username + "'"
        );

        Assert.assertFalse(
                getPage().url().contains("inventory"),
                "FAILURE FAIL — User was unexpectedly redirected to inventory.\n"
                        + "               username='" + username + "'\n"
                        + "               Actual URL: " + getPage().url()
        );

        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "FAILURE FAIL — Login button no longer visible.\n"
                        + "               username='" + username + "'"
        );

    }

}


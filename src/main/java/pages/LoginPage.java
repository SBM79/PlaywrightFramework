package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import config.QAConfig;
import org.testng.annotations.BeforeMethod;


public class LoginPage {

    private final Page page;

    // ─── Locators ────────────────────────────────────────────────────────────
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public LoginPage(Page page) {
        this.page = page;

        // ID-based selectors
        this.usernameInput = page.locator("#user-name");
        this.passwordInput = page.locator("#password");

        // Role-based selector: button with accessible name "Login"
        this.loginButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Login")
        );

        // Relative XPath using data-test attribute for the error heading
        this.errorMessage = page.locator("//h3[@data-test='error']");
    }


    public void navigate() {
        page.navigate(QAConfig.BASE_URL);
    }

    public void login(String username, String password) {
        usernameInput.clear();
        usernameInput.fill(username);
        passwordInput.clear();
        passwordInput.fill(password);
        loginButton.click();
    }

    public String getErrorMessage() {
        errorMessage.waitFor();
        return errorMessage.textContent().trim();
    }

    public boolean isLoginPageDisplayed() {
        return loginButton.isVisible();
    }
}

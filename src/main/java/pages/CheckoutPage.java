package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;


public class CheckoutPage {

    private final Page page;

    // ─── Step 1 Locators — Customer Information ───────────────────────────────

    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator zipCodeInput;
    private final String continueButton = "//input[@value=\"Continue\"]";

    private final String finishButton = "//button[@id=\"finish\"]";
    private final String confirmationHeader = "//h2[@data-test='complete-header']";

    // ─── Constructor ─────────────────────────────────────────────────────────

    public CheckoutPage(Page page) {
        this.page = page;

        // ID-based locators — form inputs
        this.firstNameInput = page.locator("#first-name");
        this.lastNameInput  = page.locator("#last-name");
        this.zipCodeInput   = page.locator("#postal-code");

    }

    // ─── Public Actions ──────────────────────────────────────────────────────

    public void fillCustomerInformation(String firstName, String lastName, String zipCode) {
        firstNameInput.fill(firstName);
        lastNameInput.fill(lastName);
        zipCodeInput.fill(zipCode);
        page.locator(continueButton).click();
    }

    public void finishCheckout() {
        page.locator(finishButton).click();
    }

    public String getOrderConfirmationMessage() {
        return page.locator(confirmationHeader).textContent().trim();
    }

}

package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;


public class CartPage {

    public final Page page;

    // ─── Locators ────────────────────────────────────────────────────────────
    private final String checkoutButton ="//button[text()='Checkout']";

    public CartPage(Page page) {
        this.page = page;
    }

    public void proceedToCheckout() {
        page.locator(checkoutButton).click();
    }

}

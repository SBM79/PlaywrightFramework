package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public class InventoryPage {

    private final Page page;

    // ─── Locators ────────────────────────────────────────────────────────────

    private final Locator cartLink;
    private final String cartBadge = "//span[@data-test='shopping-cart-badge']";
    private final String inventoryList = "//div[@data-test='inventory-list']";
    private final Locator menuButton;
    private final Locator logoutLink;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public InventoryPage(Page page) {
        this.page = page;

        // ID-based locators
        this.cartLink    = page.locator("#shopping_cart_container");
        this.menuButton  = page.locator("#react-burger-menu-btn");
        this.logoutLink  = page.locator("#logout_sidebar_link");

        }

    // ─── Public Actions ──────────────────────────────────────────────────────


    public void verifyInventoryPageLoaded() {
        // URL check using Playwright assertion (waits up to the configured timeout)
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*inventory.*"));

        // Inventory list visibility check
        page.locator(inventoryList).isVisible();
    }

    public void addProductToCart(String productName) {
        // Relative XPath to all product card containers
        Locator allItems = page.locator("//div[@class='inventory_item']");

        // Filter to the card that contains the matching product name text,
        // then locate the "Add to cart" button inside that card by role
        allItems
            .filter(new Locator.FilterOptions().setHasText(productName))
            .getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add to cart")
            )
            .click();
    }

    public void openCart() {
        cartLink.click();
    }

    public int getCartCount() {
        if (page.locator(cartBadge).isVisible()) {
            return Integer.parseInt(page.locator(cartBadge).textContent().trim());
        }
        return 0;
    }

    public void logout() {
        menuButton.click();
        logoutLink.click();
    }

    public boolean isInventoryListVisible() {
        return page.locator(inventoryList).isVisible();
    }
}

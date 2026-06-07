package tests;

import base.BaseTest;
import config.QAConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.CheckoutPage;
import pages.InventoryPage;
import pages.LoginPage;
import utils.RetryAnalyzer;


public class BuyProductTest extends BaseTest {

    // ─── Test Data ────────────────────────────────────────────────────────────

    private static final String PRODUCT_NAME = "Sauce Labs Backpack";
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME  = "Automation";
    private static final String ZIP_CODE   = "94105";
    private static final String EXPECTED_CONFIRMATION = "Thank you for your order";

    // ─── Page Objects ─────────────────────────────────────────────────────────
    private LoginPage     loginPage;
    private InventoryPage inventoryPage;
    private CartPage      cartPage;
    private CheckoutPage  checkoutPage;

    // ─── Before Each Test ─────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        loginPage     = new LoginPage(getPage());
        inventoryPage = new InventoryPage(getPage());
        cartPage      = new CartPage(getPage());
        checkoutPage  = new CheckoutPage(getPage());
    }

    @Test(
        description   = "T-E2E-001: Full end-to-end purchase of Sauce Labs Backpack",
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testCompleteProductPurchase() {

        // STEP 1 — Login with valid credentials from QAConfig
        loginPage.navigate();
        loginPage.login(QAConfig.VALID_USERNAME, QAConfig.VALID_PASSWORD);

        Assert.assertTrue(
            getPage().url().contains("inventory"),
            "STEP 1 FAIL — Login did not redirect to the inventory page.\n"
            + "              URL: " + getPage().url()
        );

        // STEP 2 — Verify Inventory page is fully loaded (browse products)

        inventoryPage.verifyInventoryPageLoaded();     // verifyInventoryPageLoaded() uses Playwright assertions internally;

        // STEP 3 — Add "Sauce Labs Backpack" to the cart

        inventoryPage.addProductToCart(PRODUCT_NAME);

        // STEP 4 — Verify cart badge shows count = 1

        int cartCount = inventoryPage.getCartCount();
        Assert.assertEquals(
            cartCount, 1,
            "STEP 4 FAIL — Expected cart badge = 1, but found: " + cartCount
        );

        // STEP 5 — Open the shopping cart

        inventoryPage.openCart();

        Assert.assertTrue(
            getPage().url().contains("cart"),
            "STEP 5 FAIL — Expected URL to contain 'cart'.\n"
            + "              Actual URL: " + getPage().url()
        );

        // STEP 7 — Proceed to checkout

        cartPage.proceedToCheckout();

        Assert.assertTrue(
            getPage().url().contains("checkout-step-one"),
            "STEP 7 FAIL — Expected URL to contain 'checkout-step-one'.\n"
            + "              Actual URL: " + getPage().url()
        );

        // STEP 8 — Fill customer information form

        checkoutPage.fillCustomerInformation(FIRST_NAME, LAST_NAME, ZIP_CODE);

        Assert.assertTrue(
            getPage().url().contains("checkout-step-two"),
            "STEP 8 FAIL — Expected URL to contain 'checkout-step-two' after Continue.\n"
            + "              Actual URL: " + getPage().url()
        );

        // STEP 9 — Complete checkout (click "Finish")
        checkoutPage.finishCheckout();

        Assert.assertTrue(
            getPage().url().contains("checkout-complete"),
            "STEP 9 FAIL — Expected URL to contain 'checkout-complete'.\n"
            + "              Actual URL: " + getPage().url()
        );

        // STEP 10 — Verify order confirmation message

        String confirmationMessage = checkoutPage.getOrderConfirmationMessage();
        Assert.assertNotNull(confirmationMessage,
            "STEP 10 FAIL — Order confirmation message element was null.");
        Assert.assertFalse(confirmationMessage.isEmpty(),
            "STEP 10 FAIL — Order confirmation message is empty.");
        Assert.assertTrue(
            confirmationMessage.contains(EXPECTED_CONFIRMATION),
            "STEP 10 FAIL — Confirmation message did not contain '" + EXPECTED_CONFIRMATION + "'.\n"
            + "               Actual: " + confirmationMessage
        );

        System.out.println("✔  Order confirmation message: \"" + confirmationMessage + "\"");
        System.out.println("\n══ TC-E2E-001 PASSED — Full purchase workflow completed. ══\n");
    }

    @Test(
            description   = "TC-NEG-001: Checkout with empty cart produces no order items",
            retryAnalyzer = RetryAnalyzer.class
    )
    public void testEmptyCartCheckout() throws InterruptedException {

        // STEP 1 — Login with valid credentials from QAConfig
        loginPage.navigate();
        loginPage.login(QAConfig.VALID_USERNAME, QAConfig.VALID_PASSWORD);

        Assert.assertTrue(
                getPage().url().contains("inventory"),
                "STEP 1 FAIL — Login did not reach inventory page.\n"
                        + "              URL: " + getPage().url()
        );

        // STEP 2 — Open cart WITHOUT adding any product
        inventoryPage.openCart();

        Assert.assertTrue(
                getPage().url().contains("cart"),
                "STEP 2 FAIL — Expected URL to contain 'cart'.\n"
                        + "              Actual URL: " + getPage().url()
        );

        // STEP 3 — Verify cart is completely empty

        int cartItemCount = inventoryPage.getCartCount();
        Assert.assertEquals(
                cartItemCount, 0,
                "STEP 3 FAIL — Cart should contain 0 items before checkout attempt.\n"
                        + "              Found: " + cartItemCount + " item(s)."
        );

        int badgeCount = inventoryPage.getCartCount();
        Assert.assertEquals(
                badgeCount, 0,
                "STEP 3 FAIL — Cart badge should show 0 / not be visible for an empty cart.\n"
                        + "              Found badge value: " + badgeCount
        );

        // STEP 4 — Attempt to proceed to checkout from empty cart

        cartPage.proceedToCheckout();

        Assert.assertTrue(
                getPage().url().contains("checkout-step-one"),
                "STEP 4 FAIL — Expected to reach checkout-step-one.\n"
                        + "              Actual URL: " + getPage().url()
        );

        // STEP 5 — Fill form with dummy data to advance to order overview
        checkoutPage.fillCustomerInformation(FIRST_NAME, LAST_NAME, ZIP_CODE);
        Assert.assertTrue(
                getPage().url().contains("checkout-step-two"),
                "STEP 5 FAIL — Expected URL to contain 'checkout-step-two'.\n"
                        + "              Actual URL: " + getPage().url()
        );

        // STEP 6 — Verify order overview lists ZERO product line-items

        int overviewCount = inventoryPage.getCartCount();
        Assert.assertEquals(
                overviewCount, 0,
                "STEP 6 FAIL — Order overview should contain 0 items for an empty-cart checkout.\n"
                        + "              Found: " + overviewCount + " item(s) in the overview."
        );

        // STEP 7 — Verify no order confirmation is reached

        Assert.assertFalse(
                getPage().url().contains("checkout-complete"),
                "STEP 7 FAIL — Should NOT have reached order-complete page with empty cart."
        );

    }











}














/*
Generate 2 automated test cases focused on negative/error-handling scenarios.

Test Case 1: Empty Cart Checkout

Preconditions: User is logged in and shopping cart is empty.
Steps:
Navigate to the cart page.
Click the Checkout button.
Validations:
Verify checkout is blocked.
Verify appropriate error/warning message is displayed (e.g., "Your cart is empty").
Verify user remains on the cart page or is redirected appropriately.
Verify no order is created.

Test Case 2: Form Validation Errors

Preconditions: User is on the checkout/registration form.
Steps:
Leave all mandatory fields blank.
Enter invalid values where applicable (invalid email, invalid phone number, etc.).
Submit the form.
Validations:
Verify field-level validation messages are displayed.
Verify required fields are highlighted.
Verify form submission is prevented.
Verify error messages match expected text.
Verify focus is moved to the first invalid field if applicable.

Include assertions, expected results, and edge-case validations. Follow the existing project framework and coding conventions.
* */

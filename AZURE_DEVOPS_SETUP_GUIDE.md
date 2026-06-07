# Azure DevOps CI Setup Guide

Complete guide to push the Playwright Java framework to Azure DevOps and configure Continuous Integration so tests run automatically on every push to `main`.

---

## Prerequisites

Before starting, confirm you have:

| Requirement | How to check |
|---|---|
| Git installed | `git --version` in terminal |
| Java 17+ installed | `java -version` in terminal |
| Maven 3.8+ installed | `mvn -version` in terminal |
| Azure DevOps account | Sign up free at [dev.azure.com](https://dev.azure.com) |
| Framework on your local machine | `C:\Users\Pranay\Desktop\playwright\playwright-java-framework` |

---

## PART 1 — Create the Azure DevOps Repository

### Step 1.1 — Create a new Azure DevOps Project

1. Open your browser and go to **[dev.azure.com](https://dev.azure.com)**.
2. Sign in with your Microsoft account.
3. Click **"+ New project"** (top-right corner).
4. Fill in the details:
   - **Project name**: `playwright-java-framework`
   - **Description**: `Playwright Java Automation Framework for SauceDemo`
   - **Visibility**: Private (or Public if sharing)
   - **Version control**: Git
   - **Work item process**: Basic
5. Click **"Create"**.

### Step 1.2 — Note your repository URL

After the project is created:

1. Click **"Repos"** in the left sidebar.
2. You will see an empty repo page with a URL like:
   ```
   https://dev.azure.com/YourOrgName/playwright-java-framework/_git/playwright-java-framework
   ```
3. Copy this URL — you will use it in the next section.

---

## PART 2 — Push the Framework Using Git

Open **Command Prompt** or **PowerShell** and run the following commands one by one.

### Step 2.1 — Navigate to your framework folder

```cmd
cd C:\Users\Pranay\Desktop\playwright\playwright-java-framework
```

### Step 2.2 — Initialise a Git repository

```cmd
git init
```

### Step 2.3 — Add all files to Git tracking

```cmd
git add .
```

### Step 2.4 — Verify which files will be committed

```cmd
git status
```

You should see all your framework files listed in green. The `.gitignore` file ensures `target/`, `screenshots/`, `traces/`, and IDE files are excluded.

Expected tracked files:
```
azure-pipelines.yml
pom.xml
testng.xml
.gitignore
src/main/java/config/QAConfig.java
src/main/java/pages/LoginPage.java
src/main/java/pages/InventoryPage.java
src/main/java/pages/CartPage.java
src/main/java/pages/CheckoutPage.java
src/test/java/base/BaseTest.java
src/test/java/utils/RetryAnalyzer.java
src/test/java/tests/LoginTest.java
src/test/java/tests/BuyProductTest.java
src/test/java/tests/LoginDataDrivenTest.java
src/test/resources/testdata/login_data.csv
README.md
```

### Step 2.5 — Create the initial commit

```cmd
git commit -m "Initial commit: Playwright Java automation framework"
```

### Step 2.6 — Rename the default branch to main

```cmd
git branch -M main
```

### Step 2.7 — Connect to Azure DevOps remote

Replace `YourOrgName` with your actual Azure DevOps organization name:

```cmd
git remote add origin https://dev.azure.com/YourOrgName/playwright-java-framework/_git/playwright-java-framework
```

### Step 2.8 — Push to Azure DevOps

```cmd
git push -u origin main
```

When prompted, enter your Azure DevOps credentials. If using MFA, you will need a **Personal Access Token (PAT)** instead of your password.

**How to create a PAT (if needed):**
1. In Azure DevOps, click your profile icon (top-right) → **"Personal access tokens"**.
2. Click **"+ New Token"**.
3. Name: `git-push-token`
4. Scopes: **Code → Read & Write**
5. Click **"Create"** and copy the token immediately.
6. Use this token as your password when `git push` prompts for credentials.

### Step 2.9 — Verify the push

Go back to Azure DevOps → **Repos** → **Files**. You should now see all your framework files including `azure-pipelines.yml` at the root level.

Your repository should look like this:
```
playwright-java-framework/          ← repo root
├── azure-pipelines.yml              ← CI pipeline (MUST be at root)
├── pom.xml
├── testng.xml
├── .gitignore
├── README.md
└── src/
    ├── main/java/
    │   ├── config/QAConfig.java
    │   └── pages/*.java
    └── test/
        ├── java/
        │   ├── base/BaseTest.java
        │   ├── utils/RetryAnalyzer.java
        │   └── tests/*.java
        └── resources/
            └── testdata/login_data.csv
```

---

## PART 3 — Create the CI Pipeline

### Step 3.1 — Open the Pipelines section

1. In your Azure DevOps project, click **"Pipelines"** in the left sidebar.
2. Click **"Create Pipeline"** (or **"New Pipeline"** if pipelines already exist).

### Step 3.2 — Select the code source

1. Azure DevOps asks **"Where is your code?"**.
2. Select **"Azure Repos Git"**.
3. Select your repository: **playwright-java-framework**.

### Step 3.3 — Configure the pipeline

1. Azure DevOps asks **"Configure your pipeline"**.
2. Select **"Existing Azure Pipelines YAML file"**.
3. In the dialog that appears:
   - **Branch**: `main`
   - **Path**: `/azure-pipelines.yml`
4. Click **"Continue"**.

### Step 3.4 — Review the pipeline

1. Azure DevOps displays the contents of your `azure-pipelines.yml`.
2. Review it — you should see all 11 steps (JDK install, Maven compile, Playwright browser install, test run, result publishing, artifact uploads).
3. Click **"Run"** to trigger the first build, or click **"Save"** to save without running immediately.

---

## PART 4 — Run the First Build

### Step 4.1 — Trigger the pipeline

If you clicked **"Save"** instead of **"Run"** in the previous step:

1. Go to **Pipelines** → click your pipeline name.
2. Click **"Run pipeline"** (top-right).
3. Confirm the branch is `main`.
4. Click **"Run"**.

### Step 4.2 — Watch the build progress

1. Click the running build to open the live log viewer.
2. You will see each step execute in sequence:
   ```
   ☕ Install JDK 17                              ~10 seconds
   📦 Cache Maven dependencies                    ~5 seconds
   🔨 Maven compile                               ~30 seconds
   🔨 Maven test-compile                          ~10 seconds
   🌐 Install Playwright browsers                 ~60–90 seconds (first run)
   📁 Create output directories                   ~1 second
   ▶️ Run Playwright tests                         ~2–4 minutes
   📊 Publish test results                         ~5 seconds
   📄 Publish HTML test report                     ~5 seconds
   📸 Publish failure screenshots                  ~5 seconds
   🔍 Publish Playwright traces                    ~5 seconds
   ```
3. Click any step to expand its detailed console output.

### Step 4.3 — Expected first-run outcome

The first build will:
- Download Maven dependencies (cached for subsequent runs).
- Download Chromium and Firefox browser binaries via Playwright.
- Execute all tests across both browsers.
- Publish results regardless of pass/fail (thanks to `condition: always()`).

---

## PART 5 — View Test Results and Artifacts

### 5.1 — Test Results (Tests Tab)

1. After the build finishes, click the build run to open it.
2. Click the **"Tests"** tab at the top.
3. You will see a summary:
   - **Total tests**: number of test methods × 2 browsers × CSV rows
   - **Passed**: tests that succeeded
   - **Failed**: tests that failed (with full error messages)
4. Click any individual test to see:
   - Full assertion error message
   - Stack trace
   - Duration

### 5.2 — HTML Report (Artifact)

1. In the build run page, click **"Artifacts"** (or look for the "published" link).
2. Click **"TestNG-HTML-Report"**.
3. Download and extract the archive.
4. Open **`index.html`** in your browser to see the full TestNG visual report with pass/fail breakdown per test class and method.

### 5.3 — Failure Screenshots (Artifact)

1. In the **"Artifacts"** section, click **"Failure-Screenshots"**.
2. Download the `.png` files.
3. Each screenshot is named:
   ```
   testMethodName_yyyyMMdd_HHmmss.png
   ```
   These are full-page captures taken at the exact moment a test failed.

### 5.4 — Playwright Traces (Artifact)

1. In the **"Artifacts"** section, click **"Playwright-Traces"**.
2. Download the `.zip` trace file(s).
3. To view a trace, choose one of these methods:
   - **Online viewer**: go to [trace.playwright.dev](https://trace.playwright.dev) and drag-drop the `.zip` file.
   - **CLI viewer** (requires Node.js): run `npx playwright show-trace testMethodName.zip`.
4. The trace viewer shows a frame-by-frame recording of the browser: screenshots, DOM snapshots, network calls, and action timeline.

---

## PART 6 — Automatic CI on Every Push

Once the pipeline is created and saved, CI is already active. Verify it:

### Test the trigger

1. Make any change to a file in your local framework, for example edit `README.md`:
   ```cmd
   cd C:\Users\Pranay\Desktop\playwright\playwright-java-framework
   echo "CI test" >> README.md
   ```
2. Commit and push:
   ```cmd
   git add .
   git commit -m "Test CI trigger"
   git push
   ```
3. Go to Azure DevOps → **Pipelines**. A new build should automatically start within seconds.

### Pull Request trigger

The `azure-pipelines.yml` also includes a `pr:` trigger for the `main` branch. This means:
- When anyone creates a Pull Request targeting `main`, the pipeline runs automatically.
- If any test fails, the PR is blocked from merging (if you enable the branch policy — see below).

---

## PART 7 — Recommended Branch Policy (Optional)

To enforce that all tests must pass before code is merged to `main`:

1. Go to **Repos** → **Branches**.
2. Click the **three dots** (`...`) next to the `main` branch → **"Branch policies"**.
3. Under **"Build Validation"**, click **"+ Add build policy"**.
4. Select your pipeline: `playwright-java-framework`.
5. Configure:
   - **Trigger**: Automatic
   - **Policy requirement**: Required
   - **Build expiration**: After 12 hours
6. Click **"Save"**.

Now every PR to `main` must pass all Playwright tests before it can be merged.

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `git push` asks for password but fails | Create a Personal Access Token (PAT) — see Step 2.8 |
| Pipeline fails at "Install Playwright browsers" | Add `--with-deps` flag (already included in the YAML) to install OS-level dependencies |
| Tests pass locally but fail in CI | Ensure `QAConfig.HEADLESS = true` — CI agents have no display server |
| "No test results found" warning | Verify `testng.xml` is at the project root and `pom.xml` references it in the surefire plugin |
| Maven cache not working | The `Cache@2` task uses `pom.xml` as the key — any dependency change invalidates the cache automatically |
| Build takes too long | Subsequent runs are faster because Maven deps and browser binaries are cached |
| Screenshots/traces artifact is empty | These are only populated when a test fails — empty artifacts on a fully passing run is expected |

---

## Pipeline YAML File Location

The `azure-pipelines.yml` file **must be placed at the repository root** — the same directory level as `pom.xml`:

```
C:\Users\Pranay\Desktop\playwright\playwright-java-framework\
├── azure-pipelines.yml      ← HERE (repository root)
├── pom.xml
├── testng.xml
├── .gitignore
└── src/
    └── ...
```

Azure DevOps looks for the YAML file at the path you specify when creating the pipeline (default: `/azure-pipelines.yml` at root). If you place it elsewhere, the pipeline creation step will fail to find it.

---

## Quick Reference — Git Commands Summary

```cmd
:: One-time setup
cd C:\Users\Pranay\Desktop\playwright\playwright-java-framework
git init
git add .
git commit -m "Initial commit: Playwright Java automation framework"
git branch -M main
git remote add origin https://dev.azure.com/YourOrgName/playwright-java-framework/_git/playwright-java-framework
git push -u origin main

:: Daily workflow (after making changes)
git add .
git commit -m "describe your change here"
git push
```

Every `git push` to `main` now automatically triggers the full test suite across Chromium and Firefox in Azure DevOps.

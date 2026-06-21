package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class CardOrderTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:9999/";

    @BeforeAll
    public static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Попытка создать драйвер с повторением в случае ошибки
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                driver = new ChromeDriver(options);
                wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                driver.get(BASE_URL);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='name']")));
                System.out.println("✅ Driver created successfully on attempt " + attempt);
                return;
            } catch (Exception e) {
                System.err.println("❌ Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("Should successfully submit card order form with valid data")
    void shouldSubmitFormSuccessfully() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("+79991234567");

        WebElement checkbox = driver.findElement(By.cssSelector("[data-test-id='agreement'] .checkbox__box"));
        checkbox.click();

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='order-success']"))
        );

        String expectedText = "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";
        assertEquals(expectedText, successMessage.getText().trim());
    }

    @Test
    @DisplayName("Should show error for invalid name with digits")
    void shouldShowErrorForInvalidName() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("Иванов Иван123");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("+79991234567");

        WebElement checkbox = driver.findElement(By.cssSelector("[data-test-id='agreement'] .checkbox__box"));
        checkbox.click();

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='name'] .input__sub"))
        );

        String errorText = errorMessage.getText().trim();
        System.out.println("Error message for invalid name: '" + errorText + "'");
        assertTrue(errorText.contains("неверно") || errorText.contains("некорректно"));
    }

    @Test
    @DisplayName("Should show error for invalid phone with letters")
    void shouldShowErrorForInvalidPhone() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("+7abc1234567");

        WebElement checkbox = driver.findElement(By.cssSelector("[data-test-id='agreement'] .checkbox__box"));
        checkbox.click();

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='phone'] .input__sub"))
        );

        String errorText = errorMessage.getText().trim();
        System.out.println("Error message for invalid phone: '" + errorText + "'");
        assertTrue(errorText.contains("неверно") || errorText.contains("некорректно"));
    }

    @Test
    @DisplayName("Should show error when agreement checkbox is not checked")
    void shouldShowErrorWhenAgreementNotChecked() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("+79991234567");

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        boolean successMessagePresent = false;
        try {
            WebElement successMessage = driver.findElement(By.cssSelector("[data-test-id='order-success']"));
            successMessagePresent = true;
        } catch (Exception ignored) {}
        assertFalse(successMessagePresent, "Success message should not appear");

        WebElement agreement = driver.findElement(By.cssSelector("[data-test-id='agreement']"));
        String classes = agreement.getAttribute("class");
        System.out.println("Agreement classes: '" + classes + "'");
        assertTrue(classes.contains("invalid") || classes.contains("error") || classes.contains("has-error"));
    }

    @Test
    @DisplayName("Should show error for empty name")
    void shouldShowErrorForEmptyName() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("+79991234567");

        WebElement checkbox = driver.findElement(By.cssSelector("[data-test-id='agreement'] .checkbox__box"));
        checkbox.click();

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='name'] .input__sub"))
        );

        String errorText = errorMessage.getText().trim();
        System.out.println("Error message for empty name: '" + errorText + "'");
        assertTrue(errorText.contains("обязательно") || errorText.contains("заполнения"));
    }

    @Test
    @DisplayName("Should show error for empty phone")
    void shouldShowErrorForEmptyPhone() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("");

        WebElement checkbox = driver.findElement(By.cssSelector("[data-test-id='agreement'] .checkbox__box"));
        checkbox.click();

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='phone'] .input__sub"))
        );

        String errorText = errorMessage.getText().trim();
        System.out.println("Error message for empty phone: '" + errorText + "'");
        assertTrue(errorText.contains("обязательно") || errorText.contains("заполнения"));
    }

    @Test
    @DisplayName("Should successfully submit with name containing hyphen")
    void shouldSubmitWithNameContainingHyphen() {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id='name'] input"));
        nameInput.sendKeys("Петров-Водкин Петр");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id='phone'] input"));
        phoneInput.sendKeys("+79991234567");

        WebElement checkbox = driver.findElement(By.cssSelector("[data-test-id='agreement'] .checkbox__box"));
        checkbox.click();

        WebElement button = driver.findElement(By.cssSelector("button[type='button']"));
        button.click();

        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='order-success']"))
        );

        String expectedText = "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";
        assertEquals(expectedText, successMessage.getText().trim());
    }
}
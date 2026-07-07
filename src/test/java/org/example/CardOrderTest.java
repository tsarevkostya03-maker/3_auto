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
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='name'] input")));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
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
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='name'].input_invalid .input__sub"))
        );

        String actualText = errorMessage.getText().trim();
        assertTrue(actualText.contains("Имя") && actualText.contains("неверно"),
                "Expected error about invalid name, but got: " + actualText);
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
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='phone'].input_invalid .input__sub"))
        );

        String actualText = errorMessage.getText().trim();
        assertTrue(actualText.contains("Телефон") && actualText.contains("неверно"),
                "Expected error about invalid phone, but got: " + actualText);
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

        WebElement agreementError = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='agreement'].input_invalid"))
        );
        assertTrue(agreementError.isDisplayed());
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
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='name'].input_invalid .input__sub"))
        );

        assertEquals("Поле обязательно для заполнения", errorMessage.getText().trim());
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
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id='phone'].input_invalid .input__sub"))
        );

        assertEquals("Поле обязательно для заполнения", errorMessage.getText().trim());
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
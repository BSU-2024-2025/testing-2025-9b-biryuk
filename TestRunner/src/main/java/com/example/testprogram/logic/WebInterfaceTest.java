package com.example.testprogram.logic;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WebInterfaceTest {
    private WebDriver driver;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
    }

    @Test
    public void testManualCalculationUI() throws InterruptedException {
        driver.get("http://localhost:8080/index.html");

        driver.findElement(By.id("expressionInput")).sendKeys("10 + 20");

        driver.findElement(By.className("btn-calc")).click();

        Thread.sleep(1000);

        String resultText = driver.findElement(By.id("manualResult")).getText();
        Assert.assertTrue(resultText.contains("30.0"),
                "Результат должен содержать '30.0', но получен: " + resultText);

        Thread.sleep(2000);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
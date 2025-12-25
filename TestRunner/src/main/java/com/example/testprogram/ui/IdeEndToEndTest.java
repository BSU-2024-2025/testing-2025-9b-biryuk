package com.example.testprogram.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class IdeEndToEndTest {

    private WebDriver d1;
    private WebDriver d2;
    private final String URL = "http://localhost:8080/";

    private final String COMPLEX_SCRIPT =
            "a[5] = {10, 20, 5, 40, 0};\n" +
                    "mx = max(a);\n" +
                    "mn = min(a);\n" +
                    "print(mx);\n" +
                    "print(mn);\n" +
                    "i = 0;\n" +
                    "while(i < 5) {\n" +
                    "   delay(1);\n" +
                    "   val = a[i];\n" +
                    "   a[i] = val + 1;\n" +
                    "   print(val);\n" +
                    "   i = i + 1;\n" +
                    "}\n" +
                    "t = min(100, 50, 200);\n" +
                    "print(t);";

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        d1 = new ChromeDriver(options);
        d2 = new ChromeDriver(options);
    }

    @Test
    public void testParallelDataIntegrity() throws InterruptedException {
        prepare(d1, "ОКНО 1");
        prepare(d2, "ОКНО 2");

        d1.findElement(By.id("btnRun")).click();
        d2.findElement(By.id("btnRun")).click();

        Thread.sleep(1500);
        d1.findElement(By.id("btnPause")).click();
        System.out.println(">>> Окно 1 на паузе (i≈1), Окно 2 продолжает лететь...");

        WebDriverWait wait2 = new WebDriverWait(d2, Duration.ofSeconds(15));
        wait2.until(ExpectedConditions.textToBePresentInElementLocated(By.id("consoleOutput"), "Execution finished."));

        verifyVars(d2, "ОКНО 2 (завершено)");

        System.out.println(">>> Окно 1: Снимаем с паузы...");
        d1.findElement(By.id("btnPause")).click();
        WebDriverWait wait1 = new WebDriverWait(d1, Duration.ofSeconds(15));
        wait1.until(ExpectedConditions.textToBePresentInElementLocated(By.id("consoleOutput"), "Execution finished."));

        verifyVars(d1, "ОКНО 1 (после паузы)");
    }

    private void verifyVars(WebDriver driver, String windowLabel) {
        WebElement varsBody = driver.findElement(By.id("varsBody"));
        String finalVars = varsBody.getText();

        System.out.println("Результаты для [" + windowLabel + "]:\n" + finalVars);

        Assert.assertTrue(finalVars.contains("mx 40"), windowLabel + ": mx должен быть 40");
        Assert.assertTrue(finalVars.contains("mn 0"), windowLabel + ": mn должен быть 0");
        Assert.assertTrue(finalVars.contains("t 50"), windowLabel + ": t должен быть 50");
        Assert.assertTrue(finalVars.contains("i 5"), windowLabel + ": i должен дойти до 5");
        Assert.assertTrue(finalVars.contains("a") && finalVars.contains("11"),
                "Массив 'a' должен содержать обновленное значение 11");
    }

    private void prepare(WebDriver driver, String name) {
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement ed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("codeEditor")));
        ed.clear();
        ed.sendKeys(COMPLEX_SCRIPT);
    }

    @AfterClass
    public void tearDown() {
        if (d1 != null) d1.quit();
        if (d2 != null) d2.quit();
    }
}
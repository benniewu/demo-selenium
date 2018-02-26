package com.cathay.test.selenium.example;

import java.util.Arrays;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Example_02 {

    public static void main(String[] args) {

        WebDriver driver = null;
        try {
            String browser = args[0];

            // 設定和開啟瀏覽器
            if ("IE".equals(browser)) {
                // IE
                System.setProperty("webdriver.ie.driver", "D:\\Selenium\\WebDriver\\IEDriverServer.exe");
                DesiredCapabilities capability = DesiredCapabilities.internetExplorer();
                capability.setPlatform(Platform.WINDOWS);
                capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                driver = new InternetExplorerDriver(capability);
            } else {
                // Chrome
                System.setProperty("webdriver.chrome.driver", "D:\\Selenium\\WebDriver\\chromedriver.exe");
                DesiredCapabilities capability = DesiredCapabilities.chrome();
                capability.setPlatform(Platform.WINDOWS);
                capability.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors")); //
                capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

                ChromeOptions options = new ChromeOptions();
                options.addArguments("test-type"); // for chrome v36
                options.addArguments("start-maximized");
                options.addArguments("--js-flags=--expose-gc");
                options.addArguments("--enable-precise-memory-info");
                options.addArguments("--disable-popup-blocking");
                options.addArguments("--disable-default-apps");
                options.addArguments("test-type=browser");
                options.addArguments("disable-infobars");
                capability.setCapability(ChromeOptions.CAPABILITY, options);
                driver = new ChromeDriver(capability);
            }

            // 操作瀏覽器
            driver.manage().window().maximize();
            driver.get("https://tim.cathaylife.com.tw/IMWeb/");
            // Wait 5 Seconds for page load
            Thread.sleep(5000);
            // Wait 3 Seconds for element be visible
            new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOfElementLocated(By.id("userID"))).sendKeys("S18932419A");
            new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOfElementLocated(By.id("passWD"))).sendKeys("12345678");
            new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOfElementLocated(By.id("btn_submit"))).click();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 關閉瀏覽器
            if (driver != null) {
                driver.close();
            }
        }
    }
}
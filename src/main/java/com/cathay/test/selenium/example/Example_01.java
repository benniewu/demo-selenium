package com.cathay.test.selenium.example;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Example_01 {

    public static void main(String[] args) {

        // 設定和開啟瀏覽器
        System.setProperty("webdriver.ie.driver", "D:\\Selenium\\WebDriver\\IEDriverServer.exe");
        DesiredCapabilities capability = DesiredCapabilities.internetExplorer();
        capability.setPlatform(Platform.WINDOWS);
        capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        WebDriver driver = new InternetExplorerDriver(capability);

        // 操作瀏覽器
        driver.manage().window().maximize();
        driver.get("https://tim.cathaylife.com.tw/IMWeb/");
        driver.findElement(By.id("userID")).sendKeys("S18932419A");
        driver.findElement(By.id("passWD")).sendKeys("12345678");
        driver.findElement(By.id("btn_submit")).click();
        
        // 關閉瀏覽器
        driver.close();
    }
}
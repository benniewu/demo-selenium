package com.cathay.test.selenium.example;

import org.openqa.selenium.WebDriver;

import com.cathay.test.selenium.SeleniumHelper;
import com.cathay.test.selenium.SeleniumHelperFactory;
import com.cathay.test.selenium.WebDriverHelper;

public class Example_04 {

    public static void main(String[] args) {

        WebDriver driver = null;
        try {
            driver = WebDriverHelper.getWebDriver(WebDriverHelper.DRIVER_DEVELOP, WebDriverHelper.BROWSER_IE);
            SeleniumHelper helper = SeleniumHelperFactory.createSeleniumHelper("RZ", driver);

            // 操作瀏覽器
            driver.manage().window().maximize();
            helper.login(SeleniumHelper.SYS_IM, SeleniumHelper.ENV_TEST, "S18932419A", "12345678");

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
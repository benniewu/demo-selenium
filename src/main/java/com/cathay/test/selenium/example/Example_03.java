package com.cathay.test.selenium.example;

import org.openqa.selenium.WebDriver;

import com.cathay.test.selenium.SeleniumHelper;
import com.cathay.test.selenium.SeleniumHelperFactory;
import com.cathay.test.selenium.WebDriverHelper;

public class Example_03 {

    public static void main(String[] args) {

        WebDriver driver = null;
        try {
            driver = WebDriverHelper.getWebDriver(WebDriverHelper.DRIVER_DEVELOP, WebDriverHelper.BROWSER_IE);
            SeleniumHelper helper = SeleniumHelperFactory.createSeleniumHelper("RZ", driver);

            // 操作瀏覽器
            driver.manage().window().maximize();
            driver.get("https://tim.cathaylife.com.tw/IMWeb/");
            Thread.sleep(5000);
            helper.keyinById("userID", "S18932419A");
            helper.keyinById("passWD", "12345678");
            helper.clickById("btn_submit");

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
package com.cathay.test.selenium;

import org.openqa.selenium.WebDriver;

public class SeleniumHelperFactory {

    public static final String SYS_CD_FK = "FK";

    public static SeleniumHelper createSeleniumHelper(String sysCd, WebDriver driver) {
        if (SYS_CD_FK.equals(sysCd)) {
            return new SeleniumHelperFK(driver);
        } else {
            return new SeleniumHelper(driver);
        }
    }

    public static SeleniumHelper createSeleniumHelper(String sysCd, WebDriver driver, boolean isDevTest) {
        SeleniumHelper s = null;
        if (SYS_CD_FK.equals(sysCd)) {
            s = new SeleniumHelperFK(driver);
        } else {
            s = new SeleniumHelper(driver);
        }
        s.setDevTest(isDevTest);
        return s;
    }
}
package com.cathay.test.selenium;

import org.openqa.selenium.WebDriver;

public class ValidationPointHelperFactory {

    public static final String SYS_CD_FK = "FK";

    public static ValidationPointHelper createValidationPointHelper(String sysCd, WebDriver driver, SeleniumHelper seleniumHelper) {
        if (SYS_CD_FK.equals(sysCd)) {
            return new ValidationPointHelperFK(driver, seleniumHelper);
        } else {
            return new ValidationPointHelper(driver, seleniumHelper);
        }
    }
}
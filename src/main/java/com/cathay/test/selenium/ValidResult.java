package com.cathay.test.selenium;

import java.io.File;

public class ValidResult extends StepResult {

    private String expectStr;

    private String actualStr;

    public ValidResult(int resultCode, String exceptionMsg) {
        super.setResultCode(resultCode);
        super.setExceptionMsg(exceptionMsg);
    }

    public ValidResult(int resultCode, String exceptionMsg, File screenshotImg) {
        super.setResultCode(resultCode);
        super.setExceptionMsg(exceptionMsg);
        super.setScreenshotImg(screenshotImg);
    }

    public ValidResult(int resultCode, String expectStr, String actualStr) {
        super.setResultCode(resultCode);
        this.expectStr = expectStr;
        this.actualStr = actualStr;
    }

    public ValidResult(int resultCode, String expectStr, String actualStr, File screenshotImg) {
        super.setResultCode(resultCode);
        this.expectStr = expectStr;
        this.actualStr = actualStr;
        super.setScreenshotImg(screenshotImg);
    }

    public String getExpectStr() {
        return expectStr;
    }

    public void setExpectStr(String expectStr) {
        this.expectStr = expectStr;
    }

    public String getActualStr() {
        return actualStr;
    }

    public void setActualStr(String actualStr) {
        this.actualStr = actualStr;
    }
}
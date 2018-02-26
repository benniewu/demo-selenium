package com.cathay.test.selenium;

import java.io.File;

public class StepResult {

    public static final Integer RESULT_SUCCESS = 0;

    public static final Integer RESULT_FAIL = 1;

    public static final Integer RESULT_EXCEPTION = -1;

    private int resultCode;

    private String exceptionMsg;

    private File screenshotImg;

    private File beforeScreenshotImg;

    private File afterScreenshotImg;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

    public File getScreenshotImg() {
        return screenshotImg;
    }

    public void setScreenshotImg(File screenshotImg) {
        this.screenshotImg = screenshotImg;
    }

    public File getBeforeScreenshotImg() {
        return beforeScreenshotImg;
    }

    public void setBeforeScreenshotImg(File beforeScreenshotImg) {
        this.beforeScreenshotImg = beforeScreenshotImg;
    }

    public File getAfterScreenshotImg() {
        return afterScreenshotImg;
    }

    public void setAfterScreenshotImg(File afterScreenshotImg) {
        this.afterScreenshotImg = afterScreenshotImg;
    }
}
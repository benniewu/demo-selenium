package com.cathay.test.selenium;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.cathay.common.util.NumberUtils;

public class ValidationPonitHelper {

    private static Logger log = Logger.getLogger(ValidationPonitHelper.class);

    protected WebDriver driver;

    protected SeleniumHelper seleniumHelper;

    private List<WebElement> highlightElems = new ArrayList<WebElement>();

    //等待時間
    private int stepWait = 1;

    public ValidationPonitHelper(WebDriver _driver, SeleniumHelper _seleniumHelper) {
        driver = _driver;
        seleniumHelper = _seleniumHelper;
    }

    /**
     * 驗證點步驟執行
     * @param STEP_TYPE
     * @param STEP_ARGS
     * @return
     */
    public ValidResult doValid(String stepType, List<String> stepArgs) {

        ValidResult result = null;
        try {
            switch (stepType) {
                case "VP_BTM_MSG":
                    return this.validBottomMessage(stepArgs.get(0));
                case "VP_IM_POP_MSG":
                    return this.validIMPopupMessage(stepArgs.get(0));
                case "VP_ALERT_MSG":
                    return this.validAlertMessage(stepArgs.get(0));
                case "VP_AMT":
                    return this.validAmount(stepArgs.get(0), stepArgs.get(1));
                case "VP_TEXT":
                    return this.validText(stepArgs.get(0), stepArgs.get(1));
                case "VP_TD_MSG":
                    return this.validTdMessage(stepArgs.get(0), stepArgs.get(1));
                case "VP_TABLE_DATA":
                    return this.validTableData(stepArgs.get(0), stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
                case "VP_TUI_CNT":
                    return this.validTableUICount(stepArgs.get(0));
                case "VP_SELECT_LINKAGE":
                    return this.validSelectLinkage(stepArgs.get(0), stepArgs.get(1));
            }
        } catch (Exception e) {
            log.error("驗證點執行發生錯誤", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * 驗證點步驟執行
     * @param STEP_TYPE
     * @param STEP_ARGS
     * @return
     */
    public ValidResult doPGValid(String stepType, List<String> stepArgs) {

        ValidResult result = null;
        try {
            switch (stepType) {
                case "PG_VP_INPUT_EMPTY":
                    return this.pgValidInputEmpty();
                case "PG_VP_FUNC_POSITIVE":
                    return this.pgValidFuncPositive();
                case "PG_VP_FUNC_NEGATIVE":
                    return this.pgValidFuncNegative();
            }
        } catch (Exception e) {
            log.error("驗證點執行發生錯誤", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * 取得底部訊息欄
     * @return
     * @throws Exception
     */
    protected WebElement getBottomMessageElem() throws Exception {

        Thread.sleep(1000);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(seleniumHelper.getElement(By.id("bottomFrame"), "切換至 bottomFrame"));
        return seleniumHelper.getElement(By.id("cathay_common_msgBoard"), "底部訊息欄");
    }

    /**
     * 驗證底部回傳訊息
     * @param checkMsg
     * @return
     */
    public ValidResult validBottomMessage(String checkMsg) {

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
        }

        try {
            WebElement elem = getBottomMessageElem();
            String rtnMsg = elem.getText();

            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File screenShotImg = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, screenShotImg);
            }
        } catch (Exception e) {
            log.error("驗證底部回傳訊息錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證底部回傳訊息錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "切換回mainFrame"));
        }
    }

    /**
     * 驗證TableUI查詢筆數
     * @param validCount
     * @return
     */
    public ValidResult validTableUICount(String checkCnt) {
        try {
            WebElement elem = null;
            if (checkCnt == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            Thread.sleep(500);
            elem = seleniumHelper.getElement(By.id("grid_pageInfo"), "TableUI查詢筆數");
            String rtnCnt = elem.getText();
            String totalCnt = rtnCnt.substring(rtnCnt.lastIndexOf("共") + 1, rtnCnt.indexOf("筆")).trim();
            if (checkCnt.equals(totalCnt)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkCnt, totalCnt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkCnt, totalCnt, img);
            }
        } catch (Exception e) {
            log.error("驗證TableUI查詢筆數錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證TableUI查詢筆數錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證元素金額
     * @param id 該欄位id
     * @param strAmt 驗證比對的金額
     * @return
     */
    public ValidResult validAmount(String id, String checkAmt) {
        try {
            WebElement elem = null;
            if (id == null || checkAmt == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            Thread.sleep(500);
            checkAmt = checkAmt.replace(",", "");
            elem = seleniumHelper.getElement(By.id(id), "驗證元素金額");
            String rtnAmt = elem.getText().replace(",", "");
            if (new BigDecimal(checkAmt).compareTo(new BigDecimal(rtnAmt)) == 0) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkAmt, rtnAmt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkAmt, rtnAmt, img);
            }
        } catch (Exception e) {
            log.error("驗證元素金額錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證元素金額錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證元素文字
     * @param id 該欄位id
     * @param strAmt 驗證比對的金額
     * @return
     */
    public ValidResult validText(String id, String checkText) {
        try {
            WebElement elem = null;
            if (id == null || checkText == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            Thread.sleep(500);
            elem = seleniumHelper.getElement(By.id(id), "驗證元素文字");
            String rtnAmt = elem.getText();

            if (checkText.equals(rtnAmt)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkText, rtnAmt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkText, rtnAmt, img);
            }
        } catch (Exception e) {
            log.error("驗證元素文字錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證元素文字錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證元素對應右側的文字
     * @param fieldText 欄位名稱
     * @param checkMsg 驗證比對字串
     * @return
     */
    public ValidResult validTdMessage(String fieldText, String checkMsg) {
        try {
            if (fieldText == null || checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            WebElement elem = null;
            Thread.sleep(500);
            List<WebElement> eles = driver.findElements(By.xpath("//td[contains(text(), '" + fieldText + "')]/following-sibling::td[1]"));
            if (eles.size() == 0) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "找不到對應右側文字 ");
            }
            elem = eles.get(0);
            String rtnMsg = elem.getText();
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("驗證元素對應右側的文字錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證元素對應右側的文字錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證表格內容
     * @param id 表格id
     * @param rows 第幾列資料
     * @param column 第幾欄資料或是資料元件NAME
     * @param checkMsg 驗證比對字串
     * @return
     */
    public ValidResult validTableData(String id, String rows, String column, String checkMsg) {
        try {
            if (id == null || rows == null || column == null || checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證的id,行,列和字串不得為null");
            }
            WebElement elem = null;
            Thread.sleep(500);
            int col_nm = NumberUtils.toInt(column, 0); //傳入的是NAME非數字就轉成0

            WebElement table = driver.findElement(By.xpath("//*[@id='" + id + "']//*//table[@name='main']"));
            //*[@id="grid"]//*//table[@name="main"]
            List<WebElement> allRows = table.findElements(By.tagName("tbody"));
            Thread.sleep(500);
            WebElement row = allRows.get(NumberUtils.toInt(rows) - 1);

            String rtnMsg;
            if (col_nm != 0) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                elem = cells.get(col_nm - 1);
                rtnMsg = elem.getText();
            } else {
                rtnMsg = row.findElement(By.name(column)).getText();
            }
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("驗證表格內容錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證表格內容錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證IM POPUP訊息
     * @param checkMsg
     * @return
     */
    public ValidResult validIMPopupMessage(String checkMsg) {
        try {
            if (checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            WebElement elem = null;
            Thread.sleep(500);
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "切換回mainFrame"));
            elem = seleniumHelper.getElement(By.id("msg-win_content"), "IM POPUP訊息");
            String rtnMsg = elem.getText();
            rtnMsg = replaceWrap(rtnMsg);
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("驗證IM POPUP訊息內容錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證IM POPUP訊息內容錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證Alert訊息
     * @param checkMsg
     * @return
     */
    public ValidResult validAlertMessage(String checkMsg) {
        try {
            if (checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            Thread.sleep(500);
            Alert alert = driver.switchTo().alert();
            String rtnMsg = alert.getText();
            rtnMsg = replaceWrap(rtnMsg);
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                File img = getScreenshot();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("驗證Alert訊息內容錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證Alert訊息內容錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證連動選單
     * @param id
     * @param keyword
     * @return
     */
    public ValidResult validSelectLinkage(String id, String keyword) {

        try {
            if (id == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證連動選單ID不得為空");
            }

            Thread.sleep(1000);
            WebElement elem = seleniumHelper.getElement(By.id(id), "驗證連動選單");

            List<WebElement> opts = new Select(elem).getOptions();
            for (WebElement opt : opts) {
                String optText = opt.getText();
                if (optText != null && optText.contains(keyword)) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, keyword, optText);
                }
            }
            return new ValidResult(StepResult.RESULT_FAIL, keyword, "");
        } catch (Exception e) {
            log.error("驗證連動選單錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證連動選單錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    public ValidResult pgValidInputEmpty() {
        return validAlertMessage("檢核錯誤", new String[] { "需有值", "不得為空", "不可空白" });
    }

    public ValidResult pgValidInput(String keywords) {
        return validAlertMessage("檢核錯誤", new String[] { "檢核錯誤" });
    }

    public ValidResult pgValidFuncPositive() {
        return validBottomMessage(new String[] { "成功", "完成" });
    }

    public ValidResult pgValidFuncNegative() {
        return validBottomMessage(new String[] { "查無資料", "失敗", "錯誤", "異常" });
    }

    /**
     * 驗證Alert訊息
     * @param checkMsg
     * @return
     */
    public ValidResult validAlertMessage(String checkContainMsg, String[] optCheckContainMsgs) {
        try {
            if (checkContainMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
            }
            Thread.sleep(500);
            Alert alert = driver.switchTo().alert();
            String rtnMsg = alert.getText();

            if (rtnMsg.contains(checkContainMsg)) {
                if (optCheckContainMsgs == null) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, checkContainMsg, rtnMsg);
                }
                for (String optCheckContainMsg : optCheckContainMsgs) {
                    if (rtnMsg.contains(optCheckContainMsg)) {
                        return new ValidResult(StepResult.RESULT_SUCCESS, checkContainMsg + "," + Arrays.toString(optCheckContainMsgs),
                                rtnMsg);
                    }
                }
            }
            File img = getScreenshot();
            return new ValidResult(StepResult.RESULT_FAIL, checkContainMsg + "," + Arrays.toString(optCheckContainMsgs), rtnMsg, img);
        } catch (Exception e) {
            log.error("驗證Alert訊息內容錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證Alert訊息內容錯誤 : " + e.getMessage());
        } finally {
            seleniumHelper.confirmAlert();
            doWaitTime();
        }
    }

    /**
     * 驗證底部回傳訊息
     * @param checkMsg
     * @return
     */
    public ValidResult validBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為null");
        }

        try {
            WebElement elem = getBottomMessageElem();
            String rtnMsg = elem.getText();

            for (String checkContainMsg : checkContainMsgs) {
                if (rtnMsg.contains(checkContainMsg)) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, Arrays.toString(checkContainMsgs), rtnMsg);
                }
            }
            highLightElem(elem);
            File screenShotImg = getScreenshot();
            unhighLightLastElems();
            return new ValidResult(StepResult.RESULT_FAIL, Arrays.toString(checkContainMsgs), rtnMsg, screenShotImg);
        } catch (Exception e) {
            log.error("驗證底部回傳訊息錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證底部回傳訊息錯誤 : " + e.getMessage());
        } finally {
            seleniumHelper.confirmAlert();
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "切換回mainFrame"));
        }
    }

    /**
     * 強調元素
     * @param elem
     */
    protected void highLightElem(WebElement elem) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", elem);
        highlightElems.add(elem);
    }

    /**
     * 取消強調當前強調元素
     */
    protected void unhighLightLastElems() {
        try {
            for (WebElement elem : highlightElems) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='0px'", elem);
            }
        } catch (StaleElementReferenceException sere) {
            // the page got reloaded, the element isn't there
        } finally {
            highlightElems.clear();
        }
    }

    /**
     * 執行中畫面擷圖存檔
     * @return
     */
    protected File getScreenshot() {

        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    }

    /**
     * 驗證點作完後的等待時間
     * @return
     */
    protected void doWaitTime() {
        try {
            TimeUnit.SECONDS.sleep(stepWait);
        } catch (Exception e) {
            log.error("驗證點完成後，等待時間發生異常", e);
        }
    }
    
    /**
     * 去掉驗證文字的換行
     * @return String
     */
    private String replaceWrap(String str){
        log.debug("Strat replaceWrap");
        return str.replaceAll("(\r\n|\n\r|\r|\n)", " ");
    }
}
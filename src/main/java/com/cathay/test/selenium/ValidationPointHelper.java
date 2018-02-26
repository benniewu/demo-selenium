package com.cathay.test.selenium;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.cathay.common.hr.WorkDate;
import com.cathay.common.util.CathayDate;
import com.cathay.common.util.DATE;
import com.cathay.common.util.NumberUtils;

public class ValidationPointHelper {

    private static Logger log = Logger.getLogger(ValidationPointHelper.class);

    protected WebDriver driver;

    protected SeleniumHelper seleniumHelper;

    private List<WebElement> highlightElems = new ArrayList<WebElement>();

    //等待時間
    private int stepWait = 1;

    public ValidationPointHelper(WebDriver _driver, SeleniumHelper _seleniumHelper) {
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
                    return this.validText(stepArgs.get(0), formatInputValue(stepArgs.get(1)));
                case "VP_VAL":
                    return this.validVal(stepArgs.get(0), formatInputValue(stepArgs.get(1)));
                case "VP_TD_MSG":
                    return this.validTdMessage(stepArgs.get(0), stepArgs.get(1));
                case "VP_TABLE_DATA":
                    return this.validTableData(stepArgs.get(0), stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
                case "VP_TUI_CNT":
                    return this.validTableUICount(stepArgs.get(0));
                case "VP_SELECT_LINKAGE":
                    return this.validSelectLinkage(stepArgs.get(0), stepArgs.get(1));
                case "VP_STEP_OK":
                    return this.validStepOk();
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
    public ValidResult doDevValid(String stepType, List<String> stepArgs) {

        ValidResult result = null;
        try {
            switch (stepType) {
                case "DEV_VP_INPUT":
                    if (stepArgs.size() == 1) {
                        return this.devValidInput(stepArgs.get(0));
                    } else if (stepArgs.size() == 2) {
                        return this.devValidInput(stepArgs.get(0), stepArgs.get(1));
                    }
                case "DEV_VP_INPUT_EMPTY":
                    if (stepArgs.size() == 0) {
                        return this.devValidInputEmpty();
                    } else if (stepArgs.size() == 1) {
                        return this.devValidInputEmpty(stepArgs.get(0));
                    }
                case "DEV_VP_FUNC_POSITIVE":
                    return this.devValidFuncPositive();
                case "DEV_VP_FUNC_POSITIVE_QUERY":
                    return this.devValidFuncPositiveQuery();
                case "DEV_VP_FUNC_POSITIVE_EXPORT":
                    return this.devValidFuncPositiveExport();
                case "DEV_VP_FUNC_NEGATIVE":
                    return this.devValidFuncNegative();
            }
        } catch (Exception e) {
            log.error("驗證點執行發生錯誤", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * 驗證底部回傳訊息
     * @param checkMsg
     * @return
     */
    public ValidResult validBottomMessage(String checkMsg) {

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            WebElement elem = seleniumHelper.getBottomMessageElem();
            String rtnMsg = elem.getText();

            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            }
            highLightElem(elem);
            File screenShotImg = getScreenshot();
            unhighLightLastElems();
            return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, screenShotImg);
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

        if (checkCnt == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            waitTime(1000);

            WebElement elem = seleniumHelper.getElement(By.id("grid_pageInfo"), "TableUI查詢筆數");
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

        if (id == null || checkAmt == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            waitTime(1000);

            checkAmt = checkAmt.replace(",", "");
            WebElement elem = seleniumHelper.getElement(By.id(id), "驗證元素金額");
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

        if (id == null || checkText == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            waitTime(1000);

            WebElement elem = seleniumHelper.getElement(By.id(id), "驗證元素文字");
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
     * 驗證元件值value
     * @param id
     * @param checkText
     * @return
     */
    public ValidResult validVal(String id, String checkText) {

        if (id == null || checkText == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            waitTime(1000);

            WebElement elem = null;
            String rtnAmt = "";
            try {
                elem = driver.findElement(By.id(id));
                rtnAmt = elem.getAttribute("value");
            } catch (NoSuchElementException e) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                rtnAmt = ObjectUtils.toString(js.executeScript("return document.getElementById('" + id + "').value;"));
            }

            if (checkText.equals(rtnAmt)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkText, rtnAmt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkText, rtnAmt, img);
            }
        } catch (Exception e) {
            log.error("驗證元素值錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證元素值錯誤 : " + e.getMessage());
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

        if (fieldText == null || checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            waitTime(1000);

            List<WebElement> eles = driver.findElements(By.xpath("//td[contains(text(), '" + fieldText + "')]/following-sibling::td[1]"));
            if (eles.size() == 0) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "找不到對應右側文字 ");
            }
            WebElement elem = eles.get(0);
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

        if (id == null || rows == null || column == null || checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證的id,行,列和字串不得為null");
        }

        try {
            waitTime(1000);
            WebElement table = driver.findElement(By.xpath("//*[@id='" + id + "']//*//table[@name='main']"));
            //*[@id="grid"]//*//table[@name="main"]
            List<WebElement> allRows = table.findElements(By.tagName("tbody"));
            WebElement row = allRows.get(NumberUtils.toInt(rows) - 1);

            WebElement elem = null;
            int col_nm = NumberUtils.toInt(column, 0); //傳入的是NAME非數字就轉成0
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

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            waitTime(1000);

            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "切換回mainFrame"));
            WebElement elem = seleniumHelper.getElement(By.id("msg-win_content"), "IM POPUP訊息");
            String rtnMsg = replaceWrap(elem.getText());

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
                return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
            }

            String rtnMsg;
            if (seleniumHelper.isDevTest()) {
                rtnMsg = seleniumHelper.getStoredAlertMsg();
            } else {
                waitTime(1000);
                Alert alert = driver.switchTo().alert();
                rtnMsg = replaceWrap(alert.getText());
            }

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

        if (id == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證連動選單ID不得為空");
        }

        try {
            waitTime(1000);

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

    /**
     * 驗證步驟正確
     * @return
     */
    public ValidResult validStepOk() {
        return new ValidResult(StepResult.RESULT_SUCCESS, "", "");
    }

    /**
     * 驗證輸入
     * @param keywords
     * @return
     */
    public ValidResult devValidInput(String keywords) {
        return validAlertMessage("檢核錯誤", keywords.split(","));
    }

    /**
     * 驗證輸入
     * @param keywords
     * @param isAlert
     * @return
     */
    public ValidResult devValidInput(String keywords, String isAlert) {
        if ("IS_ALERT_N".equals(isAlert)) {
            return validFormValidMessage(keywords.split(","));
        }
        return validAlertMessage("檢核錯誤", keywords.split(","));
    }

    /**
     * 驗證輸入無值
     * @return
     */
    public ValidResult devValidInputEmpty() {
        return validAlertMessage("檢核錯誤", new String[] { "需有值", "不得為空", "不可空白" });
    }

    /**
     * 驗證輸入(無值)
     * @param isAlert
     * @return
     */
    public ValidResult devValidInputEmpty(String isAlert) {
        if ("IS_ALERT_N".equals(isAlert)) {
            return validFormValidMessage(new String[] { "欄位為必輸入" });
        }
        return validAlertMessage("檢核錯誤", new String[] { "需有值", "不得為空", "不可空白" });
    }

    /**
     * 驗證功能正向
     * @return
     */
    public ValidResult devValidFuncPositive() {
        return validContainBottomMessage(new String[] { "成功", "完成" });
    }

    /**
     * 驗證功能正向(查詢)
     * @return
     */
    public ValidResult devValidFuncPositiveQuery() {
        ValidResult validResult = validContainBottomMessage(new String[] { "成功", "完成" });
        if (validResult.getResultCode() == ValidResult.RESULT_SUCCESS) {
            return validTableUICountGtZero();
        }
        return validResult;
    }

    /**
     * 驗證功能正向(匯出)
     * @return
     */
    public ValidResult devValidFuncPositiveExport() {
        ValidResult validResult = validNoAlert();
        if (validResult.getResultCode() == ValidResult.RESULT_SUCCESS) {
            return validNotContainBottomMessage(new String[] { "查無資料", "失敗", "錯誤", "異常" });
        }
        return validResult;
    }

    /**
     * 驗證功能負向
     * @return
     */
    public ValidResult devValidFuncNegative() {
        return validContainBottomMessage(new String[] { "查無資料", "失敗", "錯誤", "異常" });
    }

    /**
     * 驗證無Alert訊息
     * @return
     */
    protected ValidResult validNoAlert() {

        try {
            String rtnMsg = null;
            if (seleniumHelper.isDevTest()) {
                rtnMsg = seleniumHelper.getStoredAlertMsg();
            } else {
                waitTime(1000);
                try {
                    Alert alert = driver.switchTo().alert();
                    rtnMsg = replaceWrap(alert.getText());
                } catch (NoAlertPresentException e) {
                }
            }

            if (StringUtils.isBlank(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, "No Alert", "No Alert");
            }
            return new ValidResult(StepResult.RESULT_FAIL, "No Alert", rtnMsg);

        } catch (Exception e) {
            log.error("驗證無Alert訊息錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證無Alert訊息錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證 Form 驗證訊息
     * @param checkMsg
     * @return
     */
    protected ValidResult validFormValidMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            List<WebElement> elems = driver.findElements(By.className("validation-advice"));
            if (elems.size() == 0) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "找不到 Form 驗證訊息");
            }

            StringBuilder rtnMsgSb = new StringBuilder();
            for (WebElement elem : elems) {
                String rtnMsg = elem.getText();
                for (String checkContainMsg : checkContainMsgs) {
                    if (rtnMsg.contains(checkContainMsg)) {
                        return new ValidResult(StepResult.RESULT_SUCCESS, checkContainMsg, rtnMsg);
                    }
                }
            }

            File screenShotImg = getScreenshot();
            return new ValidResult(StepResult.RESULT_FAIL, Arrays.toString(checkContainMsgs), rtnMsgSb.toString(), screenShotImg);
        } catch (Exception e) {
            log.error("驗證 Form 驗證訊息內容錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證 Form 驗證訊息內容錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證Alert訊息
     * @param checkMsg
     * @return
     */
    protected ValidResult validAlertMessage(String checkContainMsg, String[] optCheckContainMsgs) {

        if (checkContainMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            String rtnMsg;
            if (seleniumHelper.isDevTest()) {
                rtnMsg = seleniumHelper.getStoredAlertMsg();
            } else {
                waitTime(1000);
                Alert alert = driver.switchTo().alert();
                rtnMsg = replaceWrap(alert.getText());
            }

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
            return new ValidResult(StepResult.RESULT_FAIL, checkContainMsg + "," + Arrays.toString(optCheckContainMsgs), rtnMsg);

        } catch (Exception e) {
            log.error("驗證Alert訊息內容錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證Alert訊息內容錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * 驗證底部回傳訊息
     * @param checkMsg
     * @return
     */
    protected ValidResult validContainBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            WebElement elem = seleniumHelper.getBottomMessageElem();
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
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "切換回mainFrame"));
        }
    }

    /**
     * 驗證底部回傳訊息
     * @param checkMsg
     * @return
     */
    protected ValidResult validNotContainBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證字串不得為空");
        }

        try {
            WebElement elem = seleniumHelper.getBottomMessageElem();
            String rtnMsg = elem.getText();

            for (String checkContainMsg : checkContainMsgs) {
                if (rtnMsg.contains(checkContainMsg)) {
                    highLightElem(elem);
                    File screenShotImg = getScreenshot();
                    unhighLightLastElems();
                    return new ValidResult(StepResult.RESULT_FAIL, Arrays.toString(checkContainMsgs), rtnMsg, screenShotImg);
                }
            }
            return new ValidResult(StepResult.RESULT_SUCCESS, Arrays.toString(checkContainMsgs), rtnMsg);

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
     * 驗證TableUI查詢筆數大於0
     * @return
     */
    protected ValidResult validTableUICountGtZero() {

        try {
            WebElement elem = seleniumHelper.getElement(By.id("grid_pageInfo"), "TableUI查詢筆數");
            String rtnCnt = elem.getText();
            String totalCnt = rtnCnt.substring(rtnCnt.lastIndexOf("共") + 1, rtnCnt.indexOf("筆")).trim();
            if (Integer.valueOf(totalCnt) > 0) {
                return new ValidResult(StepResult.RESULT_SUCCESS, " > 0 ", totalCnt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, " > 0 ", totalCnt, img);
            }
        } catch (Exception e) {
            log.error("驗證TableUI查詢筆數大於0錯誤", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "驗證TableUI查詢筆數大於0錯誤 : " + e.getMessage());
        } finally {
            doWaitTime();
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

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        screenshot.deleteOnExit();
        return screenshot;
    }

    /**
     * 等待時間
     * @param time
     */
    public void waitTime(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error("Thread sleep error", e);
        }
    }

    /**
     * format input內容
     * @param arg
     * @return
     * @throws ParseException 
     */
    private String formatInputValue(String arg) throws ParseException {
        if (StringUtils.isBlank(arg)) {
            return "";
        }

        String DB_DATE_MARK = "@{DB_DATE}";
        String ROC_DB_DATE_MARK = "@{ROC_DB_DATE}";
        String DB_TIMESTAMP_MARK = "@{DB_TIMESTAMP}";
        String SHUTDOWNDAY = "@{SHUTDOWNDAY}";
        if (arg.contains(DB_DATE_MARK)) {
            return calDate(DB_DATE_MARK, arg);
        } else if (arg.contains(SHUTDOWNDAY)) {
            return calDate(SHUTDOWNDAY, arg);
        } else if (arg.contains(ROC_DB_DATE_MARK)) {
            return DATE.formatToROCDate(calDate(ROC_DB_DATE_MARK, arg));
        } else if (arg.contains(DB_TIMESTAMP_MARK)) {
            return calTimeStamp();
        } else {
            return arg;
        }
    }

    /**
     * 計算系統日期+-天數
     * @param MARK
     * @param arg
     * @return
     * @throws ParseException 
     */
    private String calDate(String MARK, String arg) throws ParseException {

        String strAddDay = arg.replace(MARK, "");
        strAddDay = StringUtils.isBlank(strAddDay) ? "0" : strAddDay.trim();
        String DBDate = DATE.getDBDate();
        if (MARK.contains("@{SHUTDOWNDAY}")) {
            DBDate = new CathayDate().getShutdownDay(DATE.getDBDate());
        }
        //有指定是工作日
        if (strAddDay.endsWith("WD")) {
            strAddDay = strAddDay.replace("WD", "");
            boolean theDIR = true;
            if (strAddDay.startsWith("-")) {
                theDIR = false;
                strAddDay = strAddDay.replace("-", "");
            } else {
                strAddDay = strAddDay.replace("+", "");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = sdf.parse(DBDate);
            return sdf.format(WorkDate.getXWorkingDate(today, Integer.parseInt(strAddDay), theDIR));
        } else {
            try {
                int addDay = 0;
                addDay = Integer.parseInt(strAddDay);
                return DATE.addDate(DBDate, 0, 0, addDay);
            } catch (NumberFormatException e) {
                strAddDay = strAddDay.replace("+", "");
                String thisTime = DBDate + " " + strAddDay;
                if (DATE.isDBTimeStamp(thisTime)) {
                    return thisTime;
                } else {
                    return DBDate;
                }
            }
        }
    }

    /**
     * 計算系統時間
     * @param MARK
     * @param arg
     * @return
     */
    private String calTimeStamp() {
        return ObjectUtils.toString(new Timestamp(new Date().getTime()));
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
    private String replaceWrap(String str) {
        return str.replaceAll("(\r\n|\n\r|\r|\n)", " ");
    }
}
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

    //���ݮɶ�
    private int stepWait = 1;

    public ValidationPointHelper(WebDriver _driver, SeleniumHelper _seleniumHelper) {
        driver = _driver;
        seleniumHelper = _seleniumHelper;
    }

    /**
     * �����I�B�J����
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
            log.error("�����I����o�Ϳ��~", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * �����I�B�J����
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
            log.error("�����I����o�Ϳ��~", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    public ValidResult validBottomMessage(String checkMsg) {

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
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
            log.error("���ҩ����^�ǰT�����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҩ����^�ǰT�����~ : " + e.getMessage());
        } finally {
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
        }
    }

    /**
     * ����TableUI�d�ߵ���
     * @param validCount
     * @return
     */
    public ValidResult validTableUICount(String checkCnt) {

        if (checkCnt == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
        }

        try {
            waitTime(1000);

            WebElement elem = seleniumHelper.getElement(By.id("grid_pageInfo"), "TableUI�d�ߵ���");
            String rtnCnt = elem.getText();
            String totalCnt = rtnCnt.substring(rtnCnt.lastIndexOf("�@") + 1, rtnCnt.indexOf("��")).trim();
            if (checkCnt.equals(totalCnt)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkCnt, totalCnt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkCnt, totalCnt, img);
            }
        } catch (Exception e) {
            log.error("����TableUI�d�ߵ��ƿ��~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����TableUI�d�ߵ��ƿ��~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ������B
     * @param id �����id
     * @param strAmt ���Ҥ�諸���B
     * @return
     */
    public ValidResult validAmount(String id, String checkAmt) {

        if (id == null || checkAmt == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
        }

        try {
            waitTime(1000);

            checkAmt = checkAmt.replace(",", "");
            WebElement elem = seleniumHelper.getElement(By.id(id), "���Ҥ������B");
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
            log.error("���Ҥ������B���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ������B���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ�����r
     * @param id �����id
     * @param strAmt ���Ҥ�諸���B
     * @return
     */
    public ValidResult validText(String id, String checkText) {

        if (id == null || checkText == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
        }

        try {
            waitTime(1000);

            WebElement elem = seleniumHelper.getElement(By.id(id), "���Ҥ�����r");
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
            log.error("���Ҥ�����r���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ�����r���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ����value
     * @param id
     * @param checkText
     * @return
     */
    public ValidResult validVal(String id, String checkText) {

        if (id == null || checkText == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
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
            log.error("���Ҥ����ȿ��~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ����ȿ��~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ��������k������r
     * @param fieldText ���W��
     * @param checkMsg ���Ҥ��r��
     * @return
     */
    public ValidResult validTdMessage(String fieldText, String checkMsg) {

        if (fieldText == null || checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
        }

        try {
            waitTime(1000);

            List<WebElement> eles = driver.findElements(By.xpath("//td[contains(text(), '" + fieldText + "')]/following-sibling::td[1]"));
            if (eles.size() == 0) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "�䤣������k����r ");
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
            log.error("���Ҥ��������k������r���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ��������k������r���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҫ�椺�e
     * @param id ���id
     * @param rows �ĴX�C���
     * @param column �ĴX���ƩάO��Ƥ���NAME
     * @param checkMsg ���Ҥ��r��
     * @return
     */
    public ValidResult validTableData(String id, String rows, String column, String checkMsg) {

        if (id == null || rows == null || column == null || checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҫ�id,��,�C�M�r�ꤣ�o��null");
        }

        try {
            waitTime(1000);
            WebElement table = driver.findElement(By.xpath("//*[@id='" + id + "']//*//table[@name='main']"));
            //*[@id="grid"]//*//table[@name="main"]
            List<WebElement> allRows = table.findElements(By.tagName("tbody"));
            WebElement row = allRows.get(NumberUtils.toInt(rows) - 1);

            WebElement elem = null;
            int col_nm = NumberUtils.toInt(column, 0); //�ǤJ���ONAME�D�Ʀr�N�ন0
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
            log.error("���Ҫ�椺�e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҫ�椺�e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ����IM POPUP�T��
     * @param checkMsg
     * @return
     */
    public ValidResult validIMPopupMessage(String checkMsg) {

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
        }

        try {
            waitTime(1000);

            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
            WebElement elem = seleniumHelper.getElement(By.id("msg-win_content"), "IM POPUP�T��");
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
            log.error("����IM POPUP�T�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����IM POPUP�T�����e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ����Alert�T��
     * @param checkMsg
     * @return
     */
    public ValidResult validAlertMessage(String checkMsg) {
        try {
            if (checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
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
            log.error("����Alert�T�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����Alert�T�����e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���ҳs�ʿ��
     * @param id
     * @param keyword
     * @return
     */
    public ValidResult validSelectLinkage(String id, String keyword) {

        if (id == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҳs�ʿ��ID���o����");
        }

        try {
            waitTime(1000);

            WebElement elem = seleniumHelper.getElement(By.id(id), "���ҳs�ʿ��");
            List<WebElement> opts = new Select(elem).getOptions();
            for (WebElement opt : opts) {
                String optText = opt.getText();
                if (optText != null && optText.contains(keyword)) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, keyword, optText);
                }
            }
            return new ValidResult(StepResult.RESULT_FAIL, keyword, "");
        } catch (Exception e) {
            log.error("���ҳs�ʿ����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҳs�ʿ����~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���ҨB�J���T
     * @return
     */
    public ValidResult validStepOk() {
        return new ValidResult(StepResult.RESULT_SUCCESS, "", "");
    }

    /**
     * ���ҿ�J
     * @param keywords
     * @return
     */
    public ValidResult devValidInput(String keywords) {
        return validAlertMessage("�ˮֿ��~", keywords.split(","));
    }

    /**
     * ���ҿ�J
     * @param keywords
     * @param isAlert
     * @return
     */
    public ValidResult devValidInput(String keywords, String isAlert) {
        if ("IS_ALERT_N".equals(isAlert)) {
            return validFormValidMessage(keywords.split(","));
        }
        return validAlertMessage("�ˮֿ��~", keywords.split(","));
    }

    /**
     * ���ҿ�J�L��
     * @return
     */
    public ValidResult devValidInputEmpty() {
        return validAlertMessage("�ˮֿ��~", new String[] { "�ݦ���", "���o����", "���i�ť�" });
    }

    /**
     * ���ҿ�J(�L��)
     * @param isAlert
     * @return
     */
    public ValidResult devValidInputEmpty(String isAlert) {
        if ("IS_ALERT_N".equals(isAlert)) {
            return validFormValidMessage(new String[] { "��쬰����J" });
        }
        return validAlertMessage("�ˮֿ��~", new String[] { "�ݦ���", "���o����", "���i�ť�" });
    }

    /**
     * ���ҥ\�ॿ�V
     * @return
     */
    public ValidResult devValidFuncPositive() {
        return validContainBottomMessage(new String[] { "���\", "����" });
    }

    /**
     * ���ҥ\�ॿ�V(�d��)
     * @return
     */
    public ValidResult devValidFuncPositiveQuery() {
        ValidResult validResult = validContainBottomMessage(new String[] { "���\", "����" });
        if (validResult.getResultCode() == ValidResult.RESULT_SUCCESS) {
            return validTableUICountGtZero();
        }
        return validResult;
    }

    /**
     * ���ҥ\�ॿ�V(�ץX)
     * @return
     */
    public ValidResult devValidFuncPositiveExport() {
        ValidResult validResult = validNoAlert();
        if (validResult.getResultCode() == ValidResult.RESULT_SUCCESS) {
            return validNotContainBottomMessage(new String[] { "�d�L���", "����", "���~", "���`" });
        }
        return validResult;
    }

    /**
     * ���ҥ\��t�V
     * @return
     */
    public ValidResult devValidFuncNegative() {
        return validContainBottomMessage(new String[] { "�d�L���", "����", "���~", "���`" });
    }

    /**
     * ���ҵLAlert�T��
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
            log.error("���ҵLAlert�T�����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҵLAlert�T�����~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���� Form ���ҰT��
     * @param checkMsg
     * @return
     */
    protected ValidResult validFormValidMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
        }

        try {
            List<WebElement> elems = driver.findElements(By.className("validation-advice"));
            if (elems.size() == 0) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "�䤣�� Form ���ҰT��");
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
            log.error("���� Form ���ҰT�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���� Form ���ҰT�����e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ����Alert�T��
     * @param checkMsg
     * @return
     */
    protected ValidResult validAlertMessage(String checkContainMsg, String[] optCheckContainMsgs) {

        if (checkContainMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
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
            log.error("����Alert�T�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����Alert�T�����e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    protected ValidResult validContainBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
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
            log.error("���ҩ����^�ǰT�����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҩ����^�ǰT�����~ : " + e.getMessage());
        } finally {
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
        }
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    protected ValidResult validNotContainBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o����");
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
            log.error("���ҩ����^�ǰT�����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҩ����^�ǰT�����~ : " + e.getMessage());
        } finally {
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
        }
    }

    /**
     * ����TableUI�d�ߵ��Ƥj��0
     * @return
     */
    protected ValidResult validTableUICountGtZero() {

        try {
            WebElement elem = seleniumHelper.getElement(By.id("grid_pageInfo"), "TableUI�d�ߵ���");
            String rtnCnt = elem.getText();
            String totalCnt = rtnCnt.substring(rtnCnt.lastIndexOf("�@") + 1, rtnCnt.indexOf("��")).trim();
            if (Integer.valueOf(totalCnt) > 0) {
                return new ValidResult(StepResult.RESULT_SUCCESS, " > 0 ", totalCnt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, " > 0 ", totalCnt, img);
            }
        } catch (Exception e) {
            log.error("����TableUI�d�ߵ��Ƥj��0���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����TableUI�d�ߵ��Ƥj��0���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * �j�դ���
     * @param elem
     */
    protected void highLightElem(WebElement elem) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", elem);
        highlightElems.add(elem);
    }

    /**
     * �����j�շ�e�j�դ���
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
     * ���椤�e���^�Ϧs��
     * @return
     */
    protected File getScreenshot() {

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        screenshot.deleteOnExit();
        return screenshot;
    }

    /**
     * ���ݮɶ�
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
     * format input���e
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
     * �p��t�Τ��+-�Ѽ�
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
        //�����w�O�u�@��
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
     * �p��t�ήɶ�
     * @param MARK
     * @param arg
     * @return
     */
    private String calTimeStamp() {
        return ObjectUtils.toString(new Timestamp(new Date().getTime()));
    }

    /**
     * �����I�@���᪺���ݮɶ�
     * @return
     */
    protected void doWaitTime() {
        try {
            TimeUnit.SECONDS.sleep(stepWait);
        } catch (Exception e) {
            log.error("�����I������A���ݮɶ��o�Ͳ��`", e);
        }
    }

    /**
     * �h�����Ҥ�r������
     * @return String
     */
    private String replaceWrap(String str) {
        return str.replaceAll("(\r\n|\n\r|\r|\n)", " ");
    }
}